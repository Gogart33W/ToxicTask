# Plan to Fix Missing Localization Keys and Scale Phrase Pools to 1,000 per Language

## Goal Description
The user reported a critical bug: when Ukrainian (or German) is selected, some phrases (like Wannabe, Gigachad, or notifications) switch to English. This happens because `values-uk/toxic_phrases.xml` and `values-de/toxic_phrases.xml` were missing definitions for several arrays (`wannabe_insults_*`, `gigachad_insults_*`, `too_few_tasks_*`, and notification arrays), causing Android to fall back to the default English resources.

Furthermore, the user wants a massive expansion of the phrase pools aiming for ~1,000 total phrases per language distributed across all categories (Slacker, Wannabe, Gigachad, Empty, Too Few Tasks, and Notifications) with zero language mixing and flawless grammar.

## User Review Required
> [!IMPORTANT]
> To eliminate language mixing once and for all, every single array key referenced in `Strings.kt` must be explicitly declared and translated in `values/toxic_phrases.xml`, `values-uk/toxic_phrases.xml`, and `values-de/toxic_phrases.xml`.
> I will generate comprehensive, expanded XML files for all three languages containing a massive variety of phrases (~1,000 total per language) so the app feels endlessly fresh, funny, and ruthless.

## Proposed Changes

### 1. Complete Parity Across All Languages
Ensure all resource XML files (`values`, `values-uk`, `values-de`) contain identical array keys:
- `empty_insults_mild`, `normal`, `extreme`
- `slacker_insults_mild`, `normal`, `extreme`
- `wannabe_insults_mild`, `normal`, `extreme`
- `gigachad_insults_mild`, `normal`, `extreme`
- `too_few_tasks_mild`, `normal`, `extreme`
- Notification titles and bodies (`notif_*`)

### 2. Massive Content Scaling (~1,000 phrases per language)
- Expand each bucket to have 50-100+ unique, highly polished, native-sounding phrases in Ukrainian, English, and German.
- Double-check that no English slips into Ukrainian or German configurations.

## Verification Plan
### Automated Tests
- Build the app (`app:assembleDebug`) to ensure all XML files are syntactically valid and all arrays resolve correctly.
- Test language switching locally to verify zero English fallback when UA or DE is selected.
