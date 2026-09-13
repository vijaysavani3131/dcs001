package com.navratri.access.data

import android.content.Context
import androidx.room.withTransaction
import com.navratri.access.mesh.NearbyMeshManager
import com.navratri.access.net.ApiClient
import com.navratri.access.security.TicketVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID

data class ScanResult(val allowed:Boolean,val title:String,val detail:String,val code:String?=null,val passName:String?=null,val state:String?=null,val holderName:String?=null,val peopleCount:Int=1)
data class ActivationResult(val eventId:Long,val deviceId:Long,val deviceName:String,val role:String)

class AppRepository(private val context:Context,private val db:AppDatabase,private val mesh:NearbyMeshManager){
    private val dao=db.access();private val prefs=context.getSharedPreferences("access",Context.MODE_PRIVATE)
    fun savedBaseUrl()=prefs.getString("base_url","http://10.0.2.2:8000")!!
    fun hasDeviceToken()=!prefs.getString("device_token",null).isNullOrBlank()

    suspend fun activate(base:String,eventId:Long,code:String):ActivationResult=withContext(Dispatchers.IO){
        val r=ApiClient(base).post("/api/v1/devices/activate",JSONObject().put("event_id",eventId).put("activation_code",code))
        val d=r.getJSONObject("device")
        prefs.edit().putString("base_url",base).putString("device_token",r.getString("device_token")).putLong("event_id",d.getLong("event_id")).putLong("device_id",d.getLong("id")).putString("device_name",d.getString("name")).putString("role",d.getString("role")).putString("gate_name",d.optString("gate_name","")).apply()
        ActivationResult(d.getLong("event_id"),d.getLong("id"),d.getString("name"),d.getString("role"))
    }

    suspend fun syncPack():Int=withContext(Dispatchers.IO){
        val eventId=prefs.getLong("event_id",0);val token=prefs.getString("device_token","")!!;val api=ApiClient(savedBaseUrl(),token);var cursor=0L;var count=0;var first=true
        do {
            val root=api.get("/api/v1/events/$eventId/pack?cursor=$cursor&limit=2000");val ev=root.getJSONObject("event");val theme=root.optJSONObject("theme")?:JSONObject();val dev=root.optJSONObject("device")?:JSONObject();val deviceId=prefs.getLong("device_id",0)
            if(first){
                val days=root.getJSONArray("days");val dayEntities=mutableListOf<EventDayEntity>();for(i in 0 until days.length()){val d=days.getJSONObject(i);dayEntities+=EventDayEntity(eventId,d.getInt("day_number"),d.getString("label"),d.getString("starts_at"),d.getString("ends_at"))}
                val active=ev.optInt("active_day",dayEntities.minOfOrNull{it.dayNumber}?:1)
                val gateRaw=dev.optString("gate_name",prefs.getString("gate_name","") ?: "");val gate=gateRaw.takeIf{it.isNotBlank()}
                db.withTransaction{
                    dao.putConfig(EventConfigEntity(eventId=eventId,eventName=ev.getString("name"),venue=ev.optString("venue",null),publicKeyB64=ev.getString("public_key_b64"),meshSecret=ev.getString("mesh_secret"),activeDay=active,deviceId=deviceId,deviceName=dev.optString("name",prefs.getString("device_name","DEVICE") ?: "DEVICE"),role=dev.optString("role",prefs.getString("role","ENTRY") ?: "ENTRY"),gateName=gate,dynamicSigned=ev.optBoolean("dynamic_signed_tickets",true),minPeers=ev.optInt("mesh_min_peers",1),cooldownSeconds=ev.optInt("reentry_cooldown_seconds",0),capacity=ev.optInt("capacity",12000),warningCapacity=ev.optInt("warning_capacity",10800),blockIsolated=ev.optBoolean("block_isolated_scanners",true),autoCloseDays=ev.optBoolean("auto_close_days",true),kioskMode=ev.optBoolean("kiosk_mode",true),themeTitle=theme.optString("event_title",ev.getString("name")),welcomeText=theme.optString("welcome_text","Swagat Hai"),footerText=theme.optString("footer_text",null),language=theme.optString("language","en"),primaryColor=theme.optString("primary_color","#8B1538"),accentColor=theme.optString("accent_color","#FFB703"),backgroundColor=theme.optString("background_color","#10071B"),successColor=theme.optString("success_color","#16A34A"),errorColor=theme.optString("error_color","#DC2626"),successSound=theme.optString("success_sound","success"),errorSound=theme.optString("error_sound","alert")))
                    dao.clearDays();dao.putDays(dayEntities)
                };first=false
            }
            val arr=root.getJSONArray("tickets")
            for(i in 0 until arr.length()){
                val t=arr.getJSONObject(i);val zones=t.optJSONArray("access_zones")?.let{a->(0 until a.length()).joinToString(","){a.optString(it)}}?:"PUBLIC"
                val te=TicketEntity(t.getLong("id"),t.getString("code"),t.getString("status"),t.optString("token",null),t.optString("holder_name",null),t.optString("holder_photo_url",null),t.optString("wristband_code",null),t.optString("pass","PASS"),t.optInt("max_reentries",2),t.optInt("people_count",1),t.optString("identity_level","QR"),t.optInt("cooldown_seconds",ev.optInt("reentry_cooldown_seconds",0)),zones)
                val local=dao.ticket(te.ticketCode);dao.putTicket(if(local?.status=="active" && te.status=="inventory")te.copy(status="active") else te);val valid=t.getJSONArray("days");val ds=mutableListOf<TicketDayEntity>();for(j in 0 until valid.length())ds+=TicketDayEntity(te.ticketCode,valid.getInt(j));dao.putTicketDays(ds);count++
            }
            cursor=root.optLong("next_cursor",0)
        }while(cursor>0)
        dao.config()?.let{mesh.start(it.eventId,it.deviceId,it.deviceName,it.role,it.meshSecret)};count
    }

