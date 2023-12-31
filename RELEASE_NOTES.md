## Release Notes

### Changes

- Table Upload Dialog: Fixed radio checkboxes in the table upload dialog so that the full text can be clicked now.
- Highscore Card Settings: Fixed a critical error that when the highscore card settings invoked a "no content" error was shown (a follow up error from migrating the card settings from a properties file to the database). 
- Overlay and Highscore Card Overlay: The cursor is hidden now when overlays are shown.
- Icons: Replaced ugly database-edit icons with a pencil icon, indicating general editing of the selected item.
- Icons: Unified icons by using the same size everywhere (there was a mixture of three different sizes all over the UI).
- Icons: Unified different reload icons.
- Icons: Added custom icons for the "Asset Manager" and the "Popper Table Edit" dialogs.
- VPS Section: Fixed "download" button enablement that was not updated for new table selections.
- VPS Section: Table versions without a format value are also shown in the version selector now.
- VPS Section: Fixed missing table version restting for tables that did have a VPS table selected.
- VPS Section: Added ROM name as additional lookup criteria when the automatic lookup for the VPS entry is triggered.