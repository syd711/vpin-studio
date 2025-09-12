## Release Notes 4.2.2

## Bugfixes

- **Highscore Cards**: Fixed centering of highscore cards when shown on startup.
- **Server**: The DOF process is not killed anymore when the frontend is terminated. This hopefully avoids issues that after recordings, outputs remain on HIGH state.
- **Main Window / Jobs Menu**: Fixed jobs progress drop-down menu which did not proper show the current queue state of the jobs to execute.
- **Main Window / File System Folder Buttons**: Fixed resolving of network drives for the yellow file system buttons on the table overview sidebar panels. 

---


## Release Notes 4.2.1

## Bugfixes

- **Backups**: Fixed mix-up of .res and .ini files.
- **Backups**: Added target folder selection to the backup folder so that backup "sources" can also be chosen as target.
- **DMD Position**: Removed ending version in filename for tables using ultraDMD or flexDMD.
- **DMD Position**: Fixed save of values when section name contains dots.
- **VPS Settings**: Fixed issue that for new tables, the VPS updates have been disabled by default.

---

## Release Notes 4.2.0

## Changes

- **Highscore Card Editor**
  - The highscore card editor has undergone a huge revamp. Not only all visible elemens support **drag and drop** now, but also new elements have been added. E.g. you can now add the **manufacturer logo** or **media from other screens**. Also, the templating mechanism has been reimplemented which should simplify the re-use and adaption of existing cards layout. **Note that due to the refactoring of the editor, old layouts may got broken or need adaptions.** 
   <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/cards/cards.png?raw=true" width="700" />

- **Table Backups**
  - The Studio supports creating and restoring table backups now. **Note that this feature is solely for private use only!!!** For this reason, the backups are password protected. To enable the feature, you need to configure your VPU or VPF account for authentication. Check out the YouTube channel video linked on our Discord for more details. Note that VPBM has been removed entirely from the Studio for this.
  
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/backups/overview.png?raw=true" width="700" />
  
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/backups/backup.png?raw=true" width="500" />

  
- **Media Sources**
  - The Studio allows to configure additional asset sources now. These will be visible, configurable and selectable during the table asset selection. In addition to this new configuration option, the Studio comes with new pre-defined asset sources. These are:
    - Kongedam Video Tutorials (you can now download these directly for one of your frontend screens)
    - Superhac's VPX Media Database (https://github.com/superhac/vpinmediadb)

    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/preferences/media-sources.png?raw=true" width="700" />
    
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/preferences/media-sources-selection.png?raw=true" width="500" />
  
  
- **Tables Overview**:
  - **Added optional column "Tutorials"** also to the regular table overview. This way, users can easier see if there will be a tutorial available in the pause menu.
  - **Improved ROM column**: The tooltip of a ROM displays additional alias information now. In addition to that, you can use the table search field to filter games by their ROM name or alias.
  - **Improved VPin MAME section**: In addition to the alias mapping, the section shows also the tables which share the same ROM.
  - **NVOffset Management** For the tables that are shown with the same ROM usage, you can edit the nvoffset now too. The script is immediately updated, so you don't even need to open the script editor anymore for this.

    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/alias.png?raw=true" width="400" />
  
  - **Responsive Toolbar**: Since a lot of users use the Studio with a full HD monitor a second toolbar row has been added so that all toolbar actions are visible all time.
  - **VPS Updates Indicators**: You can disable VPS updates for a table now. The checkbox for this can be found in the sidebar VP spreadsheet section. Note that updates are still recorded for the table, but not visualized anymore.

    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/ignore-updates.png?raw=true" width="700" />
  
- **Table Imports**
  - The table import dialog has been improved. The file size is now shown too. Also, you can directly delete .vpx of .fp files so get rid of file corpses.
  
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/imports.png?raw=true" width="400" />
  
- **Table Deletions**:
  - The last selection of the deletion dialog is remembered now.
- **Pause Menu**
  - Added pause menu support for stand-alone mode of VPX.
- **Monitoring View / Screenshots**
  - Added a summary screenshot to the screenshot bundle which contains all screens as one image, including a timestamp.
- **Drop-In Folder**
  - Added option to move the uploaded file to the trash bin.
- **Image Previews**
  - All image preview action button have been reworked and using the build in image preview dialog now.
- **Backglass Manager**
  - Added divider component so that the editor view is resizeable.
- **iScored Subscription**
  - Improved logging about game subscription synchronizations.
  - Added additional "Ignore Hidden" configuration option for games rooms. This option skips the synchronization of games that are not public visible to the user.


## Bugfixes

- Fixed maintenance video, so it is now always displayed on the primary screen.
- Fixed modal mode toggling of the asset manager dialog. The dialog can now stay open without blocking again.
- Fixed MAME settings status for some tables (e.g. Addams Family) caused by upper case ROM names.
- Fixed "Missing Backglass or PUP Pack" validator which sometimes raised a false positive when the PUP pack detection wasn't finished.
- Fixed ALT sound upload so that a game selection is mandatory now. This enforces the correct ROM name to be used from the game instead of guessing it from the uploading file.
- Fixed initial selection of the backglass manager: When you press the backglass button from the backglass sidebar in the tables overview, the backglass manager tab initializes with the correct selection again.
- Fixed saving the PinUP Popper preferences and added option to set the default media directory. 
- Fixed installation of DMD bundles.
- Fixed VPS updates to match the new tutorials data format.
- Improved detection of music bundles. 
- ...and improved the wiki documentation (well, at least a bit...) 
