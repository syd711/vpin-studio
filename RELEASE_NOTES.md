## Release Notes

### Changes

- Added green "open folder" button for highscore section which will open the nvrams folder.
- Added resetted nvram support: for online competitions which use tables with nvram highscores, a resetted nvram can replace the existing one so that all highscores of the player are set to "0" so that every change is tracked and no default score must be beaten.
- Added automatic resetted nvram synchronization with https://github.com/syd711/nvrams. The server will synchronize the existing nvrams with this repository, so that new ones are downloaded automatically. Everytime a highscore reset is executed, the server will try to use the resetted nvram if available. The repository is public and everyone can download the existing resetted nvram files.
- The channel topic for Discord competitions is automatically set when a competition is created and contains the table name.
- The channel topic for Discord competitions is automatically set when a competition is finished and contains the info that there is no active competition.

### Bugfixes

- Fixed remaining issues with case-sensitivity of ROM names, e.g. "Addam's Family".
- Fixed parsing issues with resetted nvrams.


