## Release Notes 3.8

- **Drop-In Folder: Trash-bin support**: Files deleted from the drop-in folder menu are not deleted anymore, but moved to the trash-bin.
- **Drop-In Folder: Added subfolders support** : The drop-ins folder can now contains sub-folders that are also monitored. The drop-in section displays all files flat from the tree structure.
- **DofLinx Support**: You can select your DOFLinx installation in the preferences now and auto-start it this way together with the VPin Studio Server. If configured, it will also appear inside the System Manager and can be updated there.
- **Tables / Backglass Manager: Playlists Filtering** : Added the possibility to filter backglasses by playlists as it is possible in the tables tab.
- **Tables / VPS Tables: Search Result Highlighting**: Inside the VPS Tables tab, not only the table component is filtered but the VPS table versions in the detailed view are also highlited according to filters.
- **Tables / VPS Tables: Add filter on last update date**: in VPS Tables tab, add a new filter on last update date of VPS table versions.

## Bugfixes

- **Added 7-zip support**: Actually it was there since the .rar support, but we forgot to add the .7z suffix to dialogs and drag-and-drop filters.
- **Added RAR support for MacOS (ARM)**: The sevenzip library that is used for .rar files has been replaced with a patched version that is supposed to work for macOS with ARM.
- **Maintenance Mode**: Fixed issue that the Windows taskbar is not shown when the Studio is exited and this mode was enabled. 
- **Validation of assets with image and video**: when Asset validation is set to video and the tables contains a video asset and an  image one, the validation was claiming an error when it shouldn't .
- **Highscore Parsing**: Fixed "Red & Ted's Road Show" highscore.

  