    suspend fun config()=dao.config()
    suspend fun stats():Triple<Int,Int,Int>{val c=dao.config()?:return Triple(0,0,0);return Triple(dao.entriesToday(c.activeDay),dao.insideNow(c.activeDay),dao.pendingCount())}

    private suspend fun denied(c:EventConfigEntity,ticket:String,reason:String,detail:String){
        dao.putScan(ScanEventEntity(UUID.randomUUID().toString(),ticket,c.activeDay,"DENIED_$reason",System.currentTimeMillis(),c.deviceId,JSONObject().put("detail",detail).toString()))
    }

    suspend fun scan(token:String,mode:String):ScanResult=withContext(Dispatchers.IO){
        val c=dao.config()?:return@withContext ScanResult(false,"NOT READY","Sync event pack first")
        val v=runCatching{TicketVerifier.verify(token,c.publicKeyB64)}.getOrElse{return@withContext ScanResult(false,"INVALID QR",it.message?:"Signature failed")}
        if(v.eventId!=c.eventId){denied(c,v.code,"WRONG_EVENT","Ticket belongs to another event");return@withContext ScanResult(false,"WRONG EVENT","This pass belongs to another event",v.code,v.passName)}
        if(c.activeDay !in v.validDays){denied(c,v.code,"WRONG_DAY","Not valid on day ${c.activeDay}");return@withContext ScanResult(false,"NOT VALID TODAY","Valid days: ${v.validDays.sorted().joinToString()}",v.code,v.passName)}
        var t=dao.ticket(v.code)
        if(t==null){
            if(!c.dynamicSigned){return@withContext ScanResult(false,"UNKNOWN TICKET","Final pack does not contain this ticket",v.code,v.passName)}
            t=TicketEntity(v.ticketId,v.code,if(v.activationRequired)"inventory" else "active",token,null,null,null,v.passName,v.maxReentries,1,"QR",c.cooldownSeconds,"PUBLIC","DYNAMIC_SIGNED");dao.putTicket(t);v.validDays.forEach{dao.putTicketDay(TicketDayEntity(v.code,it))}
        }
        val action=mode.uppercase();val now=System.currentTimeMillis()

        if(action=="HANDOVER"){
            if(t.status!="inventory"){denied(c,v.code,"ALREADY_HANDED","Ticket status ${t.status}");return@withContext ScanResult(false,"ALREADY HANDED OVER","This pre-printed card is already active or unavailable",v.code,t.passName,holderName=t.holderName,peopleCount=t.peopleCount)}
            if(c.blockIsolated && mesh.peerCount.value<c.minPeers){denied(c,v.code,"ISOLATED","HANDOVER blocked without mesh");return@withContext ScanResult(false,"SCANNER ISOLATED","Peers ${mesh.peerCount.value}/${c.minPeers}. Do not hand over this card.",v.code,t.passName)}
            if(!mesh.claim(v.code,c.activeDay,"HANDOVER")){denied(c,v.code,"DUPLICATE_RACE","Concurrent handover");return@withContext ScanResult(false,"DUPLICATE RACE","Another counter is processing this card",v.code,t.passName)}
            dao.setTicketStatus(v.code,"active");val id=UUID.randomUUID().toString();dao.putScan(ScanEventEntity(id,v.code,c.activeDay,"ACTIVATE",now,c.deviceId));mesh.broadcastActivation(v.code,c.activeDay)
            return@withContext ScanResult(true,"HANDOVER APPROVED","Give this pre-printed card to the visitor. It is now valid at entry.",v.code,t.passName,"ACTIVE",t.holderName,t.peopleCount)
        }

        if(t.status=="inventory"){denied(c,v.code,"HANDOVER_REQUIRED","Pre-print stock not activated");return@withContext ScanResult(false,"HANDOVER REQUIRED","Pre-printed card has not been issued to a visitor",v.code,t.passName)}
        if(t.status in setOf("cancelled","blocked","expired")){denied(c,v.code,t.status.uppercase(),"Access denied");return@withContext ScanResult(false,t.status.uppercase(),"Access denied",v.code,t.passName)}
        val d=dao.ticketDay(v.code,c.activeDay)?:return@withContext ScanResult(false,"NOT ENTITLED","No entitlement for this event day",v.code,t.passName)
        if(action=="ENTRY"){
            val inside=dao.insideNow(c.activeDay);if(c.capacity>0 && inside+t.peopleCount>c.capacity){denied(c,v.code,"CAPACITY","Venue capacity ${c.capacity} reached");return@withContext ScanResult(false,"CAPACITY REACHED","Entry paused by venue capacity control",v.code,t.passName,d.state,t.holderName,t.peopleCount)}
            if(d.state=="INSIDE"){denied(c,v.code,"ALREADY_INSIDE","Exit required");return@withContext ScanResult(false,"EXIT REQUIRED","Already inside. Exit scan required before re-entry.",v.code,t.passName,"INSIDE",t.holderName,t.peopleCount)}
            if(d.entries>0 && d.reentries>=t.maxReentries){denied(c,v.code,"REENTRY_LIMIT","Nightly re-entry limit reached");return@withContext ScanResult(false,"RE-ENTRY LIMIT","Nightly re-entry limit reached",v.code,t.passName,"OUTSIDE",t.holderName,t.peopleCount)}
            val cooldown=if(t.cooldownSeconds>0)t.cooldownSeconds else c.cooldownSeconds
            if(d.lastExitAt!=null && cooldown>0 && now-d.lastExitAt<cooldown*1000L){denied(c,v.code,"COOLDOWN","Re-entry cooldown");return@withContext ScanResult(false,"RE-ENTRY COOLDOWN","Please wait before re-entry",v.code,t.passName,"OUTSIDE",t.holderName,t.peopleCount)}
        } else if(action=="EXIT" && d.state!="INSIDE"){
            denied(c,v.code,"ALREADY_OUTSIDE","No active entry");return@withContext ScanResult(false,"ALREADY OUTSIDE","No active entry found",v.code,t.passName,"OUTSIDE",t.holderName,t.peopleCount)
        } else if(action !in setOf("ENTRY","EXIT")) return@withContext ScanResult(false,"BAD MODE",action)

        if(c.blockIsolated && mesh.peerCount.value<c.minPeers){denied(c,v.code,"ISOLATED","Peers ${mesh.peerCount.value}/${c.minPeers}");return@withContext ScanResult(false,"SCANNER ISOLATED","Connected peers ${mesh.peerCount.value}/${c.minPeers}. Supervisor required.",v.code,t.passName,d.state,t.holderName,t.peopleCount)}
        if(!mesh.claim(v.code,c.activeDay,action)){denied(c,v.code,"DUPLICATE_RACE","Another gate processing");return@withContext ScanResult(false,"DUPLICATE RACE","Another gate is processing this pass",v.code,t.passName,d.state,t.holderName,t.peopleCount)}
        val id=UUID.randomUUID().toString()
        db.withTransaction{
            val fresh=dao.ticketDay(v.code,c.activeDay)?:d
            if(action=="ENTRY")dao.putTicketDay(fresh.copy(state="INSIDE",entries=fresh.entries+1,reentries=fresh.reentries+(if(fresh.entries>0)1 else 0),lastEntryAt=now)) else dao.putTicketDay(fresh.copy(state="OUTSIDE",exits=fresh.exits+1,lastExitAt=now))
            dao.putScan(ScanEventEntity(id,v.code,c.activeDay,action,now,c.deviceId,JSONObject().put("people",t.peopleCount).put("pass",t.passName).toString()))
        }
        mesh.broadcastCommit(id,v.code,c.activeDay,action,now)
        ScanResult(true,if(action=="ENTRY" && d.entries>0)"RE-ENTRY OK" else "$action APPROVED",if(action=="ENTRY")c.welcomeText else "Exit recorded. Re-entry is now available.",v.code,t.passName,if(action=="ENTRY")"INSIDE" else "OUTSIDE",t.holderName,t.peopleCount)
    }

