## Release Notes 3.9.2

## Bugfixes / Changes

- **Tables / VPS Tables**: Fixed features based filtering and showing all available table features in the table info of the sidebar.  
- **Tables / Asset Columns**: Fixed missing status refresh.
- **System Manager / Serum**: Disabled Serum integration since this is coming with freezy 2.3 now. Existing serum dll files will be deleted from your VPinMAME directory on freezy update.

---

## Release Notes 3.9.1

## Bugfixes / Changes

- **VPS Tables**: Fixed missing VPS table version resolving for Zen Studios games.
- **VPS Tables**: Fixed VPS auto matching for Zen Studios games.
- **VPS Tables**: Removed (already half broken) table filtering from the days where only VPX games were allowed.
- **VPS Tables**: On the VPS tab, the name of the table inside the sidepanel can be double-clicked in order to select the text.
- **Tables / Uploads**: Fixed table upload dialog so that the option are shown independent of the selected upload option. The filtering is applied to all uploads, so the filter button and asset list was at the wrong location.
- **Tables / Uploads**: Fixed VPS auto-matching. The config flag in the upload dialog has been ignored so that an auto-match was executed for every upload type.
- **Tables / Uploads**: Fixed various issues when cloning tables into subfolders.
- **Tables / PUP Packs**: Fixed the checkbox for enabling/disabling of PUP packs again.
- **Tables / PUP Packs**: Added option to remote edit the available option and additional files that are listed for a PUP pack in the PUP pack section.
- **System Manager / Visual Pinball**: Added button to remote edit the **VPinballX.ini** file.
- **Drop-in Folder**: Fixed **new** star icon again.
- **PINemHi**: Fixed auto-updater which did not update all .exe files, but only the **pinemhi.exe**.
- **Preferences / Discord Bot FAQ**: Updated FAQ with updates screenshots and the concrete permissions info which was only available in the wiki before.

---

## Release Notes 3.9

### Breaking Changes

- **DOF Integration**: The VPin Studio no longer supports older DOF installations that have different configuration folders for 32 and 64 bit. Instead, only the new folder structure with a shared **Config** folder is support. Please update your installation by downloading the latest DOF version from https://github.com/mjrgh/DirectOutput/releases.

### Changes

- **PinballY Frontend**: Added support of the pinballY frontend, like others, possibility to manage tables, favorites, playlists and media, But no media search is available. (http://mjrnet.org/pinscape/PinballY.php)
- **Tables / Uploads**: Re-implemented the upload dialog for media packs and large parts of the backend here. The big disadvantage of the previous version was, that archives with a backglass and frontend media must have been uploaded twice. The new dialog detects all assets types and lets you also select/de-select them for uploading. It is also used as filter/inspection dialog for table archive uploads.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/media-upload.png" width="650" />

- **Tables / Asset Management View**: The screen columns are sortable now.
- **Tables / Table Uploads**: Added additional server setting to keep the modification date of VPX files when they have been uploaded and replaced.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/modification-date.png" width="650" />

- **Tables / Enable or Disable Tables**: In the context menu of tables, a menu item to enable/disable the selected table has been added. This action works as a bulk action and is applied to all selected games.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/status-toggle.png" width="650" />
 
- **Tables / Notes Box**: In notes dialog, the help texts //TODO, //ERROR and //OUTDATED are clickable and insert their text in the comment box.
- **Tables / VPS Entry**: In the VPS tab, the VPS table version links to VPF or VPU has a context menu that can be used to insert a TODO note in the table
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/vps/add-todo.png" width="600" />
- **Tables / Future Pinball**: The support for Future Pinball has been massively improved.
  - Added the launch button to launch Future Pinball tables.
  - Added columns that were previously not shown for Future Pinball.
  - Added the possibility to upload Future Pinball tables.
  - Added ability to match Future Pinball tables against the VPS database. 
  - The VPS versions dropdown has been filtered to show only versions of same emulator type as the selected table.
  - The backglass manager now displays all backglasses coming from all VPX and FP emulators.
- **Preferences / Backglass Server**: Selection of emulator is no more needed, uses instead the B2SServer installation folder.
- **Preferences / System Settings**: Added the new preferences menu **System Settings** where operating relevant settings are configured. The auto-shutdown and shutdown options have been moved to this new preference page.
- **Preferences / System Settings**: Added the new option **Disabled Sticky Keys** to disable the sticky key options of Windows.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/system-settings.png" width="750" />
  
- **Tables**: Added edit and open button to the VPS and backglass manager view for a better navigation back to the table overview.
 
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/navi-buttons.png" width="100" />


### Bugfixes

- **Studio Update/Restart Error** Fixed an issue where the macOS client update process would fail to restart client. Improved macOS upgrade process. **Note: the macOS update process will work correctly AFTER this release update.**
- **System KeyEvent Handling**: Fixed key handling to avoid table deletion on DEL key press in preference panel.
- **Studio Client Exit Confirmations**: The question about launching the frontend on exit is now only shown if the server setting "Launch Frontend on exit" is set to _false_.
- **Tables / PUP Packs**: Fixed duplicate showing of error messages for PUP pack option scripts.
- **Drop-in Folder**: Fixed drop-in folder for **macOS and Linux**. The recursive watching of filesystem changes also works for these operating systems now. 
- **Drop-in Folder**: Fixed menu button being initially visible even though being disabled in the preferences. 
- **VPS Data**: Fixed issue that caused all tutorial URLs being ignored.
- **Highscore Cards**: Fixed missing status updates. When a backglass is uploaded, the default background information for a game is refreshed too.
- **Dialog Positioning**: Dialogs are now always opened on the screen the main Studio Window is located. So only the size of a dialog is restored, not the previous position. 

### VPin Mania

- Added **Delete Table Scores** button to the **Player Statistics** view. This way you can delete e.g. default highscores that have been pushed with your account name. I know this is not an optimal solution yet, but it helps users to at least clean up their own statistics. In the long run, some filtering must be provided to avoid the submission of default scores.


  