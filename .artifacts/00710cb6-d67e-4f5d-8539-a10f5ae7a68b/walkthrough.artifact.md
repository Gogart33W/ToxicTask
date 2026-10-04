# Massive Content Update and Configurable Goal Settings

I've reviewed the files provided by the other AI. It did an excellent job generating the content and the patch file, which smoothly handles the code changes. I've successfully integrated all of it into the project.

## What was implemented:

1. **450+ New Phrases per Language!**
   - The huge text files (`uk.txt`, `en.txt`, `de.txt`) containing the new phrases have been successfully converted into `toxic_phrases.xml` resources for each locale.
   - **Crucially:** I ensured that the `Mild` (Легкий) toxicity level contains zero profanity. It is strictly motivational, slightly sarcastic, but completely clean. The real aggressive phrasing and swearing are reserved strictly for the `Extreme` level.
   - The original arrays in `arrays.xml` that were hardcoded with just 5 phrases were stripped out, and the app now pulls from these massive new pools.

2. **Configurable "Minimum Tasks per Day"**
   - The patch file successfully updated `SettingsManager.kt`, `TaskViewModel.kt`, `ToxicAlarmReceiver.kt`, and `TaskUtils.kt`.
   - Instead of a hardcoded `3`, users can now set their daily task goal (between 2 and 10) directly in the Settings menu using the new `+/-` UI.
   - The "GIGACHAD" status calculations and the `Too Few Tasks` insults now correctly respect this user-defined minimum value. If a user sets the minimum to 5, the app will insult them until they add 5 tasks.

3. **Plurals Fix for "Too Few Tasks"**
   - Because the "too few tasks" phrasing now needs to inject both the current task count AND the user-defined minimum (e.g. "Only 1 of 5 tasks?"), the old plurals were modified.
   - `Strings.kt` has been updated to use `String.format` properly to inject these two variables into the text.

The build is successful. Since you requested not to bump the version yet, `versionCode` remains at 11 (`1.0.10`). Everything is committed locally.