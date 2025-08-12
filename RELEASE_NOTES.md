## Release Notes 4.2.0

## Changes
- **Table Backups**
  - The Studio supports creating and restoring table backups now. **Note that this feature is solely for private use only!!!** For this reason, the backups are password protected. To enable the feature, you need to configure your VPU or VPF account for authentication. Check out the YouTube channel video for more details.
- **Asset Sources**
  - The Studio allows to configure additional asset sources now. These will be visible, configurable and selectable during the table asset selection. In addition to this new configuration option, the Studio comes with new pre-defined asset sources. These are:
    - Kongedam Video Tutorials (you can now download these directly for one of your frontend screens)
    - Superhac's VPX Media Database (https://github.com/superhac/vpinmediadb)
- **Tables Overview**:
  - **Added optional column "Tutorials"** also to the regular table overview. This way, users can easier see if there will be a tutorial available in the pause menu.
  - **Improved ROM column**: The tooltip of a ROM displays additional alias information now. In addition to that, you can use the table search field to filter games by their ROM name or alias.
  - **Improved VPin MAME section**: In addition to the alias mapping, the section shows also the tables which share the same ROM.
  - **Responsive Toolbar**: Since a lot of users use the Studio with a full HD monitor a second toolbar row has been added so that all toolbar actions are visible all time.
  - **VPS Updates Indicators**: You can disable VPS updates for a table now. The checkbox for this can be found in the sidebar VP spreadsheet section. Note that updates are still recorded for the table, but not visualized anymore.
- **Table Imports**
  - The table import dialog has been improved. The file size is now shown too. Also, you can directly delete .vpx of .fp files so get rid of file corpses.
- **Table Deletions**:
  - The last selection of the deletion dialog is remembered now.
- **Pause Menu**
  - Added pause menu support for stand-alone mode of VPX.
- **Monitoring View / Screenshots**
  - Added a summary screenshot to the screenshot bundle which contains all screens as one image, including a timestamp.
- **Drop-In Folder**
  - Added option to move the uploaded file to the trash bin.
- **Image Previews**
  - All image preview action button have been reworked and using the build in image preview dialog now. 

## Bugfixes

- Fixed maintenance video, so it is now always displayed on the primary screen.
- Fixed modal mode toggling of the asset manager dialog.
- Fixed MAME settings status for some tables (e.g. Addams Family) caused by upper case ROM names.
- Fixed "Missing Backglass or PUP Pack" validator which sometimes raised a false positive when the PUP pack detection wasn't finished.
- Fixed ALT sound upload so that a game selection is mandatory now. This enforces the correct ROM name to be used from the game instead of guessing it from the uploading file.

