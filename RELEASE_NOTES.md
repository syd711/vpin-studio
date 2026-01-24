## Release Notes 4.6.1

## Changes

- **Drop-in folder**: Added configuration for a post-action after a file in drop-in folder is installed. Possible options are:
    - Do nothing, the file is left in the folder.
    - Move the file in a configured target folder.
    - Move the file in a sub-folder of a target folder. The sub-folder name is the name of the Game associated with the asset.
    - Choose a folder and move the file into it.
    - Move the file in trash bin.
    - Delete the file, the file cannot be recovered.
- **Global DMDDevice.ini Settings**: 
  - Added possibility to change ignore settings.
  - Added update button to update the network stream url so that the server gets the DMD frames.
- **Table DMDDevice.ini Settings**: Fixed error that happened during resolving the section name for the DMDDevice.ini for a table entry. For some tables this entry name was not resolved during the table scan and the fallback ROM name was not used. 
- **Pause Menu Preferences**: Added option to include DMD frame capture, useful for cabs with a real DMD.
- **Controller Setup**: Added expert mode for the controller bindings dialog that allows manual input of key values. This might be necessary in case the VPin Studio server detected different key codes than Windows.
- **cROMC Support**: Added missing file filter in the ALTColor upload dialog.
- **Preferences**: Added missing vertival scrollbars (backglass, pinvol, ...)

---

## Release Notes 4.6.0

## Changes

- **Table Validators**: Introduced new table validator which checks if the table has a backglass, but the backglasses are disabled in the VPX settings. (I assume it is an edge case, but can be annoying to trouble-shoot).
- **ALT Color**: Added support for .cROMc files, which includes uploading, backup and backup on replace of .cROMc files.
- **Pause Menu**: 
  - Added rotation option for the tutorials screen.
  - Added margin options to position the tutorials screen pixel perfect.
  - Added scaling option to adjust the size of the pause menu.
  - Added "Apron Mode" setting which hide the navigation from the pause menu in case you have a screen too small to display the full pause menu.
  - Added offset settings to fix the positioning of the pause menu (might be required for the apron mode).
- **DropIn Menu**: The deletion of an asset is proposed for all asset types now when installed from the drop-in menu (not only for backglasses).
- **VPS Entries**: Generalized the display of comments across assets in Virtual-Pinball-Spreadsheet views.
- **Preferences Menu**: The **Mute System** entry detects the mute state of the cabinet and reflects it by its toggle state.
- **Preferences**: Added the new entry **DMD Device** which lets you configure some settings for the file **DMDDevice.ini** file.
- **Backups**:
  - Added VPin Studio settings to table backups. These settings include e.g. comments which are saved and restored as part of the backup now.
  - Added backup of the DMDDevice.ini settings for a table. This includes the DMD position (if not stored for the registry). Note that the restoring is only applied if the registry usage is disabled.
- **Screenshots**: Added service URL to fetch latest pause menu screenshot (http://localhost:8089/api/v1/recorder/screenshot/latest)


## Bugfixes

- **Server Startup**: Fixed critical error that lead to a server crash when the **ScreenRes.txt** file location could no be determined.
- **Media Recorder Table Filtering**: Fixed issue that the media recorder used the filter settings from the table overview.
- **Table Data Manager**: The "Auto-save" flag for the dialog is not remembered.
- **Pause Menu**:
  - Fixed screen configuration that can be used for showing the tutorials video (@kongedam It is finally there!).
  - Fixed mute on pause: If the system was already muted, the pause menu does not unmute on exit anymore.
  - Fixed **iScored** pause menu item that was always ignored.
  - Added retry attempts in case the video rendering for tutorials fails. Hopefully this fixes the issue that videos are not starting.
- **Dialogs**: Fixed issue that after releasing changes on dialogs the contents of these were cropped.