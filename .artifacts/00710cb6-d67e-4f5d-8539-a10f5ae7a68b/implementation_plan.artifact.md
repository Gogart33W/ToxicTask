# Plan to Refactor Localization and Expand "Toxic" Strings

## Goal Description
The current string architecture hardcodes "toxic" phrases in Kotlin logic (`Strings.kt`) across `when` expressions, which doesn't scale and makes translation expansion hard. English and German translations are heavily lacking in volume and variety (only 2-3 phrases per bucket), and grammar is broken in places (plurals, incorrect context).
The goal is to refactor string logic to Android's built-in `string-array` and `plurals` resource system, fix the existing EN, UK, and DE grammar, expand the phrase pools significantly, and ensure push notifications respect the toxicity levels.

## User Review Required
> [!IMPORTANT]
> - We will be removing the hardcoded phrases from `Strings.kt` and converting them into `arrays.xml` and `plurals.xml` in the resource folders (`res/values`, `res/values-uk`, `res/values-de`).
> - We will also increase the phrase variation for all 3 supported languages, ensuring EN and DE are as expressive and varied as the UK version.
> - The profanity in the UK version ("довбойоб", "їбаш") is kept for the "Extreme" level as it's the core identity, but I will make sure the EN and DE "Extreme" versions match that tone accurately.
> - **We are not bumping the version code** as requested, as this will just be a commit on the current version branch.

## Proposed Changes

### Feature 1: Move Phrases to `arrays.xml`

#### [NEW] `res/values/arrays.xml`
Create English (default) string arrays for:
- 3 Toxicity Levels x 3 Roles (e.g. `insults_slacker_mild`, `insults_gigachad_extreme`)
- Notification variants (e.g., `notif_inactive_title`, `notif_inactive_body`)
- Empty state insults

#### [NEW] `res/values-uk/arrays.xml` & `res/values-de/arrays.xml`
Create the localized versions, expanding the arrays to have at least 5-8 unique variations per bucket.

### Feature 2: Fix Plurals

#### [MODIFY/NEW] `res/values/strings.xml`, `values-uk/strings.xml`, `values-de/strings.xml`
- Introduce `<plurals name="rollover_text">` to handle "1 task" vs "2 tasks".
- Fix streak plurals logic to rely on native `plurals.xml` instead of the hardcoded mod-10 logic in Kotlin.

### Feature 3: Refactor Kotlin Logic

#### [MODIFY] [Strings.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/Strings.kt)
- Remove all hardcoded string lists.
- Update functions like `getInsults`, `getEmptyInsults`, and `getNotificationStrings` to accept `Context` and load from the respective XML resources using `context.resources.getStringArray(R.array.xxx)`.
- Improve the randomizer to avoid repeating the last seen phrase (we can hold the last phrase in memory).

#### [MODIFY] [ToxicAlarmReceiver.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/worker/ToxicAlarmReceiver.kt)
- Pass `context` to the refactored `ToxicStrings` functions.

#### [MODIFY] [TaskViewModel.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/viewmodel/TaskViewModel.kt)
- Access strings using `getApplication<Application>()` as context for the `Combine` flow that resolves the insult.

#### [MODIFY] [ToxicTaskScreen.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/ToxicTaskScreen.kt)
- Fix the hardcoded "M T W T F S S" and "Пн Вт Ср..." for repeating days. Use the `day_1` through `day_7` string resources already present in the XML.

## Verification Plan
### Automated Tests
- Gradle sync and build.
### Manual Verification
- Review the `ToxicTaskScreen.kt` changes to ensure localized day letters are used.
- Check `Strings.kt` to ensure Context is correctly passed and strings are loaded from arrays.
