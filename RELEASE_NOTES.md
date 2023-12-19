## Release Notes

### Updates

#### VPS Change Listener

Added VPS change listener that can be connected with your Discord bot.
If you have a Discord bot, you can select which channel should receive notifications about VPS updates.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/vps/vps-bot-settings.png?raw=true" width="700" />

The same information is always available within the Studio, but there the list is always(!) filtered by the games that 
have a matching VPS id.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/vps/vps-notifications.png?raw=true" width="600" />

#### Highscore Card Pop-Up

You can let you show the highscore card of a table when it is started. Set the duration value > 0 in the highscore card settings for 
the "Highscore Card Notifications" setting.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/highscores/card-popup.png?raw=true" width="600" />


### Bugfixes

- Fixed update info dialog: unfortunately, this was only shown once. It will be shown again now if the automatic updater is used.
- Fixed offset of table rows: I had to change the selected row styling for this again.
- Improved exception handling for failed updates.
- Fixed issue that the VPS sheet has been downloaded, but the data has not been refreshed (until next start).
- Added "Alternative ROM Name" of Popper (1.5) as fallback for highscore text files. So if you want to keep your (manually adapted!) ROM name or highscore filename persisted (so that it survives a re-scan), use the table settings dialog and enter the two values there. (Yes, this could be done automatically, but I would have to kill all Popper processes for this, which I want to avoid.) 
- Fixed updating of "ROM Name" and "Alt. ROM Name" in the table settings dialog: When these values change, the values are updated in the "Script Details" section too now.
- Fixed error in uploading zipped VPX files when VPX files where in sub-folders within the zip archive.
- Fixed "POPMedia" lookup: Instead of simply assuming it under the "PinUPSystem" folder, the "POPMedia" folder is now read from the Popper database where the correct value is stored in the emulator configuration.
- Improved lookup of auto-start folder for fresh installations.
- Filtered debug log files from list of possible highscore filenames.
- Removed confirmation dialog for the auto-fill action in the VPS section.
- Fixed a bunch of PUP pack upload issues.
