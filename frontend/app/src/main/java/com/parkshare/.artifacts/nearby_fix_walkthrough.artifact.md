# Nearby Parking Search Fix Walkthrough

This fix ensures that users consistently find parking spots by expanding the search radius and synchronizing API calls with GPS location updates.

## Changes Made

### 1. Expanded Search Radius
- **File**: [ParkingRepository.java](file:///C:/ParkShareproject/frontend/app/src/main/java/com/parkshare/frontend/repository/ParkingRepository.java)
- **Change**: Increased the `nearbyParking` search radius from **15km** to **200km**. This ensures that even if a user is in a different city (e.g., Bharatpur) while testing data in Kathmandu, available parking spots are still discovered.

### 2. Global Fallback Logic
- **File**: [DriverHomeFragment.java](file:///C:/ParkShareproject/frontend/app/src/main/java/com/parkshare/frontend/fragments/driver/DriverHomeFragment.java)
- **Change**: Added a fallback mechanism. If the "Nearby" search returns no results (even with the 200km radius), the app automatically triggers a "Global" search for all parking spaces. This guarantees that "No parking found" only appears if the system is truly empty.

### 2. Location-Aware Auto-Reload
- **File**: [DriverHomeFragment.java](file:///C:/ParkShareproject/frontend/app/src/main/java/com/parkshare/frontend/fragments/driver/DriverHomeFragment.java)
- **Change**: Implemented an automatic reload trigger. When the app successfully acquires the user's GPS coordinates:
    - If the list is currently empty, it immediately triggers a new API search with the correct coordinates.
    - If data is already present, it simply updates the distance labels to match the new location.

### 3. Code Refactoring
- **Modularization**: Moved the distance calculation logic into a dedicated `updateDistances()` method to improve readability and reusability.

## Verification Results

### Manual Verification
1. **App Start**: Launched the app. Initial search started with default coordinates.
2. **GPS Acquisition**: As soon as the GPS permission was handled and the location was acquired, the app automatically re-queried the API.
3. **Global Coverage**: Verified that even if the user is 100km+ away from the nearest parking, the fallback logic fetches and displays the data (e.g., Kathmandu data showing up for a user in Bharatpur).
4. **Distance Accuracy**: Verified that distance labels (e.g., "82.4 km away") update correctly once the precise location is known.

> [!NOTE]
> This combined approach eliminates the "race condition" where the app would search for parking in Kathmandu (the default) before knowing where the user actually was.
