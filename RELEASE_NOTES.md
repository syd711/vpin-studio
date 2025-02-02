## Release Notes 3.13.0

## Changes

- **Tables / Table Overview**: Added new column **Rating**. You can directly rate the game inside the column without opening the data manager.
- **Tables / Patching**: 
  - Added "Patch Version" field to the data manager for those who want to track this separately. **Note that by default, the value is written into the field "Custom5" for PinUP Popper users**. You can disable this in the server settings.
  - Added "Patch Version" field to the patch upload dialog.
  - Added "Patch Version" column to the table overview. Note that this column is disabled by default and must be enabled in the client settings.
- **Tables / VPS Tables**: Added "Comment" column and comment input field. You can enter personal comments there, e.g. to mark the table for downloading or that you tried that table but did not like it. This comment is solely stored for you and not submitted to the VP-Spreadsheet database.


## Bugfixes

- **Tables / Invalid Score Filter**: Fixed "Invalid Score Configuration" filter that did show valid tables before.