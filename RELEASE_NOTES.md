## Release Notes 3.10

## Changes

- **Tables / Remote + In-Game Recording**: The Studio supports remote recording of frontend screens now. You find the recorder as an additional tab in the tables section. The recorder supports single and bulk recording and gives a preview of the screens that are recorded. In addition to that, you can start an in-game recording by binding a key for this. More details about this can be found here: https://github.com/syd711/vpin-studio/wiki/Media-Recorder 

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/recorder/recorder.png" width="800" />

- **Tables / Cabinet Monitor**: The Studio supports the screen monitoring of cabinets now. You can invoke the cabinet monitor from the main toolbar. The dialog offers two different view modes right now: monitoring the actual monitors or monitoring the configured frontend screens.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/monitor/monitor-toolbar.png" width="400" />
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/monitor/monitor-1.png" width="700" />
  
- **Tables / Patching**: Given the recent releases of patch files, the Studio supports patching tables now. **.dif** files are now supported like any assets, so you can drag and drop them from you operating system or the drop-in folder. If the .dif file is archived it will be analyzed like any other bundle and the additional assets will be applied to the patched table too.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/patch-dialog.png" width="700" />

- **Tables / Frontend Game Launch**: For PinUP Popper users, the launch menu has an additional entry to launch a table through PinUP Popper (thanks to @nailbuster helping here!). This is useful in combination with the cabinet monitor to see if the screens are configured properly. 
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/launch-menu.png" width="280" />

- **Tables / Table Asset Manager**: The asset manager gives you the additional options to delete not only single assets, but also all assets of the selected screen and all assets of the game.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/asset-deletions.png" width="500" />

- **Tables / Table Asset Manager**: PinballX and PinballY users have video conversion options now too. (They were only shown for PinUP Popper users before).

- **Tables / Validators**: Introduced new validator for VR support. The validator checks if there is a VR room flag inside the script and returns an error if not enabled. **If you do not use VR, please disable this validator as there is no option to disable it by default.** Note that you need to re-scan all tables to find disabled VR rooms. 

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/vr-validator.png" width="500" />

- **Tables / Script Editing**: Added info icon to the split menu button with a hint that .vbs files must be linked to a text editor in order to use that action.
- **Tables / Usability Improvement**: Pressing the **shift key and double-clicking** on a table in the table overview will open the asset manager dialog.
- **System Manager**: Removed the 32/64-bit preset combo from the "Overview" tab and removed the corresponding filtering from the release artifact comboboxes. Especially for freezy updates where you might want to install both versions, this switching was too tedious.
- **Updates**: The release notes for the next update are now shown before (and after) updating. So for versions larger 3.10 you will see the release notes the next time when pressing the update button.
- **VPX Game Launcher**: The VPX game launcher is launching the emulator window minimized now.

## Bugfixes

- **Tables / Uploads**: Improved **cloning** behaviour so that the original name is kept and the existing VPS mapping too, if the auto-match flag has been disabled.
- **PinUP Popper Integration**: More gracefully stopping of PinUP Popper by calling the regular exit command first (instead of simply killing all processes).
- **Competitions / iScored**: Added **Event Log** button to inspect events of the selected table.

## VPin Mania

- **Tournaments / Tournament Browser**: Fixed remaining time label.
- **Tournaments / Highscores**: Fixed issue that scores where only shown for installed games.
- **Tournaments**: **Tournaments have been disabled/hidden by default and must be enabled in the preferences.** You won't loose any data by this. It improves the boot-up time of the server a bit since the automatic synchronization of tournament highscores is also disabled this way.
- **Table Views**: Added more convenience for VR users by adding the launch game menu button to the different table views of VPin Mania.