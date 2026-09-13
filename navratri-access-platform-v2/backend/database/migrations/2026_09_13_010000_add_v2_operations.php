<?php
use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::table('events', function (Blueprint $t) {
            $t->unsignedInteger('capacity')->default(12000)->after('mesh_min_peers');
            $t->unsignedInteger('warning_capacity')->default(10800)->after('capacity');
            $t->boolean('block_isolated_scanners')->default(true)->after('warning_capacity');
            $t->boolean('auto_close_days')->default(true)->after('block_isolated_scanners');
            $t->boolean('kiosk_mode')->default(true)->after('auto_close_days');
        });
        Schema::table('pass_types', function (Blueprint $t) {
            $t->unsignedSmallInteger('people_count')->default(1)->after('max_reentries');
            $t->unsignedSmallInteger('choose_days_count')->nullable()->after('people_count');
            $t->string('identity_level')->default('QR')->after('choose_days_count');
            $t->json('access_zones')->nullable()->after('identity_level');
            $t->unsignedInteger('cooldown_seconds')->nullable()->after('access_zones');
        });
        Schema::create('inventory_batches', function (Blueprint $t) {
            $t->id();
            $t->foreignId('event_id')->constrained()->cascadeOnDelete();
            $t->foreignId('pass_type_id')->constrained()->cascadeOnDelete();
            $t->string('batch_code')->unique();
            $t->string('status')->default('generated')->index();
            $t->unsignedInteger('quantity');
            $t->json('day_ids');
            $t->string('assigned_to')->nullable();
            $t->text('notes')->nullable();
            $t->timestamp('printed_at')->nullable();
            $t->timestamp('handed_over_at')->nullable();
            $t->timestamps();
        });
        Schema::table('tickets', function (Blueprint $t) {
            $t->foreignId('inventory_batch_id')->nullable()->after('pass_type_id')->constrained('inventory_batches')->nullOnDelete();
            $t->string('holder_photo_url')->nullable()->after('holder_phone');
            $t->string('wristband_code', 40)->nullable()->after('holder_photo_url');
            $t->timestamp('printed_at')->nullable()->after('activated_at');
            $t->timestamp('handed_over_at')->nullable()->after('printed_at');
        });
        Schema::table('devices', function (Blueprint $t) {
            $t->string('gate_name')->nullable()->after('role');
            $t->unsignedSmallInteger('last_peer_count')->default(0)->after('last_seen_at');
            $t->string('mesh_status')->default('unknown')->after('last_peer_count');
            $t->timestamp('revoked_at')->nullable()->after('active');
        });
        Schema::table('theme_configs', function (Blueprint $t) {
            $t->string('logo_url')->nullable()->after('background_asset_url');
            $t->string('language', 10)->default('en')->after('logo_url');
            $t->string('success_sound')->default('success')->after('language');
            $t->string('error_sound')->default('alert')->after('success_sound');
        });
    }

    public function down(): void
    {
        Schema::table('theme_configs', fn (Blueprint $t) => $t->dropColumn(['logo_url','language','success_sound','error_sound']));
        Schema::table('devices', fn (Blueprint $t) => $t->dropColumn(['gate_name','last_peer_count','mesh_status','revoked_at']));
        Schema::table('tickets', function (Blueprint $t) {
            $t->dropConstrainedForeignId('inventory_batch_id');
            $t->dropColumn(['holder_photo_url','wristband_code','printed_at','handed_over_at']);
        });
        Schema::dropIfExists('inventory_batches');
        Schema::table('pass_types', fn (Blueprint $t) => $t->dropColumn(['people_count','choose_days_count','identity_level','access_zones','cooldown_seconds']));
        Schema::table('events', fn (Blueprint $t) => $t->dropColumn(['capacity','warning_capacity','block_isolated_scanners','auto_close_days','kiosk_mode']));
    }
};
