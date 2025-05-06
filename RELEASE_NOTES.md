## Release Notes 4.0

## Changes

- **iScored**: Large parts of the iScored integration have been re-implemented. The concept has changed so that you don't have to manually subscribe to tables anymore. A complete instruction about the new mechanism can be read here: https://github.com/syd711/vpin-studio/wiki/iScored or on YouTube (https://www.youtube.com/@vpin-studio). The changes in short are:
  - Added preferences section where iScored Game Rooms can be setup and selected for synchronization.
  - Added option to disable the complete iScored integration for users who don't need it.
  - Added iScored badge for wheels (only available for fresh installations, these are not updated automatically).
  - Added iScored playlist to the list of SQL playlist templates.
  
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/preferences/iscored.png?raw=true" width="650" />
- **Competitions**: Added quick-settings for every tab and collapsible button for the sidebar. So this matches with the tables view now.
- **System Manager**: 
  - The overall update check is not blocking the UI anymore. This way, you can immediately switch to other system manager tabs now.
  - Full support of target folder that were ignored before for component fresh installation.
- **Table Overview**: 
  - De-cluttered context menu and removed less used function with focus on supporting more bulk actions there.
  - Added context menu option to bulk reset table ratings.
  - Replace error status icon for better contrast.
  - Added competition button to the status column, shown when the table is used in any competition or tournament. The button takes you to the corresponding competition view.
  
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/competition-button.png?raw=true" width="250" />
  
- **Highscore Backups**: Added bulk-operation support for highscore backups.
- **Backglass Management**: 
  - Backglass validators have been added, reusing the status column. 4 validators are supported, and can be activated / deactivated in the new backglass validator preference page.
  - Added possibility to remove the table specific 'Run as Exe' value and use the server default.
  - Added option to set "Simple LEDs" as default in the backglass server preferences.
  - Added button in the matching table toolbar to launch the game from the backglass.
- **Pause Menu**:
  - The pause menu has undergone an overhaul. The browser solution just did not work reliable enough and had a bunch of issues. As a result, **the tutorial video from Michael Kongedam/@kongedam are now hosted on vpin-mania.net too**. This way they can be directly streamed into the media player of the pause menu. So right now tutorials videos are restricted to this author. There is likely more to come here.
- **Playlists Management**:
  - Custom icons depending on the playlists have been re-introduced. (We had to disable the first attempt but came up with a better solution now.)
  
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/playlist-icons.png?raw=true" width="150" />
  
- **DMD Position Tool**: 
  - No more excuse for having an empty full dmd. It is now possible to add a full dmd image directly from the dmd position tool or keep the full dmd video from the frontend active. In that case a frame is picked to position the DMD onto the video.
  - Added possibility to mass edit DMD positions with next / prev buttons, and a save button that saves the position but does not close the dialog.
  - **Important change:** Disconnected the display of a screen from the move of a zone in that screen. The different screens with associated zones can be displayed thank to a new tab bar in the top of the window, and the "move to" radio buttons are used to move the selected DMD zone onto the selected screen.
  - When there is no B2S full DMD or it is not active, and when the frontend has a full DMD video that is flagged to be kept displayed when game is launched, then a random frame of that video is picked to position the DMD onto it. 
  - No more excuse for not having a full dmd. It is now possible to add a full dmd image directly from the dmd position tool or keep the full dmd video from the frontend active (also see previous change).
  - Added support of alphanumeric DMD. The DMD zones are inherited from the backglass (number and default positions). The zones can then be modified and saved. Also a reset button permits to restore the default positions of zones taken from backglass if modified.
  - For alphanumeric DMD, added the possibility to remove the rendering of backglass scores. It generally causes problem as the alphanumeric DMD mays not cover the full backglass scores and not totally hide them. Mind the backglass is modified, and previous score state is backup within the backglass.
  - Added possibility to completely disconnect DMD and use backglass scores only. When choosing this option, the Freezy ext DMD can be disabled by turning off the ext DMD in VPinMame and/or disabling the DMD in dmdevice.ini.
- **Media Recorder**: 
  - Added option to set the VPX parameter "-Primary" for the recording.
  - For convenience, the screen validators are sychronized with the media recording screens now. So if validations for a screen are disabled, the screen will also be hidden from the Media Recorder. Of course, they can be enabled there again.
- **Table Data Manager**: 
  - The comments dialog has been integrated into the Table Data Manager dialog. The feature was a bit too hidden.
  - The Table Data Manager dialog has an additional tab "Playlists" now where the game can be assigned to playlists.
  - The auto-matching has been improved for a better accuracy in the match with the VPS database.
- **Table Asset Management** 
  - Added highscore reset button to "Scores" tab.
  - Added additional dialog for media bulk conversions. The action for this is only available in the asset mode view. Note that you can extend the given conversion options on your own (https://github.com/syd711/vpin-studio/wiki/Table-Asset-Manager#media-converter).
 
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/bulk-conversion.png?raw=true" width="400" />


## Bugfixes

- **Media Recorder**: 
  - Fixed issue that the selection was kept when the emulator selection was switched. Because of the possible emulator recording mode, only recording from one emulator type are allowed. 
  - Fixed issue that the "default" VPX emulator was used for emulator recordings instead of the actual VPX emulator selection.
  - Fixed issue existing recordings couldn't be overwritten by new ones. To avoid the file lock, the copy process for the recordings is executed after the emulator/frontend has been closed now.
  - Fixed issue that the media overview was not properly refreshed after a recording was finished.
  - Changed the Media Recorder view to a split view, so that the vertical splitter can be resized. 
- **DMD Position Tool**: Fixed issue when rom contains a dot like PiratesLife, positions were not properly saved. The rom name in the dmddevice.ini has to be ecsaped.
- **Default Emulator Resolving**: More of a technical detail: On several occasions the first VPX emulator was used instead of providing an actual selection or using the one that belongs to the corresponding game. Especially for people running multiple VPX emulators, this may have caused issues.

## VPin Mania

The VPin Mania integration has undergone a complete overhaul. The whole "VPin Mania" section has been removed and migrated into the VPin Mania app. Also, all preferences have been consolidated into a separate toolbar menu (the former "Friends" menu). **If you have connected with friends already, please revisit the privacy settings.** Here is a summary of all the changes:
- The account management and tournaments settings have been moved from the regular settings into the "VPin Mania" preferences.
- The preferences section "My Cabinet" has been duplicated for the VPin Mania preferences to highlight that these changes are reflected on the new website too.
- The tournaments view has a quick-preferences and sidebar toggle button now, so it matches with the tables and competitions view now.
- The tournaments dialog was revisited and adapted to the new iScored integration. Several bugs have been fixed and performance optimizations been added.
- The playlist manager has now also a SQL template for tournament tables now.

## VPin Mania Webapp
<img src="https://app.vpin-mania.net/android-icon-144x144.png" width="80" />

**https://app.vpin-mania.net/ has been launched!** You can now browse all your highscores online.
The website replaces the old VPin Mania view from the Studio and has the same feature set the older view and even more!
Check out the YouTube video (https://www.youtube.com/@vpin-studio) for more details.

Note for the highscore admins of vpin-mania: The website comes with an additional admin interface to put highscores on the deny list.

