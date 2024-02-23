## Release Notes

### Changes

- Updated to Virtual Pinball Backup Manager 2.0. (Installer only). For existing installation, you can update the manager in the preferences.
- Added hint for the backglass server settings that fozzy matching is not supported by the VPin Studio. You can enable it, but the Studio will always only look for exact .directb2s matches.
- Added **Blank Media** button for the **Audio Launch** screen in the asset manager - Not sure if it is useful for other screens too.
- Added **Statistics** tab to the **Table Data Manager** dialog.
- Added **System Shutdown** button in the **Server Settings** to shutdown the cabinet. (Who wants to get up from the sofa for this?)
- Added bulk dismissals: You can now multi-select a set of tables from the overview and dismiss all issues for these (useful in combination with filters).

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/validation-error-bulk.png" width="600" />

- Moved preferences about **Upload and Replace** options into the table upload dialog, because that's where they are used.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/uploads.png" width="600" />

- Added Table Filters.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/filters.png" width="600" />

- Added Backglass Manager: You can finally also access the .directb2s files that are not linked to any table.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/backglass-manager.png" width="600" />


### Bugfixes

- Overlay Key Binding: Added check so that the overlay is only shown, when the Popper game selector is opened.
- Table Data Manager: Fixed miscellaneous issues with the info badges and ROM validations.
- Table Data Manager: Fixed "Fix Version" action.
- Table Data Manager: Added hints which fields will be overwritten on auto-fill.
- Table Data Manager: Fixed error that the the Table Data Manager did close on enter pressed.
- Table Asset Manager: Fixed error that the the Table Asset Manager did close on enter pressed.
- Table Uploads: Fixed "Upload and Replace" option in combination with the "Keep..." settings and fixed related asset renaming.
- Pause Menu: Fixed video author allow list so that an empty list shows all videos available in the pause menu. 
- Added additional "Open" button to the right section of the VPS table overview.
