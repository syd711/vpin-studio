## Release Notes

### Changes

- **Table Management**: Added support for **loading tables from sub-folders**. Up until now, the Studio did only assume that all VPX files and backglasses are located in the **Tables** folder of the emulator. While Popper supports sub-folders here, the Studio does now too. Because of the complexity, I disabled some additional operations for these tables, like the renaming option of the VPX file. **This required quite a lot of changes, so there might be some aftermath**.
- **Table Management**: Added additional check for **PUP Pack** names during table scans. Some tables define this name in a separate variable that differs from the default ROM name. An additional field **Name** has been added to the PUP Pack section to show the effective PUP pack.
- **Table Management**: Added **.ini** file uploads.
- **Highscore Parsing**: Re-implemented all highscore parsers for all formats: VPReg.stg entries, text files and nvram files. Kudos here to **@ED209** here who provided me tons of highscore data to make this happen. Because of this data I was able to support way more tables.  
- **Highscore Parsing**: Added support for single score tables in general (no matter if stored in nvram or text files, these were ignored before completely).
- **Highscore Parsing**: Added some custom parsers, e.g. for "godzilla" or "monopoly" to support more tables.
- **Pause Menu**: Added view option "Style" to the preferences. You can now choose if entries should be rendered as part of the pause menu or if Popper assets of the "Info", "Help" and "Other2" screen should be shown on their configured screen locations. 

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/pause-menu.png" width="600" />

- Introduced **Default Player**: The build-in players dialog has an additional checkbox now to mark the default player of a cabinet. If set, the highscore filter won't filter anonymous highscores anymore. So for tables with anonymous highscores, the default initials will be used when Discord messages are posted. The flag will become more relevant in the future too when it comes to tournament setups.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/players/add-player.png" width="600" />

- **Header Toolbar**: De-cluttered the toolbar by moving entries into a split menu. I also added a DOF Sync entry there.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/header-toolbar.png" width="300" />

- **Backglass Management**: Added option to check the "Execute as EXE" flag. It seems this may have quite some performance improvement when checked.

### Bugfixes

- **Backglass Management**: Fixed accidental backglass save on selection.
- **Backglass Manager**: Fixed a bunch of issues here, looks like I tested this one half asleep.
- Fixed table overview errors of tables without VPS updates.
- Fixed list sorting int the "Table Import" dialog.
- Fixed list sorting in the "Backglass Manager" dialog.
- **Subscription Channels**: For new subscription channel, a set of highscores was posted before the actual initial message. This has been fixed so that if no highscore filters are (player definitions) set, these score are now appended **after** the initial message.
- **Subscription Channels**: Added support for tables with anonymous tables. Since anonymous highscore changes are not filtered anymore when a default user is set, subscription will now created for these type of tables too.
- Added progress dialog for loading player highscore in the players section. This avoids possible concurrent database accesses when double-clicking a player.
- Emulator Validation: The Studio can now start even if no emulator is found. This may happen when not folders have been set up in PinUP Popper.
- **ROM Aliases**: Fixed possible error when used with multiple emulators.
- **Highscore Card Preferences**: Fixed resetting the target screen to an empty entry to disable the generation.
