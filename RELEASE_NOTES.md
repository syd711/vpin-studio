## Release Notes 3.6.0

## Changes

- **Highscore Card**: Added option to center the canvas by the x-coordinate.
- **Highscore Card**: The non-raw highscore list is centered now automatically when no left/right margins are set.
- **Studio Client Toolbar**: Added mute/unmute option to system preferences drop-down menu.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/mute-btn.png" width="350" />


## Bugfixes

- **Highscore Card Editor / Performance**: Fixed performance issue that the view caused to load all available games.
- **Highscore Card Editor / Fonts**: Fixed rendering of fonts. This one has been a flow right from the beginning. You finally get what you see when you select a font from the font selector, including the font weight and style. The issue that not all true-type-fonts (ttf files) have been rendered properly has been fixed too.
- **Table Uploads**: Deleting existing .vbs files on table replacements.
- **Smaller Screen Support**:
  - Fixed all views of the tables section to be useable for 1280x768.
  - The Studio window now has a minimum with of 1280x700 pixel.
  - Fixed navigation and header toolbar to be useable on smaller screen resolutions too.