<?php
namespace App\Models;
use Illuminate\Database\Eloquent\Model;
class InventoryBatch extends Model {
    protected $fillable=['event_id','pass_type_id','batch_code','status','quantity','day_ids','assigned_to','notes','printed_at','handed_over_at'];
    protected $casts=['day_ids'=>'array','printed_at'=>'datetime','handed_over_at'=>'datetime'];
    public function event(){return $this->belongsTo(Event::class);}
    public function passType(){return $this->belongsTo(PassType::class);}
    public function tickets(){return $this->hasMany(Ticket::class);}
}
