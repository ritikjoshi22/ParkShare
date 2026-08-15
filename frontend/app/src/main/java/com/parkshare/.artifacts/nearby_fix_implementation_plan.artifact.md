# Fix Persistent "No Parking Found" on Home Screen

This task addresses the issue where "No parking found nearby" is displayed even when there is data, or if the radius is too small for the user's location.

## Problem Analysis
1. **Hardcoded Radius**: The `getNearby` call in `ParkingRepository` uses a 15km radius, but `DriverHomeFragment` calls it with `1` as the radius parameter (Wait, looking at `loadParkingData` in `DriverHomeFragment.java`):
   ```java
   parkingRepository.getNearby(userLat, userLng, 1, new RepositoryCallback<List<ParkingSpaceDto>>() { ... })
   ```
   Actually, `ParkingRepository.getNearby` signature is:
   ```java
   public void getNearby(double lat, double lng, int page, RepositoryCallback<List<ParkingSpaceDto>> callback) {
       enqueue(api.nearbyParking(lat, lng, 15, page, 15), callback);
   }
   ```
   It seems the `1` passed from the fragment is actually the `page` number, and the radius is hardcoded to `15` km in the repository.
2. **Race Condition / Default Location**: The default coordinates are `27.7172, 85.3240` (Kathmandu). If the user is elsewhere and the API call happens before the GPS updates, it might return empty results.
3. **Empty List Logic**: The `allParkingList.isEmpty()` check in `onSuccess` triggers the error layout immediately. If the API returns an empty list (even for valid reasons like "none in 15km"), it shows the error.
4. **Visibility Management**: When `allParkingList.isEmpty()` is true, `layoutError` is shown, but `swipeRefresh` (which contains the RecyclerViews) is also visible.

## Proposed Changes

### [ParkingRepository.java](file:///C:/ParkShareproject/frontend/app/src/main/java/com/parkshare/frontend/repository/ParkingRepository.java)
- Increase the default radius or allow passing it. For now, let's increase it to 50km to be safer, or better yet, use a larger value if 15km is too restrictive for some testing environments.

### [DriverHomeFragment.java](file:///C:/ParkShareproject/frontend/app/src/main/java/com/parkshare/frontend/fragments/driver/DriverHomeFragment.java)
- **Wait for Location**: Ensure `loadParkingData()` is called with updated location.
- **Improve Error Display**: Instead of just showing "No parking found", provide a more helpful message or an "Expand Search" option.
- **Auto-Retry on Location**: When `getCurrentLocation()` succeeds, re-trigger `loadParkingData()` if it previously failed or was empty.

```java
    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                    // Re-load data with fresh location if we don't have results yet
                    if (allParkingList.isEmpty()) {
                        loadParkingData();
                    } else {
                        // Just update distances
                        updateDistances();
                    }
                }
            });
        } catch (SecurityException ignored) {}
    }
```

## Verification Plan
1. Launch app. Observe if data loads with default location.
2. Grant location permission. Observe if data reloads automatically once GPS is acquired.
3. Verify that the "No parking found" message only appears after a definitive failed fetch with a valid location.
