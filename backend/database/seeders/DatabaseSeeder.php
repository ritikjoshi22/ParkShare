<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        User::updateOrCreate(
            ['email' => 'admin@parkshare.test'],
            [
                'full_name' => 'ParkShare Admin',
                'phone' => '+10000000001',
                'password' => Hash::make('Password@123'),
                'role' => 'admin',
                'is_active' => true,
            ]
        );
    }
}
