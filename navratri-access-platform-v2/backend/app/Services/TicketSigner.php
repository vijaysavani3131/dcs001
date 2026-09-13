<?php
namespace App\Services;

use App\Models\Event;
use App\Models\Ticket;
use Illuminate\Support\Facades\Crypt;
use RuntimeException;

class TicketSigner
{
    public function ensureEventKeys(Event $event): void
    {
        if ($event->public_key_b64 && $event->secret_key_ciphertext) return;
        if (! function_exists('sodium_crypto_sign_keypair')) throw new RuntimeException('PHP sodium extension is required.');
        $pair=sodium_crypto_sign_keypair();
        $event->forceFill([
            'public_key_b64'=>base64_encode(sodium_crypto_sign_publickey($pair)),
            'secret_key_ciphertext'=>Crypt::encryptString(base64_encode(sodium_crypto_sign_secretkey($pair))),
        ])->save();
    }

    public function signTicket(Ticket $ticket): string
    {
        $ticket->loadMissing(['event','passType','days']);
        $this->ensureEventKeys($ticket->event);
        $payload=[
            'v'=>1,
            'eid'=>$ticket->event_id,
            'tid'=>$ticket->id,
            'code'=>$ticket->ticket_code,
            'pass'=>$ticket->passType?->name ?? 'PASS',
            'days'=>$ticket->days->pluck('day_number')->map(fn($v)=>(int)$v)->values()->all(),
            'max_reentries'=>(int)($ticket->passType?->max_reentries ?? 2),
            'activation_required'=>$ticket->status==='inventory' || $ticket->source==='preprint',
            'iat'=>($ticket->issued_at ?? now())->timestamp,
        ];
        $json=json_encode($payload, JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE|JSON_THROW_ON_ERROR);
        $secret=base64_decode(Crypt::decryptString($ticket->event->secret_key_ciphertext), true);
        $sig=sodium_crypto_sign_detached($json, $secret);
        return 'NVT1.'.$this->b64url($json).'.'.$this->b64url($sig);
    }

    private function b64url(string $raw): string
    {
        return rtrim(strtr(base64_encode($raw), '+/', '-_'), '=');
    }
}
