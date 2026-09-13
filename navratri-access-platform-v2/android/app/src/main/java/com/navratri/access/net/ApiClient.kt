package com.navratri.access.net

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ApiClient(private val baseUrl:String,private val token:String?=null){
    private fun conn(path:String,method:String):HttpURLConnection=(URL(baseUrl.trimEnd('/')+path).openConnection() as HttpURLConnection).apply{requestMethod=method;connectTimeout=12000;readTimeout=25000;setRequestProperty("Accept","application/json");setRequestProperty("Content-Type","application/json");token?.let{setRequestProperty("X-Device-Token",it)}}
    fun post(path:String,body:JSONObject):JSONObject{val c=conn(path,"POST");c.doOutput=true;c.outputStream.use{it.write(body.toString().toByteArray())};return read(c)}
    fun get(path:String):JSONObject=read(conn(path,"GET"))
    private fun read(c:HttpURLConnection):JSONObject{val code=c.responseCode;val stream=if(code in 200..299)c.inputStream else c.errorStream;val text=stream?.bufferedReader()?.use{it.readText()}.orEmpty();if(code !in 200..299)throw IllegalStateException("HTTP $code: $text");return JSONObject(text)}
}
