## Release Notes 3.12.3

## Changes

- **Tables / Backglasses**:  Support Alias Mapping for DMD Positioning.
- **VPin Mania / Online Status**: Again, improved check to set the online status back to "offline"/"online".
- **Tables / Highscores**: Added button to open the table on VPin Mania.
- **Tables / Locale Settings:** Removed the hard-coded "English" locale from the VPin Studio client. I don't why I've set this once, but depending on the country, the dot is shown as number separator again.
- **Competitions / iScored**: Added missing highscore reset for iScored subscriptions.
- **Tables / Media Recorder**: Added information how many videos have actually been recorded to the status and summary messages.
- **Pause Menu**: Fixed issue that the pause menu did not hide on table exit.
- **Pause Menu / Tutorial Videos**: Switched from **Chrome** to **Edge**. By default, **Microsoft Edge** will now be used for the tutorial playback via YouTube. This way, users don't have to install additional software.
- **Tables / Validations**: Switched back to a fix height for the validation error box to avoid accidental double-clicking the wrong table (avoids the table jumping when switching between valid and invalid tables).
- **Tables / Table Data Manager**: Switched the order of **Auto-Naming** and **VPS Entry** panels on the first tab. The renaming panel belongs closer to the actual fields it changes.
- **Tables / Launch Actions** When a table launched via Studio (through VPX.exe selection), the game status is set now. This allows the pause menu and in-game recording to work without the need to launch a game through the frontend.
- **Tables / PUP Pack Section**: Removed the PUP pack tweaker tool again as it is writing invalid configs for PinUP Popper.


---

## Release Notes 3.12.2

## Changes

- **VPin Studio Server**: Fixed critical possible deadlock that would block all tables from being read. 
- **Tables / Playlist Manager**: Improved error handling for media assets.
- **VPin Mania / Deny Lists**: The initials are evaluated for denied score anymore, so scores are only filtered by their value. Although not relevant anymore, the initials of the denied score are still shown with a hint.
- **VPin Mania / Online Status**: Added additional check to set the online status back to "offline".
- **Tables / Uploads**: Added "wav" as additional audio format for music packs and alt sound.

---


## Release Notes 3.12.1

## Changes

- **Tables / Playlist Manager**: Fixed several tooltips.
- **Tables / Table Data Manager**: Fixed broken auto-renaming where the filename was duplicated.
- **Tables / Playlist Manager**: Added missing ordering of playlist via drag and drop (PinUP Popper only).
- **Tables / Pin Vol Settings**: Fixed mixed up front and rear exciter inputs.  
- **VPin Mania / Invites**: Fixed some display issues with invites.

---

## Release Notes 3.12.0

## Changes

- **Tables / Playlist Manager** Added playlist manager to create, edit and delete playlists. Note that the interaction concept differs from Popper here, as you can only remove tables from playlists there and use the table overview to add tables instead. This feature comes with a bunch of other changes regarding the playlist management.
  - The playlist section in the table overview has been re-designed to support multi-selection. So you select multiple tables from the table overview and add them to a playlist.
  - Added icons to indicate if a playlist is a curated one or a SQL playlist.
  - Added "edit" button next to the playlist selector on the table overview toolbar.
  - Predefined SQL templates support
  
    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/sql-templates.png" width="500" />
  - Predefined curated playlists support
  
    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/playlist-templates.png" width="600" />
    
  - All playlist icon from the table overview and playlist sidebar section have been converted into a button which directly opens the playlist inside the management dialog.
  - Added separate icon for "Pinball M".

    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/playlist-manager.png" width="750" />

