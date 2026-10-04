# Complete Edge-to-Edge Fix

The app has been thoroughly updated to conform strictly with modern Android edge-to-edge layout requirements.

## Changes Made
- **Removed Layout Constraint**: Removed `windowInsetsPadding(WindowInsets.safeDrawing)` directly from the root `Scaffold` in `ToxicTaskScreen.kt`. The Scaffold components (TopAppBar, NavigationBar) inherently handle edge rendering to create a genuine borderless look.
- **Keyboard Handling**: Added `android:windowSoftInputMode="adjustResize"` to the `<activity>` tag in `AndroidManifest.xml` to ensure software keyboards properly shift layout elements instead of overlapping them.
- **Clean Navigation Bar**: Appended `window.isNavigationBarContrastEnforced = false` in `MainActivity.kt` to disable the default Android system scrim placed behind the bottom navigation bar on newer API levels (API 29+).
- **Version Bump**: Bumped to **1.0.10** (`versionCode 11`).

## Result
Google Play's automated pre-launch reports should now report zero warnings related to layout edge-to-edge support or accessibility issues under the system bars.

The release build (`assembleRelease`) is verified and ready for upload to Google Play.
