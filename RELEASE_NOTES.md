## Release Notes 3.7.1

## Bugfixes

- **Highscore Parsing**: Refactored text parsers and moved all parsing information into the **scoringdb.json** file which is updated on every server startup. This way, all text file based highscores can now be updated without a separate Studio update. This work required a LOT(!) copy'n paste, so I hope it did not break any of the previously working highscores here. 
- **Table Overview**: Fixed initial selection. Because the initial selection is done multiple times (the first row of the table), this resulted into a client crash for some users.
- **Table Overview**: Added sorting for the "Launcher" column.
- **VPin Mania**: Fixed broken table based highscore synchronization where no VPin Mania account was found.
- **VPin Studio Launcher**: Added asynchronous shutdown of broadcast thread which was looking for clients and may have caused a delayed Studio client startup.
- **VPin Studio Client**: Fixed initialization routine which prohibited the update of the VPS database when connected from remote.
- **Backglass Manager**: Fixed "Replace Media" option which did not check if a video or image is overwritten.
