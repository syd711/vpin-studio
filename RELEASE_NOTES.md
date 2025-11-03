## Release Notes 4.3.10

- DOF Server: Removed installation check.
- Backups: Added option to overwrite existing backups, enabled by default.
- Backups: Fixed refresh bug that had shown duplicated entries.

---

## Release Notes 4.3.9

- Notifications: Added **text margin** spinner which allows you to adjust the notification text positions in case it is off.
- Backups: Fixed missing backup of .directb2s files when .vpx files are located in their own sub-folder.
- Backups: Added auto-detection for sub-folder based backups. Tables are now restored into their own-subfolder when they have been backed-up this way.
- Backups: Added backup and restore of the table's **B2STableSettings.xml** data. (Note that you have to backup your tables again if you want to have this data included.)
- Table Data Section: Added missing field **Media Search**.
- Table Media Section: Added label for value **Media Search** which can override the asset name value.
- Table Data Manager: Added missing input field for the **Media Search** data on the "Meta Data" tab, including a help tooltip.
- Table Asset Manager: Added check for the field value **Media Search** which can override the asset name value. (_Note that no assets can be searched for tables which have a **Media Search** set. I'm not sure if this solution is valid yet, but I found it confusing that you can download assets and they don't appear in the asset list afterwards)_.
- Miscellaneous: Enabled text wrap for most textareas for a better readability.


--- 

## Release Notes 4.3.8

- Jobs Menu: Added **Cancel All** button to cancel all queued jobs.
- Jobs Menu: Fixed several UI freezes when hundreds of backups are started at once.
- Recording Dialog: Added hint that the GL version is not supported for recordings.
- Recording Dialog: Filtered the VPX GL version from the emulator combobox.
- Backups: Fixed memory leak and possible concurrent access errors.
- Notifications: Added **test button** that shows a test notification.
- Notifications: Added **margin offset** spinner which allows you to adjust the notification position in case it is off.

---

## Release Notes 4.3.7

- Backups: Fixed memory leak that happened when apng files have been used as preview icon for backup files.
- Table Overview: Changed order of status icons. The backup icons are the aligned left to the status icon.
- DOF Preferences: Fixed error in installation check.
- The startup time for the server has been significantly improved. Thank you @gorgatron for helping us testing these changes. 

---

## Release Notes 4.3.6

- Fixed delete action for backups.
- Performance optimization: Added caching for game media data which will improve the performance of the dashboard and player's score list.
- Performance optimization: Improved all list views (dashboard, player highscores, competitions) so that only the first frame of .apng wheel icons is loaded. When shown in dialogs, competition headers or in the asset manager, the full .apng animations are loaded and shown.

---

## Release Notes 4.3.5

## Changes

- Improved performance of backup loading.
- Fixed table sorting of backups.
- Fixed some upload dialog issues that where misaligned for long filenames.
- Fixed of PinVOL settings: If the PinVOL service is running while saving changes, it is restarted now so that the in-memory values are not used on table start.
- Added additional "Hide Toolbar" call for frontend based recordings. The frontend itself (e.g. Popper) should take care of the, but there are reports about the Windows toolbar not hiding.

---

## Release Notes 4.3.4

## Changes

- Fixed VPin Mania view that was broken due to previous icon changes.
- Fixed drag and drop issues where the drop zones for screens have not been resetted on drag-exit.
- Added performance logging for backup to evaluate possible issues.
- Fixed spaces in tag-names of b2STableSettings.xml.
- Added drag and drop support for assets. You can now **copy** assets directly between screens via drag and drop. This might come in handy when you want to use the playfield video as loading video too. Note that the asset type is not checked for this operation.

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/dnd.png?raw=true" width="700" />

---

## Release Notes 4.3.3

## Changes

- Backups: Fixed memory leak.

---

## Release Notes 4.3.2

## Changes

- Backups: Fixed critical refresh issue that lead to an endless reload of backups after creating a backup and restarting the Studio.
- Backups: Added footer label to show the amount of backup files.

---

## Release Notes 4.3.1

## Changes

- Fixed missing icons that prevented the preferences from being loaded.

---

## Release Notes 4.3.0

## Changes

- **Table Backups**
  - The "Default Backups Folder" is not mandatory anymore and can be re-configured with another name and folder.
  - Added backup indicator for the tables overview so that you can immediately see which tables are backed up already. 
    
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/backups/indicator.png?raw=true" width="500" />

- **Folder Selections**
  - Added new dialog to select folders from the remote system. This allows the configurations at a lot of places now, where it only had been possible when the Studio was used on the cabinet itself. This also helps set up **backup folders** from remote.
  
     <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/misc/folder-chooser.png?raw=true" width="700" />

- **Tables Overview**
  - Support of next/previous navigation in DmdPosition tool when opened from the tables overview.
  - Support of next/previous navigation in the Table Data dialog.
  - Added "Save" shortcut (Ctrl+S) in Table Data dialog.
  - Added support of puppack in studio for PinballX and PinballY

- **BackglassManager**
  - Added a frame generator directly in studio, on an idea and python script provided by @Himura95. Four different generators are provided: 
    - Ambilight: Uses the pixels on the edge of the backglass to generate the frame.
    - Blurred: Creates a blurred zoom of the backglass.
    - Mirror: Creates a blurred mirror reflection with perspective.
    - Gradient: Calculates the dominant color of the image and use it to draw a gradient to black.

     <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/backglassmanager/res-default.png?raw=true" width="350" />   
     <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/backglassmanager/res-mirror.png?raw=true" width="350" />   

- **Assets and Media** 
  - In the Table Media tab of the tables sidebar, videos are now previewed as one frame vs. the full video that is consuming lots of CPU. On mouse over, a play button is shown to start the video playback. A stop icon button stops the video.
  - In Table Asset Manager, added "Set A Default" button to choose default asset in the list of assets.
  - Improved streaming of table assets and frontend media.
   
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/media-sidebar.png?raw=true" width="500" /> 

- **Highscore Cards** 
  - Raw highscore text is split into different columns now to fit the zone.

- **PinVol Preferences** 
  - Added possibility to change the installation folder of PinVol and switch between PinVol provided as part of Studio to an installed version.
  - Improved monitoring of the PinVol process.

- **Misc**
  - Updated vpxtools.exe to v0.24.2. Thanks again to @francisdb here for his awesome tool!

## Bugfixes

- Fixed wrong update check that happened when one client was connected/disconnected to different servers.
- Fixed pause menu that did not load the highscore card.
- Fixed performance issues during re-installations for cabinets that have already been registered on vpin-mania.net.
- Fixed refresh after table patching: The table scan is triggered after patching a table now. This will detect additional changes made by the patch, e.g. the DMD type.