## Release Notes 4.8.0

- **Filter Settings**: The table filter panel allows you to filter by issue type now.
- **Backglass Manager**:
  - Added support for **Zen Studio** tables (you must configure DOFLinx first!).
  - Added combobox to filter backglasses by emulator.
  - Added emulator name column (which also better explains why for duplicated emulators, backglasses are shown multiple times).
  - Enabled "Backglass" section for the table overview and Zen Studio tables.
- **MAME Game Support (not VPinMAME!)** :
  - Added import support for MAME games/roms. 
  - Added deletion support for MAME games.
  - Added option to the **Play** button to launch MAME games.
  - Fixed several issues for the overview when MAME emulator was selected.
  - Added ROM, Playlist and date columns for the overview and MAME emulators.
- **Emulator Management**
  - The **curl** calls to tell the Studio server that a game has been launched or exited have been added to the emulator types **Zaccaria, Pinball FX/3/M and MAME**. This allows the **in-game recording and pause menu** for these emulators.
- **Media Recorder**
  - Added emulator recording support for **Zaccaria, Pinball FX/3/M and MAME games**.
- **Designer**
  - Added support for custom highscore card sizes. You can change the size in the "Highscore Cards Settings". 
- **Pause Menu for non-VPX Games (first draft)**
  - Added pause menu support for **Zaccaria, Pinball FX/3/M and MAME** games. Note that you need to have the graphics settings set to "borderless window". Otherwise you will run into focus issues.
- **Music Management**: The music management for VPX games has been reimplemented. Audio files from the "Music" folder are detected through the table script including those that are computed from variables. Note that there are still some tables (e.g. Iron Maiden) where the .mp3 filename computation prohibits the detection of the actual filenames. As a result from this...
  - **You need to perform a re-scan of all tables to detect the table's audio files**.
  - Backups include the table's audio files now. Previous implementations took only the subfolder from **Music/<ROM NAME>** for the backup - if available.
  - A new **Missing Audio Files** validators shows if the resolved .mp3 files have been found.
- **Drop-In Manager**: Added search field to filter assets.
- **Studio System Backup**: Added the pinvol and pinemhi .ini files to the VPin Studio backup file.

### Bugfixes

- **WOVP Pause Menu Item**: Screenshots for portrait mode screens are not rotated anymore.
- **WOVP Synchronization**: Fixed issue that discontinued competition types have not been removed automatically.
- **iScored Synchronization**: On table exit, only the active game is synchronized.
- **Notification Delay**: Fixed issue that notifications are shown late because of long iScored synchronizations.
- **Table Overview**: Fixed backup button being visible for all emulator types.
- **Media Recorder**: Added filtering of disabled emulators.
- **Table Data Manager**: Fixed tab focus order for all tabs.
- **Wheel Augmentation**: Fixed various issues and superfluous calls when applying badges to wheels.
- **Media Recorder**: Added timeout of 10 minutes for recordings in case the in-game recorder is never turned off.
- **Table Backups**: Fixed issue that the VPS mapping was not detected on restore.
- **VPS Tables**: Fixed sorting of the sound column.