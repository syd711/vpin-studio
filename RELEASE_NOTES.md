## Release Notes 5.7.3

### Changes

- **Critical Bugfix: Deleting a table could delete the complete tables folder.**
  - **Since version 4.1.0**, the DMD folder of a table is read from the table script (FlexDMD/UltraDMD "ProjectFolder"). If a script set this to the current folder (e.g. `"."`), deleting the table with the DMD option enabled deleted the whole tables folder, and with it the other tables. Up to 5.6.x this was a permanent delete, since 5.7.0 the folder was moved to the recycle bin, but deleted permanently if that failed (e.g. because a file was in use).
  - The DMD folder is now only deleted when it is a subfolder below the table file and does not contain any table files.
  - The same protection was added for ALTSound, ALT color and music files, whose folders and file names are also derived from the table script or ROM name. A music pattern like `"*.mp3"` no longer matches all files of the music folder.
  - On Windows, files are no longer deleted permanently when moving them to the recycle bin fails. The deletion is skipped instead.
  - Resources that are still used by other tables (ROM, nvram, PUP pack, DMD folder, ALTSound, ALT color, music, MAME cfg, backglass, ini files, etc.) are no longer deleted. This applies especially to VPX 10.8.1/10.9 tables located directly in the tables folder, where the "altsound", "serum", "vni", "music" and "pinmame" folders are shared by all of these tables.
  - Table folders are only deleted when they are a subfolder of the tables folder and no other table is located in them.
- **Critical Performance Fixes**: Fixed one additional performance issue caused by the maintenance mode screen loader.
- **VPX 10.8.1/10.9**: 
  - Fixed ALT color files in the shared VPinMAME "altcolor" folder not being detected for tables in the per-table folder layout, which caused false "ALT color missing" validation errors.
  - Fixed highscores of nvram based tables not being read when VPinMAME is not installed inside the VPX installation folder, unless a "pinmame\nvram" folder was created next to the table.

--- 

## Release Notes 5.7.2

### Changes

- **Critical Performance Fixes: Applied performance fixes to the server while tables are played: highscore card on table launch, emulator process detection, highscore monitoring, input handling, pause menu and the server's JVM settings.**
  - **Since version 5.1.8**, the server checked every 5 seconds whether an emulator is running, even when the highscore monitoring was disabled. Every check scanned all system processes multiple times, causing CPU load while tables are played. The check is now only done when highscore changes are pending, and the emulator detection itself is much faster.
- **VPX 10.8.1/10.9**: 
  - Fixed highscores, NVRAM and ROMs not being found for tables in the per-table folder layout when VPX/VPinMAME still writes into the shared folders (user folder, VPReg.stg, VPinMAME nvram/roms).
  - Fixed calculation of the active backglass when used with 10.8.1.
  - Fixed the DMD Position dialog not reading or changing the DMD type (e.g. switching to alphanumeric) for VPX 10.8.1 tables. 
  - Fixed the emulator validation reporting "no games found" for VPX 10.8.1 installations. Subfolders are now included in the check.
  - Fixed the folder buttons of the table sidebar (highscore, directb2s, POV, DMD, PUP pack, ALT sound, ALT color) opening the wrong folder or nothing at all for tables in the per-table folder layout. 
  - The "Open script in Visual Pinball X" button of the table sidebar now starts the VPX executable configured for the emulator, or the first existing VPX build (e.g. BGFX or GL), instead of always using "VPinballX64.exe".
- **Table Data Manager**: Fixed drop-down fields (e.g. Custom2, Custom3, Alternative Launcher) keeping the previous table's value when switching to a table where the field is empty.
- **Backups**: All columns of the backups table are now sortable.
- **Backglasses**: 
  - Fixed deleting the backglass from the table sidebar removing the wrong file when a table had both a .directb2s and a .B2SPro backglass. Only the active backglass file is deleted now, all other versions are kept.
  - Fixed the B2STableSettings.xml getting corrupted when settings were saved concurrently. The file is no longer overwritten when it cannot be read, so the settings of other tables are not lost.
