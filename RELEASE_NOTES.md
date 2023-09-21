## Release Notes

### Changes

- Added green "open folder" button for highscore section which will open the nvrams folder.
- Added resetted nvram support: for online competitions based on nvrams, a resetted nvram may replace the existing one so that all highscores of the player are tracked and no default score must be beaten.
- Added automatic resetted nvram synchronization with https://github.com/syd711/nvrams. The server will synchronize the existing nvrams with the server, so that new ones are downloaded automatically. Everytime a highscore reset is executed, the server will try to use the resetted nvram if available.
- The channel topic for Discord competitions is automatically set when a competition is created and contains the table name.
- The channel topic for Discord competitions is automatically set when a competition is finished and contains the info that there is active no competition.

### Bugfixes

- Fixed remaining issues with case-sensitivity of ROM names, e.g. "Addam's Family".
- Fixed parsing issues with resetted nvrams.


