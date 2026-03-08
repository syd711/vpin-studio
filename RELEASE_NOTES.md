## Release Notes 4.7.3
 
- **Table Options**: Added "Options" tab to the table data manager dialog. The new tab analysis the table script options so that you canfigure them into a .ini file for the table.
   
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/table-options.png?raw=true" width="550" />

- **Table Backups/Deletions**: Fixed additional issues with the VPX music folder during table deletions and backups.
- **Table Installation**: Fixed issue that upper case .ZIP files were rejected for uploading.
- **Backglass Manager**: Added 'No Focus' setting on backglass server preferences and by table.
- **Backglass Manager**: When a new backglass is setup, default all values from server settings.
- **DMD Position Tool**: Restore type correctly when dmd is deactivated and use backglass scores.
- **DMD Position Tool**: Store latest margin value as default and restore it on new usage.
- **VPin Mania Score Dates**: Fixed issue that the creation date was used instead of the last modification date for the score submitting to VPin Mania.
- **Studio Window Manager**: Fixed _gtk_window_resize: assertion 'height > 0' failed_ error (blind fix).
- **VPin MAME Settings**: Added input field for the table volume. Note that this field is not support for all tables and the values vary for ROM/table.
- **Highscore Parsing**: Fixed table "Eye of the Tiger".
- **WOVP Synchronization**: Fixed issue that if the game script validation fails, the old game id was not resetted. 
- **WOVP Settings**: Added option to disable API keys/users. 
- **WOVP Pause Menu Item**: 
  - Fixed issue that the existing score of the player was not shown anymore. 
  - Added score reload after score submissions with a small delay which should show the updated score that was submitted.
  - Fixed layout glitches.  


---

## Release Notes 4.7.2

- **WOVP Competitions**: Added proper cleanup of games that are no longer competed on WOVP (augmented wheels and competition ids).
- **VPin MAME Settings**: Fixed override and apply of default values (finally).
- **.vpt File Support**: Added missing detection of .vpt files for the table import dialog.
- **Universal Installer**: Fixed extraction of music bundles.

---

## Release Notes 4.7.1

- **Table Management**: Added support for older VPX file format **.vpt**.
- **Preferences Menu**: Fixed rendering the menu, even if the status check for the server fails.
- **Table Validation**: The missing ALT color validation error is only triggered when a non-pinsound bundle is available. 
- **Table Validation**: Fixed lookup of the scripts folder which caused validation issues.


---


## Release Notes 4.7.0

## Changes

### VPX 10.8.1 Support

With version 4.7.0, we are building the foundation for the upcoming VPX 10.8.1 release, which introduces a completely new folder structure for VPX files and their companion files (table override INI file, backglass file, PuP video folder, DMD colorization, music, etc.).

Please note that not all companion software supports the new folder layout yet. For example, PinUP Popper currently does not look into the actual table folder for assets. Therefore, the transition will take some time, and we are working closely with the VPX team to ensure full support.

**Right now, nothing changes for you.** We needed to implement major server-side changes to support the new format for all available companion assets.
First, we will ensure everything continues to work with the old folder structure. Later, we will enable specific flags in the backup restore process that allow backups to be extracted into the new folder structure.

Further reading:
https://github.com/vpinball/vpinball/blob/master/docs/FileLayout.md

### .vpxz File Support

Support for .vpxz files has been added. Check out the YouTube video to see how you can connect your phone with VPin Studio (https://www.youtube.com/watch?v=A-mzXOkTD7E) and upload and install .vpxz files on your mobile device.

A huge shoutout to @jsm174 for his awesome VPX app!

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/vpxz/vpxz-view.png?raw=true" width="700" />

### VPin Mania 2.0

VPin Mania has been relaunched with a new registration system and additional features.
Please watch the YouTube video (https://youtu.be/gjTapjVT3qY) to get an overview of what has changed or visit the [VPin Mania Announcement](https://discord.com/channels/1043199618172858500/1376784123238023168) channel.

**So, is all my data gone now?**

No! Although you now need to re-register with a real user account, your existing cabinet data will be reused once you link your cabinet to VPin Mania again. If this does not work, you can always perform a complete sync between your cabinet and VPin Mania.

**Table statistics are not affected by this update — rankings and play counts remain intact.**

## Changes

- **Table Scans**: Improved PUP pack detection.
- **Pause Menu**: Fixed misaligned position when "too many" entries have been added on the pause menu item list. 
- **Pause Menu**: For the **WOVP** menu entry, the scoring list is refreshed for the selected player (if you have multiple account registered).
- **Pause Menu**: For colorized DMD, the frame is now correctly processed and included in screenshot.
- **Kill Button**: Added MAME to the list of processes to kill when all processes should be stopped. 
- **VPinMAME Settings**: Fixed applying the default values which simply have been deleted before.
- **Tagging**: Fixed issue where tags have been added multiple times for tables.
- **HighScores**: Fix parsing of french highscores.
- **iScored**: Fixed wheel badges for iScored competitions.