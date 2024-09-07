## Release Notes 3.5.1


## Bugfixes

- **Studio Server**: Fixed possible dead-lock causing the whole server to be stuck. I could not reproduce this issue, but found two possible reasons that caused this issue.
- **Table Uploads**: Fixed uploads of files with a whitespace at the end of the base name. This lead to invalid folder names when the file was uploaded to a table subfolder.
- **VPin Mania Player Ranking**: Fixed showing of duplicated players.


### Release Notes 3.5.0

### Changes

- **Table Statistics**: Added reload button.
- **Table Statistics**: Added click handler for single table statistics which opens the table data dialog with the statistics tab.
- **Table Statistics**: Change to a more responsive design, so that the view supports smaller resolutions too.
- **Table Data Manager Dialog**: Added reset options to the table statistics tab.
- **Table Data Manager Dialog**: Added statistic options to the table statistics tab which allows to set specific values for the table statistics.
- **Playlist for PinballX**: Added support of playlists and favorites for PinballX frontend.
- **Table Uploads**: The dialog remembers the last selected upload type now.
- **Dashboard**: Removed competition widgets since the data can be shown in the competition section and caused layout issues for smaller screens.
- **Table Overview / Table Media**: Added additional drag-hover indicator which looks like the one for the table overview. This way, user have a better visual feedback where file drops are allowed.
- **Backglass Manager Tab**: Added type-ahead input option. Like for the other table overviews, you can now simply input a search term when the table is focussed and the matching entry gets selected.
 
### Bugfixes

- **Table Overview / Filter**: Fixed sticky filter issue where the filter updates have not been saved and restored on startup.
- **Drop-In Folder**: Fixed refresh issues and filtered system files.
- **Drop-In Folder**: The "Install" button is not hidden for images to avoid confusion that this button might become enabled somehow.
- **Table Uploads**: Fixed file drop handler for the table upload dialog which did not enable the "Upload" button.
- **Table Uploads**: Improved the additional check for existing tables after "Upload" has been pressed. The textual description and the check is much sharper now, so I hope it will produce less false positives. 
- **Dashboard**: "Smoothend" the "Latest Scores" loading.
- **Studio Client Window**: Optimized a lot of sizing issue for smaller resolutions. Maybe we are not still there, but we've taken a huge step on the road.
- **Notifications**: Added missing check for notification which validates that VPX is not running. Otherwise these pop-up windows might pause the game which is not intended.  
- **Preferences / Highscore Cards Settings**: Fixed key binding error message that didn't check if a joystick mapping is available.  
- **System Manager**: Changed text for the "Start Installation" when the installation can only be executed on the cabinet.   