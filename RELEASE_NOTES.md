## Release Notes 5.2.0

### Changes

#### Highscore Monitoring


  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/highscore-monitor.png?raw=true" width="600" />

#### Table Move/Clone

To improve the management of tables that should run on VPX 10.8.0 and 10.8.1 the new table overview context menu actions
"Move Table" and "Clone Table" have been added.

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/move-clone.png?raw=true" width="400" />

Executing gives you the option to move or copy a table with it's related files to another VPX emulator.
  
  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/move-clone-dialog.png?raw=true" width="450" />

Note that the actions are only available for VPX tables and that for the "Move" action only the table files (.vpx, .vbs, .ini, .pov) are moved.
Assets like the ROM, ALTSound or ALTColor are copied, but not never removed from the source.

#### Future Pinball Support for iScored


### Bugfixes

- **Player Management**
  - Fixed issue that the player highscores were not sorted by modification/creation date.
- **Backglass Sidepanel**
  - Fixed endless refresh loop.
- **Wheel Designer**
  - Fixed issue that the background image position couldn't be modified.
- **Playlist Management**
  - Fixed issue with Favorites and Global Favorites playlists from PinUP Popper.
- **Highscore Lookup**
  - Fixed issue looking up the VPReg.stg file.
