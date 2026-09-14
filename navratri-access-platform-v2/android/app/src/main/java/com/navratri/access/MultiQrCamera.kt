package com.navratri.access

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.security.MessageDigest
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

data class QrObservation(
    val id: String,
    val rawValue: String?,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val decoded: Boolean
) {
    val width: Float get() = (right - left).coerceAtLeast(0.01f)
    val height: Float get() = (bottom - top).coerceAtLeast(0.01f)
    val area: Float get() = width * height
}

private fun qrTrackId(raw: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
    return digest.take(8).joinToString("") { "%02x".format(it) }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun MultiQrCamera(
    modifier: Modifier = Modifier,
    onFrame: (List<QrObservation>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val onFrameState by rememberUpdatedState(onFrame)
    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAllPotentialBarcodes()
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(Unit) {
        onDispose {
            scanner.close()
            analyzerExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FIT_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE

                val providerFuture = ProcessCameraProvider.getInstance(ctx)
                providerFuture.addListener({
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                        val media = imageProxy.image
                        if (media == null) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val rotation = imageProxy.imageInfo.rotationDegrees
                        val input = InputImage.fromMediaImage(media, rotation)
                        val uprightWidth = if (rotation == 90 || rotation == 270) media.height else media.width
                        val uprightHeight = if (rotation == 90 || rotation == 270) media.width else media.height

                        scanner.process(input)
                            .addOnSuccessListener { barcodes ->
                                val result = barcodes.mapNotNull { barcode ->
                                    val box = barcode.boundingBox ?: return@mapNotNull null
                                    val l = (box.left.toFloat() / uprightWidth).coerceIn(0f, 1f)
                                    val t = (box.top.toFloat() / uprightHeight).coerceIn(0f, 1f)
                                    val r = (box.right.toFloat() / uprightWidth).coerceIn(0f, 1f)
                                    val b = (box.bottom.toFloat() / uprightHeight).coerceIn(0f, 1f)
                                    val raw = barcode.rawValue
                                    val id = raw?.let(::qrTrackId)
                                        ?: "potential-${(l * 100).toInt()}-${(t * 100).toInt()}-${(r * 100).toInt()}-${(b * 100).toInt()}"
                                    QrObservation(
                                        id = id,
                                        rawValue = raw,
                                        left = min(l, r),
                                        top = min(t, b),
                                        right = max(l, r),
                                        bottom = max(t, b),
                                        decoded = !raw.isNullOrBlank()
                                    )
                                }
                                mainExecutor.execute { onFrameState(result) }
                            }
                            .addOnFailureListener {
                                mainExecutor.execute { onFrameState(emptyList()) }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    }

                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                    } catch (_: Throwable) {
                        onFrameState(emptyList())
                    }
                }, mainExecutor)
            }
        }
    )
}

@Composable
fun QrCamera(
    modifier: Modifier = Modifier,
    onQr: (String) -> Unit
) {
    var lastValue by remember { mutableStateOf("") }
    var lastAt by remember { mutableStateOf(0L) }
    MultiQrCamera(modifier) { observations ->
        val decoded = observations
            .filter { it.decoded && !it.rawValue.isNullOrBlank() }
            .maxByOrNull { it.area }
            ?.rawValue
            ?: return@MultiQrCamera

        val now = SystemClock.elapsedRealtime()
        if (decoded != lastValue || now - lastAt > 1800L) {
            lastValue = decoded
            lastAt = now
            onQr(decoded)
        }
    }
}
