## Release Notes 4.0.4

## Changes

- **Table Overview**: 
  - Duplicated the uploads drop-down menu with a table into the side section "Table Data". This way, the menu is available without overflow for smaller screens too. Also, it is somehow expected there.
  - Re-added actions "Table Scan" and "Reset VPS Updates" to the table overview's context menu.
  - Added shortcut support for "Reset VPS Updates" (Ctrl+W).
  - Added context menu entry and shortcut to **reload** the selected tables (Ctrl+R). In addition to that, the "Table Data" section has also a "Reload" button now.
  - Fixed VPinMAME initialization, so the alias mapping is initially shown correct now again.
  - Fixed alias mapping caching issue: After editing the **VPMAlias.txt** file, the matching games are invalidated now (client + server).
  - Added "Backup All" to the "Highscores" section. Note that this only works on the current table data view and ignores the ones from other (VPX) emulators that are not selected.
- **Table Uploader**: Fixed issue that .pov files were detected as ROM files (RobbyKingPin tables). These files are simply excluded from the import now. 
- **Playlist Icons**: Added option to switch back to legacy icons.
- **Auto-Connect**: Re-enabled auto-connect. (The feature got lost by accident during the 4.0 creation).

---

## Release Notes 4.0.3

## Changes

- **Table Data Manager**: Fixed icon loading issue blocking the dialog.
- **Confirmation Dialogs**: Fixed sizing issues.
- **Preferences**: Fixed refresh of Avatar when uploading a new image
- **DMD Position Tool**: 
  - Show watermark when DMD image comes from frontend or PupPack.
  - Fixed message when B2S DMD is hidden, instead of uploading an Image, propose to show the image.

---

## Release Notes 4.0.2

## Changes

- **Pause Menu**: The time amount the game is paused through the pause menu is now substracted from the overall play time of game. So you don't falsify your stats anymore, when you let your cabinet alone in pause mode.
- **Uploads**: Fixed issue for uploads that were not related to a game, e.g. ROM files.
- **Playlists**: Fixed favorites icon.
- **VPin Mania**: 
  - Fixed rating submission. The initial value for ratings was not always synchronized properly which led to a wrong total rating count which then again falsified the rating a bit.
  - Fixed possible endless refresh in the client during registration.

---

## Release Notes 4.0.1

## Changes

- Fixed ignoring of the new VPS validators for ALT sound, ALT color and PUP packs.
- Fixed Pinball FX3 playlist icon and errors during playlist icon resolution.
- Fixed "modal" mode for dialogs.

---

## Release Notes 4.0.0

## Changes

- **Server Side Caching**: The games are now cached on the server side too. You will notice a general improvement regarding the responsiveness of the Studio. This was a large backend change, so please let me know if anything does not refresh properly. A manual reload in the table overview triggers a full reset of the client and server cache.
- **iScored**: Large parts of the iScored integration have been re-implemented. The concept has changed so that you don't have to manually subscribe to tables anymore. A complete instruction about the new mechanism can be read here: https://github.com/syd711/vpin-studio/wiki/iScored or on YouTube (https://www.youtube.com/@vpin-studio). Here is the summary of the changes:
  - Added preferences section where iScored Game Rooms can be setup and selected for synchronization.
  - Added option to disable the complete iScored integration for users who don't need it.
  - Added iScored badge for wheels (only available for fresh installations, these are not updated automatically).
  - Added iScored playlist to the list of SQL playlist templates.
  
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/preferences/iscored.png?raw=true" width="650" />
- **Competitions**: Added quick-settings for every tab and collapsible button for the sidebar (the two button at the righ of the tab-bar). So this matches with the tables view now.
- **Table Statistics**: Added the column "Last Played" to the table statistics overview which shows in chronological order the last played games.
- **System Manager**: 
  - The overall update check is not blocking the UI anymore. This way, you can immediately switch to other system manager tabs now.
  - Full support of target folder that were ignored before for component fresh installation.
  - Added DOF to the list of components in the system mananger. Note that the tab is only active when a valid installation folder has been set in the preferences.
