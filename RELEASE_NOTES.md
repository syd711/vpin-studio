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
