## Release Notes 4.8.2

- **Competition Wheels**: Reverted "rotation check" because it was causing issues for some users. 
- **DMD Score submission**: Changed processing of frames to support colorized rgb frames.
- **Table Overview**: 
  - Added **Backup Date column**. Note that the column is hidden by default and needs to be enabled in the settings.
  - Added **VBS column**. Note that the column is hidden by default and needs to be enabled in the settings.
  - Added **Music column**. The columns status icon only indicates if music file references have been found in the script, not if actual files have been found.
  - Fixed sorting of several date columns.
- **Table Installer**:
  - Fixed music bundle detection: Added missing **.wav file detection** and target folder resolving (instead of .mp3 only). You need to re-scan tables that use .wav files as audio files.
- **Client Settings**: You can now hide Zen Studio emulators in the **Client Settings** too (the checkboxes were disabled previously). 
- **Server Settings**: 
  - Added "Startup Delay" option. Because the Studio is started with the frontend, this can result into a longer loading/waiting for the frontend. The startup can now be delayed to give more CPU resources to the frontend loading.  
  - Added option to configure fallback folder for the VPX detection if the folder is not detected properly.
- **ZEN/Zaccaria Games**:
  - Fixed issue that for some users these emulators weren't detected anymore. 
  - Improved detection of **pupgames** database changes: additional tables are now automatically added for Zen and Zaccaria emulators when the database is updated with the latest additions.
  - **Zaccaria**: Updated **pupgames** database and added missing tables.
  - **Pinball M**: Updated **pupgames** database and added missing tables.
  - **Pinball FX3**: Manually fixed some special names used for ALT color files.
  - **Pinball FX/3/M**: Fixed and extended the database which contains the required backglass filenames for the different emulators.

## Release Notes 4.8.1

- **ZEN Games**:
  - Fixed upload and detection of backglass files for Zen Studio games. I did not do my homework here and the whole feature needs probably a few more releases to mature.
  - Fixed upload/correct naming for Zen ALTColor files.
  - The ALTColor sidebar section and table column for PinballM is hidden now (Pinball M exclusively uses fullscreen scoreboards, not DMD, so there are no altcolor files there.).
  - The ALTColor sidebar section shows also the ALTColor folder now. This should help to identify possible naming issue regaring Zen Studio games and ALT color files.
- **MAME Games**: Added option not only to import existing ROMs, but also to upload new ones (which will also result in a game creation for the frontend).
- **VPX Launcher**: Added auto-focus of the **Visual Pinball Player** window after a VPX table has been launched.
- **Wheel Augmentation**: The augmented wheel rotation is skipped if the playfield is rotated already.
- **Popper Settings**: Added auto-start option. If enabled the VPin Studio server will start the PinUPMenu.exe (for those who do no want to use the auto-start provided by Popper).

---


## Release Notes 4.8.0

- **VR Support (Experimental - feedback needed!)**: Added support to toggle your installation into VR mode. More details about this can be found here:
   https://github.com/syd711/vpin-studio/wiki/VR-Support
    
  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/vr-mode.png?raw=true" width="500" />

- **Filter Settings**: The table filter panel allows you to filter by issue type now.
 
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/issue-filter.png?raw=true" width="400" />

- **Backglass Manager**:
  - Added support for **Zen Studio** tables (you must configure DOFLinx first!).
  - Added combobox to filter backglasses by emulator.
  - Added emulator name column (which also better explains why for duplicated emulators, backglasses are shown multiple times).
  - Enabled "Backglass" section for the table overview and Zen Studio tables.
- **MAME Game Support (not VPinMAME!)** :
  - Added import support for MAME games/roms. 
  - Added deletion support for MAME games.
  - Added option to the **Play** button to launch MAME games.
  - Fixed several issues for the overview when MAME emulator was selected.
  - Added ROM, Playlist and date columns for the overview and MAME emulators.
- **Emulator Management**
  - The **curl** calls to tell the Studio server that a game has been launched or exited have been added to the emulator types **Zaccaria, Pinball FX/3/M and MAME**. This allows the **in-game recording and pause menu** for these emulators.
- **Media Recorder**
  - Added emulator recording support for **Zaccaria, Pinball FX/3/M and MAME games**.
- **Designer**: Added support for custom highscore card sizes. You can change the size in the "Highscore Cards Settings".
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/custom-cards.png?raw=true" width="700" />
- **Pause Menu for non-VPX Games (first draft)**
  - Added pause menu support for **Zaccaria, Pinball FX/3/M and MAME** games. Note that you need to have the graphics settings set to "borderless window". Otherwise you will run into focus issues.
- **Music Management**: The music management for VPX games has been reimplemented. Audio files from the "Music" folder are detected through the table script including those that are computed from variables. Note that there are still some tables (e.g. Iron Maiden) where the .mp3 filename computation prohibits the detection of the actual filenames. As a result from this...
  - **You need to perform a re-scan of all tables to detect the table's audio files**.
  - Backups include the table's audio files now. Previous implementations took only the subfolder from **Music/<ROM NAME>** for the backup - if available.
  - A new **Missing Audio Files** validator shows if the resolved .mp3 files have been found.
- **Drop-In Manager**: Added search field to filter assets.
- **Studio System Backup**: Added the pinvol and pinemhi .ini files to the VPin Studio backup file.

### Bugfixes

- **WOVP Pause Menu Item**: Screenshots for portrait mode screens are not rotated anymore.
- **WOVP Synchronization**: Fixed issue that discontinued competition types have not been removed automatically.
- **iScored Synchronization**: On table exit, only the active game is synchronized.
- **Notification Delay**: Fixed issue that notifications are shown late because of long iScored synchronizations.
- **Table Overview**: Fixed backup button being visible for all emulator types.
- **Media Recorder**: Added filtering of disabled emulators.
- **Table Data Manager**: Fixed tab focus order for all tabs.
- **Wheel Augmentation**: Fixed various issues and superfluous calls when applying badges to wheels.
- **Media Recorder**: Added timeout of 10 minutes for recordings in case the in-game recorder is never turned off.
- **Table Backups**: Fixed issue that the VPS mapping was not detected on restore.
- **VPS Tables**: Fixed sorting of the sound column.
- **Table Tagging**: Fixed removing of tags.
- **Dashboard**: Fixed possible error in the ranking view.
- **Media Manager**: 
  - Fixed blank video: The existing file was corrupted and caused issues with the new PinUP Popper version. You can fix all broken blank videos by downloading this script (https://raw.githubusercontent.com/syd711/vpin-studio/refs/heads/main/resources/blank_fix.bat), copy and execute it in your PinUP installation directory (e.g. C:\vPinball\PinUPSystem).   
  - Fixed blank video naming which was missing the file number before the screen info.