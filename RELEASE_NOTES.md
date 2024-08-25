## Release Notes 3.3.0

## Changes

- **Table Overview / Playlists**: Added color picker for local and global favorites.
- **Backglass Manager**: Added upload options via dialog and drag-and-drop:

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/backglass-manager-dnd.png" width="700" />


## Bugfixes

- **iScored**: Fixed **broken score submissions**, caused by a missing cache reset of the iScored game room by the server.
- **Table Overview / Table Uploads**: Added missing .res file support for table bundle uploads.
- **Auto-Shutdown**: Fixed resetting of auto-shutdown on key events. This reset got lost when changing to the new controller input API. (And I had such a great start on "Attack From Mars" when the shutdown warning popped up).
- **Highscores**: Fixed highscore resolving for **White Water** by adding "RIVER MASTER" to the list of highscore titles.
- **Highscores**: Fixed ROM resolving and highscore parsing for **American Most Haunted**.
- **Highscores**: Fixed highscore parsing for **Gemini**, which can now be differed from **Gemini 2000** which is not supported.
- **Highscores**: Fixed highscore parsing for **Punchy the Clown**.
- **Table Overview / Validators**: Fixed "Controller.stop" validator.
- **Table Repository**: Fixed Cancel button not cancelling progress when uploading ZIP file.