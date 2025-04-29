## Release Notes 3.15

## Changes

- **iScored V2**: 
  - game lock
  - game multiscore
  - added badge

- **System Manager**: The overall update check is not blocking the UI anymore. This way, you can immediately switch to other system manager tabs now.
- **Table Data Manager**: The comments dialog has been integrated into the Table Data Manager dialog. The feature was a bit too hidden.
- **Table Data Manager**: The Table Data Manager dialog has an additional tab "Playlists" now where the game can be assigned to playlists.
- **Highscore Backups**: Added bulk-operation support for highscore backups.
- **Backglass Server**: Added option to set "Simple LEDs" as default in the backglass server preferences.
- **Media Recorder**: Added option to set the VPX parameter "-Primary" for the recording.
- **Table Asset Management** Added highscore reset button to "Scores" tab.
- **Table Asset Management** Added additional dialog for media bulk conversions. The action for this is only available in the asset mode view. Note that you can extend the given conversion options on your own (https://github.com/syd711/vpin-studio/wiki/Table-Asset-Manager#media-converter).
 
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/bulk-conversion.png?raw=true" width="400" />

---
## Release Notes 3.14.6

## Changes

- **Table Overview / Table Media Outliner**: Added "Open DMD Positioner" for Apron, DMD and Backglass screen. They are enabled when a backglass is available.
- **Launcher**: Added auto-connect for remote clients so that the latest known connection is used.
- **Launcher / Toolbar Menu**: Added direct login buttons for other cabinets to the preferences drop-down menu. The entries are only generated for remote clients and for cabinets where the connection was established before.
- **Table Asset Manager**: Fixed dialog size calculation. Instead of trying to calculate the monitor resolution, the size of the main Studio window is used to determine which (more compact) dialog version of the asset manager dialog should be used.
- **Drop-In Menu** Limited the amount of items to 100 to avoid a client crash when a folder with thousands of entries is added.
- **Drop-In Menu** Removed the system tray notification. It was ugly anyway.
- **Drop-In Menu** Added filtering for hidden files.
- **Backglass Manager**: Skipping serialization of empty XML elements instead of writing them with an empty value. (This is a blind-fix, and should make the backglass defaults work, but is not confirmed yet.)
- **Screen Recorder**: Fixed recording error when "expert mode" has been selected but the command has not been re-confirmed.
- **VB Script Editing**: Fixed error handling so that when the extraction of the VBS fails a proper error message is shown.
- **VB Script Editing**: Updated to vpxtools version v0.23.5. Once again a big thank you to @francisdb for this tool: https://github.com/francisdb/vpxtool
- **Highscore Parsing:** Added support for the table "Hot Line".
- **Highscore Matching:** Fixed highscore matching (in the players section) for players with initials smaller than 3 characters.
- **Pause Menu**: After revisiting the Pause Menu, only minor fixes have been made for this patch release. **A bigger change tackling the issues with tutorials will follow soon.**
  - Removed initial focus forcing for VPX GL emulators. This lead to stuttering, but was necessary to "win" the focus over the emulator to show the pause menu on top. (I could not reproduce this with the latest GL version anymore, but this may need to be revisited).
  - Added "Resume Delay" configuration option. This is 1 second by default. But when VPX is run in "force fullscreen" mode, it can take longer for VPX to regain the focus. An additional help tooltip has been added.


---
## Release Notes 3.14.5

## Changes

- **Highscore Resolving**: Fixed highscore resetting with values for .ini highscores.
- **Performance Updates and Fixes:**
  - Enabled gzip compression for requests: Unfortunately this was not enabled yet and will improve the response times when working remote.
  - Fixed and improved table refreshes: Fixed several situations where all games from all emulators were loaded (e.g. after editing assets).
  - Fixed and improved table refreshes: Fixed duplicated table refreshes.

---

## Release Notes 3.14.4

## Changes

- **Highscore Resolving**: Added highscore support for **Dark Chaos**.
- **Table Data Manager / VPS Integration**: Removed copy table id button, only the copy version button should be used.
- **iScored**: Added support for multi-score submissions. You have to add the tag **vps:multiscore** for all tables where this should be enabled. See also: https://github.com/syd711/vpin-studio/wiki/iScored#multi-score-submissions
- **iScored**: Improved logging for iScored competitions.

---

## Release Notes 3.14.3

## Changes

- **iScored**: Fixed critical error that skipped the highscore submission for game room games without existing highscores.
- **Tables / Drag and Drop**: Fixed UI freezing for file drops when no table was selecting.
- **PinballY**: Improved error handling for emulators where not games folder was found.
- **Pause Menu**: Fixed accidental delay when shown, introduced with the latest mute option.

---

## Release Notes 3.14.2

## Changes

- **Tables / Asset Manager**: Added another compact dialog variant used on smaller screens (<= 1024px height).
- **Tables / Media Recorder**: Instead of moving the Studio window to the back, it is minimized during recordings now.
- **Tables / Media Recorder**: The VPX emulator recording option is disabled when a non-VPX game is part of the recording session now. You need to go through the frontend for these cases.
- **Tables / Play Button**: Fixed resolving of a games emulator, so that additional menu entries can be added.
- **Backglass Manager / res generator**: Fixed positioning of Full DMD B2S that was offset when using background.
- **Backglass Manager / preferences**: To ease understanding where backglass options are stored, the backglass preferences screen now displays the location of the used B2STableSettings.xml. 
- **Pause Menu / Auto-Shutdown**: Added additional check during the automatic shutdown timer that checks if the pause menu or overlay is currently opened. In this case, the shutdown is not executed so you can pause as long as you want.
- **Pause Menu** Added option to mute the system on pause.
- **Competitions / iScores**: Fixed reading highscores from the iScored dashboard, leading to an empty list in the competitions overview.
- **Preferences / Emulators** Fixed configuration issue with the visibility of the Future Pinball emulator.
- **Default Emulator Resolving**: There are some situations, e.g. for the component manager, where just the first emulator is picked to see calculate VPX related summaries. This may not hold up when the Popper defaults have changed. For now, at least the first VPX emulator with the lowest id is picked. This is yet subject to change since may need to provide more emulator selections in these situations.
- **Remote Monitor**: The button for toggling the remote monitor screens is only shown when connected from remote now.
- **Studio Window Closing**: Fixed properly closing the Studio window when closed via shortcut and the action was cancelled.
- **System Manager / Freezy**: Fixed folder of freezy for generating summary. It was using first VPX emulator instead of the Mame installation folder.

---

## Release Notes 3.14.1

## Changes

- **Server Game Launch Events**: Fixed issue that when multiple VPX emulators have been setup with the same installation directory, the active game was not detected properly. Therefore, the pause menu did not work and no highscore change event could be fired.  
- **VPin Mania Table Rating Enablement**: Fixed possible database deadlock that froze the UI.

---

## Release Notes 3.14

## Changes

- **Tables / Launch Button**: Added options to launch VPX versions in camera mode.
- **Tables / Media Previews**: Added tooltips with filename, size and modification date to all previews.
- **Tables / Media Recorder**: Added new "expert" mode which allows to customize the ffmpeg.exe command for every screen. Please refer to the ffmpeg documentation for more details. And if someone manages to record with audio, please share the corresponding command on my Discord!
- **Competitions / iScored**: Added error message in case the game room's read API is not enabled.
- **DMD Position Tool**: Added ability to turn external DMD off in VpinMame settings and/or disable DMD in DmdDevice.ini.


## Bugfixes

- **Media Recorder**: Fixed issue that the selection was kept when the emulator selection was switched. Because of the possible emulator recording mode, only recording from one emulator type are allowed. 
- **Media Recorder**: Fixed issue that the "default" VPX emulator was used for emulator recordings instead of the actual VPX emulator selection.
- **Media Recorder**: Fixed issue existing recordings couldn't be overwritten by new ones. To avoid the file lock, the copy process for the recordings is executed after the emulator/frontend has been closed now.
- **Default Emulator Resolving**: More of a technical detail. On several occasions the first VPX emulator was used instead of providing an actual selection or using the one that belongs to the corresponding game. Especially for people running multiple VPX emulators, this may have caused issues. 


## VPin Mania
