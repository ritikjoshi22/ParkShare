<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('system_settings', function (Blueprint $table) {
            $table->id();
            $table->string('key')->unique();
            $table->json('value');
            $table->string('group')->default('booking');
            $table->string('label')->nullable();
            $table->timestamps();
        });

        Schema::create('pricing_rules', function (Blueprint $table) {
            $table->id();
            $table->foreignId('parking_space_id')->nullable()->constrained()->cascadeOnDelete();
            $table->unsignedTinyInteger('hour_from')->default(1);
            $table->unsignedTinyInteger('hour_to')->nullable();
            $table->decimal('rate', 10, 2);
            $table->string('rule_type')->default('hourly');
            $table->decimal('daily_cap', 10, 2)->nullable();
            $table->boolean('is_active')->default(true);
            $table->unsignedSmallInteger('sort_order')->default(0);
            $table->timestamps();
        });

        Schema::create('booking_payments', function (Blueprint $table) {
            $table->id();
            $table->foreignId('booking_id')->constrained()->cascadeOnDelete();
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->string('type');
            $table->decimal('amount', 10, 2);
            $table->string('currency', 3)->default('usd');
            $table->string('payment_status')->default('pending');
            $table->string('stripe_payment_intent_id')->nullable();
            $table->string('stripe_charge_id')->nullable();
            $table->json('metadata')->nullable();
            $table->json('receipt')->nullable();
            $table->timestamps();
        });

        Schema::table('bookings', function (Blueprint $table) {
            $table->string('payment_status')->default('pending')->after('overtime_fee');
            $table->decimal('amount_due', 10, 2)->default(0)->after('payment_status');
            $table->dateTime('original_end_time')->nullable()->after('end_time');
            $table->unsignedInteger('extended_minutes')->default(0)->after('original_end_time');
        });

        Schema::table('parking_spaces', function (Blueprint $table) {
            $table->unsignedSmallInteger('booking_buffer_minutes')->nullable()->after('closing_time');
            $table->unsignedSmallInteger('early_check_in_minutes')->nullable()->after('booking_buffer_minutes');
        });
    }

    public function down(): void
    {
        Schema::table('parking_spaces', function (Blueprint $table) {
            $table->dropColumn(['booking_buffer_minutes', 'early_check_in_minutes']);
        });

        Schema::table('bookings', function (Blueprint $table) {
            $table->dropColumn(['payment_status', 'amount_due', 'original_end_time', 'extended_minutes']);
        });

        Schema::dropIfExists('booking_payments');
        Schema::dropIfExists('pricing_rules');
        Schema::dropIfExists('system_settings');
    }
};
