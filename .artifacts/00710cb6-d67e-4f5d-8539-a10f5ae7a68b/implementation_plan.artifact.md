# Plan to Polish Localization and Fix UI Formatting (Release 1.0.11)

## Goal Description
The current UI strings are suffering from several translation inconsistencies, grammatical errors, and layout bugs.
Specifically:
- German translation has missing keys, bad terminology ("STREBER", "ABFALL"), formal/informal mixing ("Sie" vs "du"), and inconsistent caps.
- Ukrainian translation mixes "місії", "таски", "цілі", and "задачі". The user prefers unifying this around "Таски" / "Таска" / "Таску".
- UI bug: The "Екстремальний" chip in settings wraps awkwardly and breaks the layout.
- The date formatter is hardcoded to US format ("MMM dd, yyyy").
- Accessibility lacks a localized string for the disclaimer icon.

The goal is to fix all translation files, align terminology, fix date formatting using a locale-aware formatter, fix the Chip layout bug, and bump the version to 1.0.11.

## User Review Required
> [!NOTE]
> - Terminology in Ukrainian will be unified to "Таска" / "Таски" (e.g., "НОВА ТАСКА", "РЕДАГУВАТИ ТАСКУ").
> - The layout issue with the "Екстремальний" chip will be fixed by allowing the chips to wrap to a new line (using FlowRow or standard scrollable Row if horizontal space is tight). Since it's inside a `Row(horizontalArrangement = Arrangement.spacedBy(4.dp))`, we will change it to an `Auto-wrapping` layout or just let the text size down if needed.
> - Date formatting will use `DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)`.

## Proposed Changes

### Feature 1: Polish Translations and Unify Terminology
#### [MODIFY] [res/values/strings.xml](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/res/values/strings.xml)
- Add missing keys if any.
- Change `disclaimer_content_desc` (new string for accessibility).

#### [MODIFY] [res/values-uk/strings.xml](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/res/values-uk/strings.xml)
- Unify terminology: `НОВА МІСІЯ` -> `НОВА ТАСКА`, `ПОТОЧНІ ЦІЛІ` -> `ПОТОЧНІ ТАСКИ`, `МІСІЇ ВІДСУТНІ` -> `ТАСКИ ВІДСУТНІ`, etc.
- Change "Екстремальний" to "Екстрим" (or "Жорсткий") to prevent the severe text-wrapping UI bug on narrow screens, while keeping the meaning.

#### [MODIFY] [res/values-de/strings.xml](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/res/values-de/strings.xml)
- Fix missing keys.
- Fix grammar and terminology (e.g., `role_wannabe` -> `MÖCHTEGERN`, `role_extreme_lox` -> `VERSAGER`, `disclaimer_text` -> informal "du").
- Ensure caps match English equivalents.

### Feature 2: Fix UI Layouts and Formatters
#### [MODIFY] [ToxicTaskScreen.kt](file:///home/gogart/AndroidStudioProjects/ToxicTask/app/src/main/java/com/gogart/toxictask/ToxicTaskScreen.kt)
- **Date Format**: Change `DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())` to `DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)`.
- **Accessibility**: Add localized `contentDescription` for the Info icon.
- **Chip Wrapping**: Change the `Row` holding the toxicity levels to `ExperimentalLayoutApi FlowRow` so if translations are too long (like "Екстремальний" or "Extrem"), they wrap gracefully instead of squishing and breaking text. Alternatively, use a `ScrollableRow`. Since it's a settings dialog, `FlowRow` is perfect.

### Feature 3: Version Bump
- Bump `versionCode` to 12.
- Bump `versionName` to `1.0.11`.

## Verification Plan
### Automated Tests
- `app:assembleDebug`

### Manual Verification
- Verify the settings dialog chips no longer squish text.
- Verify the date selector displays correctly localized dates (e.g., "04 жовт. 2026" for UK, "04. Okt. 2026" for DE).