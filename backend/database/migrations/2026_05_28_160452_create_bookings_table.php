<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('bookings', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')
                ->constrained('users')
                ->onDelete('cascade');

            $table->foreignId('parking_space_id')
                ->constrained('parking_spaces')
                ->onDelete('cascade');

            $table->date('booking_date');

            $table->dateTime('start_time');

            $table->dateTime('end_time');

            $table->decimal('total_hours', 5, 2);

            $table->decimal('total_amount', 10, 2);

            $table->enum('booking_status', [
                'pending',
                'confirmed',
                'completed',
                'cancelled'
            ])->default('pending');

            $table->string('qr_code')->nullable();

            $table->dateTime('checked_in_at')->nullable();

            $table->dateTime('checked_out_at')->nullable();

            $table->decimal('overtime_fee', 10, 2)->default(0);
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('bookings');
    }
};
