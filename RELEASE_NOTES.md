## Release Notes 2.21.0

- Added new initial settings section **Cabinet Settings** where the cabinet and avatar is image is located.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/universal-upload.png" width="900" />

### Bugfixes

- **ALT COLOR**: Fixed wrong filename when importing and validating serum files.
- **Table Overview**: Fixed broken table layout which remained in a fix size when window was resized.
- **Table Overview**: Fixed status column sorting.
- **Table Overview / Loading Performance Optimization**: Tables/games are not loaded anymore all at once, but only for the selected emulator(s). E.g. all VPX tables are still loaded as bulk request, but not together with MAME or FP tables. 
- **Table Overview / Filtering Performance Optimization**: Improved filtering for VPX/non-VPX games.
- **Table Overview / Validation Performance Optimization**: Non-VPX games are only checked for missing assets issues and do not run through the full VPX validation anymore.
- **Offline Competitions**: Fixed missing highscore reset when no Discord server was selected. This should be independent from each other of course.