## Release Notes

### Changes


- Table Management: Added support for **loading tables from sub-folders**. Up until now, the Studio did only assume that all VPX files and backglasses are located in the **Tables** folder of the emulator. While Popper supports sub-folders here, the Studio does now too. Because of the complexity, I disabled some additional operation for these tables, like the renaming option of the VPX file. This required quite a lot of changes, so there might be some aftermath.
- Highscore Parsing: Added support for a lot more VPReg.stg based highscore formats.
- Highscore Parsing: Added support for single score tables.
- Pause Menu: Added view option "Style" to the preferences. You can now choose if entries should be rendered as part of the pause menu or if Popper assets of the "Info", "Help" and "Other2" screen should be shown on their configured screen locations. 

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/pause-menu.png" width="600" />

- Introduced "Default Players": The build-in players dialog has an additional checkbox now to mark the default player of a cabinet. If set, the highscore filter won't filter anonymous highscores anymore. So for tables with anonymous highscores, the default initials will be used when Discord messages are posted. The flag will become more relevant in the future too when it comes to tournament setups.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/players/add-player.png" width="600" />

### Bugfixes

- Fixed list sorting int the "Table Import" dialog.
- Fixed list sorting in the "Backglass Manager" dialog.
- Fixed posting into subscription channels: For new subscription channel, a set of highscores was posted before the actual initial message. This has been fixed so that if no highscore filters are (player definitions) set, these score are now appended **after** the initial message.
- Added progress dialog for loading player highscore in the players section. This avoids possible concurrent database accesses when double-clicking a player.
