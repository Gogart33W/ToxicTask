# Rollover Fix & Firebase Integration

The task rollover logic has been updated to only fetch tasks from strictly yesterday, and Firebase Analytics & Crashlytics have been integrated without affecting the existing Room database schema.

## Rollover Logic Fix
- **Room DAO Update**: In `TaskDao.kt`, the SQL query was modified. Instead of fetching all tasks before the current date (`< :date`), it now strictly queries for tasks matching the specified date (`= :date`).
- **ViewModel Update**: `TaskViewModel.kt` was updated to pass exactly yesterday's date (`LocalDate.now().minusDays(1).toString()`) to the new `observeUncompletedTasksOn` DAO function. This ensures only tasks from the previous day trigger the rollover popup.

## Firebase Analytics & Crashlytics
- **Version Catalogs**: Added `firebase-crashlytics-gradle`, `firebase-analytics`, and `firebase-crashlytics` to `libs.versions.toml`.
- **Gradle Configuration**:
  - Added the Google Services and Crashlytics plugins to both the project-level and app-level `build.gradle.kts`.
  - Added the Firebase Bill of Materials (BoM) platform and dependencies for Analytics and Crashlytics.
  - Bumped the `versionCode` to 9 and `versionName` to "1.0.8".
- **AnalyticsManager**: Created `AnalyticsManager.kt` to encapsulate `FirebaseAnalytics` functionality. It exposes `logTaskRollover` and `logTaskCreated` suspend functions that operate on `Dispatchers.IO` to avoid blocking the main thread.
- **Event Logging**: Hooked the `AnalyticsManager` into `TaskViewModel` to log custom events when a user adds a task or performs a rollover.
- **Dummy `google-services.json`**: Added a mock config file temporarily to allow Gradle to sync and build successfully. **You must replace this file with your actual configuration from the Firebase Console before releasing the app.**

All code builds correctly and the changes have been committed locally.