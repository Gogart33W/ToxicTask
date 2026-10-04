# Plan to Resolve Remaining Edge-to-Edge Warnings (Release 1.0.10)

## Goal Description
The Google Play Console still shows two warnings related to edge-to-edge display:
- "Безрамковий показ може працювати не для всіх користувачів"
- "У вашому додатку використовуються застарілі інтерфейси API або параметри для безрамкового показу"

Upon investigation, the root cause is that the main `Scaffold` has `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)` applied directly to it. This shrinks the entire scaffold (including the top and bottom app bars) away from the system edges, effectively creating black/white borders instead of true edge-to-edge rendering where the app bar backgrounds extend behind the system bars. Also, `android:windowSoftInputMode="adjustResize"` is missing from the Manifest, and `window.isNavigationBarContrastEnforced` needs to be set to `false` so the system doesn't draw an ugly semi-transparent box over the bottom navigation bar.

We will fix these UI/UX parameters and bump the version to `1.0.10` (`versionCode 11`) to submit a perfectly clean build.

## User Review Required
> [!TIP]
> This plan will fully align the app with modern Android 15 edge-to-edge standards. No action is required from you other than approval, but after I implement this, you will need to push the new `.aab` to Play Console.

## Proposed Changes

### Resolve Edge-to-Edge Misconfigurations

#### [MODIFY] [ToxicTaskScreen.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/ToxicTaskScreen.kt)
- Remove `.windowInsetsPadding(WindowInsets.safeDrawing)` from the main `Scaffold` modifier.
- Since we use standard `TopAppBar` and `NavigationBar`, they handle insets themselves. We just need to make sure we use `paddingValues` correctly inside the `Box` content lambda (which is already happening!).

#### [MODIFY] [AndroidManifest.xml](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/AndroidManifest.xml)
- Add `android:windowSoftInputMode="adjustResize"` to the `<activity>` tag for `.MainActivity`. This is a requirement for modern Compose IME edge-to-edge handling so the keyboard doesn't overlap input fields.

#### [MODIFY] [MainActivity.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/MainActivity.kt)
- Add `window.isNavigationBarContrastEnforced = false` (for Android Q / 29+) in `onCreate()`. This prevents the system from drawing an automatic scrim behind the bottom bar, ensuring the `NavigationBar` uses the app's clean color.

### Version Bump

#### [MODIFY] [app/build.gradle.kts](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/build.gradle.kts)
- Increment `versionCode` to `11`.
- Increment `versionName` to `"1.0.10"`.

## Verification Plan

### Automated Tests
- Build the project successfully (`app:assembleRelease`).

### Manual Verification
- Review the `AndroidManifest.xml`, `ToxicTaskScreen.kt` and `MainActivity.kt` files.
- The new `app-release.aab` will be ready to upload to Play Console to clear the final two UI warnings.