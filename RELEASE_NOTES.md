## Release Notes 3.2.0

## Changes

- **Table Validators**: Added additional "Script Validators" section for the table validator preferences. The new "Controller.stop" validator checks if a ROM based table with no nvram file, has a table exit routine without a "Controller.stop" call (quite special). Calling this routine there is required for the serialization of the nvram file which contains the tables highscores. Note that this validator requires a new table scan to be triggered.
- **VPS Tables**: Added combo box to filter all VPS entry by their format. The VPX table format is used as default. 

## Bugfixes

- **Highscore**: Fixed highscores for table "King Kong" Data East.
- **Event Log**: Improved error logging for issues regarding failed iScored highscore submissions.
- **Table Data Manager**: Fixed issues opening the dialog with broken VPS data. 
