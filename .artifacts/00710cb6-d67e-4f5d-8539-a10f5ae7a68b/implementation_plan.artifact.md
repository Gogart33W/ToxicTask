# ToxicTask Feature Implementation Plan

## Goal Description
Implement strict "yesterday-only" logic for task rollover and integrate Firebase Analytics & Crashlytics, ensuring clean architecture, best practices, and no breaking changes to the existing database schema.

## User Review Required
> [!IMPORTANT]
> The Firebase integration requires a valid `google-services.json` file to be placed inside the `app/` directory to compile and run successfully with Firebase enabled. Since I cannot generate this file on your behalf, you'll need to add it yourself either before or after I apply these code changes. **Please confirm if you are okay with proceeding, knowing this file is required for successful builds.**

## Proposed Changes

### Feature 1: Fix Task Rollover Logic (Strictly Yesterday)

#### [MODIFY] [TaskDao.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/data/TaskDao.kt)
- Update the SQL query for fetching pending rollover tasks.
- Change `scheduledDate < :date` to `scheduledDate = :date`.
- Rename `observeUncompletedTasksBefore` to `observeUncompletedTasksOn`.

#### [MODIFY] [TaskViewModel.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/viewmodel/TaskViewModel.kt)
- Update the call to the DAO in `pendingRolloverTasks`.
- Pass exactly yesterday's date `LocalDate.now().minusDays(1).toString()` instead of today's date.
- This ensures only yesterday's unfinished tasks prompt the user for rollover.

---

### Feature 2: Firebase Integration (Analytics & Crashlytics)

#### [MODIFY] [libs.versions.toml](file:///home/gogart/AndroidStudioProjects/ToxicTask/gradle/libs.versions.toml)
- Add versions for Firebase BoM (`33.9.0`) and Crashlytics Gradle Plugin (`3.0.3`).
- Define libraries for `firebase-bom`, `firebase-analytics`, and `firebase-crashlytics`.
- Define the plugin alias for `firebase-crashlytics`.

#### [MODIFY] [build.gradle.kts (Project)](file:///home/gogart/AndroidStudioProjects/ToxicTask/build.gradle.kts)
- Include the `google-services` and `firebase-crashlytics` plugins at the top level with `apply false`.

#### [MODIFY] [build.gradle.kts (App)](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/build.gradle.kts)
- Apply `com.google.gms.google-services` and `com.google.firebase.crashlytics` plugins.
- Add dependencies for the Firebase BoM, Analytics, and Crashlytics using the platform paradigm.

#### [NEW] [AnalyticsManager.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/utils/AnalyticsManager.kt)
- Create a helper class injected or instantiated in the ViewModel.
- Initialize `FirebaseAnalytics.getInstance(context)`.
- Expose suspend functions (`logTaskRollover`, `logTaskCreated`) that safely execute logging on `Dispatchers.IO`.

#### [MODIFY] [TaskViewModel.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/viewmodel/TaskViewModel.kt)
- Instantiate `AnalyticsManager`.
- Hook up event logging within the `rolloverTasks` and `addTask` coroutines.

## Verification Plan

### Automated Tests
- Validate Gradle sync successfully processes the new dependencies and plugins.
- Note: Application build will likely fail locally *unless* the `google-services.json` file is present.

### Manual Verification
- Review the modified DAO and ViewModel code to ensure Room schema is completely untouched.
- Verify `AnalyticsManager` uses `Dispatchers.IO` for logging calls, as requested for clean coroutines execution.
