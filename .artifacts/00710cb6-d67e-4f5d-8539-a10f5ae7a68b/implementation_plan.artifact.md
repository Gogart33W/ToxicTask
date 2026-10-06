# Plan to Overhaul and Expand Toxic Phrases Grammar (Release 1.1.0 Content Fix)

## Goal Description
The current AI-generated phrase pools (`toxic_phrases.xml` for UK, EN, and DE) contain unnatural phrasing, awkward sentence structures, occasional Russianisms (in Ukrainian), and stylistic inconsistencies ("pzd" grammar).

The goal is to completely rewrite, polish, and **expand** all phrase pools (1,500+ phrases total across UK, EN, and DE):
1. **Ukrainian (UA):** Purge all Russianisms, improve native slang, ensure sharp, brutal, and grammatically impeccable phrasing.
2. **English (EN):** Refine idioms, eliminate awkward phrasing, and inject sharp, natural native roasting slang.
3. **German (DE):** Fix grammatical gender, case errors, formal/informal mixing, and replace awkward literal translations with authentic colloquial German insults and motivation.
4. **Volume:** Ensure we *not* decrease the count, but rather enrich and expand each category (Mild, Normal, Extreme across Slacker, Wannabe, Gigachad, Empty, and Notifications) to 100+ high-quality phrases per major category.

## User Review Required
> [!IMPORTANT]
> Because this is a massive linguistic overhaul of over 1,500 phrases across 3 languages, I will rewrite the XML files in clean, structured batches and verify they build successfully without XML syntax errors or missing items.

## Proposed Changes

### 1. Ukrainian Phrases Overhaul (`values-uk/toxic_phrases.xml`)
- Fix all items in `slacker_insults_mild`, `slacker_insults_normal`, `slacker_insults_extreme`.
- Fix `too_few_tasks_mild`, `too_few_tasks_normal`, `too_few_tasks_extreme`.
- Fix notifications and empty states.
- Ensure 100% natural, punchy Ukrainian profanity and slang where appropriate (Extreme), and clean motivation (Mild).

### 2. English Phrases Overhaul (`values/toxic_phrases.xml`)
- Polish grammar, native rhythm, and ensure biting American/British roasting style.

### 3. German Phrases Overhaul (`values-de/toxic_phrases.xml`)
- Correct German capitalization (nouns), correct verb placements, and use authentic colloquial insults ("Nichtsnutz", "Vollidiot", "Faulpelz").

## Verification Plan
### Automated Tests
- Run `app:assembleDebug` to verify that all XML files are syntactically valid and all string arrays compile successfully.
