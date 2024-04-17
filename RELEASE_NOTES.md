## Hotfix 2.16.3

- **Installer**: Fixed another critical error caused by new "Force Stereo" validator.
- 


## Hotfix 2.16.2

- **Validators**: Fixed several issues with the "Force Stereo" validator.
- **Popper Custom Options**: Added missing "useAltWheels" option.
- **System Manager**: Fixed error when analyzing serum release artifacts.

## Hotfix 2.16.1

- **PINemHi**: Added auto-updater for PINemHi files that is independent from Studio updates.
- **VPin MAME**: Added "Force Stereo" option which is relevant for 7.1 sound system users.
- **VPX Validator**: Added "Force Stereo" validator which is relevant for 7.1 sound system users.
- **Preferences: Validators**: The validators are sorted alphabetically now.
- **Updater**: Added missing status label for the remote client update progress bar.
- **Asset Manager**: Fixed layout issues happening for playfield and loading previews for smaller screen resolutions.
- **UI Preferences**: Fixed refresh issue when new avatar image has been uploaded.
- **Table Data Manager**: Fixed missing renaming of VPX file that may leads to the error, that the VPX file is not found anymore.
- **Table Uploads**: Upload and replace no longer applies the archive filename as target name for the VPX file.

## Release Notes 2.16.0

### Changes

- **Preferences: UI Settings**: Converted the negative "Hide" description and checkboxes to positive "Show" descriptions.
- **Preferences: Server Settings**: Added option to restart the VPin Studio Server.
- **VPS Updates**: The VPS update calculation has been **re-implemented** so that less updates are fired. The new differencing returns the details of the updates now too. Additionally, you can see update markers in the VPS section now.
  
<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/vps/update-markers.png" width="500" />

- **VPS Updates**: Added settings so that you can choose which update notifications to check.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/ui-options.png" width="600" />

- **Table Backups**: Added backup option so that tables are not simply overwritten but copied into a separate "Tables (Backups)" folder.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/uploads.png" width="600" />

- **Highscore Cards**: Added preview for table assets in case you decide to put your transparent highscore card above another Popper screen.
- Updated **pinemhi** (available only for new installations).
- Added **vpxtool** from https://github.com/francisdb/vpxtool. This tool is used to import and export VB script from VPX files.
- **VPX Script ~~Viewer~~ Editor(!)**: Finally, thanks to the **vpxtool** you now can not only view the VB script of a table, but also edit it and save it back to the VPX file. The editor for this **does not offer much**, but it will allow you to do some quick table adjustments.
  
<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/script-viewer.png" width="600" />

- **VPin Studio Installer**: Added registry option to auto-start the server. The installation will no longer ask for the auto-start folder but will use the registry instead. For existing users: you can keep the existing auto-start as is.
- **VPin Studio Updates**: Added additional remote update for clients. When you update the installation from remote, the client installed on your cabinet will be updated to.
- **Table Data Manager**: Changed label from "Game Display Name" to "Game Screen Name" (same like in Popper) and added auto-renmaing flag, so that the screen name is updated of VP-spreadsheet table selection. 
- **Table Uploads**: The uploaded table is now selected after the upload is finished. The table data manager is opened then too. You can disable this behaviour in the settings.
- **Table Overview Context Menu**: Improved table overview's table context menu.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/context-menu.png" width="450" />


### Bugfixes

- **Preferences: Validators**: The validators are sorted alphabetically now.
- **Updater**: Added missing status label for the remote client update progress bar. 
- **Asset Manager**: Fixed layout issues happening for playfield and loading previews for smaller screen resolutions.
- **UI Preferences**: Fixed refresh issue when new avatar image has been uploaded.
- **Table Data Manager**: Fixed missing renaming of VPX file that may leads to the error, that the VPX file is not found anymore. 
- **Table Uploads**: Upload and replace no longer applies the archive filename as target name for the VPX file.  