- **Tables / Cabinet Monitor**: Added screenshot option. The action takes screenshots from the activates screens and writes a timestamp into them. The screens can be used for score submission of online competitions, like https://worldofvirtualpinball.com/.
- **Tables / Media Recorder**: Added 180 degree rotation option for playfield recordings.
- **Tables / PUP Packs**: Added [PupPackScreenTweaker.exe](https://github.com/matiou11/PupPackScreenTweaker) as additional PUP pack editor to the PUP pack section. Note that this editor is only available when working on the cabinet itself.
- **Tables / Table Data Manager**: The auto-naming has no restrictions on VPX files that are located in sub-folders anymore. You can also rename them now.
- **Tables / Backglasses**: Added button for DMD positioning.
- **Tables / VPS Section**: This section supports multi-selection now. This way, you can bulk auto-match a selection of tables. 
- **Tables / Highscores Section**: This section has undergone a revamp:
  - Added support for multi-selection from the table overview.
  - Added bulk operation support for highscore resets.
  - Added bulk operation support for highscore backups.
  - Re-implemented the highscore reset dialog which shows more information about the actual reset, e.g. if a resetted nvram is available.
  - Added a **reset value** input option for the highscore reset dialog. Note that this input is not enabled for non-rom based tables.
  
    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/highscore-reset.png" width="600" />
  
- **Preferences / Controller Setup**: Added error message that is displayed when "SET FSMODE=EnableTrueFullScreen" is set in the emulator launch script, as this will avoid any VPin Studio overlays from getting the focus.
- **Preferences / Backglass Server**: Add possibility to configure default visibility for grill, DMD and B2S DMD.
- **Preferences / Backglass Server**: Add possibility to configure default Bring Forms settings.
- **Tables / Overview:** Added new column "Comment". The colum is hidden by default, not sortable and placed as last table column. 
- **Tables / Overview:** Added context menu item "Edit Comment". 
- **Tables / Overview:** Added ROM alias name in square brackets to the ROMs column (if set). 
- **Tables / Overview:** Added VPS, Playlist and Comments columns for FX emulators. 
- **Tables / Filter:** Added filter option "No comment". 
- **Tables / Overview:** De-cluttering:
  - **Context Menu**: Removing less used entries.
  - **Toolbar**: When switching into asset-view mode, unnecessary actions are hidden.
- **Backglass Manager / DMD Positioning**:
  - Added "Center Horizontally" button that will center the selection canvas in the frame.
  - Added +Shift in mouse gesture to resize DMD while keeping its center at same location
  - Added ability to move dmd with arrow keys and resize it with Ctrl / Alt / Shift
  - Added 3:1 aspect ratio for large Sega DMDs and a smaller 8:1 ratio for Data East displays.
  - Added margin field to configure margins added to the calculated auto position. Useful when the DMD zone has rounded corner
  
    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/dmd-positioner.png" width="700" />
- **Backglass Manager / Bring Forms**: revisited the 'bring BG form' to support Form to Back option. Also modified in Tables sidebar
- **Backglass Manager / Misc**
  - Added "Open" button to show backglass in Explorer (only available when working on the cabinet).
  - Added "Open VPS Table" button for backglasses that have a game and are linked via VPS.
- **Hook Support**: The VPin Studio allows to execute customs scripts from any client. You can add these "hooks" by adding .exe or .bat files into the server installation directory **resources/hooks**. The list of files is picked up and added to the preferences split button of the Studio client and will be executed on click. See also: https://github.com/syd711/vpin-studio/wiki/Hooks

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/hooks.png" width="300" />

- **Pause Menu / iScored**: New highscore views have been added to the pause menu. If the active table is part of an iScored competition, the top-5 iScored dashboard scores are shown as a menu entry now.
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/pause-menu/pause-menu-iscored.png" width="700" />

- **Pause Menu / VPin Mania**: If enabled, the top-5 VPin Mania scores are shown as a menu entry now.
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/pause-menu/pause-menu-mania.png" width="700" />

- **Pause Menu / Preferences**: Both new views are configurable in the preferences. Note that the "Test" parameters for the dialog are also remembered now.
  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/pause-menu/pause-menu-scores.png" width="600" />

## VPin Mania Changes

- **Added Friends**: Added new "Friends" button on the top window toolbar.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/mania/friends-btn.png" width="300" />

  Connecting with friends (cabinets) allows you to see their online status and what table they are playing. Once you connected your cabinet with with your friends, you user players that have been added as **VPin Mania Players** are visible for your friends too, which allows to share highscores with then. You can adjust the visibility of the accounts in the "Friends" menu.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/mania/friends.png" width="670" />

- **Merged Highscore Cards**: Adding friends means you can merge the highscores from your friends accounts with yours. The highscore card template editors comes with a new preference for this. To highlight these, you and choose another color for these scores.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/mania/merged-card.png" width="500" />

- **VPin Mania Preferences**: The preferences section "VPin Mania Preferences" has been split into an "Account" and a "Tournaments" section to separate these two.
- **Open in VPin Mania Action**: For more convenience you can open a VPin Mania table overview directly from
    - the table overview using the corresponding context menu action.
    - the highscore editor by clicking on the VPin Mania logo.
- **Deny Lists**: A list of selected user has additional permissions to add and remove highscores to/from deny lists. These highscores are ignored for all rankings and for merged highscore cards. Default highscores that are posted on VPin Mania can be ignored this way. For more information, visit our Discord server linked in the preferences.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/mania/deny-list.png" width="700" />

- **Installed Indicator**: The recent scores and table overview item show an additional green icon if the table with the given VPS mapping is installed on the users cabinet. Note that only the table is checked, not the version.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/mania/score-entry.png" width="350" />


## Bugfixes

- **Tables / PUP Packs**: The PUP pack data in the PUP pack section is now refreshed on table selection.
- **Tables / Asset Preview Dialog**: The dialog has been re-implemented to ensure the shown media is properly scaled.
- **Tables / Auto-Naming**: Fixed suggested file name suffixes for .fpt tables.
- **Tables / Overview**: Default sorting by display name is not case-sensitive anymore.
- **Backglass Manager / Grill visibility**: When grill visibility is standard, Backglass preview now takes in account the global grill visibility.
- **Backglass Manager / support of sub-folders**: When table is in sub-folder, the backglass and associated res or screenres files were not properly got.
- **Studio Exit Dialog**: Fixed issues when cancelling the exit dialog.
- **Highscore Cards Editor**: Fixed various weight and posture issues with the font selection and the preview of it.
- **Tables / Table Data Manager**: Fixed error during auto-applying values from VPS tables selection.
- **VPS Mappings**: Added missing FX2 Support for table versions.
