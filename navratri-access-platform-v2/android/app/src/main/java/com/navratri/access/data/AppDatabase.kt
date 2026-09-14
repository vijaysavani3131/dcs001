package com.navratri.access.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities=[EventConfigEntity::class,EventDayEntity::class,TicketEntity::class,TicketDayEntity::class,ScanEventEntity::class],version=2,exportSchema=false)
abstract class AppDatabase:RoomDatabase(){
    abstract fun access():AccessDao
    companion object{@Volatile private var instance:AppDatabase?=null;fun get(c:Context)=instance?:synchronized(this){instance?:Room.databaseBuilder(c.applicationContext,AppDatabase::class.java,"navratri-access.db").fallbackToDestructiveMigration().build().also{instance=it}}}
}
