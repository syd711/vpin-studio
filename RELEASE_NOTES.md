## Release Notes 3.15

## Changes

- **iScored V2**: 
  - game lock
  - game multiscore
  - added badge
- competition sidebar
- new error icons
- **System Manager**: The overall update check is not blocking the UI anymore. This way, you can immediately switch to other system manager tabs now.
- **Table Data Manager**: The comments dialog has been integrated into the Table Data Manager dialog. The feature was a bit too hidden.
- **Table Data Manager**: The Table Data Manager dialog has an additional tab "Playlists" now where the game can be assigned to playlists.
- **Highscore Backups**: Added bulk-operation support for highscore backups.
- **Backglass Server**: Added option to set "Simple LEDs" as default in the backglass server preferences.
- **DMD Position Tool**: No more excuse for having an emtpy full dmd. It is now possible to add a full dmd image directly from the dmd position tool or keep the full dmd video from the frontend active. In that case a frame is picked to position the DMD onto the video.
- **DMD Position Tool**: Added possibility to mass edit DMD positions with next / prev buttons, and a save button that saves the position but does not close the dialog.
- **DMD Position Tool**: Added support of alphanumeric DMD. The DMD zones are inherited from the backglass (number of default position). The zones can then be modified and saved. A reset button permits to restore the default positions of zones if modified.
- **DMD Position Tool**: With the new support of alphanumeric, disconnected the display of a screen from the move of a zone in that screen. The different screens with assoicted zones can be displayed thank to a new tab bar in the top of the window, and the "move to" radio buttons are used to move the selected DMD zone onto the selected screen.
- **DMD Position Tool**: Added possibility to disconnect DMD and use backglass scores only. When chosing this option, ability to disconnect DMD by turning off ext dmd in VPinMame and/or disabling DMD in dmdevice.ini.
- **DMD Position Tool**:
- **DMD Position Tool**:
- **Media Recorder**: Added option to set the VPX parameter "-Primary" for the recording.
- **Table Asset Management** Added highscore reset button to "Scores" tab.
- **Table Asset Management** Added additional dialog for media bulk conversions. The action for this is only available in the asset mode view. Note that you can extend the given conversion options on your own (https://github.com/syd711/vpin-studio/wiki/Table-Asset-Manager#media-converter).
 
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/bulk-conversion.png?raw=true" width="400" />


## Bugfixes

- **Media Recorder**: Fixed issue that the selection was kept when the emulator selection was switched. Because of the possible emulator recording mode, only recording from one emulator type are allowed. 
- **Media Recorder**: Fixed issue that the "default" VPX emulator was used for emulator recordings instead of the actual VPX emulator selection.
- **Media Recorder**: Fixed issue existing recordings couldn't be overwritten by new ones. To avoid the file lock, the copy process for the recordings is executed after the emulator/frontend has been closed now.
- **Default Emulator Resolving**: More of a technical detail: On several occasions the first VPX emulator was used instead of providing an actual selection or using the one that belongs to the corresponding game. Especially for people running multiple VPX emulators, this may have caused issues. 


## VPin Mania
