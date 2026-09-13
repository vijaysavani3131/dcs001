<?php
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\{Device,Event,ScanEvent,Ticket};
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class DeviceController extends Controller
{
    public function activate(Request $r){
        $d=$r->validate(['event_id'=>'required|integer','activation_code'=>'required|string','device_fingerprint'=>'nullable|string|max:255']);
        $hash=hash('sha256',strtoupper($d['activation_code']));
        $device=Device::where('event_id',$d['event_id'])->where('activation_code_hash',$hash)->where('active',true)->whereNull('revoked_at')->first();
        if(!$device)return response()->json(['message'=>'Invalid or revoked activation code'],422);
        $plain=Str::random(64);$device->update(['token_hash'=>hash('sha256',$plain),'last_seen_at'=>now(),'mesh_status'=>'provisioned']);
        return response()->json(['device_token'=>$plain,'device'=>['id'=>$device->id,'name'=>$device->name,'role'=>$device->role,'gate_name'=>$device->gate_name,'event_id'=>$device->event_id]]);
    }

    public function pack(Request $r, Event $event){
        /** @var Device $device */$device=$r->attributes->get('device');abort_unless($device->event_id===$event->id,403);
        $cursor=max(0,(int)$r->query('cursor',0));$limit=min(3000,max(200,(int)$r->query('limit',2000)));
        $tickets=$event->tickets()->with(['passType:id,name,max_reentries,people_count,identity_level,cooldown_seconds,access_zones','days:id,day_number'])->where('id','>',$cursor)->orderBy('id')->limit($limit)->get();
        $next=$tickets->count()===$limit?$tickets->last()->id:null;
        $activeDay=$event->days()->where('starts_at','<=',now())->where('ends_at','>=',now())->first() ?? $event->days()->where('starts_at','>',now())->orderBy('starts_at')->first();
        return response()->json([
            'pack_version'=>now()->timestamp,
            'next_cursor'=>$next,
            'event'=>[
                'id'=>$event->id,'name'=>$event->name,'venue'=>$event->venue,'timezone'=>$event->timezone,'public_key_b64'=>$event->public_key_b64,
                'dynamic_signed_tickets'=>(bool)$event->dynamic_signed_tickets,'mesh_secret'=>hash_hmac('sha256','mesh:'.$event->id,config('app.key')),
                'mesh_min_peers'=>(int)$event->mesh_min_peers,'reentry_cooldown_seconds'=>(int)$event->reentry_cooldown_seconds,'active_day'=>$activeDay?->day_number,
                'capacity'=>(int)$event->capacity,'warning_capacity'=>(int)$event->warning_capacity,'block_isolated_scanners'=>(bool)$event->block_isolated_scanners,
                'auto_close_days'=>(bool)$event->auto_close_days,'kiosk_mode'=>(bool)$event->kiosk_mode,
            ],
            'device'=>['id'=>$device->id,'name'=>$device->name,'role'=>$device->role,'gate_name'=>$device->gate_name],
            'theme'=>$event->theme,
            'days'=>$event->days()->get(['id','day_number','label','starts_at','ends_at']),
            'tickets'=>$tickets->map(fn($t)=>[
                'id'=>$t->id,'code'=>$t->ticket_code,'status'=>$t->status,'token'=>$t->qr_token,'holder_name'=>$t->holder_name,'holder_photo_url'=>$t->holder_photo_url,'wristband_code'=>$t->wristband_code,
                'pass'=>$t->passType?->name,'max_reentries'=>(int)($t->passType?->max_reentries??2),'people_count'=>(int)($t->passType?->people_count??1),
                'identity_level'=>$t->passType?->identity_level??'QR','cooldown_seconds'=>(int)($t->passType?->cooldown_seconds??$event->reentry_cooldown_seconds),
                'access_zones'=>$t->passType?->access_zones??['PUBLIC'],'days'=>$t->days->pluck('day_number')->map(fn($x)=>(int)$x)->values()
            ]),
        ]);
    }

    public function heartbeat(Request $r, Event $event){
        /** @var Device $device */$device=$r->attributes->get('device');abort_unless($device->event_id===$event->id,403);
        $d=$r->validate(['peer_count'=>'required|integer|min:0|max:200','mesh_status'=>'required|in:secure,limited,isolated,provisioned','coordinator_id'=>'nullable|integer']);
        $device->update(['last_seen_at'=>now(),'last_peer_count'=>$d['peer_count'],'mesh_status'=>$d['mesh_status']]);
        return response()->json(['ok'=>true,'device_active'=>(bool)$device->active,'server_time'=>now()->toIso8601String()]);
    }

    public function sync(Request $r, Event $event){
        /** @var Device $device */$device=$r->attributes->get('device');abort_unless($device->event_id===$event->id,403);
        $d=$r->validate(['events'=>'required|array|max:5000','events.*.id'=>'required|uuid','events.*.ticket_code'=>'required','events.*.day_number'=>'required|integer','events.*.action'=>['required','string','regex:/^(ENTRY|EXIT|MANUAL_EXIT|ACTIVATE|DENIED_[A-Z_]+)$/'],'events.*.occurred_at'=>'required|date','events.*.metadata'=>'nullable|array']);
        DB::transaction(function()use($d,$event,$device){foreach($d['events'] as $e){$ticket=Ticket::where('event_id',$event->id)->where('ticket_code',$e['ticket_code'])->first();if(!$ticket)continue;$day=$event->days()->where('day_number',$e['day_number'])->first();if(!$day)continue;ScanEvent::firstOrCreate(['id'=>$e['id']],['event_id'=>$event->id,'ticket_id'=>$ticket->id,'device_id'=>$device->id,'event_day_id'=>$day->id,'action'=>$e['action'],'occurred_at'=>$e['occurred_at'],'metadata'=>$e['metadata']??[]]);if($e['action']==='ACTIVATE' && $ticket->status==='inventory')$ticket->update(['status'=>'active','activated_at'=>now(),'handed_over_at'=>now()]);}});
        return response()->json(['ok'=>true]);
    }
}
