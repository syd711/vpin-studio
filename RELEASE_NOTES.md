## Release Notes 3.8.1

## Bugfixes

- **Tables / Emulator Filter**: Fixed critical error in the emulator detection, causing empty views for non VPX emulators.
- **VPin Mania / Wheel Icons**: Changed loading of the wheel icons for the VPin Mania tables, so that they can be updated independently from the Studio now.


### Release Notes 3.8

- **Drop-In Folder: Trash-bin support**: Files deleted from the drop-in folder menu are not deleted anymore, but moved to the trash-bin.
- **Drop-In Folder: Added subfolders support** : The drop-ins folder can now contains sub-folders that are also monitored. The drop-in section displays all files flat from the tree structure.
- **DOFLinx Support**: You can select your DOFLinx installation in the preferences now and auto-start it this way together with the VPin Studio Server. If configured, it will also appear inside the System Manager and can be updated there.
- **Tables / Backglass Manager: Playlists Filtering** : Added the possibility to filter backglasses by playlists as it is possible in the tables tab.
- **Tables / VPS Tables: Search Result Highlighting**: Inside the VPS Tables tab, not only the table component is filtered but the VPS table versions in the detailed view are also highlited according to filters.
- **Tables / VPS Tables: Add filter on last update date**: in VPS Tables tab, add a new filter on last update date of VPS table versions.
- **Tables / VPS Tables: Table format filtering**: Add grouping of Zen Studio tables (FX, FX2, FX3) and highlight VPS table versions based on the table format selection

### Bugfixes

- **Added 7-zip support**: Actually it was there since the .rar support, but we forgot to add the .7z suffix to dialogs and drag-and-drop filters.
- **Added RAR support for MacOS (ARM)**: The sevenzip library that is used for .rar files has been replaced with a patched version that is supposed to work for macOS with ARM.
- **Detection of VPX emulator**: check presence of VPX files within the table folder of the emulator 
- **Maintenance Mode**: Fixed issue that the Windows taskbar is not shown when the Studio is exited and this mode was enabled. 
- **Tables / Validation of assets with image and video**: When Asset validation is set to video and the tables contains a video asset and an  image one, the validation was claiming an error when it shouldn't .
- **Highscore Parsing**: Fixed "Red & Ted's Road Show" highscore.
- **Maintenance Mode**: Fixed issue that the Windows taskbar is not shown when the Studio is exited and this mode was enabled.
- **Emulator Detection**: Improved the way emulator types are determined.

### VPin Mania

- **Window 11 Support**: Fixed cabinet registration issue for Windows 11.