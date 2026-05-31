# ParkShare Demo Data

Run migrations and seed the database:

```bash
cd backend
php artisan migrate:fresh --seed
```

## Demo accounts (password: `Password@123`)

| Role       | Email                      |
|------------|----------------------------|
| Admin      | admin@parkshare.test       |
| Driver     | driver@parkshare.test      |
| Driver 2   | driver2@parkshare.test     |
| Owner      | owner@parkshare.test       |
| Owner 2    | owner2@parkshare.test      |
| Technician | technician@parkshare.test  |
| Technician | technician2@parkshare.test |

Sample data includes Kathmandu-area parking spaces, bookings, reviews, favorites, SOS history, and notifications.

## API for Android

Start the server:

```bash
php artisan serve --host=0.0.0.0 --port=8000
```

- **Emulator:** `http://10.0.2.2:8000/api/` (default in `frontend/app/build.gradle.kts`)
- **Physical device:** set `API_BASE_URL` in `build.gradle.kts` to `http://<YOUR_PC_LAN_IP>:8000/api/`
