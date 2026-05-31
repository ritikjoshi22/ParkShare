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

## Filament admin panel (web)

After seeding, start the server and open:

**http://127.0.0.1:8000/admin**

| Field    | Value                  |
|----------|------------------------|
| Email    | admin@parkshare.test   |
| Password | Password@123           |

Admin features: user management (suspend/activate), parking approval, booking overview, SOS assignment & resolution, dispute/report resolution, analytics dashboard.

## API for Android

Start the server:

```bash
php artisan serve --host=0.0.0.0 --port=8000
```

- **Emulator:** `http://10.0.2.2:8000/api/v1/` (update `API_BASE_URL` in `frontend/app/build.gradle.kts`)
- **Physical device:** set `API_BASE_URL` in `build.gradle.kts` to `http://<YOUR_PC_LAN_IP>:8000/api/v1/`

## Maps (Android)

- **In-app map view:** OpenStreetMap via osmdroid (Home → Open Map, parking details preview, full map screen).
- **Turn-by-turn navigation:** Tap **Navigate** on parking details or SOS requests — opens the **Google Maps app** (or browser fallback). No Maps SDK API key is required in the app.
- For emulators, use a system image **with Google Play** and install/update Google Maps if navigation does not launch.
