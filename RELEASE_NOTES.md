## Release Notes 5.7.0

### Changes

- **World of Virtual Pinball**: Added the context menu actions **Add WoVP Telemetry…** and **Remove WoVP Telemetry…** to the table overview. The telemetry is required for WoVP challenges: it adds two timers and a logging script to the selected VPX tables without affecting gameplay. Optionally, a backup of the original table (.vpx.bak) is created. The actions are only shown when the WoVP integration is enabled.
- **Move/Clone Table…**: Added support for the VPX 10.8.1 file layout, based on the detected version of the target emulator. Tables moved to VPX 10.8.1 get their own folder with all table files except the frontend media (ROM, NVRAM, highscores, ALT sound/color, PUP pack, music). Tables moved to VPX 10.8.0 or older have these files placed into the shared VPinMAME folders. Frontend media stays in the PinUP Popper media folders, as Popper does not support the new structure yet. See [VPX 10.8.1 Migration](https://github.com/syd711/vpin-studio/wiki/VPX-10.8.1-Migration).
- **Media Bulk Importer**: Added the fullscreen loading video check of the Asset Manager. When PinUP Popper is used and an imported landscape video is the only loading video of a table, the importer asks once after the import whether a blank video should be added for these tables. 
- **Localization**: Added a lot of missing translation, especially for tooltips and progress dialogs.
- **Competitions**: Removed Discord competitions and Discord subscriptions.

### Bugfixes

- **Competitions / VPin Mania Challenges**:
    - The challenges view now loads the challenges via the server, which synchronizes them first. The view shows exactly the table that has been assigned to a challenge (and so is part of the challenge playlist), and the issues of challenges without a matching table. Opting in or out of challenges is applied to the tables immediately.
    - Only tables with the exact table version of a challenge are assigned to it, as scores of other versions do not count for the challenge. If only another version is installed, the challenges view shows that table with a warning, but it is not part of the challenge playlist.
- **Backglasses / .res File Generator**: Fixed frame images of different tables overwriting each other when they had the same filename. Uploaded frames were stored under their original filename in the shared "_Frames" folder, so every table using a frame with that name showed the frame uploaded last. Uploaded frames are now stored with the table name as prefix. Re-select the frame and regenerate the .res file of affected tables.
- **Competitions / VPin Mania Challenges**: 
  - Fixed new challenges not showing up in the challenge playlist until the server was restarted. The server only synchronized the challenges on startup, so challenges started afterwards had no table assigned. The challenges are now synchronized every 5 minutes and whenever the challenges view is opened.
  - Fixed the highscore of a challenge table being reset again when the table was re-matched to the challenge, e.g. after it had been disabled. The highscore is now only reset once per challenge.
- **Competitions / WoVP Challenges**: Fixed WoVP challenges not being synchronized completely. When a challenge matched a table whose VPX file did not exist anymore, the synchronization stopped and all following challenges were skipped. Tables with missing VPX files are now ignored for the matching, and a failing challenge no longer stops the synchronization of the others.
- **VPF/VPU Login**: Fixed security issue that the password for the VPU and VPF sites were not stored/transferred encrypted. 