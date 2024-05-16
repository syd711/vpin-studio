## 2.18.5

- **Pause Menu Preferences**: Added cabinet **button recorder** for those who are having troubles opening or navigating the pause menu. Note that this is kind of a blind fix, since I'm currently not able to test this with a real controller board.
- **Table Management / Dismiss All**: Fixed critical performance issue when dismissing validation errors of multiple tables.
- **System Manager/ Backglass Server**: Fixed usage of the actual backglass server installation folder for the system actions (green buttons).
- **System Manager/ Backglass Server**: Fixed preferences page when no emulator with a **B2STableSettings.xml** could be resolved.
- **System Manager/ Backglass Server**: Fixed resolving of the **B2STableSettings.xml** that is not necessarily located in the **Tables** folder anymore.
- **Discord Messages**: Added proper number formatting to all Discord score messages, including images. (Let me know when I've missed some, this was a quick shot.).
- **Highscore Cards**: Added proper number formatting for formatted score list so that all values are right-aligned.
- **Table Management / Table Asset Manager**: Fixed initial selection when opened from sidebar media button.
- **Table Management / Asset Management**: Fixed detection of media files, caused by a superflous camel case check.
- **Logging**: Improved very chatty pinemhi error handling.
- **Window Manager**: Fixed saving the position when positioned left to the main screen.

## 2.18.4

- **Pause Menu**: In addition to the Popper navigation keys, the VPX key settings are loaded and used as an alternative binding. Note that some key combinations still might not work because I had to map some DirectX key mapping of VPX to Java key events. Also, I added another alternative key binding: arrow keys, enter and "1" for are also useable when navigating the menu.
- **Video Player**: The actual media size is used to determine the orientation of the video now. I hope this will fix the orientation problem for portrait users.
- **Main Toolbar**: Fixed styling issues of "Jobs" button.
- **Icons**: Replaced some of the remaining non-material UI icons (e.g. the preferences icon).

## 2.18.3

- **Highscore Cards**: Re-enabled the custom color picker for the card designer. I've hidden the link to this picker before because it has some focus issues, but it's better to have a bad solution for custom colors than nothing.
- **Highscore Cards**: Converted template name in the overview list into buttons so that templates can be edited directly.
- **Table Management / PinUP Popper Media Section**: Fixed gear icon button so that it navigates to the media settings.
- **Table Management / Asset Manager**: Changed asset preview so that a regular selection will preview the Popper asset, no double-clicking required anymore.
- **Table Management / Asset Manager Dialog**: Fixed previous/next buttons and table selection that broke when introducing the asset manager view.
- **Pause Menu**: Fixed configuration issues and improved error handling.
- **Asset Management**: Fixed preview for playfield and loading videos when running in portrait mode.
- **Table Data Manager**: Fixed "Fix Version" button + action.
- **Table Asset Management**: Forcing audio out to play only one audio file at a time.
- **Table Asset Management**: Improved text of the validator message that is shown when assets do not match with the screen validator configuration.
- **Window Positioning**: Fixed wrong window positioning when opened after the Studio has been closed being minimized.

## 2.18.2

- **Server Settings**: Added option to keep the imported/exported .vbs files when script editing is started. The option is enabled by default.
- **Table Management**: Fixed context menu action "Reset VPS Updates".
- **Table Management / Popper Media View**: Added delete button to the on-hover actions of the "PinUP Popper Media" section.
- **Highscore Cards**: Fixed error in card generator that for non-raw highscore cards all scores have been rendered. Usually only 3x scores have been displayed here. I've added a configurable value now so you can select this on your own.
- **Highscore Cards**: Fixed missing refresh of the "Popper Background" tab panel after applying templates.
- **Table Management / VPin MAME View**: Fixed possible breaking of settings when double-clicking the reload the button.
- **Popper Table/Launch Events**: Fixed listening to the table start and exit events so that these are properly triggered for tables that are located in subfolders too. Highscore cards for these tables have not been updated on exit.
- **VPBM**: Added latest updates (including orphaned content finder) and added entry to context menu in table overview.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/orphaned-children.png" width="500" />

 
## 2.18.1

- Fixed critical error caused by the new asset validation and files with unknown mime types. 


## Release Notes 2.18.0

### Changes

- **Clients for Linux and OSX (ARM)**: New zip files are available now in the release artifacts. Make sure to read the **installation-notes.txt** inside the zip files. I managed to modify the updater for the systems too. Note that I can only offer limited support regarding OSX! 
- **Preferences**: Added **Help & Support** section, including a download option which zips and downloads all log files for **ticket submissions**.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/help.png" width="500" />
  
- **Preferences**: Added **Network Settings** to the UI preferences. You can input the **network share** of your VPin there. After restarting the Studio, you can access the remote folders via the yellow-ish folder buttons that have only been available when working on the cabinet before.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/network-share.png" width="700" />

- **Table Data Manager**: The Popper field "Notes" remains untouched from the auto-fill function now.
- **Script Editing**: Use your favorite development IDE for editing scripts! You can edit VB script now with any editor that is linked to the file suffix **.vbs**. Select the default program for .vbs files on your operating system and Studio will open and save(!) the changed VB sources automatically back to the remote VPX file.
- **VPS Tables**: Added list of all available table versions, including an indicator of your installed version.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/vps/vps-marker.png" width="700" />

- **VPS Mapping**: Added reset button in the Table Data Manager and the VPS table section to unlink a VPS mapping from a table.
- **Highscore Cards**: Added **Background Canvas** option to the template designer. This additional background square can be used to improve the readability of the scores. 
- **Highscore Cards**: Added **Margins** option to the template designer. The padding value will be used as additional separator between the initials and the score value.
- **Point of View (POV) Section**: Removed **Export** button. I think it's time to let go here.
- **Pause Menu**: After taking some turns here I once again changed the Pause Menu preferences. You "simply" choose the design and the target screen for the tutorial videos. This way, you can decide on your own, where the YouTube videos should be played. Using the backglass here as default didn't work out so well, since it wasn't always possible to bring the Chrome browser in front of the current PUP video. This may not be the end of the road, but I hope it works at least better than before.
- **Backglass Manager**: Added download button for the backglass and DMD image.
- **Table Overview**: **VPin Studio supports all emulators now**. If non-VPX emulators are selected, the view will be reduced to the supported functions.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/emulator-combo.png" width="500" />

- **Table Overview**: The **Emulator Column** is now always shown, its visibility is no longer configurable.
- **Table Overview Tabs**: The **VPX Tables** has been renamed to **All Tables** and the VPX icon has been removed again, since all emulators are supported now.
- **Table Overview Toolbar De-Cluttering**: With the new emulator combo, it was about time to clean up the main toolbar:
  - The **Backglass Manager** button has been moved into the context menu.
  - The **VPS** buttons have been moved into the context menu.
  - The **Export** have been moved into the context menu.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/toolbar.png" width="700" />

- **Asset Manager**: The **Asset Management** is now available as an additional view. The existing **Asset Management** dialog is still available, but you can toggle the table management into a separate view which only **shows you the assets of all tables and all screens at once**. 

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/asset-management-view.png" width="700" />

- **Screen Validators**: The preference page has been re-vamped, including additional preferences. This way, you can exactly define what screens should have which media.
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/screen-validators.png" width="500" />

- **Screen Preferences**: This section has been removed in order to streamline the table validators. The section was only used to pre-select (or disable) the corresponding asset validators which felt too cumbersome.
- **Overlay & Card Settings:** Added **Input Debounce** configuration that should avoid possible duplicated input events (experimental).

### Bugfixes

- **Table Overview Refresh**: Fixed issue that all tables have been reloaded on changes. The "single table refresh" broke when the filters had been introduced and I didn't notice this before. 
- **Key Events**: Fixed issue with empty overlay key assignment which led to flood of log entries.
- **Table Overview**: Fixed context menu item "Upload Backglass".
- **Color Picker**: Disabled **custom** color picking, since the dialog seems to be broken.
- **Studio Window**: Added (fixed) Studio version in the window title.
- **PUP Pack Column**: Fixed wrong PUP pack indicator. The indicator has been shown for table that declare a **pGameName** value, but it has not been checked if the PUP pack actually exist.
- **Table Data Manager**: Removed duplicate entries from the "Alternative Launcher" combo-box. The list is now solely filled with the available .exe files of the default VPX emulator.
- **Asset Management**: Improved search suggestions so that it is more likely to get results from the search based on the table name.
- **Backglass Manager**: Adapted layout a bit so that the dialog fits for HD resolutions.