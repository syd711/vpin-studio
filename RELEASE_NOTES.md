## Release Notes 3.13.0

## Changes

- **Tables / Table Overview**: Added new column **Rating**. You can directly rate the game inside the column without opening the data manager.
- **Tables / VPS Tables**: Added "Comment" column and comment input field. You can enter personal comments there, e.g. to mark the table for downloading or that you tried that table but did not like it. This comment is solely stored for you and not submitted to the VP-Spreadsheet database.
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/vps-comments.png" width="700" />
  
- **System Manager / Screens**: The **screens view is a new experimental view** and should show you all screens of your VPin, ordered by the different software components. It should help you to troubleshoot screen position issues. 
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/components/screens-manager.png" width="700" />

- **System Manager / Emulators**: Added emulator management. The manager works for PinballX and PinUP Popper. For PinUP Popper you can add new emulators too.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/components/emulator-manager.png" width="700" />

- **Tables / Patching**:
  - Added "Patch Version" field to the data manager for those who want to track this separately. **Note that by default, the value is written into the field "Custom5" for PinUP Popper users**. You can disable this in the server settings.
  - Added "Patch Version" field to the patch upload dialog.
  - Added "Patch Version" column to the table overview. Note that this column is **disabled by default** and must be enabled in the client settings.
- **Preferences / Webhooks**:

## Bugfixes

- **Tables / Table Overview**: Added missing disabled colors on new columns (playlists, rating, ...).
- **Tables / Invalid Score Filter**: Fixed "Invalid Score Configuration" filter that did show valid tables before.
- **Tables / Table Asset Manager**: Fixed various issues for the Asset Manager when opened for playlists.
- **System Manager**: Aligned layout of all "Open Folder" buttons.
- **Players**: Fixed "Visible For Friends" checkbox that must be disabled, when the player is not registered.