- **Pause Menu**: Removed the "Expert Mode" from the button binding dialog. Buttons are now only bound with "Bind Key". Existing bindings are kept.
- **Overlay**: Removed the overlay design "Recent Highscores / Player Ranking / Offline Competitions / Online Competitions" from the overlay preferences, as online competitions are no longer supported. Overlays that used this design are switched to "Recent Highscores / Player Ranking / Offline Competitions".

Thank you to @Buffdriver, @wrique, @YabbaDabbaDoo and @gonzonia!

---


## Release Notes 5.7.1

### Changes

- **Move/Copy Table**: 
  - The frontend metadata of the table (manufacturer, year, rating, notes, tags, custom fields, …) is now copied too.
  - Fixed frontend media not being copied when the table kept its game name, which caused the media to be deleted on a move. Media is now only deleted after it has been copied, and multiple media items of a screen no longer overwrite each other.

---

## Release Notes 5.7.0

### Changes

- **World of Virtual Pinball**: 
  - Added the context menu actions **Add WoVP Telemetry…** and **Remove WoVP Telemetry…** to the table overview. The telemetry is required for WoVP challenges: it adds two timers and a logging script to the selected VPX tables without affecting gameplay. Optionally, a backup of the original table (.vpx.bak) is created. The actions are only shown when the WoVP integration is enabled.
  - Added the option **Auto-patch competed tables with WOVP telemetry** to the "Subscription Settings" of the WoVP preferences (enabled by default). The WoVP synchronization adds the telemetry to the tables of the active challenges and removes it again once a table is no longer competed. A backup of the original table (.vpx.bak) is created before a table is modified, and tables that are currently played are skipped until the next synchronization.
- **Move/Copy Table (10.8.1 Migration)**: Added support for the VPX 10.8.1 file layout, based on the detected version of the target emulator. Tables moved to VPX 10.8.1 get their own folder with all table files except the frontend media (ROM, NVRAM, highscores, ALT sound/color, PUP pack, music). Tables moved to VPX 10.8.0 or older have these files placed into the shared VPinMAME folders. Frontend media stays in the PinUP Popper media folders, as Popper does not support the new structure yet. See [VPX 10.8.1 Migration](https://github.com/syd711/vpin-studio/wiki/VPX-10.8.1-Migration).
- **Media Bulk Importer**: Added the fullscreen loading video check of the Asset Manager. When PinUP Popper is used and an imported landscape video is the only loading video of a table, the importer asks once after the import whether a blank video should be added for these tables. 
- **Localization**: Added a lot of missing translation, especially for tooltips and progress dialogs.
- **Competitions**: Removed Discord competitions and Discord subscriptions.

### Bugfixes

- **Competitions / VPin Mania Challenges**:
    - The challenges view now loads the challenges via the server, which synchronizes them first. The view shows exactly the table that has been assigned to a challenge (and so is part of the challenge playlist), and the issues of challenges without a matching table. Opting in or out of challenges is applied to the tables immediately.
    - Only tables with the exact table version of a challenge are assigned to it, as scores of other versions do not count for the challenge. If only another version is installed, the challenges view shows that table with a warning, but it is not part of the challenge playlist.
- **Backglasses / .res File Generator**: Fixed frame images of different tables overwriting each other when they had the same filename. Uploaded frames were stored under their original filename in the shared "_Frames" folder, so every table using a frame with that name showed the frame uploaded last. Uploaded frames are now stored with the table name as prefix. Re-select the frame and regenerate the .res file of affected tables.
- **Competitions / VPin Mania Challenges**: 
  - Fixed new challenges not showing up in the challenge playlist until the server was restarted. The server only synchronized the challenges on startup, so challenges started afterwards had no table assigned. The challenges are now synchronized every 5 minutes and whenever the challenges view is opened.
  - Fixed the highscore of a challenge table being reset again when the table was re-matched to the challenge, e.g. after it had been disabled. The highscore is now only reset once per challenge.
  - Fixed WoVP challenges not being synchronized completely. When a challenge matched a table whose VPX file did not exist anymore, the synchronization stopped and all following challenges were skipped. Tables with missing VPX files are now ignored for the matching, and a failing challenge no longer stops the synchronization of the others.
- **VPF/VPU Login**: Fixed security issue that the password for the VPU and VPF sites were not stored/transferred encrypted. 