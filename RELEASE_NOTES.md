## Release Notes

### Changes

- Removed "ID" column from the tables overview (no need to display this technical value).
- Added column "Date Added" to table overview. Note that this value may be empty because previous version of the Studio did not set this value when new tables have been uploaded.
- Improved "Players" section: Added default background image for score items.
- Added fancy splash screen.
- Added "Tutorials" to the virtual-pinball-spreadsheet change listener: While not visible in the Studio itself, this change type is now also emitted by the Discord bot if used.
- Added "Pause" menu: The menu can be configured in the preferences section "Overlay & Pause Menu" (former "Overlay").

### Bugfixes

- Fixed virtual-pinball-spreadsheet change listener: Changes have been pre-filtered by the *updateDate* inside the VPS data, but this field is not necessarily updated when data has changed. So this check has been removed.
- Fixed window size: The size of the launcher was used for the actual Studio window size too, when the "Disconnect" action has been used.


