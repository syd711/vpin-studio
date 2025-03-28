## Release Notes 3.14

## Changes

- **Tables / Launch Button**: Added options to launch VPX versions in camera mode.
- **Tables / Media Previews**: Added tooltips with filename, size and modification date to all previews.
- **Tables / Media Recorder**: Added new "expert" mode which allows to customize the ffmpeg.exe command for every screen. Please refer to the ffmpeg documentation for more details. And if someone manages to record with audio, please share the corresponding command on my Discord!
- **Competitions / iScored**: Added error message in case the game room's read API is not enabled.


## Bugfixes

- **Tables / Asset Manager**: Fixed reset of the server asset list on game selection change.
- **Tables / Asset Manager**: Fixed reset media players when the dialog is closed and media playback is still running.
- **Tables / Media Recorder**: Fixed flag for 60fps recording.
- **Tables / Media Recorder**: Added "move back" behaviour when the recording is started from the client on the cabinet. This should solve the issue that the Studio stays in front during the recording.
- **Webhooks**: Added missing change listener for the game lifecycle events (create, update and delete).
- **Backglass Manager / .res Editor**: Fixed saving .res files without frame images.
- **Highscore Event Handling**: Fixed issue that if the player achieved multiple new highscores during one game, only one highscore change event was emitted. The server emits now messages for every change, starting with the highest one. This way, all new scores are posted to iScored too (or only the highest if the single score submission is enabled).
- **Future Pinball Support**: Phew, this was neglected a lot. So here comes a bunch of fixes based on table archives from **Terry Red**. I still lack some knowledge here, so feel free to drop some feedback on the VPin Studio's Discord server.
  - Fixed auto-renaming for .fpt files.
  - Fixed some validations that have been skipped for .fpt files.
  - Added renaming support for BAM configuration files.
  - Added upload detection for BAM configuration files, including a separate upload dialog.
  - Added option in the table deletion dialog for BAM configuration files.

## VPin Mania

- Reworked registration dialog and preferences page.
- Players statistics are loaded with a single request now. So you don't have to wait until all scores are loaded anymore.
- Additional statistic data is submitted to VPin Mania when the corresponding privacy settings are enabled. They are disabled by default, but you'll get a notification on the first table rating to enable these. The data is **anonymous** and will help to gather insights about the popularity of tables and their versions. 
