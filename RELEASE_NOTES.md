## Release Notes 4.5.7

## Changes

- **Screenshots**: Added service URL to fetch latest pause menu screenshot (http://localhost:8089/api/v1/recorder/screenshot/latest)
- **Table Filtering**: Fixed issue that the media recorder used the filter settings from the table overview.
- **Table Validators**: Introduced new table validator which checks if the table has a backglass, but the backglasses are disabled in the VPX settings. (I assume it is an edge case, but can be annoying to trouble-shoot).
- **Pause Menu**: 
  - Fixed screen configuration that can be used for showing the tutorials video (@kongedam It is finally there!).
  - Fixed mute on pause: If the system was already muted, the pause menu does not unmute on exit anymore.
  - Added rotation option for the tutorials screen.
  - Added margin options to position the tutorials screen pixel perfect.
  - Added scaling option to adjust the size of the pause menu.
  - Added "Apron Mode" setting which hide the navigation from the pause menu in case you have a screen too small to display the full pause menu.
  - Added offset settings to fix the positioning of the pause menu (might be required for the apron mode).
- **DropIn Menu**: The deletion of an asset is proposed for all asset types now when installed from the drop-in menu (not only for backglasses).
- **VPS Entries**: Generalized the display of comments across assets in Virtual-Pinball-Spreadsheet views.
- **Dialogs**: Fixed issue that sometimes the contents of dialogs were cropped.
- **Preferences Menu**: The **Mute System** entry detects the mute state of the cabinet and reflects it by its toggle state.
- **Preferences**: Added the new entry **DMD Device** which lets you configure some settings for the file DMDDevice.ini.

---

## Release Notes 4.5.6

## Changes

- Fixed update check for several files that haven't been properly updated (e.g. the .pupgames files to import game data for non VPX emulators.)
- Fixed the server shutdown which was blocked (again) and therefore **updates did not run properly**.

**If you face the issue that the server is not updating, please follow these steps - again :-/**

**Download https://github.com/syd711/vpin-studio/releases/download/4.5.6/VPin-Studio-Server.zip and follow these instructions:**
**https://github.com/syd711/vpin-studio/wiki#manual-updates**


---

## Release Notes 4.5.5

## Changes

- Fixed rendering issue in the WOVP pause menu item (again).
- Fixed layout issues with the WOVP scores on the dashboard.
- Added persistent caching for the WOVP flag images to speed up the loading process. 
- Fixed WOVP multi-player mode for the pause menu that got broken during the last revamp.
- Added auto-scrolling to the WOVP players score in the competitions dashboard section.

---

## Release Notes 4.5.4

## Changes

- Fixed rendering issue in the WOVP pause menu item.

## Release Notes 4.5.3

## Changes

- Media Recorder: Removed hint that the GL version of VPX can't be used for recordings.
- Pause Menu: Reduced delay of the menu showing up.
- Pause Menu: Revamped the WOVP score submit entry which shows the top scores now too.
- DMD Positioner: Fixed detection of the correct backglass monitor.

--- 


## Release Notes 4.5.2

### Unfortunately the previous attempt to fix the updater issue was not resolved. But the issue has been fixed now. If you have a version larger than 4.4.6 installed, you need to install this(!) release manually.

**Download https://github.com/syd711/vpin-studio/releases/download/4.5.2/VPin-Studio-Server.zip and follow these instructions:**
**https://github.com/syd711/vpin-studio/wiki#manual-updates**

## Changes 

- Pause Menu: Added quick-fix for 4k/21:9 resolutions.
- Added more logging to resolve broken DMD positioning issue (not fixed yet!).

---

## Release Notes 4.5.1

## Changes 

- Designer: Improved the centered/right calculation for texts when drawn on the canvas. (_Note that this calculation is only a best approach, depends on the font size and not pixel perfect!_)
- Pause Menu: Fixed VPin Mania menu entry settings that vanished. Thanks @JRVirtual34 for helping here!
- WOVP Integration: Added missing highscore backup + reset on automatic competition change.

---


## Release Notes 4.5.0

Note that there are breaking changes in the pause menu preferences. Please revisit these settings!

## Updater Issues

In case the updater stalls and the server update (happens because of issues introduced with version 4.4.5), please replace the server .exe file manually:
https://github.com/syd711/vpin-studio/releases/download/4.5.0/VPin-Studio-Server.zip

Additional instructions regarding manual updates can be found here:
https://github.com/syd711/vpin-studio/wiki#manual-updates


## Changes

- **World Of Virtual Pinball Competitions**
  - The VPin Studio has supports **World Of Virtual Pinball** competitions now (https://worldofvirtualpinball.com/en).
  - A new tab **WOVP Competitions** has been added to the competition section. Note that the tab is only visible after you have added a valid API key on the corresponding preferences section. More details are described on YouTube (https://www.youtube.com/watch?v=a2phlDiCSEY).
   
- **Pause Menu**
  - The pause menu supports **desktop environments** too (there are layout issues for HD resolutions not fixed yet).
  - The **tutorial** video can be shown on a separate frontend screen now (again).
  - The **P key** configuration is now optional. You will lose the "one second"/configurable delay on resume, otherwise the focus management should ensure that the game will continue once the pause menu is closed.
  - Added menu entry for the score submission of **World Of Virtual Pinball** competitions.

- **VPin Mania Tournaments**
  - The feature has been removed from the Studio. The focus of the tool will remain the table management and smaller competitions. This will include 3rd party systems and improvements regarding the existing Discord based solutions.


## Bugfixes

- **Discord Table Subscriptions**: Solved the channel limit issue for table subscriptions. Discord allows only 50 channels for every category and up to 500 channels in total. The VPin Studio bot can now automatically create new categories and create additional subscription channels if you have already reached the first 50 entries. Note that you bot needs the **"Manage Channel" permission** for this. So you might need to re-add the bot to your server with more permissions that before. (For help, see: https://github.com/syd711/vpin-studio/wiki/Discord-Integration#discord-permissions-for-adding-a-bot-to-your-server)
- **Discord Bot Settings**: Fixed issues with the issues checker and improved validation.