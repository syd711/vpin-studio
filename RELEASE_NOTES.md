## Hotfix

### Changes

- Added link buttons to the link fields in the table data dialog.
- Added help icons for the fields "Game Name" and "Game File Name" in the table data dialog with hints about the renaming that results out of changing this fields.
- Added all possible uploads into one split-button menu. This way, the you don't have to search for the uploads buttons in the different sections anymore.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/upload-button.png" width="400" />


### Bugfixes

- Fixed missing re-styling of some link buttons that were still green instead of blue.
- Fixed possible database lock when Popper is running and Studio is started with new tables detected.
- Added missing .mp4 and .apng file formats to the list of possible drag and drop files for the assets dialog.
- Improved emulator start/exit script analysis when writing the required "curl" calls for the VPin Studio server.
- Added missing automatic ".ini" file renaming when the "Game File Name" is renamed.