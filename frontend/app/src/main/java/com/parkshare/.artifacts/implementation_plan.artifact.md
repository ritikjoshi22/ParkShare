# Redesign Profile Screen

This task involves redesigning the profile screen (specifically for the Driver role) to match the provided design image. The new design includes a custom top bar, a stylized header with a large profile image, a "Switch to Owner" promotional card, and a clean list of menu options.

## Proposed Changes

### Resources

#### [NEW] [ic_clock_24.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/drawable/ic_clock_24.xml)
- Vector icon for "Parking History".

#### [NEW] [ic_car_24.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/drawable/ic_car_24.xml)
- Vector icon for "My favorites".

#### [NEW] [ic_settings_24.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/drawable/ic_settings_24.xml)
- Vector icon for "Settings".

#### [NEW] [ic_help_24.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/drawable/ic_help_24.xml)
- Vector icon for "Help & Support".

#### [NEW] [ic_logout_24.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/drawable/ic_logout_24.xml)
- Vector icon for "Logout".

#### [NEW] [ic_pencil_24.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/drawable/ic_pencil_24.xml)
- Vector icon for "Edit Profile".

#### [NEW] [ic_bell_24.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/drawable/ic_bell_24.xml)
- Vector icon for "Notifications".

#### [NEW] [ic_shop_24.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/drawable/ic_shop_24.xml)
- Vector icon for "Switch to Owner".

#### [strings.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/values/strings.xml)
- Add new strings: `profile_label`, `switch_to_owner`, `list_parking_earn`, `verify_owner`, `parking_history`, `my_favorites`, `help_support`.

---

### UI Layout

#### [fragment_driver_profile.xml](file:///C:/ParkShareproject/frontend/app/src/main/res/layout/fragment_driver_profile.xml)
- Redesign the layout with the following sections:
    - **Top Bar**: `LinearLayout` with small profile pic, "Profile" title, and notification bell.
    - **Header**: Large circular `ImageView` with a pencil icon overlay, followed by User Name, Phone, and Email.
    - **Owner Card**: Blue `MaterialCardView` with "Switch to Owner" text and "verify owner" link.
    - **Menu List**: A series of items (can use `MaterialCardView` or stylized `LinearLayout`) for Parking History, My favorites, Settings, Help & Support, and Logout.

---

### Logic

#### [DriverProfileFragment.java](file:///C:/ParkShareproject/frontend/app/src/main/java/com/parkshare/frontend/fragments/driver/DriverProfileFragment.java)
- Update data binding to match new view IDs.
- Implement click listeners for:
    - Edit profile (pencil icon).
    - Notification bell.
    - "Switch to Owner" card and "verify owner" link.
    - Menu items: Parking History, My favorites, Settings, Help & Support, Logout.
- Fetch user info (phone number might need to be added to session if not already there).

## Verification Plan

### Automated Tests
- N/A (UI focused change, manual verification preferred).

### Manual Verification
- Deploy the app and navigate to the Profile screen.
- Verify that the layout matches the provided image.
- Click each menu item to ensure it performs the expected action (e.g., Logout works, Favorites opens the activity, etc.).
- Verify that user information (Name, Email, Phone) is correctly displayed from the session.
- Test "Switch to Owner" interaction (should probably show a toast or navigate if implemented).