- **Table Overview**: 
  - De-cluttered context menu and removed less used function with focus on supporting more bulk actions there.
  - Added context menu option to bulk reset table ratings.
  - Replaced error status icon for having a better contrast.
  - Replaced the launch drop-down menu to a combo box with separate "Launch" button. The selection of this combo box stored too. In addition to that, the launch button has moved to the left, so that less used button go into the overflow menu for smaller resolutions instead.
  
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/launcher-menu.png?raw=true" width="250" />
    
  - Added competition button to the status column, shown when the table is used in any competition or tournament (e.g. all iScored subscriptions). The button takes you to the corresponding competition view.
  
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/competition-button.png?raw=true" width="250" />
  
- **Highscore Backups**: Added bulk-operation support for highscore backups.
- **Table Validation**: Added new Virtual Pinball Spreadsheet validators that are triggered when the table has an **ALT sound, ALT color or PUP pack** available, but not installed. The new validators are enabled by default. You can disable them in the "Table Validator" preferences if this is not interesting for you.
- **Backglass Management**: 
  - Backglass validators have been added, reusing the status column. 4x validators are supported, and can be activated / deactivated in the new backglass validator preference page.
  - Added possibility to remove the table specific 'Run as Exe' value and use the server default.
  - Added option to set "Simple LEDs" as default in the backglass server preferences.
  - When Backglass and/or B2S DMD is hidden, the corresponding preview is translucent, reflecting the hidden state while still showing an image is present within the backglass
  - Added button in the matching table toolbar to launch the game from the backglass. Only default launcher is supported from there.

    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/preferences/backglass-validators.png?raw=true" width="300" />
  
- **Pause Menu**:The pause menu has undergone an overhaul. The browser solution just did not work reliable enough and had a bunch of issues. As a result, **the tutorial video from Michael Kongedam/@kongedam are now hosted on vpin-mania.net too**. This way they can be directly streamed into the media player of the pause menu. So right now tutorials videos are restricted to this author. There is likely more to come here.
- **Playlists Management**:
  - Custom icons depending on the playlists have been re-introduced. (We had to disable the first attempt but have a better solution now.)
  
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/playlist-icons.png?raw=true" width="150" />
  
