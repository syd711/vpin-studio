## Release Notes 3.4.0

## Changes

- **Backglass Manager**: The backglass manager dialog has been moved into a separate tab of the "Tables" view.
- **Table Uploads**: Added additional matcher check for uploads (import / import and replace). The matcher checks if there the given files matches the table that should be overwritten or if there is an existing table that may match with the given uploads and could be selected for replacement. Note that the check is a bit vague, I hope we found the correct degree of matching.
- **Support of statistics for the PinballX frontend**: The statistics tab is now supported for the PinballX frontend.
- **Filters in VPS Tables**: The VPS tab now supports filtering and full column sorting like other tabs.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/vps-tab.png" width="700" />

- **Added drop-in folder feature**: 

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/drop-ins-menu.png" width="700" />


## Bugfixes

- **Pinemhi Settings**: Removed num-pad keys from the list of selectable keys for the pinemhi settings.
- **Backglass Editing**: Fixed **Start as Exe** setting that was set to **false** when any other option was changed than itself. The field remains empty now so that the server default will be used. You have to toggle previously touched .directb2s files to reset the flag to the default.
- **VPin Server**: Fixed a database locked issue during the highscore reading. 
- **Table Uploads**: Fixed Cancel button and invalid archive when uploading ZIP file.
- **Table Repository**: Fixed Cancel button not cancelling progress when uploading ZIP file.
- **Highscore Cards**: Fixed "margins" for the non-raw score rendering, so that only the score values are positioned and the table title remains centered.
- **Table Overview / Table Uploads**: The table overview does not perform a full reload after table uploads anymore. 
- **Table Overview / Table Deletions**: The table overview does not perform a full reload after table deletions anymore. 
- **Highscores Parsing**: Text based highscores are now resetted by their values and not simply deleted anymore.
- **Highscores Parsing**: Added filtering for duplicated scores. There are tables like "Hurricane" or "Whirlwind" where the "GRAND CHAMPION" is also listed on the first place. Instead of fixing all affected tables one-by-one, a filter for duplicated scores has been added.
- **Highscores**: Fixed highscore for "No good gofers" by adding "CLUB CHAMPION" to the default list of highscore titles.
