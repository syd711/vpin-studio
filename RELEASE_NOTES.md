## Release Notes 2.19.0

- **Table Management**: Added **Notes** editor which is available when **clicking on the status icon**. You can use some keywords to force the notes icon in different colors.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/notes.png" width="700" />

- **Table Management / Filters**: Added filter option to filter for different comment types.
- **Preferences / PinVol & Volume**: Added option to set the initial system volume when the cabinet/VPin Server is started.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/volume-control.png" width="400" />

- **Table Validators**: Added validator for outdated recordings. //TODO check date
- **Table Management / Backglasses**: Added "Delete" button.
- **Table Management / ALT Sound**: Added "Delete" button.
- **Table Management / ALT Color**: Added "Delete" button.
- **Table Management / PUP Packs**: Added "Delete" button.
- **Table Management / Validations**: Added preferences button to the validator message section to provide a quick-access for enabling/disabling them.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/validator-buttons.png" width="400" />

- **Table Management / Upload Menus:** Added new entry **Media Pack Upload** which allows to upload archives that contain media files for Popper screens. The content of the files are dispatched to the PinUP Popper screens based on their name and path inside the archive.

- **Universal Uploader**: All upload and file drag/drop operations have been re-implemented. The overall goal is to be able to upload everything everywhere. This includes:
  - Dropping/Uploading files out of archives (e.g. 7z windows) to the table overview or to table asset screen.
  - Dropping/Uploading complete bundles of files for existing tables or...
  - Dropping/Uploading complete table bundles which include not only the VPX file, but also backglass files, PUP packs and Popper media files.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/universal-upload.png" width="900" />


### Bugfixes

- **Table Overview**: Fixed suppressed errors in the VPS updates column.
