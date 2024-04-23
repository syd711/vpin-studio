## Release Notes 2.18.0

### Changes

- **Clients for Linux and OSX (ARM)**: New zip files are available now in the release artifacts. Make sure to read the **installation-notes.txt** inside the zip files. I managed to modify the updater for the systems too. Note that I can only offer limited support regarding OSX! 
- **Overlay & Card Settings:** Added **Input Debounce** configuration that should avoid possible duplicated input events (experimental).
- **Highscore Cards**: Added **Render Positions** flag available for non-raw highscore templates. The padding value will be used as additional separator between the initials and the score value.
- **Preferences**: Added **Help & Support** section, including a download option which zips and downloads all log files for **ticket submissions**.
- **Table Data Manager**: The Popper field "Notes" remains untouched from the auto-fill function now.
- **Script Editing**: User your favorite development IDE for editing scripts! You can edit VB script now with any editor that is linked to the file suffix **.vbs**. Select the default program for .vbs files on your operating system and Studio will open and save(!) the changed VB sources automatically back to the remote VPX file.
- **VPS Overview**: Added list of all available table versions, including an indicator of your installed version.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/vps/vps-marker.png" width="600" />

- **VPS Mapping**: Added reset button in the Table Data Manager and the VPS table section to unlink a VPS mapping from a table.


### Bugfixes

- **Table Overview**: Fixed context menu item "Upload Backglass".
- **Color Picker**: Disabled **custom** color picking, since the dialog seems to be broken.
- **Table Imports**: Fixed miscellaneous import fields by setting them to an empty value instead of _NULL_. Leaving some fields on _NULL_ when importing a new table has led to different behaviour for some playlist in the Studio compared to Popper. (Thanks again here to @spaceangel6998 for the detailled report).
- **Studio Window**: Added (fixed) Studio version in the window title.