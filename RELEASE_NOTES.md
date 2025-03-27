## Release Notes 3.14

## Changes

- **Tables / Media Previews**: Added tooltips with filename, size and modification date to all previews.
- **Tables / Asset Manager**: Fixed reset of the server asset list on game selection change.
- **Tables / Asset Manager**: Fixed reset media players when the dialog is closed and media playback is still running.
- **Tables / Media Recorder**: Fixed flag for 60fps recording.
- **Tables / Media Recorder**: Added new "expert" mode which allows to customize the ffmpeg.exe command for every screen. Please refer to the ffmpeg documentation for more details. And if someone manages to record with audio, please share the corresponding command on my Discord!
- **Competitions / iScored**: Added error message in case the game room's read API is not enabled.
- **Webhooks**: Added missing change listener for the game lifecycle events (create, update and delete).
- **Backglass Manager / .res Editor**: Fixed saving .res files without frame images.
- **Highscores**: Fixed issue that if the player achieved multiple new highscores during one game, only one highscore change event was emitted. The server emits now messages for every change, starting with the highest one. This way, all new scores are posted to iScored too (or only the highest if the single score submission is enabled). 


## VPin Mania

