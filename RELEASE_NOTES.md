## Release Notes 2.21.0

- Renamed **UI Settings** to **Client Settings**.
- Added new initial settings section **Cabinet Settings** where the cabinet and avatar is image is located.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/universal-upload.png" width="900" />

### Bugfixes

- **Table Overview / Loading Performance Optimization**: Tables/games are not loaded anymore all at once, but only for the selected emulator(s). E.g. all VPX tables are still loaded as bulk request, but not together with MAME or FP tables. 
- **Table Overview / Filtering Performance Optimization**: Improved filtering for VPX/non-VPX games.
- **Table Overview / Validation Performance Optimization**: Non-VPX games are only checked for missing assets issues and do not run through the full VPX validation anymore.