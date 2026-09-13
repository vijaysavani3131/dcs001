<?php
namespace App\Http\Controllers;

use App\Models\{Booking,Device,Event,EventDay,InventoryBatch,PassType,ScanEvent,ThemeConfig,Ticket};
use App\Services\{TicketFactory,TicketSigner};
use Illuminate\Http\Request;
use Illuminate\Support\Str;

class AdminController
{
    public function dashboard(){
        $events=Event::withCount(['tickets','devices'])->latest()->get();
        $todayScans=ScanEvent::whereDate('occurred_at',today())->count();
        $paid=Booking::where('status','paid')->sum('amount');
        return view('admin.dashboard',compact('events','todayScans','paid'));
    }
    public function events(){return view('admin.events.index',['events'=>Event::withCount(['tickets','devices'])->latest()->get()]);}
    public function createEvent(){return view('admin.events.create');}
    public function storeEvent(Request $r, TicketSigner $signer){
        $d=$r->validate(['name'=>'required|max:150','venue'=>'nullable|max:200','timezone'=>'required|max:80','days'=>'required|integer|min:1|max:20','capacity'=>'nullable|integer|min:1']);
        $capacity=(int)($d['capacity']??12000);
        $event=Event::create(['name'=>$d['name'],'slug'=>Str::slug($d['name']).'-'.Str::lower(Str::random(5)),'venue'=>$d['venue']??null,'timezone'=>$d['timezone'],'status'=>'draft','dynamic_signed_tickets'=>true,'reentry_cooldown_seconds'=>0,'mesh_min_peers'=>2,'capacity'=>$capacity,'warning_capacity'=>(int)floor($capacity*.9),'block_isolated_scanners'=>true,'auto_close_days'=>true,'kiosk_mode'=>true]);
        $signer->ensureEventKeys($event);
        ThemeConfig::create(['event_id'=>$event->id,'primary_color'=>'#8B1538','accent_color'=>'#FFB703','background_color'=>'#10071B','success_color'=>'#16A34A','error_color'=>'#DC2626','event_title'=>$event->name,'welcome_text'=>'Swagat Hai','footer_text'=>'Shubh Navratri','language'=>'gu']);
        for($i=1;$i<=$d['days'];$i++) EventDay::create(['event_id'=>$event->id,'day_number'=>$i,'label'=>'Navratri Day '.$i,'starts_at'=>now()->addDays($i-1)->setTime(18,0),'ends_at'=>now()->addDays($i)->setTime(3,30)]);
        return redirect()->route('admin.events.show',$event)->with('ok','Event created. Configure exact dates, passes and scanner devices.');
    }
    public function showEvent(Event $event){
        $event->load(['days','passTypes','devices','theme','inventoryBatches.passType']);
        $activeDay=$this->activeDay($event);
        $base=$event->scans()->when($activeDay,fn($q)=>$q->where('event_day_id',$activeDay->id));
        $entries=(clone $base)->where('action','ENTRY')->count();
        $exits=(clone $base)->whereIn('action',['EXIT','MANUAL_EXIT'])->count();
        $inside=max(0,$entries-$exits);
        $denied=(clone $base)->where('action','like','DENIED_%')->count();
        $entryLastHour=(clone $base)->where('action','ENTRY')->where('occurred_at','>=',now()->subHour())->count();
        $capacityPct=$event->capacity?min(100,round(($inside/$event->capacity)*100,1)):0;
        $onlineDevices=$event->devices->filter(fn($d)=>$d->active && $d->last_seen_at?->gt(now()->subMinutes(10)))->count();
        $eventStats=compact('activeDay','entries','exits','inside','denied','entryLastHour','capacityPct','onlineDevices');
        $gateStats=$event->devices->map(function($d)use($event,$activeDay){
            $count=ScanEvent::where('event_id',$event->id)->where('device_id',$d->id)->when($activeDay,fn($q)=>$q->where('event_day_id',$activeDay->id))->where('action','ENTRY')->count();
            return ['device'=>$d,'entries'=>$count];
        });
        $tickets=$event->tickets()->with(['passType','days','inventoryBatch'])->latest()->limit(80)->get();
        $alerts=$event->scans()->with(['device','ticket'])->where('action','like','DENIED_%')->latest('occurred_at')->limit(12)->get();
        return view('admin.events.show',compact('event','tickets','eventStats','gateStats','alerts'));
    }
    private function activeDay(Event $event){return $event->days()->where('starts_at','<=',now())->where('ends_at','>=',now())->first() ?? $event->days()->where('starts_at','>',now())->orderBy('starts_at')->first();}
    public function updateDays(Request $r, Event $event){$rows=$r->validate(['days'=>'required|array','days.*.label'=>'required|string|max:80','days.*.starts_at'=>'required|date','days.*.ends_at'=>'required|date']);foreach($rows['days'] as $id=>$row){abort_if(strtotime($row['ends_at'])<=strtotime($row['starts_at']),422,'End time must be after start time.');$event->days()->findOrFail($id)->update($row);}return back()->with('ok','Event-day schedule updated.');}
    public function updateOperations(Request $r, Event $event){$d=$r->validate(['capacity'=>'required|integer|min:1','warning_capacity'=>'required|integer|min:1','mesh_min_peers'=>'required|integer|min:0|max:100','reentry_cooldown_seconds'=>'required|integer|min:0|max:86400','block_isolated_scanners'=>'nullable|boolean','auto_close_days'=>'nullable|boolean','kiosk_mode'=>'nullable|boolean']);$event->update($d+['block_isolated_scanners'=>$r->boolean('block_isolated_scanners'),'auto_close_days'=>$r->boolean('auto_close_days'),'kiosk_mode'=>$r->boolean('kiosk_mode')]);return back()->with('ok','Operational safety settings updated.');}
    public function addPass(Request $r, Event $event){$d=$r->validate(['name'=>'required','code'=>'required','price'=>'nullable|numeric|min:0','max_reentries'=>'required|integer|min:0|max:50','people_count'=>'required|integer|min:1|max:20','choose_days_count'=>'nullable|integer|min:1|max:20','identity_level'=>'required|in:QR,NAME,PHOTO,WRISTBAND','cooldown_seconds'=>'nullable|integer|min:0|max:86400']);$d['access_zones']=array_values(array_filter(array_map('trim',explode(',',$r->input('access_zones','PUBLIC')))));$event->passTypes()->create($d+['active'=>true]);return back()->with('ok','Pass type added.');}
    public function addDevice(Request $r, Event $event){$d=$r->validate(['name'=>'required','role'=>'required|in:ENTRY,EXIT,HANDOVER,SUPERVISOR','gate_name'=>'nullable|max:100']);$code=strtoupper(Str::random(10));$event->devices()->create(['name'=>$d['name'],'role'=>$d['role'],'gate_name'=>$d['gate_name']??null,'activation_code_hash'=>hash('sha256',$code),'active'=>true]);return back()->with('activation_code',$code)->with('ok','Device created. Save the one-time activation code.');}
    public function toggleDevice(Event $event, Device $device){abort_unless($device->event_id===$event->id,404);$active=!$device->active;$device->update(['active'=>$active,'revoked_at'=>$active?null:now(),'token_hash'=>$active?$device->token_hash:null]);return back()->with('ok',$active?'Device re-enabled.':'Device revoked immediately.');}
    public function updateTheme(Request $r, Event $event){$d=$r->validate(['primary_color'=>'required','accent_color'=>'required','background_color'=>'required','success_color'=>'required','error_color'=>'required','event_title'=>'required','welcome_text'=>'required','footer_text'=>'nullable','background_asset_url'=>'nullable|url','logo_url'=>'nullable|url','language'=>'required|in:en,hi,gu','success_sound'=>'required','error_sound'=>'required']);$event->theme()->updateOrCreate(['event_id'=>$event->id],$d);return back()->with('ok','Scanner theme updated.');}
    public function createTicket(Request $r, Event $event, TicketFactory $factory){$d=$r->validate(['pass_type_id'=>'required|exists:pass_types,id','day_ids'=>'required|array|min:1','day_ids.*'=>'exists:event_days,id','holder_name'=>'nullable|max:120','holder_phone'=>'nullable|max:30']);$pass=PassType::where('event_id',$event->id)->findOrFail($d['pass_type_id']);$ticket=$factory->create($event,$pass,$d['day_ids'],'active','admin',$d['holder_name']??null,$d['holder_phone']??null);return back()->with('ok','Ticket '.$ticket->ticket_code.' created.')->with('ticket_token',$ticket->qr_token);}
    public function createInventoryBatch(Request $r, Event $event, TicketFactory $factory){
        $d=$r->validate(['pass_type_id'=>'required|exists:pass_types,id','day_ids'=>'required|array|min:1','day_ids.*'=>'exists:event_days,id','quantity'=>'required|integer|min:1|max:5000','assigned_to'=>'nullable|max:120','notes'=>'nullable|max:1000']);
        $pass=PassType::where('event_id',$event->id)->findOrFail($d['pass_type_id']);
        $batch=InventoryBatch::create(['event_id'=>$event->id,'pass_type_id'=>$pass->id,'batch_code'=>'B'.now()->format('ymd').'-'.strtoupper(Str::random(6)),'status'=>'generated','quantity'=>$d['quantity'],'day_ids'=>$d['day_ids'],'assigned_to'=>$d['assigned_to']??null,'notes'=>$d['notes']??null]);
        for($i=0;$i<$d['quantity'];$i++){$ticket=$factory->create($event,$pass,$d['day_ids'],'inventory','preprint');$ticket->update(['inventory_batch_id'=>$batch->id]);}
        return redirect()->route('admin.batches.show',[$event,$batch])->with('ok','Pre-print batch generated. Tickets remain INVALID until HANDOVER scan.');
    }
    public function showBatch(Event $event, InventoryBatch $batch){abort_unless($batch->event_id===$event->id,404);$batch->load(['passType','tickets.days']);return view('admin.batches.show',compact('event','batch'));}
    public function markBatchPrinted(Event $event, InventoryBatch $batch){abort_unless($batch->event_id===$event->id,404);$batch->update(['status'=>'printed','printed_at'=>now()]);$batch->tickets()->update(['printed_at'=>now()]);return back()->with('ok','Batch marked printed. QR tickets are still INVENTORY and cannot enter.');}
    public function markBatchHandover(Request $r, Event $event, InventoryBatch $batch){abort_unless($batch->event_id===$event->id,404);$d=$r->validate(['assigned_to'=>'required|max:120']);$batch->update(['status'=>'at_counter','assigned_to'=>$d['assigned_to'],'handed_over_at'=>now()]);return back()->with('ok','Physical stock assigned to '.$d['assigned_to'].'. Each card activates only when HANDOVER mode scans it.');}
    public function activateInventory(Request $r, Event $event, Ticket $ticket){abort_unless($ticket->event_id===$event->id,404);abort_unless($ticket->status==='inventory',422);$ticket->update(['status'=>'active','activated_at'=>now(),'handed_over_at'=>now()]);return back()->with('ok','Inventory ticket activated.');}
}
