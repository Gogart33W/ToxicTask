# Plan to Resolve Google Play Console Warnings (Release 1.0.9)

## Goal Description
The Google Play Console flagged three technical quality issues in the `1.0.8` release:
1. Deprecated `androidx.fragment:fragment` version.
2. Two warnings related to Edge-to-Edge display compatibility on newer Android versions.

The goal is to fix these warnings by updating dependencies, ensuring proper Edge-to-Edge implementation in Jetpack Compose, and bumping the app version to `1.0.9` (`versionCode 10`) to improve the app's ranking and recommendation potential on the Play Store.

## User Review Required
> [!NOTE]
> We will force the `androidx.fragment:fragment-ktx` version to `1.8.4` (or the latest stable) via dependencies to satisfy the Play Console warning, even though Compose handles the UI.
> We will also ensure `enableEdgeToEdge()` is called properly in `MainActivity` and check `ToxicTaskScreen` to ensure `WindowInsets.safeDrawing` or similar is used to prevent content from going under system bars.

## Proposed Changes

### Resolve Deprecated Fragment Version

#### [MODIFY] [libs.versions.toml](file:///home/gogart/AndroidStudioProjects/ToxicTask/gradle/libs.versions.toml)
- Add a version definition for `androidx-fragment` (e.g., `1.8.4`).
- Add a library definition for `androidx-fragment-ktx`.

#### [MODIFY] [app/build.gradle.kts](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/build.gradle.kts)
- Add `implementation(libs.androidx.fragment.ktx)` to force the newer fragment version into the dependency tree.

### Resolve Edge-to-Edge Warnings

#### [MODIFY] [MainActivity.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/MainActivity.kt)
- Ensure `enableEdgeToEdge()` is called *before* `setContent` (it already is, but we will double-check).
- Check for and remove any legacy window flag manipulations if they exist (though this seems unlikely given the current Compose setup).

#### [MODIFY] [ToxicTaskScreen.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/ToxicTaskScreen.kt)
- Ensure the root `Scaffold` or `Box` uses `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)` (or similar like `.systemBarsPadding()`) so UI elements don't overlap with the navigation/status bars.

### Version Bump

#### [MODIFY] [app/build.gradle.kts](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/build.gradle.kts)
- Increment `versionCode` to `10`.
- Increment `versionName` to `"1.0.9"`.

## Verification Plan

### Automated Tests
- Build the project successfully (`app:assembleRelease`).
- Check lint warnings related to Edge-to-Edge or Fragment versions.

### Manual Verification
- Review the `MainActivity.kt` and `ToxicTaskScreen.kt` changes to confirm proper Edge-to-Edge setup according to Jetpack Compose guidelines.
- Build the AAB and note that the user will upload it to Play Console to see the warnings disappear.