## Release Notes 2.20.1
- 
- **Preferences**: Renamed **UI Settings** to **Client Settings**.
- **Preferences**: Added new initial settings section **Cabinet Settings** where the cabinet and avatar is image is located (which have been part of the **UI Settings** before).
- **ALTColor**: Added automatic-backups: If you upload a new ALTColor file, the existing ones will be copied into a "backups" subfolder. This way, it is clear which ALTColor file is used.
- **Competitions**: When a competition is started/created (all modes), the Popper database entry of the table is updated with the competitions id. The id has the format **vps://competition/\<TYPE\>/\<ID\>**. The field is read and written in CSV format so that you can create multiple competitions and all of them are stored in this field. Using this information, you now create **competition playlists**.
 

### Bugfixes

- **ALTColor**: Fixed wrong filename when importing and validating serum files.
- **Table Overview**: Fixed broken table layout which remained in a fix size when window was resized.
- **Table Overview**: Fixed status column sorting.
- **Table Overview / Loading Performance Optimization**: Tables/games are not loaded anymore all at once, but only for the selected emulator(s). E.g. all VPX tables are still loaded as bulk request, but not together with MAME or FP tables. 
- **Table Overview / Filtering Performance Optimization**: Improved filtering for VPX/non-VPX games.
- **Table Overview / Validation Performance Optimization**: Non-VPX games are only checked for missing assets issues and do not run through the full VPX validation anymore.
- **Offline Competitions**: Fixed missing highscore reset when no Discord server was selected. This should be independent of each other of course.
- **Table Overview / Playfield Preview**: Trying to improve the positioning for MAME playfield videos, let's see how this went.