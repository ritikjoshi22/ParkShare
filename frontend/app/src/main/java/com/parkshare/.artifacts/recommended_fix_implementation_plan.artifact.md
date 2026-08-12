# Fix and Enhance "Recommended for You" Section

This task addresses the empty/non-functional "Recommended for You" section on the Driver Home screen and ensures it displays meaningful data to the user.

## Problem Analysis
Currently, `DriverHomeFragment.java` populates the "Recommended" section by simply taking the first 3 items from the "Nearby" list.
- If the "Nearby" list is empty or small, the "Recommended" section looks empty or redundant.
- There is no specific recommendation logic (e.g., based on user history or ratings).
- The provided image shows the "Recommended" section with a specific design that we should ensure is working correctly.

## Proposed Changes

### [DriverHomeFragment.java](file:///C:/ParkShareproject/frontend/app/src/main/java/com/parkshare/frontend/fragments/driver/DriverHomeFragment.java)

- Enhance `displayParkingData` to provide better recommendations.
- If data is available, prioritize top-rated spots or recently booked ones for the "Recommended" section.
- If nearby data is thin, ensure "Recommended" still shows something useful by leveraging the global fallback data.

```java
    private void displayParkingData(List<ParkingSpaceDto> data) {
        // ... (existing visibility logic) ...

        allParkingList.clear();
        if (data != null) {
            for (ParkingSpaceDto dto : data) {
                Parking parking = ParkingMapper.fromDto(dto);
                // ... (distance logic) ...
                allParkingList.add(parking);
            }
        }

        // IMPROVED RECOMMENDATION LOGIC:
        // Sort by rating (descending) or pick verified spots
        List<Parking> sortedList = new ArrayList<>(allParkingList);
        Collections.sort(sortedList, (p1, p2) -> Double.compare(p2.getRating(), p1.getRating()));

        List<Parking> recommended = sortedList.size() > 5
                ? new ArrayList<>(sortedList.subList(0, 5))
                : new ArrayList<>(sortedList);

        recommendedAdapter.updateList(recommended);
        nearbyAdapter.updateList(new ArrayList<>(allParkingList));

        // ... (error state logic) ...
    }
```

### [NEW] [item_parking_horizontal.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/layout/item_parking_horizontal.xml)
- A new layout specifically designed for the horizontal "Recommended" list. It uses a fixed width (280dp) to ensure multiple cards are visible and scrollable.

### [ParkingAdapter.java](file:///C:/ParkShareproject/frontend/app/src/main/java/com/parkshare/frontend/adapters/ParkingAdapter.java)
- Added support for an `isHorizontal` flag in the constructor.
- Modified `onCreateViewHolder` to switch between `item_parking` (vertical) and `item_parking_horizontal` (horizontal) based on the flag.
- Used `ItemParkingBinding.bind(view)` to maintain compatibility across different layout files with the same view IDs.

## Verification Plan

### Manual Verification
1. Open Driver Home screen.
2. Verify "Recommended for You" displays the top-rated parking spots.
3. Verify that the list scrolls horizontally.
4. Verify that clicking "View Details" on a recommended item opens the correct parking space.
