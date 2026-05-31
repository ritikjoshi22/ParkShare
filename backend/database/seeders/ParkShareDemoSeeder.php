<?php

namespace Database\Seeders;

use App\Models\Booking;
use App\Models\FavoriteParking;
use App\Models\Notification;
use App\Models\ParkingImage;
use App\Models\ParkingSpace;
use App\Models\Review;
use App\Models\SOSRequest;
use App\Models\Technician;
use App\Models\TechnicianService;
use App\Models\User;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class ParkShareDemoSeeder extends Seeder
{
  public function run(): void
  {
    $password = Hash::make('Password@123');

    $admin = User::updateOrCreate(
      ['email' => 'admin@parkshare.test'],
      [
        'full_name' => 'ParkShare Admin',
        'phone' => '+9779800000001',
        'password' => $password,
        'role' => 'admin',
        'address' => 'Durbar Marg, Kathmandu',
        'latitude' => 27.7058,
        'longitude' => 85.3173,
        'is_active' => true,
      ]
    );

    $driver = User::updateOrCreate(
      ['email' => 'driver@parkshare.test'],
      [
        'full_name' => 'Suman Thapa',
        'phone' => '+9779801111001',
        'password' => $password,
        'role' => 'driver',
        'address' => 'Thamel, Kathmandu',
        'latitude' => 27.7150,
        'longitude' => 85.3123,
        'is_active' => true,
      ]
    );

    $driver2 = User::updateOrCreate(
      ['email' => 'driver2@parkshare.test'],
      [
        'full_name' => 'Anita Gurung',
        'phone' => '+9779801111002',
        'password' => $password,
        'role' => 'driver',
        'address' => 'Patan, Lalitpur',
        'latitude' => 27.6727,
        'longitude' => 85.3252,
        'is_active' => true,
      ]
    );

    $owner = User::updateOrCreate(
      ['email' => 'owner@parkshare.test'],
      [
        'full_name' => 'Rajesh Karki',
        'phone' => '+9779802222001',
        'password' => $password,
        'role' => 'owner',
        'address' => 'New Road, Kathmandu',
        'latitude' => 27.7038,
        'longitude' => 85.3114,
        'is_active' => true,
      ]
    );

    $owner2 = User::updateOrCreate(
      ['email' => 'owner2@parkshare.test'],
      [
        'full_name' => 'Mina Shrestha',
        'phone' => '+9779802222002',
        'password' => $password,
        'role' => 'owner',
        'address' => 'Maharajgunj, Kathmandu',
        'latitude' => 27.7347,
        'longitude' => 85.3323,
        'is_active' => true,
      ]
    );

    $technicianUser = User::updateOrCreate(
      ['email' => 'technician@parkshare.test'],
      [
        'full_name' => 'Bikash Tamang',
        'phone' => '+9779803333001',
        'password' => $password,
        'role' => 'technician',
        'address' => 'Kalanki, Kathmandu',
        'latitude' => 27.6934,
        'longitude' => 85.2809,
        'is_active' => true,
      ]
    );

    $technicianUser2 = User::updateOrCreate(
      ['email' => 'technician2@parkshare.test'],
      [
        'full_name' => 'Prakash Rai',
        'phone' => '+9779803333002',
        'password' => $password,
        'role' => 'technician',
        'address' => 'Koteshwor, Kathmandu',
        'latitude' => 27.6782,
        'longitude' => 85.3487,
        'is_active' => true,
      ]
    );

    $technician = Technician::updateOrCreate(
      ['user_id' => $technicianUser->id],
      [
        'specialization' => 'Roadside Assistance',
        'experience_years' => 8,
        'service_radius_km' => 15,
        'availability_status' => 'available',
        'description' => '24/7 emergency towing, battery jump-start, and flat tire repair across Kathmandu Valley.',
        'hourly_rate' => 800.00,
      ]
    );

    $technician2 = Technician::updateOrCreate(
      ['user_id' => $technicianUser2->id],
      [
        'specialization' => 'Auto Electrician',
        'experience_years' => 5,
        'service_radius_km' => 12,
        'availability_status' => 'available',
        'description' => 'Specialist in vehicle electrical faults, fuse replacement, and on-site diagnostics.',
        'hourly_rate' => 650.00,
      ]
    );

    $services = [
      $technician->id => [
        'Emergency Towing',
        'Battery Jump Start',
        'Flat Tire Change',
        'Fuel Delivery',
        'Lockout Assistance',
      ],
      $technician2->id => [
        'Electrical Diagnostics',
        'Alternator Repair',
        'Starter Motor Fix',
        'Fuse & Wiring Repair',
        'Headlight Replacement',
      ],
    ];

    foreach ($services as $technicianId => $names) {
      foreach ($names as $name) {
        TechnicianService::firstOrCreate([
          'technician_id' => $technicianId,
          'service_name' => $name,
        ]);
      }
    }

    $parkings = [
      [
        'owner_id' => $owner->id,
        'parking_name' => 'Kathmandu Mall Parking',
        'description' => 'Multi-story secure parking near Kathmandu Mall with CCTV and 24/7 attendant.',
        'address' => 'Kanti Path, Kathmandu 44600',
        'latitude' => 27.7029,
        'longitude' => 85.3120,
        'price_per_hour' => 50.00,
        'total_slots' => 120,
        'available_slots' => 34,
        'vehicle_type' => 'both',
        'opening_time' => '06:00:00',
        'closing_time' => '22:00:00',
        'images' => [
          'https://images.unsplash.com/photo-1506521781263-d8422e82f27a?w=800',
        ],
      ],
      [
        'owner_id' => $owner->id,
        'parking_name' => 'Civil Mall Underground',
        'description' => 'Covered underground parking for Civil Mall shoppers and staff.',
        'address' => 'Sundhara, Kathmandu 44600',
        'latitude' => 27.7006,
        'longitude' => 85.3121,
        'price_per_hour' => 60.00,
        'total_slots' => 200,
        'available_slots' => 78,
        'vehicle_type' => 'car',
        'opening_time' => '08:00:00',
        'closing_time' => '21:00:00',
        'images' => [
          'https://images.unsplash.com/photo-1590674899484-d5640e854f66?w=800',
        ],
      ],
      [
        'owner_id' => $owner2->id,
        'parking_name' => 'Bhatbhateni Maharajgunj',
        'description' => 'Open and covered slots for supermarket visitors at Maharajgunj.',
        'address' => 'Maharajgunj, Kathmandu 44600',
        'latitude' => 27.7347,
        'longitude' => 85.3323,
        'price_per_hour' => 40.00,
        'total_slots' => 90,
        'available_slots' => 22,
        'vehicle_type' => 'both',
        'opening_time' => '07:00:00',
        'closing_time' => '22:00:00',
        'images' => [
          'https://images.unsplash.com/photo-1565689157206-0fddef7589a2?w=800',
        ],
      ],
      [
        'owner_id' => $owner2->id,
        'parking_name' => 'New Road City Parking',
        'description' => 'Central parking in busy New Road area, ideal for short shopping stops.',
        'address' => 'New Road, Kathmandu 44600',
        'latitude' => 27.7038,
        'longitude' => 85.3114,
        'price_per_hour' => 55.00,
        'total_slots' => 60,
        'available_slots' => 8,
        'vehicle_type' => 'car',
        'opening_time' => '07:00:00',
        'closing_time' => '20:00:00',
        'images' => [
          'https://images.unsplash.com/photo-1573348722427-f1d6819fdf98?w=800',
        ],
      ],
      [
        'owner_id' => $owner->id,
        'parking_name' => 'Patan Durbar Square Parking',
        'description' => 'Tourist-friendly parking near Patan Durbar Square heritage site.',
        'address' => 'Patan Durbar Square, Lalitpur 44700',
        'latitude' => 27.6727,
        'longitude' => 85.3252,
        'price_per_hour' => 45.00,
        'total_slots' => 50,
        'available_slots' => 15,
        'vehicle_type' => 'both',
        'opening_time' => '06:00:00',
        'closing_time' => '23:00:00',
        'images' => [
          'https://images.unsplash.com/photo-1489515217757-5fd1be406fef?w=800',
        ],
      ],
      [
        'owner_id' => $owner2->id,
        'parking_name' => 'Thamel Backpackers Lot',
        'description' => 'Budget-friendly open parking for bikes and cars in Thamel tourist district.',
        'address' => 'Thamel, Kathmandu 44600',
        'latitude' => 27.7150,
        'longitude' => 85.3123,
        'price_per_hour' => 35.00,
        'total_slots' => 40,
        'available_slots' => 12,
        'vehicle_type' => 'bike',
        'opening_time' => '00:00:00',
        'closing_time' => '23:59:59',
        'images' => [
          'https://images.unsplash.com/photo-1549924231-f129b911e442?w=800',
        ],
      ],
    ];

    $parkingModels = [];
    foreach ($parkings as $data) {
      $images = $data['images'];
      unset($data['images']);
      $data['is_verified'] = true;
      $data['is_active'] = true;

      $space = ParkingSpace::updateOrCreate(
        [
          'owner_id' => $data['owner_id'],
          'parking_name' => $data['parking_name'],
        ],
        $data
      );

      foreach ($images as $url) {
        ParkingImage::firstOrCreate(
          [
            'parking_space_id' => $space->id,
            'image_url' => $url,
          ]
        );
      }

      $parkingModels[] = $space;
    }

    $bookings = [
      [
        'user_id' => $driver->id,
        'parking_space_id' => $parkingModels[0]->id,
        'booking_date' => now()->toDateString(),
        'start_time' => now()->addHours(1),
        'end_time' => now()->addHours(3),
        'total_hours' => 2,
        'total_amount' => 100.00,
        'booking_status' => 'confirmed',
      ],
      [
        'user_id' => $driver->id,
        'parking_space_id' => $parkingModels[2]->id,
        'booking_date' => now()->subDay()->toDateString(),
        'start_time' => now()->subDay()->setHour(10),
        'end_time' => now()->subDay()->setHour(12),
        'total_hours' => 2,
        'total_amount' => 80.00,
        'booking_status' => 'completed',
      ],
      [
        'user_id' => $driver2->id,
        'parking_space_id' => $parkingModels[4]->id,
        'booking_date' => now()->addDay()->toDateString(),
        'start_time' => now()->addDay()->setHour(14),
        'end_time' => now()->addDay()->setHour(16),
        'total_hours' => 2,
        'total_amount' => 90.00,
        'booking_status' => 'pending',
      ],
    ];

    foreach ($bookings as $booking) {
      Booking::updateOrCreate(
        [
          'user_id' => $booking['user_id'],
          'parking_space_id' => $booking['parking_space_id'],
          'booking_date' => $booking['booking_date'],
        ],
        $booking
      );
    }

    $reviews = [
      [
        'user_id' => $driver->id,
        'parking_space_id' => $parkingModels[0]->id,
        'rating' => 5,
        'review_text' => 'Very secure and easy to find. Attendant was helpful.',
      ],
      [
        'user_id' => $driver2->id,
        'parking_space_id' => $parkingModels[2]->id,
        'rating' => 4,
        'review_text' => 'Good location for Bhatbhateni shopping. A bit crowded on weekends.',
      ],
      [
        'user_id' => $driver->id,
        'technician_id' => $technician->id,
        'rating' => 5,
        'review_text' => 'Arrived within 20 minutes for a flat tire. Highly recommended!',
      ],
    ];

    foreach ($reviews as $review) {
      Review::firstOrCreate(
        [
          'user_id' => $review['user_id'],
          'parking_space_id' => $review['parking_space_id'] ?? null,
          'technician_id' => $review['technician_id'] ?? null,
        ],
        $review
      );
    }

    FavoriteParking::firstOrCreate([
      'user_id' => $driver->id,
      'parking_space_id' => $parkingModels[0]->id,
    ]);

    FavoriteParking::firstOrCreate([
      'user_id' => $driver->id,
      'parking_space_id' => $parkingModels[4]->id,
    ]);

    SOSRequest::updateOrCreate(
      [
        'user_id' => $driver2->id,
        'status' => 'resolved',
      ],
      [
        'latitude' => 27.6727,
        'longitude' => 85.3252,
        'emergency_message' => 'Car battery dead near Patan Durbar Square',
        'technician_id' => $technician->id,
        'resolved_at' => now()->subDays(2),
      ]
    );

    $notifications = [
      [
        'user_id' => $driver->id,
        'title' => 'Booking Confirmed',
        'message' => 'Your booking at Kathmandu Mall Parking is confirmed.',
        'type' => 'booking',
        'is_read' => false,
      ],
      [
        'user_id' => $driver->id,
        'title' => 'Welcome to ParkShare',
        'message' => 'Find nearby parking and SOS assistance across Kathmandu.',
        'type' => 'system',
        'is_read' => true,
      ],
      [
        'user_id' => $owner->id,
        'title' => 'New Booking',
        'message' => 'New booking at Kathmandu Mall Parking.',
        'type' => 'booking',
        'is_read' => false,
      ],
      [
        'user_id' => $technicianUser->id,
        'title' => 'SOS Assigned',
        'message' => 'A new emergency SOS request has been assigned to you.',
        'type' => 'sos',
        'is_read' => false,
      ],
    ];

    foreach ($notifications as $notification) {
      Notification::firstOrCreate(
        [
          'user_id' => $notification['user_id'],
          'title' => $notification['title'],
        ],
        $notification
      );
    }
  }
}
