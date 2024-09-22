## Release Notes 3.7.0

## Changes

- **Table Validators**: Introduced new validator **VPinMAME ROM Validation**. The validator uses the results from the VPinMAME ROM tester to indicate possibly broken ROM files. The new validator is enabled by default.
- **Highscore Card Editor**: Added "Apply to all" button for the font selection which will apply the selected font to all available templates.
- **Table Overview / Reload**: A manual reload in the table overview results in full cache invalidation of the server. This should pick-up all changes done manually by the user on the cabinet.
- **Table Overview**: Added new column "Launcher" which shows the .exe file that will be used for launching the table.
- **VPBM 3.3**: Updated to VPBM 3.3, including some performance optimizations and support of multiple external host ids.

## Bugfixes
  
- **Highscore Cards Popups**: Properly centered highscore card when "show on table launch" option is used for highscore cards.
- **Uploader**: Fixed issue with uploading files with filename length smaller than three characters (e.g. "24" - Damn you, Jack!).
- **Table Asset Manager / Playlists**: Fixed asset search for playlists. We somehow forgot that. You can now search the frontend's asset database for media for your playlists, e.g. "music".