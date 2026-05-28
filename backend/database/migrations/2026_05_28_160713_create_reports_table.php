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
        Schema::create('reports', function (Blueprint $table) {
            $table->id();
            $table->foreignId('reported_by')
                ->constrained('users')
                ->onDelete('cascade');

            $table->foreignId('booking_id')
                ->nullable()
                ->constrained('bookings')
                ->onDelete('cascade');

            $table->foreignId('parking_space_id')
                ->nullable()
                ->constrained('parking_spaces')
                ->onDelete('cascade');

            $table->text('report_reason');

            $table->enum('status', [
                'pending',
                'resolved'
            ])->default('pending');
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('reports');
    }
};
