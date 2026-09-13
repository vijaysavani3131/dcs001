package com.navratri.access.mesh

import android.content.Context
import android.util.Base64
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.navratri.access.data.AppRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class NearbyMeshManager(private val context:Context){
    private val client=Nearby.getConnectionsClient(context)
    private val strategy=Strategy.P2P_CLUSTER
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private val endpoints=Collections.synchronizedSet(mutableSetOf<String>())
    private val peerDevices=ConcurrentHashMap<String,Long>()
    private val claims=ConcurrentHashMap<String,MutableSet<String>>()
    private val _peerCount=MutableStateFlow(0);val peerCount:StateFlow<Int> = _peerCount
    private val _coordinatorId=MutableStateFlow(0L);val coordinatorId:StateFlow<Long> = _coordinatorId
    var repository:AppRepository?=null
    private var eventId:Long=0;private var deviceId:Long=0;private var meshSecret:String=""

    fun start(eventId:Long,deviceId:Long,deviceName:String,role:String,secret:String){
        stop();this.eventId=eventId;this.deviceId=deviceId;this.meshSecret=secret;_coordinatorId.value=deviceId
        val endpointName="$eventId|$deviceId|$role|$deviceName".take(120)
        client.startAdvertising(endpointName,"com.navratri.access.$eventId",connectionCallback,AdvertisingOptions.Builder().setStrategy(strategy).build())
        client.startDiscovery("com.navratri.access.$eventId",endpointCallback,DiscoveryOptions.Builder().setStrategy(strategy).build())
    }
    fun stop(){client.stopAdvertising();client.stopDiscovery();client.stopAllEndpoints();endpoints.clear();peerDevices.clear();_peerCount.value=0;if(deviceId>0)_coordinatorId.value=deviceId}
    fun status(minPeers:Int)=when{_peerCount.value>=minPeers->"secure";_peerCount.value>0->"limited";else->"isolated"}

    suspend fun claim(ticket:String,day:Int,action:String,waitMs:Long=350):Boolean{
        val key="$ticket|$day|$action";val mine="${deviceId.toString().padStart(12,'0')}-${UUID.randomUUID()}"
        claims.computeIfAbsent(key){Collections.synchronizedSet(mutableSetOf())}.add(mine)
        send("CLAIM",JSONObject().put("key",key).put("claim",mine))
        delay(waitMs)
        val winner=claims[key]?.minOrNull();scope.launch{delay(1800);claims.remove(key)}
        return winner==mine
    }
    fun broadcastCommit(scanId:String,ticket:String,day:Int,action:String,occurredAt:Long){send("COMMIT",JSONObject().put("id",scanId).put("ticket",ticket).put("day",day).put("action",action).put("at",occurredAt).put("device",deviceId))}
    fun broadcastActivation(ticket:String,day:Int){send("ACTIVATE",JSONObject().put("ticket",ticket).put("day",day).put("at",System.currentTimeMillis()).put("device",deviceId))}

    private val endpointCallback=object:EndpointDiscoveryCallback(){
        override fun onEndpointFound(endpointId:String,info:DiscoveredEndpointInfo){
            parseDeviceId(info.endpointName)?.let{peerDevices[endpointId]=it;electCoordinator()}
            client.requestConnection("D$deviceId",endpointId,connectionCallback)
        }
        override fun onEndpointLost(endpointId:String){endpoints.remove(endpointId);peerDevices.remove(endpointId);refresh()}
    }
    private val connectionCallback=object:ConnectionLifecycleCallback(){
        override fun onConnectionInitiated(endpointId:String,info:ConnectionInfo){client.acceptConnection(endpointId,payloadCallback)}
        override fun onConnectionResult(endpointId:String,result:ConnectionResolution){if(result.status.isSuccess){endpoints.add(endpointId);refresh();sendTo(endpointId,"HELLO",JSONObject().put("event",eventId).put("device",deviceId))}}
        override fun onDisconnected(endpointId:String){endpoints.remove(endpointId);peerDevices.remove(endpointId);refresh()}
    }
    private val payloadCallback=object:PayloadCallback(){
        override fun onPayloadReceived(endpointId:String,payload:Payload){payload.asBytes()?.let{handle(endpointId,String(it,Charsets.UTF_8))}}
        override fun onPayloadTransferUpdate(endpointId:String,update:PayloadTransferUpdate){}
    }
    private fun handle(endpointId:String,raw:String){runCatching{
        val env=JSONObject(raw);val body=String(Base64.decode(env.getString("body"),Base64.NO_WRAP or Base64.URL_SAFE),Charsets.UTF_8)
        if(!constantTime(mac(body),env.getString("mac")))return
        val m=JSONObject(body);if(m.optLong("event")!=eventId)return
        when(m.getString("type")){
            "HELLO"->{val d=m.getJSONObject("data");peerDevices[endpointId]=d.getLong("device");refresh()}
            "CLAIM"->{val d=m.getJSONObject("data");claims.computeIfAbsent(d.getString("key")){Collections.synchronizedSet(mutableSetOf())}.add(d.getString("claim"))}
            "COMMIT"->{val d=m.getJSONObject("data");scope.launch{repository?.applyPeerCommit(d)}}
            "ACTIVATE"->{val d=m.getJSONObject("data");scope.launch{repository?.applyPeerActivation(d)}}
        }
    }}
    private fun refresh(){_peerCount.value=endpoints.size;electCoordinator()}
    private fun electCoordinator(){val candidates=peerDevices.values.toMutableList();if(deviceId>0)candidates+=deviceId;_coordinatorId.value=candidates.minOrNull()?:deviceId}
    private fun parseDeviceId(name:String):Long?=name.split('|').getOrNull(1)?.toLongOrNull()
    private fun send(type:String,data:JSONObject){val bytes=envelope(type,data);synchronized(endpoints){endpoints.forEach{client.sendPayload(it,Payload.fromBytes(bytes))}}}
    private fun sendTo(endpoint:String,type:String,data:JSONObject){client.sendPayload(endpoint,Payload.fromBytes(envelope(type,data)))}
    private fun envelope(type:String,data:JSONObject):ByteArray{val body=JSONObject().put("type",type).put("event",eventId).put("from",deviceId).put("data",data).toString();return JSONObject().put("body",Base64.encodeToString(body.toByteArray(),Base64.NO_WRAP or Base64.URL_SAFE)).put("mac",mac(body)).toString().toByteArray()}
    private fun mac(s:String):String{val m=Mac.getInstance("HmacSHA256");m.init(SecretKeySpec(meshSecret.toByteArray(),"HmacSHA256"));return m.doFinal(s.toByteArray()).joinToString(""){"%02x".format(it)}}
    private fun constantTime(a:String,b:String):Boolean{if(a.length!=b.length)return false;var x=0;for(i in a.indices)x=x or (a[i].code xor b[i].code);return x==0}
}
