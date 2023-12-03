## Maintenance Update 2.3.2

### Interaction Improvements

- Added "Open Folder" button for the screen asset list in the "Asset Manager" dialog. It comes in handy from time to time.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/am/am-open-folder.png?raw=true" width="700" />

- Added "Rename" button for the screen asset list in the "Asset Manager" dialog.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/am/am-rename-btn.png?raw=true" width="700" />

- Added drop zones for the "Asset Manager" dialog: You can drop matching media files directly on the screens there now.


<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/am/am-drop.png?raw=true" width="300" />

### Bugfixes

- Added missing 7z.dll file to the installation. Mostly 7zip is already installed on machines, but in case not, updates don't seem to work or the 7-zip installation is limited to another user? Hopefully this fixes the issue. The file will only be added for fresh installations.
- Fixed missing highscore backup on highscore reset.
- Fixed restoring highscores from "The Addam's Family". (Why only this table? Because it's the only table I know there the ROM name is stored uppercase in the script and the actual ROM file is lower case :-/ )
- Fixed "Open in System" button for the "ALT Color" sections header. The table's ALT color folder is now opened if available.
- Fixed error when loading pup packs with invalid screen values.
- Fixed VPS table selection: Because the auto-completion did only show the table name, duplicates have been filtered. The auto-completion shows the manufacturer and the year of the table, allowing the selection to be unique now (e.g. "Mustang" or "Star Trek").
- Added "PinUP Popper/VPX Running" check for ROM uploads this the process may block the file writing.
- Fixed occasional layout glitch in the table version dropdown menu of the VPS section.
- Fixed drag and drop for the "PinUP Popper Media" section: You can drop matching media files directly on the screens there now (again).

