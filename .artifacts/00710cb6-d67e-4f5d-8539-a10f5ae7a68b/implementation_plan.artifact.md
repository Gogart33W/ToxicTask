# Plan to Implement TikTok/Social Share Feature (Release 1.1.0)

## Goal Description
We want to add a viral loop to ToxicTask by allowing users to share their "Player Status" and a toxic phrase as a sleek, 9:16 vertical poster directly to TikTok, Instagram Stories, or other social media.
This feature will:
1. Render a beautiful composable poster invisibly in memory using Compose `GraphicsLayer`.
2. Save the rendered image to a cache directory.
3. Serve the image via `FileProvider` securely.
4. Launch an `ACTION_SEND` intent prioritizing TikTok (or fallback to standard share sheet).
5. Bump version to `1.1.0` (minor version bump since it's a significant new feature).

## User Review Required
> [!NOTE]
> The new sharing button will be placed on the Status Dashboard card. Users can tap a "Share" icon to instantly generate and share their roast/glory. The image will be dark-themed and edgy, fitting the app's aesthetic.

## Proposed Changes

### 1. FileProvider Setup
#### [NEW] [app/src/main/res/xml/file_paths.xml](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/res/xml/file_paths.xml)
- Define the `shared_images` cache path.

#### [MODIFY] [app/src/main/AndroidManifest.xml](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/AndroidManifest.xml)
- Add `<provider>` definition for `androidx.core.content.FileProvider`.

### 2. Social Sharing Logic
#### [NEW] [app/src/main/java/com/gogart/toxictask/utils/ShareUtils.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/utils/ShareUtils.kt)
- Create a `shareToTikTokOrSystem(context, bitmap)` utility function.

### 3. Toxic Share Card UI & Integration
#### [MODIFY] [app/src/main/java/com/gogart/toxictask/ToxicTaskScreen.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/ToxicTaskScreen.kt)
- Create `@Composable fun ToxicShareCard` to represent the 9:16 viral image layout.
- Update `StatusDashboard` to include an "export/share" icon button.
- Implement the `GraphicsLayer` off-screen rendering logic. When the share button is clicked, it will generate the bitmap of `ToxicShareCard` and pass it to `ShareUtils`.

### 4. Version Bump
#### [MODIFY] [app/build.gradle.kts](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/build.gradle.kts)
- Increment `versionCode` to 12.
- Increment `versionName` to `"1.1.0"`.

## Verification Plan
### Automated Tests
- Assemble the project successfully to ensure no Compose Graphics API issues.
### Manual Verification
- Share intent generation should not throw exceptions and `FileProvider` must resolve correctly.