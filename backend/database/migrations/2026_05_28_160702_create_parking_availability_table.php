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
        Schema::create('parking_availability', function (Blueprint $table) {
            $table->id();
            $table->foreignId('parking_space_id')
                ->constrained('parking_spaces')
                ->onDelete('cascade');

            $table->date('available_date');

            $table->time('start_time');

            $table->time('end_time');

            $table->boolean('is_available')->default(true);
            $table->unique(['parking_space_id', 'available_date', 'start_time', 'end_time']);

            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('parking_availability');
    }
};
