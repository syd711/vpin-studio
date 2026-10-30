## Release Notes 5.6.1

### Changes

- **Table Asset Manager**: Adding a landscape video to the PinUP Popper "Loading" screen now offers to add a blank video too, the same as "Add Blank Video": the video gets the "(SCREEN3)" playfield assignment and the blank video becomes the default loading video. Choose "Always Add Blank" to do this automatically, or "Do not show again" to hide the dialog. Both can be reset with "Restore Hidden Dialogs" in the client settings.



---


## Release Notes 5.6.0

### Changes

- **Discord Client Settings**: Added the new preferences page "Discord Client Settings" to the "Discord Integration" section. It shows whether the Discord desktop client is installed on the cabinet. Discord can be launched minimized together with the VPin Studio Server, and the table that is currently played can be shown as your Discord status, including the VPin Mania wheel icon of the table. Both options require the Discord desktop client to be installed on the cabinet.##  
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/discord-status.png?raw=true" width="200" />

- **Highscore Backups**: The backups in the highscore backup dialog are now sorted by date, newest first.
- **Bulk Importer**: Added the import mode "If Missing", which only imports a file if its screen has no media yet.
- **Webhooks**: Added the webhook types "Table Launch" and "Table Exit" to webhook sets. They are called with the game id when a table is started or exited.
- **Table Overview**: 
  - Enabling or disabling tables from the context menu now runs in a progress dialog, so large selections no longer block the UI.
  - Added the checkbox "Pause Menu Todo Tags" below the tag input of the table filter. It adds all tags that are used for the pause menu todo list to the filter, and unchecking it removes them again.
  - The backglass column of the table overview and the Backglass Manager now use different icons for .directb2s and .B2SPro files. The tooltip shows the backglass type.
- **VPin Mania**: 
  - Disabled tables are no longer synchronized with VPin Mania. They are excluded from the installed tables list, and their highscores and played counts are not submitted.
  - The VPin Mania settings pages now load their data in a progress dialog when they are opened, so switching between them no longer blocks the UI.
- **Table Asset Manager**: The full screen button of the "Loading" screen is now a menu button with the new entry "Add Transparent Blank Video (AMD)". AMD graphics cards do not show the transparency of the default blank video, so the loading video was not played transparent. The new entry works like the existing "Add Blank Video" entry, but adds a transparent .mov blank (kudos to the VPU author of the "AMD Blank Tarc for Loading Screens") instead of the .mp4 blank. The file is downloaded into the server's resources folder with the update.
- **Backglasses**: Added support for .B2SPro backglass files. They are handled like .directb2s files: they can be uploaded (also from archives), are shown in all backglass views and can be edited like .directb2s files. Elements and attributes that are specific to the .B2SPro format are kept when the file is saved. When a table has both a .directb2s and a .B2SPro file, the .B2SPro file is used. Both file types are listed as versions of the same backglass, and setting one as default or disabling the backglass applies to both types. Uploading a backglass in replace mode removes the backglass of the other type. Renaming or deleting a table also renames or deletes its .B2SPro file.
- **VR Support**: Tables can now have their own VR .ini file. When VR support is enabled, the table data section of the table sidebar has the new entry "VR .ini File". It creates "TABLENAME.vr.ini" as a copy of the table's .ini file. While VR mode is on, this file is used instead of the regular .ini file when the table is played, and VPX changes made during VR sessions are kept in it. The VR .ini file is handled together with its table (rename, clone, move, delete, backup).

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/vr.png?raw=true" width="500" />

### Bugfixes

- **Screens / DMD Position**: Fixed the backglass monitor lookup for screenres.txt files that set the backglass monitor by display number (e.g. "3"). The number is now matched against the Windows display name (\\.\DISPLAY3), the same way B2S does it, instead of the monitor enumeration order. On some systems, the screens overview showed the backglass and full DMD on the playfield monitor, the System Manager showed false position mismatch warnings, and "DMD Position" wrote wrong coordinates into the DmdDevice.ini. If you added the backglass monitor offset to the DMD position by hand to work around this, check your DMD positions again.
- **VPin Mania**: Fixed the privacy settings of the cabinet registration. The "Submit Installed Tables" and "Submit Play Count" options were not applied, and highscores were always synchronized. The registration also has a new "Submit All Highscores" option.
- **ALT Sound**: Fixed detection of g-sound bundles.