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
        Schema::create('parking_spaces', function (Blueprint $table) {
            $table->id();
            
            $table->foreignId('owner_id')
                ->constrained('users')
                ->onDelete('cascade');

            $table->string('parking_name');

            $table->text('description')->nullable();

            $table->text('address');

            $table->decimal('latitude', 10, 7);

            $table->decimal('longitude', 10, 7);

            $table->decimal('price_per_hour', 10, 2);

            $table->integer('total_slots');

            $table->integer('available_slots');

            $table->enum('vehicle_type', [
                'bike',
                'car',
                'both'
            ])->default('car');

            $table->time('opening_time');

            $table->time('closing_time');

            $table->boolean('is_verified')->default(false);

            $table->boolean('is_active')->default(true);

            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('parking_spaces');
    }
};
