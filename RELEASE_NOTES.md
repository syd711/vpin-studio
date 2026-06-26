## Release Notes 5.1.1
- Removed 2 older dependencies whose functions could be replaced with other dependencies already included.

### Changes

- **Pause Menu/Screenshot Generation**
  - Moved option to always generate a screenshot when the pause menu is shown from the "Integration Settings" to the "Pause Menu" settings.
  - Fixed playfield rotation for screenshots.
  - The key event based screenshot creation (see Controller Inputs) does now also check which monitors are enabled for screenshots.
- **Updater**
  - Added additional updater fixes to force the server update shutdown. In case you have faced issues, you need to perform a manual update: Extract the VPin-Studio-Server.zip and double-click the server's .exe file (or reboot the cab). See also: https://github.com/syd711/vpin-studio/wiki#manual-updates
- **Table Overview**
  - Added filter option for non-backed-up tables.

---


## Release Notes 5.1.0

### Changes

- **Pause Menu**:
    - Added new "Todo List" menu item. Here you can quickly tag issues you experience with the table so you don't forget them after playing.
    - Changed view mode settings which simply differs now between Cabinet, Desktop and Apron mode view, selectable via radio group.
    - The view settings with scaling, top and left margins are now applied for all view modes. Most likely this will only be used for apron screens and non 16:9 screens.
    - The design when used in desktop mode can now be improved by downscaling and limiting the number of visible menu items.
    - Fixed the "Mania Scores" menu item which shows highscores now again.
    - Fixed loading of wheel images for the "Mania Scores" view which was using an outdated database.
    - Fixed wrong initialization of the "Margin Left" view setting.
    - Added option to disable rule cards.
    - Added option to disable info cards.
    - Added full-screen option for tutorial videos.
    - Improved startup speed for tutorial videos.
    - The tester has been integrated into the preferences panel to have less clicks for testing.
    - No server restart is required anymore to apply all changes.
- **Table Management**: Added backglass selector to the backglass sidebar.
- **Preferences Menu**: Added cabinet selector to the preferences drop-down menu, so that you can immediately switch to other cabinets.
- **Designer**: Fixed color value parsing issues that broke the UI.
- **Script Details Section**: Added the information about the PUP pack name that was scanned by the Studio.
- **PinUP Popper Media Search**:
  - Added caching.
  - Fixed URLs with "@2a" segments.
- **DMD Info Section**: 
  - Improved detection of FlexDMD folders.
  - Fixed DMD bundle size calculation.
- **Misc**:
  - Fixed .rar extraction for PUP packs.
