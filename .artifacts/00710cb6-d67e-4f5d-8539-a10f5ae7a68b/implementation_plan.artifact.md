# Plan to Polish English and German Toxic Phrases Grammar

## Goal Description
Following the successful cleanup of the Ukrainian toxic phrases, the English (`values/toxic_phrases.xml`) and German (`values-de/toxic_phrases.xml`) phrase pools need the same treatment.

Objectives:
1. **English (EN):** Eliminate clunky AI phrasing, ensure sharp American/British roasting idioms, correct any minor grammatical awkwardness, and keep the Mild level clean and motivating while making Extreme punchy and biting.
2. **German (DE):** Fix German capitalization (all nouns capitalized), correct case/declension errors, ensure informal "du" is used consistently throughout (including disclaimers and insults), and replace literal translations with authentic colloquial German insults ("Faulpelz", "Vollidiot", "Nichtsnutz").

## User Review Required
> [!NOTE]
> This will ensure all three languages in the app sound professionally written by a native speaker with the right amount of toxicity.

## Proposed Changes

### 1. English Phrases Polish (`values/toxic_phrases.xml`)
- Review and refine `slacker_insults_mild`, `slacker_insults_normal`, `slacker_insults_extreme`.
- Review and refine `too_few_tasks_mild`, `too_few_tasks_normal`, `too_few_tasks_extreme`.
- Review notification and empty state arrays.

### 2. German Phrases Polish (`values-de/toxic_phrases.xml`)
- Review and refine all German arrays for native fluency, correct capitalization, and consistent informal tone.

## Verification Plan
### Automated Tests
- Run `app:assembleDebug` to verify XML syntax and resource compilation.
