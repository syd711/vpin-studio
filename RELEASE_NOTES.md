## Release Notes

### Updates

#### VPS Change Listener

Added VPS change listener that can be connected with your Discord bot.
If you have a Discord bot, you can select which channel should receive notifications about VPS updates.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/vps/vps-bot-settings.png?raw=true" width="600" />

The same information is always available within the Studio, but there the list is always(!) filtered by the games that 
have a matching VPS id.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/vps/vps-notifications.png?raw=true" width="600" />


### Bugfixes

- Fixed update info dialog: unfortunately, this was only shown once. It will be shown again now if the automatic updater is used.
- Fixed offset of table rows: I had to change the selected row styling for this again.
- Improved exception handling for failed updates.
- Fixed issue that the VPS sheet has been downloaded, but the data has not been refreshed (until next start).
- Added "Alternative ROM Name" of Popper (1.5) as fallback for highscore text files. So if you want to keep your (manually adapted!) ROM name or highscore filename persisted (so that it survives a re-scan), use the table settings dialog and enter the two values there. (Yes, this could be done automatically, but I would have to kill all Popper processes for this, which I want to avoid.) 
- Fixed updating of ROM name and Alt. ROM name in the table settings dialog: When set, the "Script Details" section values are updated now too.
- Fixed error in uploading zipped VPX files when VPX files where in subfolders within the zip archive.
