package com.navratri.access

import android.graphics.Color as AColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.navratri.access.data.EventConfigEntity
import com.navratri.access.data.ScanResult

fun hex(s:String,fallback:Long)=runCatching{Color(AColor.parseColor(s))}.getOrElse{Color(fallback)}

@Composable fun NavratriRoot(vm:MainViewModel,defaultUrl:String){
    val ui by vm.ui.collectAsStateWithLifecycle();var activated by remember{mutableStateOf(ui.config!=null)}
    LaunchedEffect(ui.config){if(ui.config!=null)activated=true}
    MaterialTheme(colorScheme=darkColorScheme()){if(!activated)ActivationScreen(defaultUrl,ui.loading,ui.message){b,e,c->vm.activate(b,e,c){activated=true}} else Home(vm,ui)}
}

@Composable fun ActivationScreen(defaultUrl:String,loading:Boolean,msg:String?,onActivate:(String,String,String)->Unit){
    var base by remember{mutableStateOf(defaultUrl)};var event by remember{mutableStateOf("")};var code by remember{mutableStateOf("")}
    Surface(Modifier.fillMaxSize(),color=Color(0xFF0B0610)){Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.Center){
        Box(Modifier.size(58.dp).background(Color(0xFF8B1538),RoundedCornerShape(18.dp)),contentAlignment=Alignment.Center){Text("🪔",fontSize=30.sp)}
        Spacer(Modifier.height(18.dp));Text("Navratri Access OS",fontSize=30.sp,fontWeight=FontWeight.Black,color=Color.White);Text("Secure offline scanner provisioning",color=Color(0xFFB9A9C2),modifier=Modifier.padding(top=5.dp,bottom=24.dp))
        OutlinedTextField(base,{base=it},label={Text("Backend URL")},modifier=Modifier.fillMaxWidth());OutlinedTextField(event,{event=it},label={Text("Event ID")},modifier=Modifier.fillMaxWidth());OutlinedTextField(code,{code=it.uppercase()},label={Text("One-time activation code")},modifier=Modifier.fillMaxWidth())
        Button({onActivate(base,event,code)},enabled=!loading && event.isNotBlank()&&code.isNotBlank(),modifier=Modifier.fillMaxWidth().padding(top=12.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFFB703),contentColor=Color(0xFF261600))){Text(if(loading)"Preparing offline pack…" else "Activate scanner",fontWeight=FontWeight.Bold)}
        msg?.let{Text(it,color=Color.White,modifier=Modifier.padding(top=12.dp))}
    }}
}

@Composable fun Home(vm:MainViewModel,ui:UiState){
    val c=ui.config?:return;val peers by vm.mesh.peerCount.collectAsState();val coordinator by vm.mesh.coordinatorId.collectAsState()
    var tab by remember(c.role){mutableStateOf(when(c.role){"EXIT"->"EXIT";"HANDOVER"->"HANDOVER";else->"ENTRY"})}
    val tabs=when(c.role){"ENTRY"->listOf("ENTRY","STATUS");"EXIT"->listOf("EXIT","STATUS");"HANDOVER"->listOf("HANDOVER","STATUS");else->listOf("ENTRY","EXIT","HANDOVER","STATUS")}
    if(tab !in tabs)tab=tabs.first()
    Surface(Modifier.fillMaxSize(),color=hex(c.backgroundColor,0xFF10071B)){Column(Modifier.fillMaxSize()){Header(c,peers,coordinator,ui);ModeTabs(tabs,tab,c){tab=it};when(tab){"ENTRY","EXIT","HANDOVER"->ScannerPane(vm,ui,tab);else->StatusPane(vm,ui,peers,coordinator)}}}
}

