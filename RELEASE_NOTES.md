## Release Notes 2.20.0

- **Universal Uploader**: All upload and file drag/drop operations have been re-implemented. The overall goal is to be able to upload everything everywhere. This includes:
  - Dropping/Uploading files out of archives (e.g. 7z windows) to the table overview or to table asset screen.
  - Dropping/Uploading complete bundles of files for existing tables or...
  - Dropping/Uploading complete table bundles which include not only the VPX file, but also backglass files, PUP packs and Popper media, etc.
  - Added Uploading into subfolders.
  - **RAR files are not supported, but you can open and drop contents from the 7z archive window!**

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/universal-upload.png" width="900" />

- **Table Management / Table Uploads**: Re-design of the table upload dialog with additional features.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/uploads.png" width="900" />

- **Table Management / Uploads**: Added upload options for **.cfg and .nv** files.
- **Table Management / Uploads**: Re-design of some upload dialogs, to provide more information what files have been found and for what table.
- **Table Management / Uploads**: Added new entry **Media Pack Upload** which allows to upload archives that contain media files for Popper screens. The content of the files are dispatched to the PinUP Popper screens based on their name and path inside the archive. 

- **Table Management**: Added **Notes** editor which is available when **clicking on the status icon**. You can use some keywords to force the notes icon in different colors.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/notes.png" width="700" />

- **Table Management / Filters**: Added filter option to filter for different comment types.
- **Preferences / PinVol & Volume**: Added option to set the initial system volume when the cabinet/VPin Server is started.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/volume-control.png" width="400" />

- **Table Validators**: Added validator for outdated recordings.
- **Table Management / Backglasses**: Added "Delete" button.
- **Table Management / ALT Sound**: Added "Delete" button.
- **Table Management / ALT Color**: Added "Delete" button.
- **Table Management / PUP Packs**: Added "Delete" button.
- **Table Management / Validations**: Added preferences button to the validator message section to provide a quick-access for enabling/disabling them.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/validator-buttons.png" width="300" />

- **Backglass Manager**: @leprinco hugely improved the **Backglass Manager** by adding additional columns that show the **FullDMD** and **Grill** information. See for yourself:

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/backglass-manager.png" width="900" />

- **Validation**: Replaced magnifying glass icon of validator actions with a "check" icon.


### Bugfixes

- **Table Overview**: Fixed suppressed errors in the VPS updates column.
- **ALt Color Validator**: .vni files are not mandatory when a .pal is present.
- **Uploads**: Fixed several issues which provided temp files from being deleted (and polluting the system's temp folder).
- **Removed fuzzy version matching**: The version set for the POPPER field **GAMEVER** must match with the VPS version exactly now. The previous version comparison allowed some fuzzy matching.
- **Keep existing VPX filenames**: Fixed issue that for the **Keep Existing VPX filenames** flag was ignored for uploaded zip files.
- **Table Count Label**: Fixed table count label of the table overview so that it always shows the amount of selected tables.
- **NVOffset Validator**: Improved validator message.
- **Pause Menu**: Fixed missing reset of media player, causing continuously playing the video sound in the background.