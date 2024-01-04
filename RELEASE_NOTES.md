## Release Notes

### Changes

- Added server-site VPS version change listener, that is triggered when a new VPS version is assigned to a table. As a result, the updated VPS data is automatically written to PinUP Popper **if** the setting for this is enabled. 
- Icons: Replaced ugly database-edit icons with a pencil icon, indicating general editing of the selected item.
- Icons: Unified icons by using the same size and icon-font everywhere (there is a mixture of three different sizes and three different icon-fonts all over the UI, I try to unify these).
- Icons: Added custom icons for the "Asset Manager" and the "Table Data" dialogs.
- Table Overview: Double-clicking on a table now opens the "Table Data" dialog (why didn't I came up earlier with that?).
- Preferences: Rename "Service" section to "Server Settings" (starting with some server flags there).
- Preferences (Server Settings): Added "Auto apply to Popper..." setting to toggle the data push from VPS to PinUP Popper games.
- Preferences (Server Settings): Added "Keep VPX Filenames..." (explanation text is there). 
- Preferences (Server Settings): Added "Keep display names..." (explanation text is there).

### Bugfixes

- Highscore Card Settings: Fixed a **critical error** that when the highscore card settings invoked a "no content" error was shown (a follow up error from migrating the card settings from a properties file to the database).
- Table Data Auto-Fill: Fixed errorneous or missing filling of fields "tags" and "authors", added filling "notes" fields with the VPS version comment. 
- Table Upload Dialog: Fixed radio checkboxes in the table upload dialog so that the full text can be clicked now. 
- Overlay and Highscore Card Overlay: The cursor is hidden now when overlays are shown.
- VPS Section: Fixed "download" button enablement that was not updated for new table selections.
- VPS Section: Table versions without a format value are also shown in the version selector now.
- VPS Section: Fixed missing table version reset for tables that did have a VPS table selected.
- VPS Section: Added ROM name as additional lookup criteria when the automatic lookup for the VPS entry is triggered.
- VPS Section: Improved styling of version combo-box list by adding altering background colors and selection colors.
- VPS Section: Fixed filter checkbox that resetted the table version.
- Table Editing: Editing the Pinup Popper table data via Studio will now also update the "DateUpdated" column in the database (this is relevant for dynamic playlists that use this field).
- Fixed font color of textareas.

### Known Issues

- There are still some updater/installer issues not tackled yet. More about this in future updates.
- There were request to rotate and position the highscore overlay.
- ROM Aliases: the logic/UI for the alias stuff is too confusing and needs an overhaul.
