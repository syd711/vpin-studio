## Release Notes 3.0.9

## Changes

- **VPin Mania / Table Statistics**: Added open button/open on double-click so that the table details are shown.
- **Preferences / Highscore Settings**: Added "NVRam Synchronization" button to re-download all available resetted nvram files. Usually you won't need this since new nvrams are downloaded on server startup, but this way you can update them e.g. in case the volume was adjusted too. The button for this can be found on the **Highscore Settings** page.
- **iScored**: Added "Tournament Column" info to the iScored information dialog.
- **System Manager**: Releases are now selectable via their actual name, not the tagged version anymore. Especially VPX has more meaningful names there.
- **System Manager**: Added textarea that shows the release notes of the selected release.
    
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/components/installer.png" width="760" />
- **Launcher**: Add validation to prevent multiple connections for the same server.
- **Table Overview / Highscores**: Added **Event Log**. The **Event Log** captures the most important events that are executed after a table has been exited. It tracks if and what new highscore has been created and what messages and notifications have been sent for this update. The event log is persisted for every table, but only when the table has been exited through the frontend/Popper. The button to see the event log is located in the **Highscore** section and as a context menu entry in the tables overview. I hope it will help the user to analyze why e.g. some iScored or Discord notifications are not sent.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/highscores/event-log.png" width="760" />

## Bugfixes

- **Highscore Parsing**: Fixed highscore parsing for "Doctor Who" by adding "GREATEST TIME LORD" to the default list of possible score text titles.
- **Highscore Parsing**: Fixed number format issues for French users (hopefully).
- **Launcher**: Fixed issue where multiple connections cannot be created.
- **Preferences / Notifications**: Fixed mixed up settings for "Highscore Change" and "Highscore Scan Completed" preferences.
- **VPin Mania / Player Ranks**: Fixed cache reset on manual reload. 