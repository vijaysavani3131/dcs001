package com.navratri.access

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navratri.access.data.*
import com.navratri.access.mesh.NearbyMeshManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** One AR-style overlay rendered above a detected QR in CROWD mode. */
data class CrowdMarker(
    val id: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val label: String,
    val detail: String,
    val allowed: Boolean?,
    val processing: Boolean
)

data class UiState(
    val loading: Boolean = false,
    val message: String? = null,
    val config: EventConfigEntity? = null,
    val scanResult: ScanResult? = null,
    val entries: Int = 0,
    val inside: Int = 0,
    val pending: Int = 0,
    val crowdMarkers: List<CrowdMarker> = emptyList(),
    val crowdApproved: Int = 0,
    val crowdDenied: Int = 0
)

private data class CrowdTrack(
    var observation: QrObservation,
    var lastSeenAt: Long,
    var processing: Boolean = false,
    var result: ScanResult? = null
)

class MainViewModel(
    private val repo: AppRepository,
    val mesh: NearbyMeshManager
) : ViewModel() {
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private val crowdTracks = linkedMapOf<String, CrowdTrack>()
    private var crowdCleanupJob: Job? = null
    private var crowdApproved = 0
    private var crowdDenied = 0

    fun load() {
        viewModelScope.launch {
            val c = repo.config()
            val s = repo.stats()
            _ui.value = _ui.value.copy(
                config = c,
                entries = s.first,
                inside = s.second,
                pending = s.third
            )
        }
    }

    private suspend fun refreshStats() {
        val s = repo.stats()
        _ui.value = _ui.value.copy(entries = s.first, inside = s.second, pending = s.third)
    }

    fun activate(base: String, event: String, code: String, onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                _ui.value = _ui.value.copy(loading = true)
                repo.activate(base, event.toLong(), code)
                repo.syncPack()
            }.onSuccess {
                _ui.value = _ui.value.copy(loading = false, message = "$it tickets synced")
                load()
                onDone()
            }.onFailure {
                _ui.value = _ui.value.copy(loading = false, message = it.message)
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            runCatching {
                _ui.value = _ui.value.copy(loading = true)
                repo.syncPack()
            }.onSuccess {
                _ui.value = _ui.value.copy(loading = false, message = "$it tickets synced")
                load()
            }.onFailure {
                _ui.value = _ui.value.copy(loading = false, message = it.message)
            }
        }
    }

    fun scan(token: String, mode: String) {
        if (_ui.value.scanResult != null) return
        viewModelScope.launch {
            val r = repo.scan(token, mode)
            _ui.value = _ui.value.copy(scanResult = r)
            refreshStats()
        }
    }

    fun clearResult() {
        _ui.value = _ui.value.copy(scanResult = null)
    }

    /**
     * Called for every ML Kit frame in CROWD mode.
     * A QR is processed exactly once while it remains in view. The same QR becomes
     * eligible again only after it physically leaves the frame for ~1.2 seconds.
     * This prevents a green result from immediately turning red as ALREADY INSIDE.
     */
    fun observeCrowd(frame: List<QrObservation>, actionMode: String = "ENTRY") {
        val now = SystemClock.elapsedRealtime()

        // Keep the largest observations first. Eight is a safety ceiling; practical
        // deployment should target 3-6 clearly visible phones/cards per camera.
        val visible = frame
            .sortedByDescending { it.area }
            .take(8)

        visible.forEach { observation ->
            val existing = crowdTracks[observation.id]
            if (existing == null) {
                crowdTracks[observation.id] = CrowdTrack(observation, now)
            } else {
                existing.observation = observation
                existing.lastSeenAt = now
            }
        }

        crowdTracks.entries.removeAll { (_, track) -> now - track.lastSeenAt > 1200L }
        publishCrowd()

        visible.forEach { observation ->
            val raw = observation.rawValue ?: return@forEach
            val track = crowdTracks[observation.id] ?: return@forEach
            if (track.processing || track.result != null) return@forEach

            track.processing = true
            publishCrowd()

            viewModelScope.launch {
                val result = repo.scan(raw, actionMode)
                val current = crowdTracks[observation.id]
                if (current != null) {
                    current.processing = false
                    current.result = result
                    if (result.allowed) crowdApproved++ else crowdDenied++
                }
                publishCrowd()
                refreshStats()
            }
        }

        // If the camera suddenly receives no frames/QRs, stale overlays still self-clear.
        crowdCleanupJob?.cancel()
        crowdCleanupJob = viewModelScope.launch {
            delay(1450L)
            val t = SystemClock.elapsedRealtime()
            crowdTracks.entries.removeAll { (_, track) -> t - track.lastSeenAt > 1200L }
            publishCrowd()
        }
    }

    private fun publishCrowd() {
        val markers = crowdTracks.values.map { track ->
            val o = track.observation
            val r = track.result
            val undecoded = o.rawValue.isNullOrBlank()
            CrowdMarker(
                id = o.id,
                left = o.left,
                top = o.top,
                right = o.right,
                bottom = o.bottom,
                label = when {
                    undecoded -> "MOVE CLOSER"
                    track.processing -> "VERIFYING…"
                    r != null -> r.title
                    else -> "DETECTED"
                },
                detail = when {
                    undecoded -> "QR visible but not readable yet"
                    track.processing -> "Offline ticket + mesh lock check"
                    r != null -> r.detail
                    else -> "Hold steady"
                },
                allowed = r?.allowed,
                processing = track.processing
            )
        }
        _ui.value = _ui.value.copy(
            crowdMarkers = markers,
            crowdApproved = crowdApproved,
            crowdDenied = crowdDenied
        )
    }

    fun clearCrowd(resetCounters: Boolean = false) {
        crowdTracks.clear()
        crowdCleanupJob?.cancel()
        if (resetCounters) {
            crowdApproved = 0
            crowdDenied = 0
        }
        publishCrowd()
    }

    fun cloudSync() {
        viewModelScope.launch {
            runCatching {
                val n = repo.syncPending()
                repo.heartbeat()
                n
            }.onSuccess {
                _ui.value = _ui.value.copy(message = "$it logs uploaded")
                load()
            }.onFailure {
                _ui.value = _ui.value.copy(message = it.message)
            }
        }
    }

    fun heartbeat() {
        viewModelScope.launch {
            runCatching { repo.heartbeat() }
                .onSuccess { _ui.value = _ui.value.copy(message = "Device heartbeat sent") }
                .onFailure { _ui.value = _ui.value.copy(message = it.message) }
        }
    }
}
