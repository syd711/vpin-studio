## Release Notes 3.7.0

## Changes

- **Table Validators**: Introduced new validator **VPinMAME ROM Validation**. The validator uses the results from the VPinMAME ROM tester to indicate possibly broken ROM files. The new validator is enabled by default.
- **Highscore Card Editor**: Added "Apply to all" button for the font selection which will apply the selected font to all available templates.
- **Table Overview / Reload**: A manual reload in the table overview results in full cache invalidation of the server. This should pick-up all changes done manually by the user on the cabinet.
- **Table Overview**: Added new column "Launcher" which shows the .exe file that will be used for launching the table.
- **Table Overview / Highscores**: Switched order of highscore graph and highscore card.
- **iScored Integration**: Added the additional tag support for **vps:singlescore**. If you apply this tag to a table in iScored, the VPin Studio will only submit a highscore to iScored if the user has not posted any other score for that table yet - no matter if the new score is higher or not.
- **Pause Menu**: Added additional info about the highscore data (if supported or not).
- **VPBM 3.3**: Updated to VPBM 3.3, including some performance optimizations and support of multiple external host ids.
- **Shortcuts**: Several shortcuts have been introduced to improve the accessibility of the Studio. You find an overview of all available shortcuts in the "Help & Support" section of the preferences. 
- **VPin Studio Launcher**: Add auto discovery of VPin Studio Server instances. For VPin Studio Servers running in the same network, the VPin Studio Launcher will now auto-detect the server instance(s). So no more manual IP lookups!

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/network-discovery.png" width="550" />

- **Discord Maintenance Updates**: You can now select a Discord channel where updates are posted when you upload new tables or replace existing ones. If you share your VPin with a bunch of people, you can let them know this way if there are new tables available.  

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/discord/table-updates-channel.png" width="550" />

  The channel is configurable for the Discord BOT in the preferences.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/discord/table-updates.png" width="650" />

## Bugfixes
  
- **Highscore Cards Popups**: Properly centered highscore card when "show on table launch" option is used for highscore cards.
- **Uploader**: Fixed issue with uploading files with filename length smaller than three characters (e.g. "24" - Damn you, Jack!).
- **Table Asset Manager / Playlists**: Fixed asset search for playlists. We somehow forgot that. You can now search the frontend's asset database for media for your playlists, e.g. "music".
- **Highscore Parsing**: Added additional lowercase check for VPReg.stg based highscores ("HELLBOY" problem).