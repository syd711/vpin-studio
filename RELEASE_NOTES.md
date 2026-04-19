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
- **Preferences Menu**: Added natural order for hooks.

### Bugfixes

- **Media Recorder**: 
  - Fixed error during writing the media file depending on the overwrite/append selection.
  - Added missing "emulator running" check for the frontend recording which should run before the actual configured initial delay. 
  - Fixed broken "emulator running" check which only did check if the VPX.exe is running, but not if an actual game is emulated. This resulted in an early start of recordings before the game was actually running.
- **Zen Studio Tables**: 
  - Disabled auto update of missing tables. For some reason I did not figure out yet, duplicates are created for some Pinball FX users.
  - The table overview shows the file name (e.g. "Table_123") as ROM name now.
- **ALT Sounds**: Fixed upload button from the sidebar.
- **VPin MAME**: Fixed saving VPin MAME default preferences for values **Compact Display** and **Double DisplaySize**.
- **Pause Menu**: Fixed orientation for "Desktop Mode" playfield screenshots.
- **Competition Wheels**: Added deletion of the **pthumbs** folder in the wheel icons folder after competition wheel created, to force Popper to re-create the matching thumbnails variants.
- **Competition Duplications**: Change the **Duplicate** actions for competitions so that the start and end date are also duplicated, but only if the competition has not ended yet.
- **Sidebar Media Preview**: For the loading screen, the correct video is shown again if the screen contains a blank.
- **FX ALT Color Support**: Additional naming fixes when the folder name for the ALT color is determined.
- **Competitions / iScored**: Fixed broken/missing highscore reset for iScored competed tables.
- **Table Overview**: Fixed time formatting for the modification date and added info tooltip to the column header.
- **Wheel Badges**: Fixed orientation of augmented wheels (again).
- **VPX File Scanner**: Added .ogg audio format to the music scan detection.