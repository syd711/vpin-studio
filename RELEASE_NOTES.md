## Release Notes 4.8.0

- **Filter Settings**: The table filter panel allows you to filter by issue type now.
- **Backglass Manager**:
  - Added support for Zen Studio tables (you must configure DOFLinx first!).
  - Added combobox to filter backglasses by emulator.
  - Added emulator name column (which also better explains why for duplicated emulators, backglasses are shown multiple times).
  - Enabled "Backglass" section for the table overview and Zen Studio tables.
- **MAME Support**:
  - Added import support for MAME games/roms. 
  - Added deletion support for MAME games.
  - Added option to the "Play" button to launch MAME games.
  - Fixed several issues for the overview when MAME emulator was selected.
  - Added ROM, Playlist and date columns for the overview and MAME emulators.
- **Emulator Management**
  - The **curl** calls to tell the Studio server that a game has been launched or exited have been added to the emulator types Zaccaria, Pinball FX/3/M and MAME. This allows the in-game recording for these emulators.
- **Media Recorder**
  - Added emulator recording support for Zaccaria, Pinball FX/3/M and MAME games.

### Bugfixes

- **WOVP Pause Menu Item**: Screenshots for portrait mode screens are not rotated anymore.
- **Table Overview**: Fixed backup button being visible for all emulator types.
- **Media Recorder**: Added filtering of disabled emulators.