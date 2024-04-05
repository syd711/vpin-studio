## Release Notes

### Changes


- **UI Settings**: Converted the negative "Hide" description and checkboxes to positive "Show" descriptions.
- **VPS Updates**: The VPS update calculation has been re-implemented so that less updates are fired. The new differencing returns the details of the updates now too. Additionally, you can see update markers in the VPS section now.
  
<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/vps/update-markers.png" width="300" />

- **VPS Updates**: Added settings so that you can choose which update notifications to check.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/ui-settings.png" width="500" />

- **Table Uploads**: Added backup option so that tables are not simply overwritten but copied into a separate "Tables (Backups)" folder.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/uploads.png" width="600" />

- **Highscore Cards**: Added preview for table assets in case you decide to put your transparent highscore card above another Popper screen.
- Updated **pinemhi** (available only for new installations).
- Added **vpxtool** from https://github.com/francisdb/vpxtool. This tool is used to import and export VB script from VPX files.
- **VPX Script Editor(!)**: Finally, thanks to the **vpxtool** you now can not only view the VB script of a table, but also edit it and save it back to the VPX file. The editor for this does not offer much, but it will allow you to do some quick table adjustments.
  
<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/uploads.png" width="600" />

- **Server Settings**: Added option to restart the VPin Studio Server.
- **VPin Studio Installer**: Added registry option to auto-start the server. The installation will no longer ask for the auto-start folder but will use the registry instead. For existing users: you can keep the existing auto-start as is.
- **VPin Studio Updates**: Added additional remote update for clients. When you update the installation from remote, the client installed on your cabinet will be updated to.

### Bugfixes

- **NVOffsets**: Fixed problem that **pinemhi** is not able to read nvram files with offset. I worked around this problem by temporary renaming the .nv files before parsing them.
- **UI Settings**: Fixed missing refresh after changing VPS visiblity preferences.
- **Table Statistics**: Fixed issue where tiles were not filled because of too large values.
- **Table Cloning**: Added missing cloning of .ini files.
- **VPX Commands**: Fixed VPX operations (start/stop/launch to edit) for Visual Pinball installations having a whitespace in the directory path.
- **Overlay Key Events**: Added button debounce that fixes possible duplicated key events sent by input controllers.