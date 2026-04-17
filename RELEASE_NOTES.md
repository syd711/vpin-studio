## Release Notes 4.9.0

- **Media Recorder**:
  - Updated ffmpeg.exe to version 8.0.1 to have OpenGL recording support.
  - Added error logging in case the custom ffmpeg.exe command fails.
  - Added custom GL mode which is **only used when you select "Use Custom Launcher" and VPX GL.** This mode will work with different parameters to allow smooth recordings with OpenGL. Note that this is still in an experimental mode, because the correct recording location for screens needs to verified. But you can still use the regular/non-GL recording.

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/recorder/recorder-custom.png?raw=true" width="700" />

### Bugfixes

- **Media Recorder**: Fixed error during writing the media file depending on the overwrite/append selection.
- **Zen Studio Tables**: Disabled auto update of missing tables. For some reason I did not figure out yet, duplicates are created for some Pinball FX users.
- **ALT Sounds**: Fixed upload button from the sidebar.
- **VPin MAME**: Fixed saving VPin MAME default preferences for values **Compact Display** and **Double DisplaySize**.
- **Pause Menu**: Fixed orientation for "Desktop Mode" playfield screenshots.
- **Competition Wheels**: Added deletion of the **pthumbs** folder in the wheel icons folder after competition wheel created, to force Popper to re-create the matching thumbnails variants.
- **Competition Duplications**: Change the **Duplicate** actions for competitions so that the start and end date are also duplicated, but only if the competition has not ended yet.
- **Sidebar Media Preview**: For the loading screen, the correct video is shown again if the screen contains a blank.
- **FX ALT Color Support**: Additional naming fixes when the folder name for the ALT color is determined.
