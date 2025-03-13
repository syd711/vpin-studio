## Release Notes 3.13.2

## Changes

- **Screen Resolving**: Fixed issues reading the resolutions from Visual Pinball X which resulted in broken tabs for the media recorder, creating screenshots and for the screens section in the system manager.
- **Screen Resolving**: Fixed calculation of the Playfield screen which is determined through the Visual Pinball X settings since 3.13 (and not through the Popper playfield location anymore). 
- **Screen Resolving**: Fixed parsing issues for the _screenres.txt_. 
- **System Manager / Screens**: Removed filtering for PinUP Popper where monitors left to the primary one have been ignored.
- **System Manager / Emulators**: Added validator that checks if there are matching games in the games folder.
- **Backglass Manager**: Fixed issues handling multiple backglasses when located in a separate folder.
- **Tables / Table Patching**: Fixed possible error during patching.
- **Webhooks**: Fixed issue that highscore update events fired **after** they result has been recorded.
- **Webhooks**: Added DELETE request endpoint to delete a webhook set.
- **Highscore Parsing**: Added support for "Pool Sharks".
- **macOS Clients**: Fixed location of image cache.
- **PinVol Service**: Fixed new system mute option that did not work in combination with the PinVol auto-start.

---

## Release Notes 3.13.1

## Changes

- **Tables / Table Overview**: Disabled playlist icon previews again, because of a really bad rendering performance which blocks the whole table usage.

---

## Release Notes 3.13.0

## Changes

- **Tables / Backglass Manager**: Added support for managing multiple backglasses for one table. Check out our YouTube channel for more details.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/backglass-manager-versions.png" width="700" />
  
- **Tables / Table Overview**: Added new column **Rating**. You can directly rate the game inside the column without opening the data manager.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/rating.png" width="100" />
  
- **Tables / VPS Tables**: Added "Comment" column and comment input field. You can enter personal comments there, e.g. to mark the table for downloading or that you tried that table but did not like it. This comment is solely stored for you and not submitted to the VP-Spreadsheet database.
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/vps-comments.png" width="700" />
  
- **System Manager / Screens**: The **screens view is a new experimental view** and should show you all screens of your VPin, ordered by the different software components. It should help you to troubleshoot screen position issues. 
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/components/screens-manager.png" width="700" />

- **System Manager / Emulators**: Added emulator management. The manager works for PinballX and PinUP Popper. For PinUP Popper you can add new emulators too. The view comes with some validators too which do some folder checks for the corresponding values.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/components/emulator-manager.png" width="700" />

- **Tables / Overview**: Added preview for playlist icons. The actual icons are used for playlist now. The tooltips for those provide a detail view for the icon.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/playlist-icon.png" width="400" />

- **Tables / Patching**: Several improvements have been made:
  - Added "Patch Version" field to the data manager for those who want to track this separately. **Note that by default, the value is written into the field "Custom5" for PinUP Popper users**. You can disable this in the server settings.
  - Added "Patch Version" field to the patch upload dialog.
  - Added "Patch Version" column to the table overview. Note that this column is **disabled by default** and must be enabled in the client settings.
- **Preferences / Controller Setup**: Added input option to generate **screenshots**. This allows you to generate screenshots in-game. The feature might come in handy for competitions that require screenshot references. For more details see: https://github.com/syd711/vpin-studio/wiki/Taking-Screenshots
- **Preferences / PinVOL / Volume**: Added option to mute the cabinet on startup.
- **Preferences / Webhooks**: In order to support the development of https://github.com/mikedmor/ArcadeScore, we have added webhooks that can be used to integrate 3rd party systems into VPin Studio. A detailled documentation can be found here: https://github.com/syd711/vpin-studio/wiki/Webhooks. Note that this API is not final yet as the development is still in progress. We will give updates about this soon.

  <img src="https://raw.githubusercontent.com/mikedmor/ArcadeScore/refs/heads/main/app/static/images/icons/arcadescore_256.png" width="150" />

## Bugfixes

- **Tables / Table Overview**: Added missing disabled colors on new columns (playlists, rating, ...).
- **Tables / Invalid Score Filter**: Fixed "Invalid Score Configuration" filter that did show valid tables before.
- **Tables / Table Asset Manager**: Fixed various issues for the Asset Manager when opened for playlists.
- **System Manager**: Aligned layout of all "Open Folder" buttons.
- **Players**: Fixed "Visible For Friends" checkbox that must be disabled, when the player is not registered.
- **Tables / Uploads**: Fixed detection of bundles ALT color serum files.
- **Tables / ALT Color**: Fixed "Open Folder" action.
- **DOF / Config Sync**: Fixed DOF sync job that corrupted config files when DOF configuration is smaller than installed one.
- **Media Recorder**: Changed ffmpeg preset from **fast** to **ultrafast** to avoid stuttering. This matches with the recording parameters of PinUP Popper.
- **iScored**: Fixed iScored integration according to their new API.

## VPin Mania

- Fixed additional registration problems and duplicate account generations.