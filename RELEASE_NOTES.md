## Release Notes 3.7.2

## Bugfixes

- **VPin Studio Server Tray**: Fixed the "Launch Studio" action from the context menu of the tray icon.
- **Table Parser**: Fixed issue in the VPX table analyzer which caused the missing resolving for highscore textfile names.
- **Media Cache**: For generating highscore cards and backglass previews, the Studio extracts the data from backglasses and other sources. Unfortunately the generation of these assets were stored with non-unique names, so this index must be regenerated. You can do that manually in the server settings but the Studio will also regenerate them on the fly, e.g. when a highscore card is rendered.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/media-cache.png" width="650" />
