## Hotfix

### Change

- Updated to the new Virtual Pinball Spreadsheet database URL.

### Bugfixes

- Fixed DMD uploads (finally): The last hotfix did lowercase the target folder and ignored subfolders. Both issues have been fixed.
- Disabled auto-apply of VPS data to the Popper database for now. The action must be separated from the "auto-fix" action for finding the matching VPS table. The impact is not obvious. I'll come up with a better UI here soon.
- Fixed possible error for some backglasses preventing them from being uploaded.
- Fixed missing "local" check for the VPSaveEdit button in the highscores section.
- Fixed error in emulator detection: Additional checks have been added here, e.g. if the media folder and ROM folder is set. Even though Popper says they are optional, please set them for now. Check the server log file, if your emulator is missing.
- Fixed error in the regular expression for detecting assets (the error came with the last update).