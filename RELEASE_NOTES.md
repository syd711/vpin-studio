## Release Notes 3.7.0

## Changes

- **Refactored Game Media Access**: For performance optimization, the media access has been refactored widely. As a result, the table (re)load should be noticeable faster. 
- **Table Validators**: Introduced new validator **VPinMAME ROM Validation**. The validator uses the results from the VPinMAME ROM tester to indicate possibly broken ROM files. The new validator is enabled by default.
- **Highscore Card Editor**: Added "Apply to all" button for the font selection which will apply the selected font to all available templates.
- **Table Overview / Reload**: A manual reload in the table overview results in additional cache invalidation of the server. This should pick-up all changes done manually by the user on the cabinet.
- **Table Overview**: Added new column "Launcher" which shows the .exe file that will be used for launching the table.
- **Table Overview / Highscores**: Switched order of highscore graph and highscore card.
- **iScored Integration**: Added the additional tag support for **vps:singlescore**. If you apply this tag to a table in iScored, the VPin Studio will only submit a highscore to iScored if the user has not posted any other score for that table yet - no matter if the new score is higher or not.
- **Pause Menu**: Added additional info about the highscore data (if supported or not).
- **PinVol 2.2**: Added PinVol version 2.2. The new .exe file will automatically be downloaded by the server.
- **VPBM 3.3**: Updated to VPBM 3.3 (finally).
  - Renamed **Table Repository** to **Table Backups**. VPBM does not need additional exports anymore, so this renaming should make the whole usage more intuitive.
  - Removed "Repositories" from the settings. The idea to support multiple sources or targets for backups was not bad, but the implementation was not mature enough. It may be picked up again in the future. 
  - Applied some performance optimizations from VBPM to the Studio integration.
  - Added support for the additional external host ids.
  - Added missing button tooltips and changed labels.
- **Shortcuts**: Several shortcuts have been introduced to improve the accessibility of the Studio. You find an overview in the "Help & Support" section of the preferences.
- **Studio Toolbar Design**: The toolbar design has been streamlined. The reload and filter buttons and search inputs have all the same order and size now. Also search input fields support Strg+F and ESC inputs for a better accessibility. 
- **VPin Studio Launcher**: Add auto discovery of VPin Studio Server instances. For VPin Studio Servers running in the same network, the VPin Studio Launcher will now auto-detect the server instance(s). So no more manual IP lookups!

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/network-discovery.png" width="550" />

- **Discord Maintenance Updates**: You can now select a Discord channel where updates are posted when you upload new tables or replace existing ones on your cabinet. If you share your VPin with a bunch of people (like I do), you can let them know this way if there are new tables available.  

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/discord/table-updates-channel.png" width="550" />

  The channel is configurable for the Discord BOT in the preferences.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/discord/table-updates.png" width="650" />

- **Toolbar**: Added **System Shutdown** menu item to the header toolbar menu. Note that the item is only visible when you work remote.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/header-toolbar.png" width="250" />

## Bugfixes
  
- **Highscore Cards Popups**: Properly centered highscore card when "show on table launch" option is used for highscore cards.
- **Uploads**: Fixed issue with uploading files with filename length smaller than three characters (e.g. "24" - Damn you, Jack!).
- **Table Asset Manager / Playlists**: Fixed asset search for playlists. We somehow forgot that. You can now search the frontend's asset database for media for your playlists, e.g. "music".
- **Highscore Parsing**: Added additional lowercase check for VPReg.stg based highscores ("HELLBOY" problem).
- **Highscore Parsing**: Added test coverage for over 50 new text file and VPReg.stg file based highscores (thanks to @gorgatron) and added the missing parsing support for some of them.
- **System Manager / Visual Pinball**: Fixed issue that the parent folder of the "Tables" folder was used for the VPX system manager component (instead of the actual installation folder). Some users have selected a different folder just for the tables.
- **Table Data Manager**: Fixed dialog sizing issues.
- **Backglass Data Exporter**:
  - Fixed wrong DMD image information (the data was always read from the backglass image).
  - Added additional data from the backglass settings.
- **ALT Sound**: Added missing cache invalidation after deletion of ALT sound packages.
- **PUP Pack Uploads**: Increased maximum file size for uploads to 10GB.
- **Backglass images to media assets**: Ability to extract the images inside the directb2s (Backglass + DMD) and use them as respective table media assets. If a media asset already exists, the image can replace or be appended to the collection.

## VPin Mania 

- **Player Ranking**: Added pagination to the players view, so the list is not limited to 100 anymore.
- **Navigation**: Fixed several navigation issues.
- **Player Statistics Tab**: The view has become a small revamp, showing the players rank now too.