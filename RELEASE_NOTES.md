## Release Notes 4.3.4

## Changes

- Fixed VPin Mania view that was broken due to previous icon changes.
- 

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