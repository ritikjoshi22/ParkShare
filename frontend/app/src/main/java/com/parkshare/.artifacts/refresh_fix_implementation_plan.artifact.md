# Fix Refresh Issue in DriverHomeFragment

This task fixes the bug where refreshing `DriverHomeFragment` causes the content to disappear or display an error incorrectly.

## Problem Analysis
In `DriverHomeFragment.java`, the `loadParkingData()` method handles both the initial load and the swipe-to-refresh action.
- When refreshing (via `SwipeRefreshLayout`), it currently hides the refresh indicator's visibility (`binding.swipeRefresh.setVisibility(View.INVISIBLE)`) and tries to show a shimmer effect, which is redundant and causes UI flickering.
- The `LoadingHelper.showShimmer` call is only appropriate for the initial load, not for an active swipe-to-refresh.
- The visibility of `swipeRefresh` should remain `VISIBLE` during a refresh to show the progress spinner.

## Proposed Changes

### [DriverHomeFragment.java](file:///C:/ParkShareproject/frontend/app/src/main/java/com/parkshare/frontend/fragments/driver/DriverHomeFragment.java)

- Modify `loadParkingData()` to distinguish between a background refresh and a full-screen loading state.
- Ensure `swipeRefresh` remains visible during refresh.
- Properly manage the error layout visibility.

```java
    private void loadParkingData() {
        binding.layoutError.setVisibility(View.GONE);
        boolean isRefreshing = binding.swipeRefresh.isRefreshing();

        if (!isRefreshing) {
            // Only show shimmer if not already refreshing via swipe
            LoadingHelper.showShimmer(shimmerLayout, binding.progressBar);
            // DO NOT hide swipeRefresh here as it contains the RecyclerViews
            // Instead, we rely on the shimmer overlaying the content if needed,
            // or we hide content specifically if that's the desired design.
        }

        parkingRepository.getNearby(userLat, userLng, 1, new RepositoryCallback<List<ParkingSpaceDto>>() {
            @Override
            public void onSuccess(List<ParkingSpaceDto> data) {
                LoadingHelper.hideAll(shimmerLayout, binding.progressBar);
                binding.swipeRefresh.setRefreshing(false);
                // Ensure visibility if it was hidden
                binding.swipeRefresh.setVisibility(View.VISIBLE);

                allParkingList.clear();
                // ... rest of the logic ...
            }
            // ... onError ...
        });
    }
```

## Verification Plan

### Manual Verification
1. Open the Driver Home screen. Verify data loads with shimmer.
2. Swipe down to refresh. Verify the refresh spinner appears and data updates without the screen going blank or showing "No parking found" prematurely.
3. Simulate an error (e.g., turn off internet) and refresh. Verify the error layout appears correctly.
4. Turn internet back on and click "Retry". Verify data loads correctly.