    suspend fun applyPeerActivation(o:JSONObject){dao.setTicketStatus(o.getString("ticket"),"active")}
    suspend fun applyPeerCommit(o:JSONObject){val id=o.getString("id");if(dao.scanExists(id))return;val code=o.getString("ticket");val day=o.getInt("day");val action=o.getString("action");val at=o.getLong("at");val d=dao.ticketDay(code,day)?:return;db.withTransaction{if(action=="ENTRY" && d.state!="INSIDE")dao.putTicketDay(d.copy(state="INSIDE",entries=d.entries+1,reentries=d.reentries+(if(d.entries>0)1 else 0),lastEntryAt=at));if(action=="EXIT" && d.state=="INSIDE")dao.putTicketDay(d.copy(state="OUTSIDE",exits=d.exits+1,lastExitAt=at));dao.putScan(ScanEventEntity(id,code,day,action,at,o.optLong("device",0),syncState="PENDING"))}}

    suspend fun heartbeat():Boolean=withContext(Dispatchers.IO){val c=dao.config()?:return@withContext false;val token=prefs.getString("device_token","")?:return@withContext false;ApiClient(savedBaseUrl(),token).post("/api/v1/events/${c.eventId}/heartbeat",JSONObject().put("peer_count",mesh.peerCount.value).put("mesh_status",mesh.status(c.minPeers)).put("coordinator_id",mesh.coordinatorId.value));true}
    suspend fun syncPending():Int=withContext(Dispatchers.IO){val c=dao.config()?:return@withContext 0;val token=prefs.getString("device_token","")?:return@withContext 0;val rows=dao.pending(1000);if(rows.isEmpty())return@withContext 0;val arr=JSONArray();rows.forEach{arr.put(JSONObject().put("id",it.id).put("ticket_code",it.ticketCode).put("day_number",it.dayNumber).put("action",it.action).put("occurred_at",Instant.ofEpochMilli(it.occurredAt).toString()).put("metadata",JSONObject(it.metadata)))};ApiClient(savedBaseUrl(),token).post("/api/v1/events/${c.eventId}/sync",JSONObject().put("events",arr));dao.markSynced(rows.map{it.id});rows.size}
}
