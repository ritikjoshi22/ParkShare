# Fix Bottom Navigation Responsiveness

This plan fixes the issue where clicking the "Home" item in the bottom navigation doesn't return the user to the home screen after they've navigated to the Map via a button.

## Problem Analysis
In `DriverMainActivity`, the `AppBarConfiguration` and `NavigationUI` are set up, but `driver_map` and `driver_notifications` were not added to the `topLevel` set.
While `NavigationUI.setupWithNavController` should handle basic navigation, the lack of these in the top-level configuration can sometimes cause back-stack issues or inconsistent behavior when jumping between tabs vs manual navigation via buttons.

More importantly, if "Find My Parking" uses `navigate(R.id.driver_map)`, it might be adding to the backstack in a way that the BottomNavigationView's default behavior doesn't expect if not configured as a top-level destination.

## Proposed Changes

### [DriverMainActivity.java](file:///C:/ParkShareproject/frontend/app/src/main/java/com/parkshare/frontend/activities/driver/DriverMainActivity.java)
- Add `R.id.driver_map` and `R.id.driver_notifications` to the `topLevel` set in `onCreate`.
- Ensure all items in `bottom_nav_driver.xml` are treated as top-level destinations.

```java
        Set<Integer> topLevel = new HashSet<>();
        topLevel.add(R.id.driver_home);
        topLevel.add(R.id.driver_map); // Added
        topLevel.add(R.id.driver_bookings);
        topLevel.add(R.id.driver_notifications); // Added
        topLevel.add(R.id.driver_sos); // (Optional, usually hidden from nav)
        topLevel.add(R.id.driver_profile);
```

## Verification Plan
1. Open the app (Home).
2. Click "Find My Parking" (takes you to Map).
3. Click "Home" in the bottom navbar.
4. Verify you return to the Home screen immediately.
5. Repeat for "Alerts" and "Profile" to ensure all transitions are snappy.
