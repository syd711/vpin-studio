## Release Notes 3.10.5

## Changes
- **Tables / Highscores Section"**: Fixed yellow open folder button not working for all score types.
- **Tables / Media Recorder**: Increased timeout to wait for the launched game from 30 seconds to 1 minute (this time right!).

---

## Release Notes 3.10.4

## Changes

- **Tables / Overview**: Fixed critical loading issue of games where no matching emulator is found. This is more of a workaround while the root problem has not been fixed yet.
- **Tables / Media Recorder**: When emulator based recording is selected, the Windows taskbar is hidden for the duration of the recording now.
- **Tables / Media Recorder**: Increased timeout to wait for the launched game from 30 seconds to 1 minute.

---

## Release Notes 3.10.3

## Changes

- **Tables / Media Recorder**: Added more recording options to tackle the OpenGL problem. You can record games via the VPX launcher now, skipping the frontend start and exit calls. This way, another VPX.exe can be used which allows to work around the playfield recording problem.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/recorder/recorder-dialog.png" width="700" />

- **Tables / Media Recorder**: Disabled VPin Studio overlay when Popper is started for recordings. 
- **Tables / Date Columns**: Added missing modification date column data.
- **Tables / Patching**: Fixed issues that tables located in subfolders were not cloned into a new subfolder.
- **Tables / Playlist Column**: Added missing tooltips for custom playlists.
- **Tables / Data Manager**: Fixed layout issue for super long names.
- **Tables / Asset Columns**: Darkened the in-active/empty state icons in tables so have a better contract to see which assets are available or not. 
- **Tables / Asset Manager**: Fixed styling of list selection which was not highlighted properly.
- **Tables / Asset Manager**: Provided "undock" button in the header. Using this options removes the "modal" option of the dialog, so it can be kept open. The selection is automatically updated for the current table.
- **Tables / Highscore Resetting**: Fixed resetting VPReg.stg based highscores using the correct charset this time.
- **Tables / Backglass Manager**: Fixed issue finding the matching game for a backglass.
- **Overlays**: Fixed the issue that highscore cards shown centered on the playfield or displayed via popper screens were a bit off.
- **PinballY**: Fixed the parsing of tables database preventing visualisation of medias when **mediaName** was different from **fileName**.
---

## Release Notes 3.10.2

## Changes

- **Tables / Media Recorder**:
  - Fixed broken recordings of some tables caused by invalid characters in the generated temporary recording file.
  - The columns of the recorder tab are completely dynamic and only the ones that are supported are visible.
  - For PinballX the playfield video rotation is applied directly after the recording.
  - Fixed macOS layout problem where the recorder panels where resizing slowly (hopefully).
  - Fixed issue that in case **overwrite** mode was selected for a screen and the file is still locked by the frontend, **append** will be used as fallback.
- **Tables / ROM File Uplods**: Improved ROM file upload detection.
- **Tables / Uploads**: Fixed critical error in media selector.
- **Drop-In Folder**: Improved feature description in the settings.
- **Table Launches**: The call of the **SendPuPEvent.exe** when Popper is restarted or stopped is optional now.
- **VPS Version Selector**: Limited visible item count to 5. That should avoid the to interpret the upper-most entry as being not completely visible.

---

## Release Notes 3.10.1

## Breaking Change

- **Overlay Settings**: Due to a breaking change in the configuration format, you have to reconfigure the overlay settings in the preferences.

## Changes

- **Overlay / Pause Menu / Notifications**: All VPin Studio overlay elements have the additional "Screen" setting now. This allows to show overlays on other screens that are not the primary screen (which may be the case for PinballX users). The default remains the primary screen. So PinUP Popper users should not be affected by this change (beside the fact everyone needs to re-configure the overlay settings).
- **Tables / Table Asset Manager**: Fixed deletion of wheel icons for games and playlist so that the thumbnails of these icons are deleted too. Otherwise, you still may see old icons that have not been cleared from PinUP Popper thumbs folder.
- **Tables / Media Recorder**: Fixed issue that the preview screen section remains empty when launched the first time.
- **Tables / Highscores**: Exchanged icon for the highscore reset button, to indicate that this is more than a simple deletion.
- **Tables / NVOffset Validator**: The validator is more restrictive now and complains only if two tables that use the same ROM and have the same **nvoffset**, either have no or a different VPS table mapping. Note that only the table type is checked, not the table version.
- **Toolbar**: Fixed missing hiding of the frontend UI button that should be hidden when connecting from remote.

