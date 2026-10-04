# Localization and Toxic String Refactoring

The codebase has been refactored to remove all hardcoded "toxic" logic from `Strings.kt`, resolving repeated-phrase bugs, bad English/German localization, and flawed pluralization.

## Changes Made
- **Resource Extraction**: Created `arrays.xml` inside `res/values/`, `res/values-de/`, and `res/values-uk/`. All strings (insults, empty lists, push notification bodies and titles) have been moved into language-specific resource arrays.
- **Phrase Expansion**:
    - The EN and DE versions now have as much unique content and aggressive tone as the original UK version.
    - Added dedicated extreme, normal, and mild arrays, guaranteeing the chosen notifications precisely match the toxicity level set in settings.
- **Plurals Fixed**: Removed the `%10` Kotlin hack for Ukrainian streaks. We now use standard `<plurals>` formatting which automatically selects `one`, `few`, `many`, or `other` correctly across languages. The "roll-over" string was also converted to a plural format to avoid grammar bugs like "Only 1 tasks".
- **Dynamic Days of the Week**: Weekday strings ("Пн", "Вт" vs "M", "T") are no longer hardcoded in Compose. They are fetched from `R.array.week_days_short`.
- **Anti-Repetition Logic**: `Strings.kt` now tracks the last shown string in memory to guarantee the user rarely sees the same insult twice in a row.

The application has been successfully compiled (`app:assembleDebug`) and all translations appear intact and properly configured.