## Release Notes 3.4.0

## Changes

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/backglass-manager-dnd.png" width="700" />


## Bugfixes

- **Pinemhi Settings**: Removed num-pad keys from the list of selectable keys for the pinemhi settings.
- **Backglass Editing**: Fixed **Start as Exe** setting that was set to **false** when any other option was changed than itself. The field remains empty now so that the server default will be used. You have to toggle previously touched .directb2s files to reset the flag to the default.
- **VPin Server**: Fixed a database locked issue during the highscore reading. 
- **Table Upload**: Fixed Cancel button and invalid archive when uploading ZIP file.
- **Table Repository**: Fixed Cancel button not cancelling progress when uploading ZIP file.
- **Highscore Cards**: Fixed "margins" for the non-raw score rendering, so that only the score values are positioned and the table title remains centered.