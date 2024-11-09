## Release Notes 3.10

## Changes

- **Tables / Remote Recording**: 

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/recorder/recorder.png" width="800" />

- **Tables / Cab Monitor**: The Studio supports the screen monitoring of cabinets now. You can invoke the cabinet monitor from the main toolbar. The dialog offers two different view modes right now: monitoring the actual monitors or monitoring the configured frontend screens.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/monitor/monitor-toolbar.png" width="400" />
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/monitor/monitor-1.png" width="700" />
  
- **Tables / Patching**: Given the recent releases of patch files, the Studio supports patching tables now. **.dif** files are now supported like any assets, so you can drag and drop them from you operating system or the drop-in folder. If the .dif file is archived it will be analyzed like any other bundle and the additional assets will be applied to the patched table too.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/patch-dialog.png" width="700" />

- **Tables / Frontend Game Launch**: For PinUP Popper users, the launch menu has an additional entry to launch a table through PinUP Popper (thanks to @nailbuster helping here!). This is useful in combination with the cabinet monitor to see if the screens are configured properly. 
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/launch-menu.png" width="280" />

- **Tables / Table Asset Manager**: The asset manager gives you the additional options to delete not only single assets, but also all assets of the selected screen and all assets of the game.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/asset-deletions.png" width="500" />

- **Tables / Script Editing**: Added info icon to the split menu button with a hint that .vbs files must be linked to a text editor in order to use that action.
- **Tables / Usability Improvement**: Pressing the **shift key and double-clicking** on a table in the table overview will open the asset manager dialog.

## Bugfixes

- **Tables / Uploads**: Improved **cloning** behaviour so that the original name is kept and the existing VPS mapping too, if the auto-match flag has been disabled.
- **PinUP Popper Integration**: More gracefully stopping of PinUP Popper by calling the regular exit command first (instead of simply killing all processes).