@Composable private fun Header(c:EventConfigEntity,peers:Int,coordinator:Long,ui:UiState){
    val secure=peers>=c.minPeers;val status=if(secure)"SECURE" else if(peers>0)"LIMITED" else "ISOLATED"
    Column(Modifier.fillMaxWidth().background(hex(c.primaryColor,0xFF8B1538)).padding(horizontal=16.dp,vertical=13.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(c.themeTitle,color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Black);Text("${c.deviceName}${c.gateName?.let{" · $it"}?:""} · Day ${c.activeDay}",color=Color.White.copy(.75f),fontSize=12.sp)}
            Row(Modifier.background(if(secure)Color(0xFF123522) else Color(0xFF4A1E1E),RoundedCornerShape(99.dp)).padding(horizontal=10.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(7.dp).background(if(secure)Color(0xFF4ADE80) else Color(0xFFF87171),CircleShape));Spacer(Modifier.width(6.dp));Text(status,color=Color.White,fontSize=11.sp,fontWeight=FontWeight.Bold)}}
        Spacer(Modifier.height(11.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){MiniStat("INSIDE",ui.inside.toString(),Modifier.weight(1f));MiniStat("ENTRIES",ui.entries.toString(),Modifier.weight(1f));MiniStat("PEERS","$peers/${c.minPeers}",Modifier.weight(1f));MiniStat("LEADER",if(coordinator==c.deviceId)"THIS" else "#$coordinator",Modifier.weight(1f))}
    }
}
@Composable private fun MiniStat(label:String,value:String,modifier:Modifier=Modifier){Column(modifier.background(Color.White.copy(.08f),RoundedCornerShape(10.dp)).padding(8.dp)){Text(label,color=Color.White.copy(.6f),fontSize=9.sp,fontWeight=FontWeight.Bold);Text(value,color=Color.White,fontSize=15.sp,fontWeight=FontWeight.Black)}}

@Composable private fun ModeTabs(tabs:List<String>,selected:String,c:EventConfigEntity,onSelect:(String)->Unit){
    Row(Modifier.fillMaxWidth().background(Color(0xFF0C0611)).padding(7.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){tabs.forEach{m->val active=m==selected;Box(Modifier.weight(1f).background(if(active)hex(c.accentColor,0xFFFFB703) else Color(0xFF1E1325),RoundedCornerShape(10.dp)).clickable{onSelect(m)}.padding(vertical=9.dp),contentAlignment=Alignment.Center){Text(if(m=="HANDOVER")"HANDOVER" else m,color=if(active)Color(0xFF241400) else Color(0xFFD2C3D8),fontSize=11.sp,fontWeight=FontWeight.Black)}}}
}

@Composable fun ScannerPane(vm:MainViewModel,ui:UiState,mode:String){
    val c=ui.config?:return;Box(Modifier.fillMaxSize()){
        QrCamera(Modifier.fillMaxSize()){vm.scan(it,mode)}
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.18f)))
        Column(Modifier.align(Alignment.TopCenter).padding(top=18.dp),horizontalAlignment=Alignment.CenterHorizontally){
            val title=when(mode){"ENTRY"->"Scan visitor pass";"EXIT"->"Scan compulsory exit";else->"Scan pre-printed card before handover"}
            Text(title,color=Color.White,fontWeight=FontWeight.Black,fontSize=18.sp);Text(if(mode=="HANDOVER")"INVENTORY → ACTIVE" else "Keep QR inside the frame",color=Color.White.copy(.75f),fontSize=12.sp)
        }
        Box(Modifier.align(Alignment.Center).size(252.dp).border(3.dp,hex(c.accentColor,0xFFFFB703),RoundedCornerShape(28.dp)))
        Row(Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth().background(Color(0xDD0A060E),RoundedCornerShape(16.dp)).padding(12.dp),verticalAlignment=Alignment.CenterVertically){
            Box(Modifier.size(38.dp).background(if(vm.mesh.peerCount.collectAsState().value>=c.minPeers)Color(0xFF123522) else Color(0xFF4A1E1E),CircleShape),contentAlignment=Alignment.Center){Text(if(mode=="HANDOVER")"🤝" else "QR",fontSize=14.sp)}
            Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(mode,color=Color.White,fontWeight=FontWeight.Black);Text(if(mode=="HANDOVER")"Activate only when giving physical card" else "Offline verification + mesh duplicate lock",color=Color(0xFFBAA9C2),fontSize=11.sp)}
        }
        ui.scanResult?.let{ResultOverlay(it,c,Modifier.align(Alignment.BottomCenter)){vm.clearResult()}}
    }
}

