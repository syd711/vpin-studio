## Release Notes 4.6.6

## Changes

- **Backglass Manager**: Fix default value for "bring forms", should be Form To Front when not set.
- **Pause Menu**: 
  - Reverted the ignoring of the "Mute System" option for tutorial videos.
  - Increased the buffer size for messages from Freezy DMD, used for screenshots of real DMD.
- **Score Formatting**: Fixed score formatting where replacing special character from the pinemhi output may have resulted in duplicated whitespaces.
- **Table Data Manager**: Fixed missing update of the highscore filename and alternative ROM name fields when cycling through different tables.
- **In-Game Recorder**: Fixed multiple issues causing in-game recordings to fail.
- **System Manager**: The release selection for the backglass server, FlexDMD, Freezy and VPinMAME are filled with all releases now. This allows you to downgrade your installation too in case there are issues with the latest releases.

---

## Release Notes 4.6.5

## Changes (including a breaking one!)

- **Pause Menu**: 
  - Added separate section for the screenshot preferences there. You can choose now which screens are taken for the screenshot. Note that **you need to review these settings** so that the scoring screenshot works as expected and contains your highscore.
  - Tutorials Video**: The video playback uses the **VLC Media Player** from PinUP Popper now (when found). This is a first test ballon and might need some refinement. But at least the video playback is now reliable. The VLC player is not shipped with the Studio due to license restrictions.
  - The videos are played on a separate screen, the "Mute System" option is ignored so that the video is always played with sound.
- **Asset Manager**: 
  - Fixed possible file lock caused by the media streaming that prohibited the deletion (blind fix).
  - Added same support for playlist media than game media (drag/drop, rename, set as default, delete, conversion...).
- **System Manager**: Added the **dmddevice.ini** file to the list of exclusions for VPinMAME installations.
- **Screen Recorder**: improve logs and exception in DMD frame handling
- **Table Data Manager**: Fixed issue that data was saved for the wrong table. 
- **Table Overview**: Fixed table import button that was disabled when only VPX emulator was enabled. 
- **Misc**: Fixed the resize handler for all windows which was not working properly (finally).

---

## Release Notes 4.6.4

## Changes

- **superhac** has extended his media repository: table flyers can now be searched for the GameInfo screen.
- Fixed error reading the global screenres.txt.
- Fixed missing UI disabled state update when the drop-in folder checkbox was toggled.
- Fixed broken "Generate" button in the template editor.
- Fixed builds for MacOS.

---

## Release Notes 4.6.3

## Changes (including a breaking one!)

- **Server Startup**: Handled possible error in the screen resolving which caused the server not to start for some users. 
- **File Uploads** Fixed issue that various files have been locked from the Studio client after uploading.
- **Monitor Detection**: Fixed possible issue where the order of monitors changed. **Unfortunately this is a breaking change and you might need to reconfigure the monitor for the overlay, pause menu and notifications!**

--- 

## Release Notes 4.6.2

## Changes

- **Highscore Parsing**: Reverted the **en_US** encoding and **UTF-8** codepage for the execution of the **pinemhi.exe** as it is causing issues for other locales.
- **ALT Color Upload Dialog**: Added file filtering for .cROMc files.

---

## Release Notes 4.6.1

## Changes

- **Drop-in Folder**: Added configuration for a post-action after a file in drop-in folder is installed. Possible options are:
    - Do nothing, the file is left in the folder.
    - Move the file in a configured target folder.
    - Move the file in a sub-folder of a target folder. The sub-folder name is the name of the Game associated with the asset.
    - Choose a folder and move the file into it.
    - Move the file in trash bin.
    - Delete the file, the file cannot be recovered.
- **Asset Manager**: The asset manager dialog can be maximized and its dimensions are restored from the saved position.
- **Frontend Media View**: Frontend videos are no more muted in preview dialogs.
- **Global DMDDevice.ini Settings**: 
  - Added possibility to change ignore settings.
  - Added update button to update the network stream url so that the server gets the DMD frames.
- **Table DMDDevice.ini Settings**: Fixed error that happened during resolving the section name for the DMDDevice.ini for a table entry. For some tables this entry name was not resolved during the table scan and the fallback ROM name was not used. 
- **Pause Menu Preferences / WOVP Integration**: Added option to include DMD frame capture, useful for cabs with a real DMD.
- **Pause Menu**:
  - For the test mode, the whole pause menu is rebuild. This way, you do not need to restart the server and changes are reflected immediately.
  - Fixed clipped display when repositioned in apron mode.
  - Added "bring to front" execution for every key input. This should avoid "falling" the menu behind video and also help with the GL mode.
- **Controller Setup**: Added expert mode for the controller bindings dialog that allows manual input of key values. This might be necessary in case the VPin Studio server detected different key codes than Windows.
- **cROMC Support**: Added missing file filter in the ALTColor upload dialog.
- **Preferences**: Added missing vertical scrollbars (backglass, pinvol, ...).
- **Highscore Parsing**: Added **en_US** encoding and **UTF-8** codepage for the execution of the **pinemhi.exe**.
- **Pause Menu / Overlay**: Fixed issue that sometimes the overlay opened instead of the pause menu.
- **Stop Button**: The stop button that kills all frontend and emulator processes tries to exit the emulator (if active) by emitting the "Q" key event now. This should gracefully shutdown VPX now. An additional process kill is executed with a delay anyway (always double tap!).
- **Table Data Manager** Fixed refresh issues on previous/next button clicks by synchronizing the actions.

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