## Hotfix

### Changes

- Added "Open Link" buttons for the URL fields in the "Table Data" dialog.
- Added missing automatic .ini file renaming when the "Game File Name" is renamed.
- .ini files are now deleted together table files.
- Added help icons for the fields "Game Name" and "Game File Name" in the table data dialog with hints about the renaming that results out of changing this fields.
- Added "Render Tablename" checkbox in the "Layout" section of the highscore card designer. You may find the table name superfluous and skip the rendering of it.
- Added .ini file info to the "POV" column. (I don't know what the current status is regarding both files, but they share one column for now.)
- Removed the mini-preview-iframe in the overlay settings for external page and replaced it with an "Open Link" button. (The CPU consumption of the internal browser is just too ridiculous.)
- Added "Kiosk" mode for pause menu videos: The YouTube videos will play in full-screen mode on the backglass when Chrome is used.
- Added "Autoplay" flag for pause menu videos: If you are having issues playing YouTube videos, use the "Autoplay" flag so that you don't have to press "Start" for launching videos.
- Added all possible uploads into one split-button menu. This way, you don't have to search for the uploads buttons in the different sections anymore.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/upload-buttons.png" width="300" />


### Bugfixes

- Added input validators to "Table Data" input fields "Game Name" and "File Name" (Only valid symbols are allowed there).
- Added VPX file name validator for the "Table Data" dialog (previous renaming errors caused by an existing file with the same name were ignored silently before).
- Fixed missing re-styling of some link buttons that were still green instead of blue.
- Fixed possible database lock when Popper is running and Studio is started with new tables detected.
- Added missing .mp4 and .apng file formats to the list of possible drag and drop files for the assets dialog.
- Fixed table deletion: Added additional check so that when a table is deleted, the linked Popper assets are no longer deleted too, when these are shared with another table.
- Fixed table deletion: When a table is deleted, the customized competition wheel icons are deleted now too.
- Fixed VPX emulator detection: Not only the name is checked, but also the game extension field ("vpx") which is a more reliable indicator for VPX emulators.
- Fixed Table Upload dialog: "Upload and Replace" and "Upload and Clone" options are disabled when opened without a table selection.
- Fixed "Upload and Replace" and "Upload and Clone" for tables: Both features broke through the recent changes where the renaming on the "Game Name" was introduced. (I hope no one had data losses through that :/) 
- Fixed unzip console errors during Studio updates by switching from 7z archives to "real" zip files.
- Fixed error for the _chrome.exe_ call, caused by missing quotes. 
- Fixed uploads and detection of DMD packages: The file check for the DMD packages is not case-sensitive anymore (flexDMD vs. FlexDMD vs flexdmd, the Futurama problem) and also accepts "similar" folders as DMD package. E.g. folders like "cyberracedmd" is now also detected as DMD package.
- Fixed parsing of _VPReg.stg_ scores that have a decimal point (just...why???).
- Fixed broken Popper asset downloads for tables where the "Game Name" was changed.
- Fixed erroneous VPS update indicators in the table overview for PUP packs, POV, ALT sound and ALT color (how embarrassing - copy and paste isn't for beginners :-/).
- Fixed checkboxes in deletion dialog, so that the text can they are selectable through "on-text"-clicks too.
- Fixed VPX file scanning: When there is an actual "const cGameName..." line, the ROM value of this line should be used, no matter what other values have been read before. This fixes also the highscore detection of some tables, e.g. "cyberrace". 
 
### Known Issues

There are still issues with the pause menu. I am aware of these and additional fixes will come.