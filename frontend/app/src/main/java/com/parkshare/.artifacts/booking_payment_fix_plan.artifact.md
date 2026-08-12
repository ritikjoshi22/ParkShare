# Professional Booking & Payment Flow

This plan fixes the logical error where bookings are "confirmed" before payment and enhances the UI/UX to match senior developer standards.

## Logical Error Fix
The current app creates a booking and then *optionally* proceeds to payment. If the user exits, the booking might remain in a "Pending" or "Confirmed" state in the backend.
- **Change**: Rename "Confirm Booking" in slot selection to "Proceed to Payment".
- **Change**: Show a professional **Booking Summary Bottom Sheet** before calling the booking API.
- **Change**: The actual booking creation will happen only when the user clicks "Pay & Book" in the summary.
- **Change**: Immediately follow booking creation with the Stripe payment sheet to ensure atomic-like behavior from the user's perspective.

## Professional UI/UX Enhancements
- **Booking Summary**: A sleek Material 3 Bottom Sheet containing:
    - High-level parking details (Name, Address).
    - Booking specifics (Slot label, Date/Time range, Duration).
    - Detailed Price Breakdown (Base, Discount, Total).
- **Atomic Progress**: Show a full-screen loading state while creating the booking/initiating payment to prevent multiple clicks.

## Proposed Changes

### [NEW] [bottom_sheet_booking_summary.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/layout/bottom_sheet_booking_summary.xml)
- Modern layout for the booking overview.

### [SlotBookingActivity.java](file:///C:/ParkShareproject/frontend/app/src/main/java/com/parkshare/frontend/activities/driver/SlotBookingActivity.java)
- Pass `EXTRA_PARKING_NAME` and `EXTRA_PARKING_ADDRESS` to the next activity.

### [ParkingSlotSelectionActivity.java](file:///C:/ParkShareproject/frontend/app/src/main/java/com/parkshare/frontend/activities/driver/ParkingSlotSelectionActivity.java)
- Add constants for the new extras.
- Implement `showSummaryBottomSheet()`.
- Update `confirmBooking()` logic to be triggered from the Bottom Sheet.
- Update main button text to "Proceed to Summary".

### [strings.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/values/strings.xml)
- Add professional strings: `review_booking`, `pay_now`, `total_to_pay`, `selected_slot_label`, etc.

## Verification Plan

### Manual Verification
1. Select a slot. Verify the button text changes or is appropriate.
2. Click "Proceed to Summary". Verify the Bottom Sheet appears with correct details and price.
3. Click "Pay & Book". Verify the loading state appears.
4. Verify the app navigates directly to the Payment screen (or Stripe sheet).
5. Cancel payment. Verify the user stays on the summary or selection screen (and doesn't get a "Booking Confirmed" toast).
