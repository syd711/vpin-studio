## Release Notes 4.9.0

- **Media Recorder**:
  - Updated ffmpeg.exe to version 8.0.1 to have OpenGL recording support.
  - Added error logging in case the custom ffmpeg.exe command fails.
  - Added custom GL mode which is **only used when you select "Use Custom Launcher" and VPX GL.** This mode will work with different parameters to allow smooth recordings with OpenGL. Note that **this mode still in experimental**, because the correct recording locations for screens needs verification. But you can still use the regular/non-GL recording.

    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/recorder/recorder-custom.png?raw=true" width="500" />
    
- **NVRam Highscores Parsing**: The nvram parsing has been extracted into a separate project: https://github.com/syd711/java-pinmame-nvmaps. The goal here is to provide a facade for the different nvram parsing approaches and support parsing with
  - the Pinball Memory Maps project (https://github.com/tomlogic/pinmame-nvram-maps)
  - Superhac's Score Parser (https://github.com/superhac/pinmame-score-parser)
  - and https://www.pinemhi.com/ from DNA Disturber.

### Bugfixes

- **Media Recorder**:
    - Fixed an error that occurred while writing the media file depending on the overwrite/append selection.
    - Added a missing "emulator running" check for frontend recording, which should run before the configured initial delay.
    - Fixed a broken "emulator running" check that only checked whether VPX.exe is running, but not whether an actual game was being emulated. This caused recordings to start before the game was actually running.
- **Zen Studio Tables**:
    - Disabled auto-update of missing tables. For a reason not yet identified, duplicates were being created for some Pinball FX users.
    - The table overview now shows the file name (e.g. "Table_123") as the ROM name.
- **NVOffset Validation**: The NVOffset validator has been updated to trigger for every table that shares its ROM with another table **and has a different VPS table ID (NOT VPS table version)**. For example, the validator is not triggered if you have 5 different versions of "Attack From Mars" all using the same ROM, only if a different table is also using that ROM.
- **ALT Sounds**: Fixed the upload button in the sidebar.
- **VPin MAME**: Fixed saving of VPin MAME default preferences for the **Compact Display** and **Double DisplaySize** values.
- **Pause Menu**: Fixed the orientation of "Desktop Mode" playfield screenshots.
- **Competition Wheels**: Added deletion of the **pthumbs** folder within the wheel icons folder after a competition wheel is created, to force Popper to regenerate the matching thumbnail variants.
- **Competition Duplications**: Updated the **Duplicate** action for competitions so that the start and end dates are also duplicated, but only if the competition has not yet ended.
- **Sidebar Media Preview**: For the loading screen, the correct video is now shown again when the screen name contains a space.
- **FX ALT Color Support**: Additional naming fixes when determining the folder name for ALT color.
- **Competitions / iScored**: Fixed broken/missing highscore reset for tables in iScored competitions.
- **Table Overview**:
    - Fixed time formatting for the modification date and added an info tooltip to the column header.
    - Improved table overview loading performance by ~30%.
- **Wheel Badges**: Fixed orientation of augmented wheels (again).
- **VPX File Scanner**: Added .ogg audio format to the music scan detection.
- **Backglass Preview**: Fixed backglass preview when the .directb2 file is located in a table subfolder.
- **Future Pinball**: Fixed import of FP tables, which was accidentally disabled.
- **DOFLinx**: Fixed the auto-installer, which now also checks .exe files to determine if the latest version on GitHub is newer than the installed one.
- **iScored**: Fixed game room deletion. When a game room is deleted, all associated competitions, their wheels, and TourneyIds are now reset.
- **Emulators Setup**: 
    - Fixed emulator tabs for pinballX
    - Fixed creation of Emulator for pinballX
- **Preferences Menu**: Added natural order for hooks.
- **Table Statistics**: Fixed "Last Played" timestamps.
- **Misc**: Added a cleanup check to the server that removes orphaned game entries which exist only in the Studio and not in the frontend (if available). This handles the case where a game was removed using the original frontend admin tool.
