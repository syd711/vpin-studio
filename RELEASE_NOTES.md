## Release Notes 5.5.2

### Changes

- **Highscore Backups**: The backups in the highscore backup dialog are now sorted by date, newest first.
- **Bulk Importer**: Added the import mode "If Missing", which only imports a file if its screen has no media yet.
- **ALT Sound**: Fixed detection of g-sound bundles.
- **Webhooks**: Added the webhook types "Table Launch" and "Table Exit" to webhook sets. They are called with the game id when a table is started or exited.
- **Table Overview**: Enabling or disabling tables from the context menu now runs in a progress dialog, so large selections no longer block the UI.
- **VPin Mania**: Disabled tables are no longer synchronized with VPin Mania. They are excluded from the installed tables list, and their highscores and played counts are not submitted.

---

## Release Notes 5.5.1

### Changes

- **Bulk Importer**: 
  - Improved scrolling performance.
  - Moved loading and matching into separate progress bars.
  - Improved table matching.
  - Fixed screen matching and screen naming.
  - Changed selection mode and switched from row based selection to checkboxes so that items can be easier unselected now.
  - Added backup options to move imported assets into another folder for a better progress tracking.

---


## Release Notes 5.5.0

### Changes 

- **Rom-2-AltSound Support**: Added support for [rom2altsound](https://github.com/Le-Syl21/rom2altsound) (kudos to @Le-Syl21), which plays every sound command of a ROM on PinMAME's emulated sound board and records each one to its own audio file. The result is a ready-to-use ALT sound pack in the G-Sound format. Use the new "Create from ROM…" button in the "ALT Sound" section of the table sidebar to generate a pack for tables that don't have an ALT sound yet.
  - The dialog shows the installed and latest rom2altsound version. It can be installed or updated from there.
  - The ROM is checked before the recording starts, and the dialog shows the detected game, sound board and target folder.
  - Options: recording volume ("As the game sets it" or "As loud as possible"), the longest sound length, and whether ALT sound is enabled for the ROM after the installation.
  - Advanced options: loop search and intro loop length, merging of twin sounds, DC blocking, skipping the factory settings boot, the DCS ducking check, skipping the chips pass (WPCS, System 11) and the older BSMT emulation (Data East, Sega, Whitestar).
  - Recording all sounds of a ROM takes a few minutes and runs as a background job.

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/rom2altsound.png?raw=true" width="800" />

- **G-Sound Editor**: Added various improvements and fixes to the g-sound bundle editor. Audio files from the bundle can now be played and renamed in the editor. There is also an option to submit the file name to the rom2altsound portal to use these names for future conversions.

- **Bulk Importer**: Added a new "Bulk Importer" to the table overview toolbar. Select or drop a folder of media files, and they are matched to your tables by file name, folder name or both. The minimum match score is configurable, unmatched files can be assigned manually via a table search, and the target screen is detected from the file or folder name. Screen and import mode (overwrite/append) can be set per file or for all selected rows, followed by a single import run. The bulk importer can also be used from the Drop-In menu.

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/bulk-import-1.png?raw=true" width="400" />
  
  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/bulk-import-2.png?raw=true" width="700" />
  
- **Backglass Manager**: 
  - Added "Bulk Import" to extract the backglass or DMD images of all tables that have a backglass but no backglass/DMD media asset yet, and add them as frontend media.

    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/bg-import-1.png?raw=true" width="400" />

    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/bg-import-2.png?raw=true" width="700" />
  
  - Added "Change Resolution of all .res Files…" to bulk-convert the .res files of an emulator. You can set the playfield resolution, scale the backglass (frame, DMD and positions are kept proportionally) or scale only the DMD size and position. A backup of each modified file is stored as `<name>.res.bak`.

    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/bulk-res-1.png?raw=true" width="400" />

    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/bulk-res-2.png?raw=true" width="500" />

- **DMD Position**: 
  - Added the table specific "Scale to HD" and "Scaler Mode" settings of the `DmdDevice.ini`. When set to "Default", no entry is written and the global settings apply.
  - Added zoom in/out (Ctrl + mouse wheel) and "Fit to Window" to the preview.

    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/dmd-pos.png?raw=true" width="700" />
    
- **Table Filter**: Added table metadata filters for table type, manufacturer (including an option to exclude manufacturers), minimum rating and year range.

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/table-filter.png?raw=true" width="250" />

- **Auto-Match**: The auto-match dialogs have a new "Overwrite Metadata" option. When checked, the table metadata (e.g. year, manufacturer, type, theme, author, notes and tags) is overwritten with the data from the VP-Spreadsheet.
- **Media / Audio Extraction**: Videos that are dropped or uploaded to an audio screen (e.g. "Audio" or "Audio Launch") are converted to MP3 now. Only the audio track is stored, not the video.

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/audio-extract.png?raw=true" width="500" />
  
- **Music Detection**: Improved the detection of music files referenced in table scripts. Music folders that are defined via script variables (e.g. `MusicDirectory = GetMusicFolder & "\Blood Machines OST"`) are resolved now, and audio files played via the PuPlayer of script-coded PUP packs are no longer reported as music files.
- **Highscore Cards**: The "Render Friends" option of the score layer is stored with the template now. Friends scores from VPin Mania are cached for the preview and are shown as external scores on the card.
- **Table Asset Manager**: 
  - The asset source selection is a dropdown with checkboxes now, so you can search any combination of asset sources. The menu includes "Select All" and "Deselect All" entries, and the search button is disabled when no source is selected. All sources are selected by default and the selection is remembered.
  - The search in the drop-in folder runs in the background now, so the dialog no longer freezes for larger folders.

    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/drop-in.png?raw=true" width="600" />
    
- **Scoring Database**: ROM mappings can be restricted to specific table files now, so tables that share the same scanned ROM name can be mapped differently.
- **Table Export**: Added the column "pupPackInstalled" to the table export. It contains `true` if a PUP pack is installed for the table, `false` otherwise.
- **VBS File Uploads**: Added option to upload .vbs files. These can be uploaded via the upload menu, context menu and via drag-and-drop on the workarea or from the drop-in folder menu.
- **Script Outliner**:
  - The script outliner has an upload button now, where .vbs files can be uploaded.
  - The table info contains an additional row about the .vbs file now, in case the user has not activated the .vbs file table column which is disabled by default.
  - The .vbs info row has a preferences button which points to the server settings. Note that the Studio might delete the .vbs file after editing automatically. This setting can be changed there.

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/script.png?raw=true" width="500" />


### Bugfixes

- **PinUP Popper Playlists**: Fixed orphaned playlist entries that remained when an emulator or all games of an emulator were deleted. Favorites of SQL playlists are evaluated correctly now.
- **VPin Mania**: Fixed the friends scores check which was executed even when Mania social features were disabled. The contact list is refreshed now when the Mania caches are cleared.
- **Linux**: Fixed media playback issues on systems without an audio device. Failing players are no longer retried endlessly and their native resources are released.
- **Media Players**: Fixed media players that continued loading or retrying in the background after they had been closed.
- **Highscore Card Editor**: Fixed the preview layout breaking when a background video with a different native size was used.
- **Backglass Manager**: Fixed backglasses being reloaded on startup instead of lazily when the view is opened.
- **Studio Client**: Fixed the window header styling when the window is maximized.
- **Studio Client**: Fixed the wrong return value when opening files on Windows.
- **Table Overview**: Fixed issue that the trophy icon was only shown when iScored was enabled. It is shown now if the table is competed in any competition type.
- **Localization**: Added missing localizations, mostly in the table overview area.