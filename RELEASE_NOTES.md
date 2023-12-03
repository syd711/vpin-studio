## Maintenance Update 2.3.2

### Bugfixes

- Added missing 7z.dll file to the installation. Mostly 7zip is already installed on machines, but in case not, updates don't seem to work or the 7-zip installation is limited to another user? Hopefully this fixes the issue. The file will only be added for fresh installations.
- Fixed missing highscore backup on highscore reset.
- Fixed restoring highscores from "The Addam's Family". (Why only this table? Because it's the only table I know there the ROM name is stored uppercase in the script and the actual ROM file is lower case :-/ )
- Fixed "Open in System" button for the "ALT Color" sections header. The table's ALT color folder is now opened if available.
- Removed .pal/.vni validator: I always thought both files were required, but only the .pal file is used during runtime, so I removed this validation.

### Interaction Improvements

- Added "Open Folder" button for the screen asset list in the "Asset Manager" dialog. It comes in handy from time to time.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/releases/2.3.0/am-open-folder.png?raw=true" width="700" />

- Added "Rename" button for the screen asset list in the "Asset Manager" dialog.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/releases/2.3.0/am-rename-btn.png?raw=true" width="700" />
