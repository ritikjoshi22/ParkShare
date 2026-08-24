<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('owner_profiles', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->unique()->constrained()->cascadeOnDelete();
            $table->string('status')->default('draft');
            $table->unsignedTinyInteger('current_step')->default(1);
            $table->json('step_data')->nullable();
            $table->timestamp('submitted_at')->nullable();
            $table->timestamp('verified_at')->nullable();
            $table->timestamp('rejected_at')->nullable();
            $table->text('rejection_reason')->nullable();
            $table->timestamps();

            $table->index('status');
        });

        Schema::create('owner_documents', function (Blueprint $table) {
            $table->id();
            $table->foreignId('owner_profile_id')->constrained()->cascadeOnDelete();
            $table->string('document_type');
            $table->string('file_path');
            $table->string('original_name')->nullable();
            $table->string('mime_type')->nullable();
            $table->unsignedBigInteger('file_size')->nullable();
            $table->string('status')->default('pending');
            $table->text('rejection_reason')->nullable();
            $table->timestamps();

            $table->index(['owner_profile_id', 'document_type']);
        });

        Schema::create('parking_technicians', function (Blueprint $table) {
            $table->id();
            $table->foreignId('parking_space_id')->constrained()->cascadeOnDelete();
            $table->string('name');
            $table->string('phone');
            $table->string('alternate_phone')->nullable();
            $table->string('email')->nullable();
            $table->string('specialization')->default('general_mechanic');
            $table->text('description')->nullable();
            $table->string('availability_status')->default('available');
            $table->boolean('is_primary')->default(false);
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->index(['parking_space_id', 'is_active']);
            $table->index(['parking_space_id', 'is_primary']);
        });

        // Legacy users who registered with role=owner get an approved profile
        if (Schema::hasTable('users')) {
            $ownerIds = DB::table('users')->where('role', 'owner')->pluck('id');
            foreach ($ownerIds as $userId) {
                DB::table('owner_profiles')->insert([
                    'user_id' => $userId,
                    'status' => 'approved',
                    'current_step' => 4,
                    'verified_at' => now(),
                    'created_at' => now(),
                    'updated_at' => now(),
                ]);
            }
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('parking_technicians');
        Schema::dropIfExists('owner_documents');
        Schema::dropIfExists('owner_profiles');
    }
};