---

## Release Notes 3.10.0

## Changes

- **Tables / Remote + In-Game Recording**: The Studio supports remote recording of frontend screens now. You find the recorder as an additional tab in the tables section. The recorder supports single and bulk recording and gives a preview of the screens that are recorded. In addition to that, you can start an in-game recording by binding a key for this. More details about this can be found here: https://github.com/syd711/vpin-studio/wiki/Media-Recorder 

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/recorder/recorder.png" width="800" />

- **Tables / Cabinet Monitor**: The Studio supports the screen monitoring of cabinets now. You can invoke the cabinet monitor from the main toolbar. The dialog offers two different view modes right now: monitoring the actual monitors or monitoring the configured frontend screens.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/monitor/monitor-toolbar.png" width="400" />
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/monitor/monitor-1.png" width="700" />
  
- **Tables / Patching**: Given the recent releases of patch files, the Studio supports patching tables now. **.dif** files are now supported like any assets, so you can drag and drop them from you operating system or the drop-in folder. If the .dif file is archived it will be analyzed like any other bundle and the additional assets will be applied to the patched table too.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/patch-dialog.png" width="700" />

- **Tables / Frontend Game Launch**: For PinUP Popper users, the launch menu has an additional entry to launch a table through PinUP Popper (thanks to @nailbuster helping here!). This is useful in combination with the cabinet monitor to see if the screens are configured properly. 
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/launch-menu.png" width="280" />

- **Tables / Table Asset Manager**: The asset manager gives you the additional options to delete not only single assets, but also all assets of the selected screen and all assets of the game.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/asset-deletions.png" width="500" />

- **Tables / Table Asset Manager**: PinballX and PinballY users have video conversion options now too. (They were only shown for PinUP Popper users before).

- **Tables / Validators**: Introduced new validator for VR support. The validator checks if there is a VR room flag inside the script and returns an error if not enabled. **If you do not use VR, please disable this validator as there is no option to disable it by default.** Note that you need to re-scan all tables to find disabled VR rooms. 

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/vr-validator.png" width="500" />

- **Tables / Script Editing**: Added info icon to the split menu button with a hint that .vbs files must be linked to a text editor in order to use that action.
- **Tables / Usability Improvement**: Pressing the **shift key and double-clicking** on a table in the table overview will open the asset manager dialog.
- **System Manager**: Removed the 32/64-bit preset combo from the "Overview" tab and removed the corresponding filtering from the release artifact comboboxes. Especially for freezy updates where you might want to install both versions, this switching was too tedious.
- **Updates**: The release notes for the next update are now shown before (and after) updating. So for versions larger 3.10 you will see the release notes the next time when pressing the update button.
- **VPX Game Launcher**: The VPX game launcher is launching the emulator window minimized now.

## Bugfixes

- **Tables / Uploads**: Improved **cloning** behaviour so that the original name is kept and the existing VPS mapping too, if the auto-match flag has been disabled.
- **PinUP Popper Integration**: More gracefully stopping of PinUP Popper by calling the regular exit command first (instead of simply killing all processes).
- **Competitions / iScored**: Added **Event Log** button to inspect events of the selected table.

## VPin Mania

- **Tournaments / Tournament Browser**: Fixed remaining time label.
- **Tournaments / Highscores**: Fixed issue that scores where only shown for installed games.
- **Tournaments**: **Tournaments have been disabled/hidden by default and must be enabled in the preferences.** You won't loose any data by this. It improves the boot-up time of the server a bit since the automatic synchronization of tournament highscores is also disabled this way.
- **Table Views**: Added more convenience for VR users by adding the launch game menu button to the different table views of VPin Mania.