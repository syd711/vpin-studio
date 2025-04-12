## Release Notes 3.15

## Changes

- **System Manager**: The overall update check is not blocking the UI anymore. This way, you can immediately switch to other system manager tabs now.
- **Table Data Manager**: The comments dialog has been integrated into the Table Data Manager dialog. The feature was a bit too hidden.
- **Table Data Manager**: The Table Data Manager dialog has an additional tab "Playlists" now where the game can be assigned to playlists.
- **Highscore Backups**: Added bulk-operation support for highscore backups.
- **Table Asset Management** Added additional dialog for media bulk conversions. The action for this is only available in the asset mode view. Note that you can extend the given conversion options on your own (https://github.com/syd711/vpin-studio/wiki/Table-Asset-Manager#media-converter).
 
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/bulk-conversion.png?raw=true" width="400" />


## Bugfixes

- **Drop-In Menu** Limited the amount of items to 100 to avoid a client crash when a folder with thousands of entries is added.
- **Drop-In Menu** Removed the system tray notification. It was ugly anyway.
- **Media Recorder**: Fixed issue that the selection was kept when the emulator selection was switched. Because of the possible emulator recording mode, only recording from one emulator type are allowed. 
- **Media Recorder**: Fixed issue that the "default" VPX emulator was used for emulator recordings instead of the actual VPX emulator selection. 


## VPin Mania
