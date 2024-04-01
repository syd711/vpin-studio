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

 
- Added **vpxtool** from https://github.com/francisdb/vpxtool. This tool is used to import and export VB script from VPX files. Finally allows **remote editing of VPX files** which was a long existing feature request.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/uploads.png" width="600" />

- **Server Settings**: Added option to restart the VPin Studio Server.
- Updated to a newer version of **pinemhi** (for fresh installs only).
- **VPin Studio Installer**: Added registry option to auto-start the server. The installation will no longer ask for the auto-start folder but will use the registry instead. For existing users: you can keep the existing auto-start as is. 

### Bugfixes

**NVOffsets**: Fixed problem that **pinemhi** is not able to read nvram files with offset. I worked around this problem by temporary renaming the .nv files parsing them.
**UI Settings**: Fixed missing refresh of after changing VPS column visibility changes.
**VPX Commands**: Fixed VPX operation for installations having a whitespace in the directory path.