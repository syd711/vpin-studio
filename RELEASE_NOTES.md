## Hotfix

### Changes

- Added link buttons to the link fields in the table data dialog.
- Added missing automatic .ini file renaming when the "Game File Name" is renamed.
- //TODO WinRegistry MAME settings can now be deleted together with (or without) the table.
- .ini files are now deleted together the table file.
- Added help icons for the fields "Game Name" and "Game File Name" in the table data dialog with hints about the renaming that results out of changing this fields.
- Added .ini file info to the "POV" column. (I don't know what the current status is regarding both files, but they share one column for now.) 
- Added all possible uploads into one split-button menu. This way, you don't have to search for the uploads buttons in the different sections anymore.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/upload-buttons.png" width="300" />


### Bugfixes

- Added file name validators to "Table Data" input fields "Game Name" and "File Name".
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
- Fixed erroneous VPS update indicators in the table overview for PUP packs, POV, ALT sound and ALT color (how emberassing - copy and paste isn't for beginners :-/).
- Fixed checkboxes in deletion dialog, so that the text can they are selectable through text-clicks too.
- Fixed VPX file scanning: When there is an actual "const cGameName..." line, the ROM value of this line should be used, no matter what other values have been read before. This fixes also the highscore detection of some tables, e.g. "cyberrace". 
//TODO validate  filename in popper data dialog 