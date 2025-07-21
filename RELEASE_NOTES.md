## Release Notes 4.1.2

## Changes

- **File Encoding for PinballY**
  - Unlike PinballX, PinballY does not handle UTF-8 encoded XML files very well and requires the system default encoding, e.g. "Windows-1252" for these files. PinballY users have to set this value manually in the newly introduced settings page for **PinballY**. Once set, special characters are shown correctly for the Studio and the PinballY frontend.

## Release Notes 4.1.1

## Changes

- **Table Overview**
  - Added missing cache invalidation after validator settings have been changed in the preferences. This invalidation is also triggered on a manual table overview reload now, so this might take a bit longer than before.
  - Fixed miscellaneous issues with the media preview dialog. The dialog is also resizeable now.
- **Media Recorder**
  - The maintenance mode is turned off automatically now when the recording is started.


## Release Notes 4.1.0

## Changes

- **Table/Competition Management**:
  - Added option to directly delete a competition if you want to delete a table that is used by one.
  - Added missing refreshes between the table overview and the different competition views.
- **Media Manager**:
  - **Added support of animated PNGs!**
  - The Asset Manager Dialog is now resizable.
  - Added ability to view fullscreen a media asset in the Asset Manager dialog and tab.

    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/release-notes/preview.png" width="600" />
  
- **DMD Improvements**:
  - Improved parsing of script to detect usage of UltraDMD or FlexDMD and associated project folder.
  - Added validator to check the scanned DMD folder exists.
  - Refactored DMD tables sidebar to display DMD Type from script and associated DMD Folder.
  - DMDPosition tool support for UltraDMD and FlexDMD.
  - **Note that you have to rescan the table or press the reload button in the DMD section to update the DMD information**.
- **Preferences**
  - **Reorganization**: Moved **System Settings** into the **Cabinet** section, trying to declutter the preferences navigation a bit.
  - **Studio Backups**: The backups have been re-implemented. Instead of a simple database copy, all preferences are exported as a json file. This file can be imported to any other client or cabinet, even if different tables are installed. It contains the settings of all 3rd party systems and the additional table information that are only available for the VPin Studio.

    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/release-notes/preferences.png" width="700" />
  
- **Virtual Pinball Spreadsheet**:
  - **Preferences**: The VPS related preferences have been moved into a separate preferences page under "3rd party preferences". **This is a breaking change since the preferences have also been reorganized in the backend. So you need to reconfigure the section if you did not use the default values here.**
  - **Update Filtering**: You can filter updates from authors you are not interested in now. The names can be added on the (new) VPS preferences page in comma separated value format (case-insensitive).
  - **Tutorial Column**: Improved the "Tutorials" column. The column shows the available tutorials for the given table. Note that the **Kongedam** tutorials have an additional color indicator. (This indicator has a technical reason too. It ensures that all the latest and greatest from him are available in the pause menu too).

    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/release-notes/vps.png" width="700" />
  
- **Table Overview**: 
  - In the Table Data dialog, an additional check has been added to see if the overridden ROM name exists.
  - **ALT Color Backups**: Added dialog for backups to the ALT color section. Note that you may already have some backups available since the actual backup support for the backend was already build a year ago. The dialog allows you to restore these files now.
  
    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/release-notes/altcolor.png" width="700" />
   
- **Backglass Manager**:
  - Improved refresh of backglass sidebar when multiple clicks in list.
- **Playlist Manager**:
  - Instead of auto-saving the SQL of the playlist, a separate button for saving has been added. The SQL is validated on save. Therefore the intermediate validation does not block the SQL input anymore.
  
    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/release-notes/playlists.png" width="400" />
- **Media Recorder**:
  - Added new "Wizard" button that automatically selects all video screens of all tables that are missing and configured as mandatory.

    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/release-notes/recorder.png" width="400" /> 
- **Pause Menu**:
  - Improved icon and label for tutorials.
  - Disabled additional frontend screens when the pause menu is shown. These should not have been active anymore since the latest revamp that came with version 4.0.
  - New revamped icons! Thanks to @planetxplanetx!
- **Main Window**:
  - Added options to start and stop PinVOL directly from the preferences dropdown menu. Since PinVOL may block key bindings, you can faster work around this problem this way.
  
    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/release-notes/pinvol.png" width="300" />
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
- Fixed bulk column selection in media recorder.
- Fixed duplication of emulators which did not work but only rename the selected one.

## VPin Mania

- **Added highscore synchronization view**: When you run a complete highscore synchronization overview, including the ones that have been denied. This view will also help us in case highscores have not been found on https://app.vpin-mania.net. 
- **Fixed rating submissions**: Instead of incremental ratings they are calculated by a scheduled bulk operation. Make sure to enable the rating submissions in your privacy settings of VPin Mania. The rating on https://app.vpin-mania.net will be re-enabled soon. 