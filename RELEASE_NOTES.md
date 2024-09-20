## Release Notes 3.6.0

## Changes

- **Data Exporter**: Added a new headless API (without UI) to export data in CSV format from the VPin Studio server. The usage of the API is documented here: https://github.com/syd711/vpin-studio/wiki/Data-Export. Note that this API is a first draft and likely to change, so don't consider it as stable yet in case you build your own tools based on it. Right now, it includes the following endpoints:
  - **Table Data Exporter**: Exports all table metadata of the selected tables.
  - **Highscore Exporter**: Exports all highscores of the selected tables.
  - **Backglass Exporter**: Exports the backglass metadata of the selected .directb2s files.
  - **Table Media Exporter**: Exports the number of media for available for every screen of the selected tables.

- **Highscore Card**: The canvas is centered automatically now  when no left/right margins are set.
- **Highscore Card**: The non-raw highscore list is centered automatically now when no left/right margins are set.
- **Windows VPin Studio Server Tray**: Added option to launch the Studio. The action is also executed on double-click on the tray icon.
- **Studio Client Toolbar**: Added mute/unmute option to system preferences drop-down menu.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/mute-btn.png" width="350" />

- **DMD Images in Backglass Manager**! Added ability to add/replace/remove a full dmd image to a backglass. The DMD image zone also supports drag and drop.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/backglass-manager-dmd-upload.png" width="650" />

- **Client Settings / Windows Network Share**: Added MacOS Support.
- **System / Open Folder & Open File**: Added MacOS Support.
- **Table Overview / VPS Updates**: VPS update indicators are now shown even if the asset is not installed. Previously, the update arrow icon was only shown when there was an existing asset already available. This way, you get notified about additional ALT sound or PUP packages which are often released later on for existing tables.
- **Table Overview / MAME Alias Mapping**: Added auto-sorting on save when then VPMAlias.txt file is edited.

## Bugfixes

- **Table Overview / Backglass Manager**: Missing sync between VPX filename changes and backglass manager.
- **Highscore Card Editor / Performance**: Fixed performance issue that the view caused to load all available games.
- **Highscore Card Editor / Fonts**: Fixed rendering of fonts. This one has been a flow right from the beginning. You finally get what you see when you select a font from the font selector, including the font weight and style. The issue that not all true-type-fonts (ttf files) have been rendered properly has been fixed too.
- **ALT Sound**: .ini files are not ignored anymore when installed via upload.
- **Table Uploads**: Existing .vbs files are deleted on table replacements now.
- **Notifications**: Fixed issue that the notifications were no shown anymore, because the check if VPX is runnning returned always true for some users.
- **Smaller Screen Support**:
  - Fixed all views of the tables section to be useable for 1280x768 pixel.
  - The Studio window has a minimum allowed size of 1280x700 pixel now.
  - Fixed navigation and header toolbar to be useable on screen with smaller resolutions too.
- **PinballX Frontend**: 
  - Fix small issues in Stats and Favorites.
  - Full support of pinballX.ini in UTF-8 or UTF-16.
  