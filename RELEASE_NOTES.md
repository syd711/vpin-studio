## Release Notes 5.5.0

### Changes 

- **Bulk Importer**: Added a new "Bulk Importer" to the table overview toolbar. Select or drop a folder of media files and they are matched to your tables by file name, folder name or both. The minimum match score is configurable, unmatched files can be assigned manually via a table search, and the target screen is detected from the file or folder name. Screen and import mode (overwrite/append) can be set per file or for all selected rows, followed by a single import run.

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

### Bugfixes

- **PinUP Popper Playlists**: Fixed orphaned playlist entries that remained when an emulator or all games of an emulator were deleted. Favorites of SQL playlists are evaluated correctly now.
- **VPin Mania**: Fixed the friends scores check which was executed even when Mania social features were disabled. The contact list is refreshed now when the Mania caches are cleared.
- **Linux**: Fixed media playback issues on systems without an audio device. Failing players are no longer retried endlessly and their native resources are released.
- **Media Players**: Fixed media players that continued loading or retrying in the background after they had been closed.
- **Highscore Card Editor**: Fixed the preview layout breaking when a background video with a different native size was used.
- **Backglass Manager**: Fixed backglasses being reloaded on startup instead of lazily when the view is opened.
- **Studio Client**: Fixed the window header styling when the window is maximized.
- **Studio Client**: Fixed the wrong return value when opening files on Windows.
