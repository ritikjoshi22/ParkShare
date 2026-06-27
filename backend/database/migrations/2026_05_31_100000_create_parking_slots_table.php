<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('parking_slots', function (Blueprint $table) {
            $table->id();
            $table->foreignId('parking_space_id')->constrained('parking_spaces')->cascadeOnDelete();
            $table->unsignedSmallInteger('slot_number');
            $table->string('label')->nullable();
            $table->enum('status', ['available', 'occupied', 'reserved', 'maintenance'])->default('available');
            $table->unsignedSmallInteger('sort_order')->default(0);
            $table->timestamps();

            $table->unique(['parking_space_id', 'slot_number']);
        });

        Schema::table('bookings', function (Blueprint $table) {
            $table->foreignId('parking_slot_id')->nullable()->after('parking_space_id')
                ->constrained('parking_slots')->nullOnDelete();
        });

        Schema::table('parking_images', function (Blueprint $table) {
            $table->boolean('is_primary')->default(false)->after('image_url');
            $table->unsignedSmallInteger('sort_order')->default(0)->after('is_primary');
        });
    }

    public function down(): void
    {
        Schema::table('parking_images', function (Blueprint $table) {
            $table->dropColumn(['is_primary', 'sort_order']);
        });

        Schema::table('bookings', function (Blueprint $table) {
            $table->dropConstrainedForeignId('parking_slot_id');
        });

        Schema::dropIfExists('parking_slots');
    }
};
