## Release Notes 3.14.6

## Changes

- **Launcher**: Added auto-connect for remote clients so that the latest known connection is used.
- **Table Asset Manager**: Fixed dialog size calculation. Instead of trying to calculate the monitor resolution, the size of the main Studio window is used to determine which (more compact) dialog version of the asset manager dialog should be used.
- **Drop-In Menu** Limited the amount of items to 100 to avoid a client crash when a folder with thousands of entries is added.
- **Drop-In Menu** Removed the system tray notification. It was ugly anyway.
- **Drop-In Menu** Added filtering for hidden files.


---
## Release Notes 3.14.5

## Changes

- **Highscore Resolving**: Fixed highscore resetting with values for .ini highscores.
- **Performance Updates and Fixes:**
  - Enabled gzip compression for requests: Unfortunately this was not enabled yet and will improve the response times when working remote.
  - Fixed several situations where all games from all emulators were loaded (e.g. after editing assets).
  - Removed duplicated table refreshes.

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

- **Tables / Asset Manager**: Fixed reset of the server asset list on game selection change.
- **Tables / Asset Manager**: Fixed reset media players when the dialog is closed and media playback is still running.
- **Tables / Media Recorder**: Fixed flag for 60fps recording.
- **Tables / Media Recorder**: Added "move back" behaviour when the recording is started from the client on the cabinet. This should solve the issue that the Studio stays in front during the recording.
- **Tables / ALT Sound**: Fixed case sensitive issue for upper-case ROM names.
- **Webhooks**: Added missing change listener for the game lifecycle events (create, update and delete).
- **Backglass Manager / .res Editor**: Fixed saving .res files without frame images.
- **Backglass Manager / Screen offset**: Fixed the backglass screen identification when parsing screenres.txt.
- **Highscore Event Handling**: Fixed issue that if the player achieved multiple new highscores during one game, only one highscore change event was emitted. The server emits now messages for every change, starting with the highest one. This way, all new scores are posted to iScored too (or only the highest if the single score submission is enabled).
- **Future Pinball Support**: Phew, this was neglected a lot. So here comes a bunch of fixes based on table archives from **Terry Red**. I still lack some knowledge here, so feel free to drop feedback on the VPin Studio's Discord server.
  - Fixed auto-renaming for .fpt files.
  - Fixed some validations that have been skipped for .fpt files.
  - Added renaming support for BAM configuration files.
  - Added upload detection for BAM configuration files, including a separate upload dialog. When you upload **Terry Red** bundles, make sure to select only the BAM files you wish to install by using the media filter dialog (which is part of the table upload dialog).
  - Added option in the table deletion dialog for BAM configuration files.


## VPin Mania

- Reworked registration dialog and preferences page.
- Players statistics are loaded with a single request now. So you don't have to wait until all scores are loaded anymore.
- Additional statistic data is submitted to VPin Mania when the corresponding privacy settings are enabled. They are disabled by default, but you'll get a notification on the first table rating to enable these. The data is **anonymous** and will help to gather insights about the popularity of tables and their versions. 
