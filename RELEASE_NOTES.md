## Release Notes 4.1.0

## Changes

- **Table/Competition Management**:
  - Added option to directly delete a competition if you want to delete a table that is used by one.
  - Added missing refreshes between the table overview and the different competition views.
- **Media Manager**:
  - Added support of animated PNGs!
  - The Asset Manager Dialog is now resizable.
  - Added ability to view fullscreen a media asset in the Asset Manager dialog and tab.
- **DMD Improvements**:
  - Improved parsing of script to detect usage of UltraDMD or FlexDMD and associated project folder.
  - Added validator to check the scanned DMD folder exists.
  - Refactored DMD tables sidebar to display DMD Type from script and associated DMD Folder.
  - DMDPosition tool support for UltraDMD and FlexDMD.
  - **Note that you have to rescan the table or press the reload button in the DMD section to update the DMD information**.
- **Preferences**
  - **Reorganization**: Moved system settings into the cabinet, trying to declutter the preferences navigation a bit.
  - **Backups**: 
- **Virtual Pinball Spreadsheet**:
  - **Tutorial Column**: Improved the "Tutorials" column. The column shows the available tutorials for the given table. Note that the **Kongedam** tutorials have an additional color indicator. (This indicator has a technical reason too. It ensures that all the latest and greatest from him are available in the pause menu too).
  - **Preferences**: The VPS related preferences have been moved into a separate preferences page under "3rd party preferences". **This is a breaking change since the preferences have also been reorganized in the backend. So you need to reconfigure the section if you did not use the default values here.**
  - **Update Filtering**: You can filter updates from authors you are not interested in now. The names can be added on the (new) VPS preferences page in comma separated value format (case-insensitive).
- **Table Overview**: 
  - In Table Data dialog, control the overridden ROM Name exists.
  - **ALT Color Backups**: Added dialog for backups to the ALT color section. Note that you may already have some backups available since the actual backup support for the backend was already build a year ago. The dialog allows you to restore these files now.
- **Backglass Manager**:
  - Improved refresh of backglass sidebar when multiple clicks in list
- **Media Recorder**:
  - Added new "Wizard" button that automatically selects all video screens of all tables that are missing and configured as mandatory.
- **Pause Menu**:
  - Improved icon and label for tutorials.
  - Disabled additional frontend screens when the pause menu is shown. These should not have been active anymore since the latest revamp that came with version 4.0.
  - New revamped icons! Thanks to @planetxplanetx!
- **Main Window**:
  - Added options to start and stop PinVOL directly from the preferences dropdown menu. Since PinVOL may block key bindings, you can faster work around this problem this way.
- **Uploads**:
  - Fixed and improved ROM name detection of ALT sound bundles.
- **Maintenance Mode**:
  - Switched from showing an image to an (OLED friendly) maintenance video.
- **Emulator Management**: 
  - Added missing game deletion from the database when an emulator is deleted.
  - Added missing server-side cache invalidations after emulator changes.
  - Added missing game scan for FX emulators so that these are automatically imported when the emulator is set up via Studio. **Note that you have to set the proper game folder and file extension for this.**
  - The "Import Game" dialog is now enabled for all emulators. After creating a new emulator the dialog can be used to import the related games. This was limited to FP and VPX before.

## Bugfixes

- Fixed table validation when loading media asset was missing but mandatory.
- Fixed wrong PUP pack validator that checked if the PUP pack is enabled.
- Fixed superfluous VPS updates about new table version that are not relevant for mapped tables.
- Fixed error that backglasses were not appended when they are part of a zip or rar file. 

## VPin Mania

- **Added highscore synchronization view**: When you run a complete highscore synchronization overview, including the ones that have been denied. This view will also help us in case highscores have not been found on https://app.vpin-mania.net. 
- **Fixed rating submissions**: Instead of incremental ratings they are calculated by a scheduled bulk operation. Make sure to enable the rating submissions in your privacy settings of VPin Mania. The rating on https://app.vpin-mania.net will be re-enabled soon. 