## Release Notes

### Changes


- **VPS Updates**: The VPS update calculation has been re-implemented so that less updates are fired. The new differencing returns the details of the updates now too. Additionally, you can see update markers in the VPS section now.  

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/vps/update-markers.png" width="300" />

- **Table Uploads**: Added backup option so that tables are not simply overwritten but copied into a separate "Tables (Backups)" folder.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/uploads.png" width="600" />

- **Highscore Cards**: Added preview for table assets in case you decide to put your transparent highscore card above another Popper screen.
- Updated pinemhi.
- Added **vpxtool** from https://github.com/francisdb/vpxtool. This tool is used to import and export VB script from VPX files. 

### Bugfixes

**NVOffsets**: Fixed problem that pinemhi is not able to read nvram files with offset. I worked around this problem by temporary renaming the .nv files parsing them.
