package com.navratri.access

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navratri.access.data.*
import com.navratri.access.mesh.NearbyMeshManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiState(val loading:Boolean=false,val message:String?=null,val config:EventConfigEntity?=null,val scanResult:ScanResult?=null,val entries:Int=0,val inside:Int=0,val pending:Int=0)
class MainViewModel(private val repo:AppRepository,val mesh:NearbyMeshManager):ViewModel(){
    private val _ui=MutableStateFlow(UiState());val ui:StateFlow<UiState> = _ui.asStateFlow()
    fun load(){viewModelScope.launch{val c=repo.config();val s=repo.stats();_ui.value=_ui.value.copy(config=c,entries=s.first,inside=s.second,pending=s.third)}}
    fun activate(base:String,event:String,code:String,onDone:()->Unit){viewModelScope.launch{runCatching{_ui.value=_ui.value.copy(loading=true);repo.activate(base,event.toLong(),code);repo.syncPack()}.onSuccess{_ui.value=_ui.value.copy(loading=false,message="$it tickets synced");load();onDone()}.onFailure{_ui.value=_ui.value.copy(loading=false,message=it.message)}}}
    fun sync(){viewModelScope.launch{runCatching{_ui.value=_ui.value.copy(loading=true);repo.syncPack()}.onSuccess{_ui.value=_ui.value.copy(loading=false,message="$it tickets synced");load()}.onFailure{_ui.value=_ui.value.copy(loading=false,message=it.message)}}}
    fun scan(token:String,mode:String){if(_ui.value.scanResult!=null)return;viewModelScope.launch{val r=repo.scan(token,mode);_ui.value=_ui.value.copy(scanResult=r);load()}}
    fun clearResult(){_ui.value=_ui.value.copy(scanResult=null)}
    fun cloudSync(){viewModelScope.launch{runCatching{val n=repo.syncPending();repo.heartbeat();n}.onSuccess{_ui.value=_ui.value.copy(message="$it logs uploaded");load()}.onFailure{_ui.value=_ui.value.copy(message=it.message)}}}
    fun heartbeat(){viewModelScope.launch{runCatching{repo.heartbeat()}.onSuccess{_ui.value=_ui.value.copy(message="Device heartbeat sent")}.onFailure{_ui.value=_ui.value.copy(message=it.message)}}}
}
