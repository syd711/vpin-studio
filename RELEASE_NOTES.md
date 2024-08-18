## Release Notes 3.1.1

## Changes

- **Table Validators**: Added additional "Script Validators" section for the table validator preferences. The new "Controller.stop" validator checks if a ROM based table with no nvram file, has a table exit routine without a "Controller.stop" call (quite special). Calling this routine there is required for the serialization of the nvram file which contains the tables highscores. Note that this validator requires a new table scan to be triggered. So far I've found "Checkpoint" and the older "X-Files" table affected by this.
- **VPS Tables**: Added combo box to filter all VPS entries by their table format. The VPX table format is used as default. 

## Bugfixes

- **Highscore**: Fixed highscores for table "King Kong" Data East.
- **Event Log**: Improved error logging for issues regarding failed iScored highscore submissions.
- **Table Data Manager**: Fixed issues opening the dialog with broken VPS data. 
- **Preferences / PinemHi Settings**: Fixed NumPad key bindings.
- **Preferences / PinemHi Settings**: Made all input spinner for the display settings editable and increased the maximum possible input values.
- **VPin Mania / Highscore Synchronization**: Fixed superflous highscore submissions of lower highscores. Since only the highest score on a table of a player is stored, lower scores from the same player can be skipped for synchronization. This led to a wrong synchronization count.
