## Release Notes 3.9

- **Tables / Asset Management View**: The screen columns are sortable now.
- **Tables / Table Uploads**: Added additional server setting to keep the modification date of VPX files when they have been uploaded and replaced.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/modification-date.png" width="650" />

- **Tables / Notes box**: In notes dialog, the help texts //TODO, //ERROR and //OUTDATED are clickable and insert their text in the comment box

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/vps/add-todo.png" width="750" />
  
- **Tables / VPS Entry**: In the VPS tab, the VPS table version links to VPF or VPU has a context menu that can be used to insert a TODO note in the table
- **Tables / Future Pinball Support**: The support for Future Pinball has been massively improved. We have added the launch option, additional columns and the uploader can know also deal with FP bundles.
- **Tables / Future Pinball VPS match**: Added ability to match future pinball tables against the VPS database. Also VPS versions dropdown has been filtered to show only versions of same emulator type as the selected table. 

- **Backglass Manager / Multiple Emulators**: The backglass manager now displays all backglasses coming from all VPX and FP emulators.

- **PinballY frontend**: Added support of the pinballY frontend, like others, possibility to manage tables, favorites, playlists and media, But no media search is available. 

- **Preferences / Backglass Server**: Selection of emulator is no more needed, uses instead the B2SServer installation folder.
- **Preferences / System Settings**: Added the new preferences menu **System Settings** where operating relevant settings are configured. The auto-shutdown and shutdown options have been moved to this new preference page.
- **Preferences / System Settings**: Added the new option **Disabled Sticky Keys** to disable the sticky key options of Windows.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/system-settings.png" width="750" />


## Bugfixes

- Changed **System keyEvent handling** to avoid table deletion on DEL key press in preference panel.

## VPin Mania

- Added **Delete Table Scores** button to the **Player Statistics** view. This way you can delete e.g. default highscores that have been pushed with your account name. I know this is not an optimal solution yet, but it helps users to at least clean up their own statistics. In the long run, some filtering must be provided to avoid the submission of default scores.


  
