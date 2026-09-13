package com.navratri.access.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName="event_config")
data class EventConfigEntity(
    @PrimaryKey val id:Int=1,
    val eventId:Long,val eventName:String,val venue:String?,val publicKeyB64:String,val meshSecret:String,val activeDay:Int,
    val deviceId:Long,val deviceName:String,val role:String,val gateName:String?,val dynamicSigned:Boolean,val minPeers:Int,
    val cooldownSeconds:Int,val capacity:Int,val warningCapacity:Int,val blockIsolated:Boolean,val autoCloseDays:Boolean,val kioskMode:Boolean,
    val themeTitle:String,val welcomeText:String,val footerText:String?,val language:String,
    val primaryColor:String,val accentColor:String,val backgroundColor:String,val successColor:String,val errorColor:String,
    val successSound:String,val errorSound:String
)

@Entity(tableName="event_days", primaryKeys=["eventId","dayNumber"])
data class EventDayEntity(val eventId:Long,val dayNumber:Int,val label:String,val startsAt:String,val endsAt:String)

@Entity(tableName="tickets",indices=[Index(value=["ticketCode"],unique=true)])
data class TicketEntity(
    @PrimaryKey val ticketId:Long,val ticketCode:String,val status:String,val qrToken:String?,val holderName:String?,val holderPhotoUrl:String?,val wristbandCode:String?,
    val passName:String,val maxReentries:Int,val peopleCount:Int=1,val identityLevel:String="QR",val cooldownSeconds:Int=0,val accessZones:String="PUBLIC",val source:String="PACK"
)

@Entity(tableName="ticket_days",primaryKeys=["ticketCode","dayNumber"])
data class TicketDayEntity(val ticketCode:String,val dayNumber:Int,val state:String="OUTSIDE",val entries:Int=0,val exits:Int=0,val reentries:Int=0,val lastEntryAt:Long?=null,val lastExitAt:Long?=null)

@Entity(tableName="scan_events",indices=[Index("syncState")])
data class ScanEventEntity(@PrimaryKey val id:String,val ticketCode:String,val dayNumber:Int,val action:String,val occurredAt:Long,val deviceId:Long,val metadata:String="{}",val syncState:String="PENDING")
