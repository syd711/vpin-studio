## Release Notes 5.1.7

### Changes

- **Offline Competition**
  - Fixed remaining Java 25 migration issue that broke the offline competitions view.
- **Player Management**
  - Fixed the deletion of players.
- **Installer**
  - Removed check if any other Java process is running.
- **Pause Menu**
  - Fixed regression from 5.1.5 where the pause button could stop working entirely after the first table of a session.

---

## Release Notes 5.1.6

### Changes

- **Table Overview**
    - Repaired "Not backed up" filter that got broken with the last server-side filter change.
    - Added a table refresh after backups so that a table gets the backup-indicator immediately.
- **Misc**
  - Fixed critical stack overflow error when bulk deleting folders like PUP packs.


---

## Release Notes 5.1.5

### Changes

- **Table Overview**
    - Switched to server-side filtering. This will enable more flexible playlists creations in the future.
- **Media Recorder**
  - Fixed issue that recordings with 0 bytes overwrite existing videos.
  - Fixed issue that recordings were even made with 0 byte because they were outside the visible screen area.
  - For in-game recordings it is checked if the emulator is running in full screen mode. In this case, no notifications are emitted to avoid focus conflicts.
- **Table Data Manager**
  - Fixed issue that PinVOL settings might get lost on save.
- **Drop-In Folders**
  - Added option to skip the confirmation dialog when moving a file to trash.
- **Misc**
  - When a game is deleted though the VPin-Studio, all files are moved to the trash now.
  - Fixed various issued with the dialog resize handling.
  - Added blind fix for Linux client updates.
  - Improved key handling: There are more checks now if the frontend or emulator is running so that when cabinet is maintained, the server ignores these keystrokes.


---

## Release Notes 5.1.4

### Changes

- **System Updater**
  - The process for **DOFLinx** is now killed when the backglass server is updated.
- **VPS Settings**
  - Added different configuration options for the columns visible in the VPS tab and updates shown for the VPS update indicator.
- **Media Recorder**
  - Fixed issues with possible race conditions which resulted in problems overwriting existing files.
- **Table Asset Management**
  - Fixed issue that webp files that were named as .png could not be downloaded or previewed.
- **Asset Uploads**
  - Fixed .cROMc file detection in table bundles.
- **WOVP Competitions**
  - Added custom WOVP badges that are applied for every competition type.
- **Wheel Augmentation**
  - Fixed critical badge synchronization issue so that augmented wheel icons stay augmented for the duration of the competition now.
- **Misc**
  - Updated 7z.

---


## Release Notes 5.1.3

### Changes

- **Uploader Dialog / Analysis**
  - Fixed issue that PUP packs have been installed into the VPX "Music" folder. A stricter check has been added.
  - Improved PUP pack detection of the VPX archives. 
  - Fixed various issues in the "Media Selection" dialog which previews the detected assets.
- **Table Data Manager**
  - Fixed issue that clicking forward and backwards button, the editor was marked dirty although nothing was changed.
- **iScored Settings**
  - Fixed issue loading Game Rooms because the URL was not trimmed before saving.
- **DMD Positioning**
  - Fixed issue that dot (".") version delimiters have been replaced with whitespaces.
- **Misc**
  - Blind fix for repairing opening remote folders.


---

## Release Notes 5.1.2

### Changes

- **WOVP Score Submitter**
  - Fixed issues in the error handling so that failed score submissions are reflected to the user. 
- **Wake-On-Lan**
  - Add several blind-fixes(!!!) to repair the wake-on-lan functionality. See also: https://github.com/syd711/vpin-studio/wiki/Wake-On-Lan-Support.   
- **Server Discovery**
  - Improved server discovery when the client has been started before the server.
- **VPX Emulator Execution**
  - Improved launch command for the VPX.exe which is more robust now against special characters in the file name.
- **Pause Menu**
  - Fixed the creation of screenshot bundles created via shortcut which were empty.
  - Improved description texts for screenshots to clarify the settings (hopefully).
  - Fixed issue that taking screenshots via shortcut was bound to the "Cabinet Monitor" and the view settings used there. 
- **Misc**
  - Removed two older dependencies whose functions could be replaced with other dependencies already included.
  - Increased max timeouts values in Popper and cabinet settings. 

---

## Release Notes 5.1.1

### Changes

- **Pause Menu/Screenshot Generation**
  - Moved option to always generate a screenshot when the pause menu is shown from the "Integration Settings" to the "Pause Menu" settings.
  - Fixed playfield rotation for screenshots.
  - The key event based screenshot creation (see Controller Inputs) does now also check which monitors are enabled for screenshots.
- **Updater**
  - Added additional updater fixes to force the server update shutdown. In case you have faced issues, you need to perform a manual update: Extract the VPin-Studio-Server.zip and double-click the server's .exe file (or reboot the cab). See also: https://github.com/syd711/vpin-studio/wiki#manual-updates
- **Table Overview**
  - Added filter option for non-backed-up tables.

---

## Release Notes 5.1.0

### Changes

- **Pause Menu**:
    - Added new "Todo List" menu item. Here you can quickly tag issues you experience with the table so you don't forget them after playing.
    - Changed view mode settings which simply differs now between Cabinet, Desktop and Apron mode view, selectable via radio group.
    - The view settings with scaling, top and left margins are now applied for all view modes. Most likely this will only be used for apron screens and non 16:9 screens.
    - The design when used in desktop mode can now be improved by downscaling and limiting the number of visible menu items.
    - Fixed the "Mania Scores" menu item which shows highscores now again.
    - Fixed loading of wheel images for the "Mania Scores" view which was using an outdated database.
    - Fixed wrong initialization of the "Margin Left" view setting.
    - Added option to disable rule cards.
    - Added option to disable info cards.
    - Added full-screen option for tutorial videos.
    - Improved startup speed for tutorial videos.
    - The tester has been integrated into the preferences panel to have less clicks for testing.
    - No server restart is required anymore to apply all changes.
- **Table Management**: Added backglass selector to the backglass sidebar.
- **Preferences Menu**: Added cabinet selector to the preferences drop-down menu, so that you can immediately switch to other cabinets.
- **Designer**: Fixed color value parsing issues that broke the UI.
- **Script Details Section**: Added the information about the PUP pack name that was scanned by the Studio.
- **PinUP Popper Media Search**:
  - Added caching.
  - Fixed URLs with "@2a" segments.
- **DMD Info Section**: 
  - Improved detection of FlexDMD folders.
  - Fixed DMD bundle size calculation.
- **Misc**:
  - Fixed .rar extraction for PUP packs.
