## Release Notes

### Breaking Change

Since the preferences are growing, I had to change the data format to be more flexible in the future.
As a result, your existing **"UI Settings" and "Server Settings"** in the preferences view have been resetted.
**Please revisit your settings there!**

### Changes

- Table Overview: Changed hover states so that the font remains white (not blue anymore).
- Studio Window: The size and location of the Studio window is now persisted and restored on the next launch.
- File Dialogs: The last used folder location is now stored and used as initial directory for every new file selection dialog.
- Upload Dialogs: For uploads that doesn't require any additional values, the file selection now opens immediately, e.g. Popper table assets and directb2s files.  
- Added "Reload" button to MAME section, to invalidate the MAME settings cache.
- Improved server start-up time a bit.
- Re-worked the preferences for the highscore card pop-up:
  - Renamed this feature from "notification" to "pop-up".
  - Added the option to use a PinUP player screen configuration for the pop-up positioning, instead of just displaying it on top of the playfield.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/highscores/card-popup.png" width="700" />


### Bugfixes

- Added ".apng" as valid file suffix for wheel drag and drops.
- Fixed highscore card overlays frames: The larger upper and lower white areas of the pop-up should be gone now. This requires that all highscore cards are rendered using the size of the default assets (1280x720). If you have used one of the default backgrounds download a properly rescaled one from https://github.com/syd711/vpin-studio/tree/main/resources/backgrounds and place it in the same directory of your installation. 
- Overlay: Fixed issue that when the overlay is shown on startup, it does not hide on key-press when no hotkey is defined.
- MAME Settings: Fixed issue that the MAME cached was not updated on table scan (and no additional reload button available).