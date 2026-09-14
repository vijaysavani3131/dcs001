package com.navratri.access

import android.app.Application
import com.navratri.access.data.AppDatabase
import com.navratri.access.data.AppRepository
import com.navratri.access.mesh.NearbyMeshManager

class NavratriApp:Application(){
    lateinit var db:AppDatabase
    lateinit var repository:AppRepository
    lateinit var mesh:NearbyMeshManager
    override fun onCreate(){super.onCreate();db=AppDatabase.get(this);mesh=NearbyMeshManager(this);repository=AppRepository(this,db,mesh);mesh.repository=repository}
}
