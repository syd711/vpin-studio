## Hotfix

### Change

- Updated Virtual Pinball Backup Manager 1.14. This is only applied to the installer, but you can update to VPBM 1.14 in your preferences.

### Bugfixes

- Fixed restoring of textfile based highscores.
- Fixed PUP pack analyzer: The analyzer did deny a PUP pack when the ROM named folder was on the root level of a zip.
- Fixed initial breadcrumb path which was showing the initial VPS tables selection, not the one from the table overview.
- Fixed window layout issue where the minimize/maximize/close buttons have been moved out of the toolbar.
- Corrected error message text when a client is connecting to a server with a newer version.
- MAME deletion: The registry key are now deleted (finally) and the "Mame" section is disabled when no registry entry is available for the table.
- MAME refresh: Fixed issue where the registry entries of the selected table have been resetted on "Reload" pressed.
- Fixed concurrent database access errors for new tables: The detection of new tables has been moved before the loading of the main Studio UI.