- **DMD Position Tool**: 
  - No more excuses for not having an image in your full dmd. It is now possible to add a full dmd image directly from the dmd position tool or to grab a frame from the full dmd video and bring it in the backglass. It is also possible to keep the full dmd video from the frontend active, in that case a frame is also picked to position the DMD onto the frontend video.
  - Added possibility to mass edit DMD positions with next / prev buttons, and a save button that saves the position but does not close the dialog.
  - Added detection of changes and auto-save capability, where a click on next / prev button can automatically trigger a save of the changes 
  - Added Ctrl+S shortcut to save DMD position
  - **Important change:** Disconnected the display of a screen from the move of a zone in that screen. The different screens with associated zones can be displayed thank to a new tab bar in the top of the window, and the "move to" radio buttons are used to move the selected DMD zone onto the selected screen.
  - When there is no B2S full DMD or it is not active, and when the frontend has a full DMD video that is flagged to be kept displayed when game is launched, then a random frame of that video is picked to position the DMD onto it. 
  - Added support of alphanumeric DMD. The DMD zones are inherited from the backglass (number and default positions). The zones can then be modified and saved. Also a reset button permits to restore the default positions of zones taken from backglass if modified.
  - For alphanumeric DMD, added the possibility to remove the rendering of backglass scores. It generally causes problem as the alphanumeric DMD may not cover the full backglass scores and not totally hide them. Mind the backglass is modified, and previous score state is backup within the backglass.
  - Added possibility to completely disconnect DMD and use backglass scores only. When choosing this option, the Freezy ext DMD can be disabled by turning off the ext DMD in VPinMame and/or disabling the DMD in dmdevice.ini.

   <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/dmd-positioner2.png?raw=true" width="750" /> 
  
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
 
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/bulk-conversion.png?raw=true" width="450" />
- **Preferences Hooks**: Added support for .vbs files. Also, the ROM name and the table filename from the selected table in the table overview are passed as parameters to the script. (https://github.com/syd711/vpin-studio/wiki/Preferences-Hooks)
- **Window Management**: All positions and sizes of resizeable dialogs are stored now. In case something is messed up, the settings can be resetted in the client preferences.
- **IOS and Linux Support**: A very draft support of IOS and linux for the server part and a first step forward a VPX-standalone support. The server can be installed manually and start without error. There is still a lot of work to do and many Windows function still need to be implemented for the other operating systems. 
- **PINemHi 3.6.6 Update** (releases notes taken from PINemHi):
  - Comes with new support for the ROMs "robo_a29" and "robo_a30".
  - robo_a34 (Robocop) Initials not handled correctly, making PINemHi crash (happened when you put your initials in the hiscore table)  
  - eballchp, eballch2, ladyluck, motrdome, beatclck, beatclc2  scores not handled correctly after a hiscore reset using vpinmame when hiscore being set is less than 7 digits long
- **Highscore Parsing**: Added highscore support for (thanks to @BostonBuckeye - the list is not done yet):
  - Abra Ca Dabra (Gottlieb 1975)
  - Card Whiz (Gottlieb 1976) 
  - Double-Up (Bally 1970) 
  - Eye of the Tiger (Gottlieb 1978)
  - Golden Arrow (Gottlieb 1977) 
  - Star-Jet (Bally 1963) 
  - T.K.O. (Gottlieb 1979)
  - Jungle Queen (Gottlieb 1977)
  - Miss-O (Williams 1969) 

## Bugfixes

- **Media Recorder**: 
  - Fixed issue that the game selection was kept when the emulator selection was switched. Because of the possible emulator recording mode, only recordings from one emulator type are allowed. 
  - Fixed issue that the "default" VPX emulator was used for emulator recordings instead of the actual VPX emulator selection.
  - Fixed issue existing recordings couldn't be overwritten by new ones. To avoid the file lock, the copy process for the recordings is executed after the emulator/frontend has been closed now.
  - Fixed issue that the media overview was not properly refreshed after a recording was finished.
  - Changed the Media Recorder view to a split view, so that the vertical splitter can be resized. 
- **Backglass Management**: 
  - In frame editor, use the backglass image without the grill when it is hidden.
  - Fixed possibility to activate / deactivate backglass when there is only one, which wasn't possible before.
- **DMD Position Tool**: Fixed issue when rom contains a dot like PiratesLife, positions were not properly saved. The rom name in the dmddevice.ini had to be escaped.
- **Default Emulator Resolving**: More of a technical detail: On several occasions the first VPX emulator was used instead of providing an actual selection or using the one that belongs to the corresponding game. Especially for people running multiple VPX emulators, this may have caused issues.
- **MAME Settings**: Fixed missing displaying of MAME related errors in the MAME sidepanel.
- **Pause Menu**: The navigation glitch that lead to wrong scaled items or selection positioning has been fixed.
- **Drop-In Folder**: Fixed "Open" button for unsupported filetypes (e.g. apng files).
- **PinVol**: Preferences are reloaded before saved, so manually changes won't be overwritten again.
- **Universal Uploader**: 
  - Fixed and improved detection of music packs, e.g. "Beastie Boys" ('cos you gotta fight...!).
  - Fixed extraction of .nv files when archived in .7z or .rar files.

## VPin Mania

The VPin Mania integration has undergone a complete overhaul. The top-level section "VPin Mania" has been removed and migrated into the VPin Mania app. Also, all preferences have been consolidated into a separate toolbar menu (the former "Friends" menu). **If you have connected with friends already, please revisit the privacy settings.** Here is a summary of all the changes:
- The account management and tournaments settings have been moved from the regular settings into the "VPin Mania" preferences.
- The preferences section "My Cabinet" has been duplicated for the VPin Mania preferences to highlight that these changes are reflected on the new website too.
- The tournaments view has a quick-preferences and sidebar toggle button now, so it matches with the tables and competitions view now.
- The tournaments dialog was revisited and adapted to the new iScored integration. Several bugs have been fixed and performance optimizations been added.
- The playlist manager also has a SQL template for tournament tables now.

## VPin Mania Webapp
<img src="https://app.vpin-mania.net/android-icon-144x144.png" width="80" />

**https://app.vpin-mania.net/ has been launched!** You can now browse all your highscores online.
The website replaces the old VPin Mania view from the Studio and has the same feature set the older view and even more!
Check out the YouTube video (https://www.youtube.com/@vpin-studio) for more details.

Note for the highscore admins of vpin-mania: The website comes with an additional admin interface to put highscores on the deny list.

