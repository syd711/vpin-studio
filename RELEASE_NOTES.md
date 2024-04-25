## Release Notes 2.18.0

### Changes

- **Clients for Linux and OSX (ARM)**: New zip files are available now in the release artifacts. Make sure to read the **installation-notes.txt** inside the zip files. I managed to modify the updater for the systems too. Note that I can only offer limited support regarding OSX! 
- **Overlay & Card Settings:** Added **Input Debounce** configuration that should avoid possible duplicated input events (experimental).
- **Preferences**: Added **Help & Support** section, including a download option which zips and downloads all log files for **ticket submissions**.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/help.png" width="500" />
  
- **Preferences**: Added **Network Settings** to the UI preferences. You can input the **network share** of your VPin there. After restarting the Studio, you can access the remote folders via the yellow-ish folder buttons that have only been available when working on the cabinet before.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/network-share.png" width="500" />

- **Table Data Manager**: The Popper field "Notes" remains untouched from the auto-fill function now.
- **Script Editing**: Use your favorite development IDE for editing scripts! You can edit VB script now with any editor that is linked to the file suffix **.vbs**. Select the default program for .vbs files on your operating system and Studio will open and save(!) the changed VB sources automatically back to the remote VPX file.
- **VPS Table Overview**: Added list of all available table versions, including an indicator of your installed version.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/vps/vps-marker.png" width="600" />

- **VPS Mapping**: Added reset button in the Table Data Manager and the VPS table section to unlink a VPS mapping from a table.
- **Highscore Cards**: Added **Background Canvas** option to the template designer. This additional background square can be used to improve the readability of the scores. 
- **Highscore Cards**: Added **Margins** option to the template designer. The padding value will be used as additional separator between the initials and the score value.
- **Point of View Section**: Removed **Export** button. I think it's time to let go here.
- **Pause Menu**: After taking some turns here I once again changed the Pause Menu preferences. You "simply" choose the design and the target screen for the tutorial videos. This way, you can decide on your own, where the YouTube videos should be played. Using the backglass here as default didn't work out so well, since it wasn't always possible to bring the Chrome browser in front of the current PUP video. This may not be the end of the road, but I hope it works at least better than before.
- **Backglass Manager**: Added download button for the backglass and DMD image.

### Bugfixes

- **Key Events**: Fixed issue with empty overlay key assignment which led to flood of log entries.
- **Table Overview**: Fixed context menu item "Upload Backglass".
- **Color Picker**: Disabled **custom** color picking, since the dialog seems to be broken.
- **Studio Window**: Added (fixed) Studio version in the window title.
- **PUP Pack Column**: Fixed wrong PUP pack indicator. The indicator has been shown for table that declare a **pGameName** value, but it has not been checked if the PUP pack actually exist.
- **Table Data Manager**: Removed duplicate entries from the "Alternative Launcher" combo-box. The list is now solely filled with the available .exe files of the default VPX emulator.