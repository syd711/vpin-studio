## Release Notes

### Changes


- Table Management: Added support for **loading tables from sub-folders**. Up until now, the Studio did only assume that all VPX files and backglasses are located in the **Tables** folder of the emulator. While Popper supports sub-folders here, the Studio does now too. Because of the complexity, I disabled some additional operation for these tables, like the renaming option of the VPX file. This required quite a lot of changes, so there might be some aftermath.
- Highscore Parsing: Added support for a lot more VPReg.stg based highscore formats.
- Highscore Parsing: Added support for single score tables.
- Pause Menu: Added view option "Style" to the preferences. You can now choose if entries should be rendered as part of the pause menu or if Popper assets of the "Info", "Help" and "Other2" screen should be shown on their configured screen locations. 

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/pause-menu.png" width="600" />

- Introduced "Default Players": The build-in players dialog has an additional checkbox now to mark the default player of a cabinet. 

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/players/add-player.png" width="600" />

### Bugfixes

- Fixed Studio update issues: Both of the installer .exe files did not set any privileges. So when installed in the **program folder** of Windows, the update did only work when the program was started as administrator. I've never noticed because I did use another folder all the time.
- Fixed Table Uploads: The table versions have been resetted on upload + replace which should not be the case.
- Fixed list sorting int the "Table Import" dialog.
- Fixed list sorting in the "Backglass Manager" dialog.
- Fixed posting into subscription channels: For new subscription channel, a set of highscores was posted before the actual initial message. This has been fixed so that if no highscore filters are (player definitions) set, these score are now appended **after** the initial message.
- Added progress dialog for loading player highscore in the players section. This avoids possible concurrent database accesses when double-clicking a player.
