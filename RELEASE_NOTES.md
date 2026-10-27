## Release Notes 5.6.0

### Changes

- **Highscore Backups**: The backups in the highscore backup dialog are now sorted by date, newest first.
- **Bulk Importer**: Added the import mode "If Missing", which only imports a file if its screen has no media yet.
- **Webhooks**: Added the webhook types "Table Launch" and "Table Exit" to webhook sets. They are called with the game id when a table is started or exited.
- **Table Overview**: Enabling or disabling tables from the context menu now runs in a progress dialog, so large selections no longer block the UI.
- **VPin Mania**: Disabled tables are no longer synchronized with VPin Mania. They are excluded from the installed tables list, and their highscores and played counts are not submitted.
- **VPin Mania**: The VPin Mania settings pages now load their data in a progress dialog when they are opened, so switching between them no longer blocks the UI.
- **Table Asset Manager**: The full screen button of the "Loading" screen is now a menu button with the new entry "Add Transparent Blank Video (AMD)". AMD graphics cards do not show the transparency of the default blank video, so the loading video was not played transparent. The new entry works like the existing "Add Blank Video" entry, but adds a transparent .mov blank (kudos to the VPU author of the "AMD Blank Tarc for Loading Screens") instead of the .mp4 blank. The file is downloaded into the server's resources folder with the update.

### Bugfixes

- **Screens / DMD Position**: Fixed the backglass monitor lookup for screenres.txt files that set the backglass monitor by display number (e.g. "3"). The number is now matched against the Windows display name (\\.\DISPLAY3), the same way B2S does it, instead of the monitor enumeration order. On some systems, the screens overview showed the backglass and full DMD on the playfield monitor, the System Manager showed false position mismatch warnings, and "DMD Position" wrote wrong coordinates into the DmdDevice.ini. If you added the backglass monitor offset to the DMD position by hand to work around this, check your DMD positions again.
- **VPin Mania**: Fixed the privacy settings of the cabinet registration. The "Submit Installed Tables" and "Submit Play Count" options were not applied, and highscores were always synchronized. The registration also has a new "Submit All Highscores" option.
- **ALT Sound**: Fixed detection of g-sound bundles.