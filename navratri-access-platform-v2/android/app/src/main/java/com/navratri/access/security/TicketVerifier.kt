package com.navratri.access.security

import org.json.JSONObject
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

data class VerifiedTicket(val ticketId:Long,val eventId:Long,val code:String,val passName:String,val validDays:Set<Int>,val maxReentries:Int,val activationRequired:Boolean)

object TicketVerifier {
    fun verify(token:String, publicKeyB64:String):VerifiedTicket {
        val p=token.split('.')
        require(p.size==3 && p[0]=="NVT1") { "Unsupported QR" }
        val payload=Base64.getUrlDecoder().decode(p[1]);val sig=Base64.getUrlDecoder().decode(p[2]);val rawKey=Base64.getDecoder().decode(publicKeyB64)
        require(rawKey.size==32){"Bad event key"}
        val prefix=byteArrayOf(0x30,0x2a,0x30,0x05,0x06,0x03,0x2b,0x65,0x70,0x03,0x21,0x00);val x509=prefix+rawKey
        val pub=KeyFactory.getInstance("Ed25519").generatePublic(X509EncodedKeySpec(x509));val verifier=Signature.getInstance("Ed25519");verifier.initVerify(pub);verifier.update(payload)
        require(verifier.verify(sig)){"Fake / modified QR"}
        val o=JSONObject(String(payload,Charsets.UTF_8));val arr=o.getJSONArray("days");val days=buildSet { for(i in 0 until arr.length())add(arr.getInt(i)) }
        return VerifiedTicket(o.getLong("tid"),o.getLong("eid"),o.getString("code"),o.optString("pass","PASS"),days,o.optInt("max_reentries",2),o.optBoolean("activation_required",false))
    }
}
