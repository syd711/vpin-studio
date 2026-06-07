## Release Notes 5.0.3


### Changes

- **Virtual Pinball Spreadsheet**: Fixed opening VPF links. 
- **Uploads**: Fixed installation issues for .rar files.
- **Table Overview**: Fixed issue ROM column not loading (hopefully this time).
- **WOVP**: Fixed race condition errors during the server startup which may have blocked updates for some users.

---

## Release Notes 5.0.2

### Changes

- **Server Update**: Added fix for server not shutting down properly before trying to overwrite file.
- **PinemHi Font Selection**: Fixed font selector so buttons show and font can be set.  
- **Navigation Breadcrumbs**: Fix navigation breadcrumbs updating to designer when on tables.
- **Competition Resource Folder**: Fixed creation of competition resource folder on Mac.
- **Usability**: Made numeric spinners editable so a number can be typed in for...
  - PinemHi display settings
  - PinVol preferences
  - Player ranking preferences
  - Table sidebar VpinMAME settings
- **Drop-In Menu**
  - Add lazy rendering which allows you to have hundreds of files in your drop-in folder without having performance issues while opening the menu.
  - Added better focus management.
  - Added DEL keyboard shortcut to delete focussed asset.
- **Backup File Naming**: Change Backups to use Display Name of tables instead of File Name. This fixes existing backups not being overwritten (when overwrite is selected) when a version changes and the version is in the filename.
- **PinEMHigh Updates**: Changed the update mechanism. These files are now fetched like all others via the initial file sync. 

---

## Release Notes 5.0.1

### Changes

- **VPin Studio Backups**: Added automatic VPin Studio backups for minor versions.
- **VP-Spreadsheet**: Fixed VPS view which did not show all assets because of empty date values.
- **Table Overview**: Added more logging to figure out why for some users the ROM column only shows "..." as a value.
- **Text Editing**: Fixed various issues opening text or .vbs files with the internal editor.
- **Table Scans**: Fixed critical issue for table scans which caused script change detections not being applied.
- **MacOS/Linux Clients**: Fixed missing exports for apng supports which broke the clients.
- **VPin Mania Sync**: Added result dialog for single score synchronizations from the sidebar.

---


## Release Notes 5.0.0

### !!! Important Notice !!!

We are happy to announce that the next major release of VPin Studio 5.0 has been published. While it does not introduce many flashy new features, we have extensively revamped the underlying codebase to ensure the project's long-term maintainability and support future development.
The major drawback:

**You need to reinstall the Studio Client(s) and Server. Updates for Studio 5.x cannot be applied over a 4.x installation.
Please use the existing installation folder during the reinstall process to ensure that all your data remains intact!**.

### Changes

- **Java 25 Migration**
  - Code base has been migrated from **Java 11 forward to Azul Zulu Java 25(!)**, including an update for all 3rd party dependencies. 
- **Competition Wheel Icons**
  - Added icon augmentation to APNG wheels.
- **Splash Screen**
  - Added details to splash screen showing connection steps/attempts.
- **Mac**
  - App Icon now works correctly as dynamic icon with MacOS 26+.
  - Fix splash screen not showing.
  - Added background to DMG.
  - Changed DMG creation to use create-dmg instead of jpackage for more robust options and simpler workflow.
- **Future Pinball**
  - With long overdue, VPin Studio 5 supports Future Pinball highscores now too. The support includes:
    - the Highscore Card which allows to design highscore cards for Future Pinball now.
    - Highscore Backups
    - VPin Mania Support
  
    Note that because of the limited test data a lot of highscore lists might still look broken. Please submit your fpRAM file in that case on our Discord so that we can improve the parsing.

### Breaking Changes

- **Players**: The players "iScored Name" has been renamed to "Competition Name" and is used for Discord too. **You have to reconfigure the name as the old value has been discarded!**
- **Notifications**: The notification settings for iScored have been resetted.

### Bugfixes

- **Table Overview**: Fixed issue that sometimes not all tables have been loaded initially.
- **DMD Screen Capture**: Fixed the DMD capture when dmddevice is set to double or scale2X scaler mode. Used to transfer DMD score in WoVP.
- **Statistics**: Fixed issue analytics not being updated for PinballY in the pause menu.
- **Highscores**: Fixed VPReg.stg file lookup.
- **VPU/VPF**: Fixed login tests.
- **Discord Competitions**: Fixed issue that the player name used being inconsistent for the first and consecutive scores.
- **Player Avatars**: Fixed issue of the white outer avatar ring keep growing with every save.
- **Future Pinball**: Fixed installation of .fpl files.
- **Update Info Dialog**: Fixed size issues for smaller screens.
- **Highscore Backups Dialog**: Add multi-selection for deletions.
- **PinUpPlayer.ini Parsing**: Fixed issues parsing the file caused by comments.

### VPin Mania

- Added synchronization of Future Pinball highscores.
- Fixed synchronization issues.