@Composable private fun ResultOverlay(r:ScanResult,c:EventConfigEntity,modifier:Modifier,onDismiss:()->Unit){
    val col=if(r.allowed)hex(c.successColor,0xFF16A34A) else hex(c.errorColor,0xFFDC2626)
    Box(modifier.fillMaxWidth().background(Color(0xAA000000)).padding(14.dp)){
        Column(Modifier.fillMaxWidth().background(col,RoundedCornerShape(24.dp)).clickable{onDismiss()}.padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally){
            Text(if(r.allowed)"✓" else "✕",fontSize=48.sp,color=Color.White,fontWeight=FontWeight.Black);Text(r.title,fontSize=25.sp,fontWeight=FontWeight.Black,color=Color.White,textAlign=TextAlign.Center)
            r.holderName?.takeIf{it.isNotBlank()}?.let{Text(it,color=Color.White,fontSize=17.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=4.dp))}
            Row(Modifier.padding(top=8.dp),horizontalArrangement=Arrangement.spacedBy(7.dp)){r.passName?.let{Badge(it)};r.code?.let{Badge(it.takeLast(6))};if(r.peopleCount>1)Badge("${r.peopleCount} PEOPLE")}
            Text(r.detail,color=Color.White.copy(.92f),textAlign=TextAlign.Center,modifier=Modifier.padding(top=12.dp));Text("Tap anywhere to continue",color=Color.White.copy(.65f),fontSize=11.sp,modifier=Modifier.padding(top=14.dp))
        }
    }
}
@Composable private fun Badge(text:String){Text(text,Modifier.background(Color.White.copy(.14f),RoundedCornerShape(99.dp)).padding(horizontal=9.dp,vertical=5.dp),color=Color.White,fontSize=10.sp,fontWeight=FontWeight.Bold)}

@Composable fun StatusPane(vm:MainViewModel,ui:UiState,peers:Int,coordinator:Long){val c=ui.config?:return;Column(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Scanner health",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Black);HealthCard("Offline ticket pack","READY","50k+ local records supported",true);HealthCard("Scanner mesh",if(peers>=c.minPeers)"SECURE" else "CHECK","$peers connected peers · coordinator #$coordinator",peers>=c.minPeers);HealthCard("Pending cloud logs",ui.pending.toString(),"Scanning does not depend on internet",true);HealthCard("Venue capacity","${ui.inside}/${c.capacity}",if(ui.inside>=c.warningCapacity)"Capacity warning active" else "Within configured safety limit",ui.inside<c.capacity);ui.message?.let{Text(it,color=Color(0xFFD7C8DE))};Button({vm.cloudSync()},Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=hex(c.accentColor,0xFFFFB703),contentColor=Color(0xFF241400))){Text("Internet available: sync logs + heartbeat",fontWeight=FontWeight.Bold)};OutlinedButton({vm.sync()},Modifier.fillMaxWidth()){Text("Refresh full event pack")};Text("Live gates remain offline. Wi‑Fi/Bluetooth Nearby permissions must stay enabled for duplicate protection.",color=Color(0xFF9D8BA7),fontSize=12.sp)}}
@Composable private fun HealthCard(title:String,value:String,detail:String,ok:Boolean){Row(Modifier.fillMaxWidth().background(Color(0xFF17101D),RoundedCornerShape(15.dp)).border(1.dp,Color(0xFF302037),RoundedCornerShape(15.dp)).padding(14.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(10.dp).background(if(ok)Color(0xFF4ADE80) else Color(0xFFFBBF24),CircleShape));Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text(title,color=Color.White,fontWeight=FontWeight.Bold);Text(detail,color=Color(0xFF9E8EAA),fontSize=11.sp)};Text(value,color=Color.White,fontWeight=FontWeight.Black)}}
