<?php

namespace App\Enums;

enum UserRole: string
{
    case Admin = 'admin';
    case Driver = 'driver';
    case Owner = 'owner';
    case Technician = 'technician';

    public static function values(): array
    {
        return array_column(self::cases(), 'value');
    }
}
