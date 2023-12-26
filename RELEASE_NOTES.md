## Release Notes

### Breaking Change

The preferences for filtering highscores by initials is gone and has been replaced by the build-in players.
So if you only want to see updates for your highscore, create a build-in player with your initials.

### Changes

- Moved "Analytics" section into "Tables" section tab "Table Statistics". The view doesn't have to be this prominent, that's why it has been moved.
- Added copy buttons for VPS table URLs (needed later on).
- Replaced update indicator icons with a colorized version.
- Uploaded tables are now immediately mapped against VPS.
- The VPSSaveEdit tool button is now also available in the "Highscores" section. 
- Replaced transient VPS table update notification with a persisted state. The update indicator for this is a new column in the table overview. If you are not interested in these updates, you can hide the column in the UI preferences. The update indicator can be resetted from the toolbar button.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/vps/update-colum.png?raw=true" width="600" />



### Bugfixes

- Fixed drag and drop of media assets ...again.
- Fixed empty table view when a broken VPX file was scanned.
- Virtual Pinball Spreadsheet: Table version are sorted by date now.
- Virtual Pinball Spreadsheet: Added empty version entry for older tables that are not listed in the VPS.
- Virtual Pinball Spreadsheet: Re-implemented version combo-box with a more appealing design.
