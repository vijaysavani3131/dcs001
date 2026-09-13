package com.navratri.access

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestRuntimePermissions()
        val app=application as NavratriApp
        val vm=ViewModelProvider(this,object:ViewModelProvider.Factory{override fun <T:ViewModel> create(modelClass:Class<T>):T=@Suppress("UNCHECKED_CAST") (MainViewModel(app.repository,app.mesh) as T)})[MainViewModel::class.java]
        setContent{NavratriRoot(vm,"https://eventmanagement.isavgo.com")}
        vm.load()
    }
    private fun requestRuntimePermissions(){
        val p=mutableListOf(Manifest.permission.CAMERA)
        if(Build.VERSION.SDK_INT>=33){p+=Manifest.permission.NEARBY_WIFI_DEVICES}
        if(Build.VERSION.SDK_INT>=31){p+=Manifest.permission.BLUETOOTH_SCAN;p+=Manifest.permission.BLUETOOTH_CONNECT;p+=Manifest.permission.BLUETOOTH_ADVERTISE}
        else p+=Manifest.permission.ACCESS_FINE_LOCATION
        ActivityCompat.requestPermissions(this,p.distinct().toTypedArray(),101)
    }
}
