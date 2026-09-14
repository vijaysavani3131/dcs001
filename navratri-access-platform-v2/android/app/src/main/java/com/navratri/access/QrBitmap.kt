package com.navratri.access

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

fun qrBitmap(text:String,size:Int=800):Bitmap{val m=QRCodeWriter().encode(text,BarcodeFormat.QR_CODE,size,size);return Bitmap.createBitmap(size,size,Bitmap.Config.RGB_565).also{b->for(x in 0 until size)for(y in 0 until size)b.setPixel(x,y,if(m[x,y])android.graphics.Color.BLACK else android.graphics.Color.WHITE)}}
