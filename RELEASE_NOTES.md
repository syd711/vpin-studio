## Maintenance Update 2.3.2

### Interaction Improvements

- Improved design of table selection in the table overview. This way, it should be easier to find the selected table. 
- Added "zip" file support for VPX uploads: If you downloaded zipped VPX files, you don't have to extract the file before uploading it anymore.
- Added "Open Folder" button for the screen asset list in the "Asset Manager" dialog. It comes in handy from time to time.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/am/am-open-folder.png?raw=true" width="700" />

- Added "Rename" button for the screen asset list in the "Asset Manager" dialog.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/am/am-rename-btn.png?raw=true" width="700" />

- Added drop zones for the "Asset Manager" dialog: You can drop matching media files directly on the screens there now.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/am/am-drop.png?raw=true" width="300" />

- Added "Dismiss All" button for table validations.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/validation-error.png?raw=true" width="700" />

- Added "Troubleshooting" text for the highscore section.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/highscores/troubleshooting.png?raw=true" width="600" />


### Bugfixes

Phew, here it comes...

- Added missing 7z.dll file to the installation. Mostly 7zip is already installed on machines, but in case not, updates don't seem to work or the 7-zip installation is limited to another user? Hopefully this fixes the issue. The file will only be added for fresh installations.
- Fixed missing highscore backup on highscore reset.
- Fixed restoring highscores from "The Addam's Family". (Why only this table? Because it's the only table I know there the ROM name is stored uppercase in the script and the actual ROM file is lower case :-/ )
- Fixed "Open in System" button for the "ALT Color" sections header. The table's ALT color folder is now opened if available.
- Fixed error when loading pup packs with invalid screen values.
- Fixed VPS table selection: Because the auto-completion did only show the table name, duplicates have been filtered. The auto-completion shows the manufacturer and the year of the table, allowing the selection to be unique now (e.g. "Mustang" or "Star Trek").
- Added "PinUP Popper/VPX Running" check for ROM uploads this the process may block the file writing.
- Fixed occasional layout glitch in the table version dropdown menu of the VPS section.
- Fixed drag and drop for the "PinUP Popper Media" section: You can drop matching media files directly on the screens there now (again).
- Removed Serum ALT color validator: This is part of freezy now, so the validator is obsolete.
- Fixed error when deleting tables which may have led to remaining data in Popper.
- The "Option" dropdown in the "Pup Pack" section shows all available .bat files of the pup pack now.
- Improved error message when ALT sound is not checked in VPin MAME.
- Fixed the layout of the update news dialog a bit ... as you can see.

