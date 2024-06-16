## Release Notes 2.21.0

- **VPS Tables**: Added "Status" column to see the VPS mapping status in that view too.
- **VPS Tables**: Added "Updated" column to see the last update of a table in the VPS database.
- **Highscore Card Editor**: Switched from modal dialog templated editing into an embedded mode. The dialog always felt cumbersome and I hope the new design improves the usability.
- **Highscore Card Editor / Default Background**: The "Default Background" section has been moved into a collapsible section that is closed by default. This way, the default backgrounds are only loaded when visible and it does not slow down the editing process, becaues the images are not loaded on table selection anymore.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/cards/cards.png" width="800" />

### Bugfixes

- **Universal Uploader:** Fixed superflous folder creation when extracting Popper assets from zip and rar files.