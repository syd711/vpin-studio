## Release Notes 5.7.0

### Changes

- **Localization**: Added a lot of missing translation, especially for tooltips and progress dialogs.
- **Competitions**: Removed Discord competitions and Discord subscriptions.
- **Competitions / VPin Mania Challenges**: 
  - The challenges view now loads the challenges via the server, which synchronizes them first. The view shows exactly the table that has been assigned to a challenge (and so is part of the challenge playlist), and the issues of challenges without a matching table. Opting in or out of challenges is applied to the tables immediately.
  - Only tables with the exact table version of a challenge are assigned to it, as scores of other versions do not count for the challenge. If only another version is installed, the challenges view shows that table with a warning, but it is not part of the challenge playlist.

### Bugfixes

- **Backglasses / .res File Generator**: Fixed frame images of different tables overwriting each other when they had the same filename. Uploaded frames were stored under their original filename in the shared "_Frames" folder, so every table using a frame with that name showed the frame uploaded last. Uploaded frames are now stored with the table name as prefix. Re-select the frame and regenerate the .res file of affected tables.
- **Competitions / VPin Mania Challenges**: 
  - Fixed new challenges not showing up in the challenge playlist until the server was restarted. The server only synchronized the challenges on startup, so challenges started afterwards had no table assigned. The challenges are now synchronized every 5 minutes and whenever the challenges view is opened.
  - Fixed the highscore of a challenge table being reset again when the table was re-matched to the challenge, e.g. after it had been disabled. The highscore is now only reset once per challenge.
- **VPF/VPU Login**: Fixed security issue that the password for the VPU and VPF sites were not stored/transferred encrypted. 