## Release Notes 5.2.0

### Changes

#### Highscore Monitoring

In the "Highscore Settings", you can enable the "Highscore Monitor" now. It observes all files and folders that are relevant for highscores
and fires highscore change events after file changes. The feature aims for VR users that launch games via Studio.
Not that the existing table start and exit events fired by your frontend are still in place. 
So if you are a PinUP Popper user, this feature is less relevant for you.

The highscore monitoring was already implemented a while ago, but withdrawn because of various issues. This re-implementation hopefully does a better job.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/highscore-monitor.png?raw=true" width="600" />

#### Table Move/Clone

To improve the migration from VPX 10.8.0 to 10.8.1 the new table overview context menu actions "Move Table" and "Clone Table" have been added.

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/move-clone.png?raw=true" width="400" />

Executing them gives you the option to move or copy a table with it's related files to another VPX emulator.
  
  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/move-clone-dialog.png?raw=true" width="450" />

Note that the actions are only available for VPX tables and that for the "Move" action only the table files (.vpx, .vbs, .ini, .pov) are moved.
Assets like the ROM, ALTSound or ALTColor are copied, but not removed from the source.

#### Future Pinball Support for iScored

Future Pinball tables are now detected by the iScored integration. The support was missing until now. 

#### Display Name Support for Highscore Cards

For the highscore card designer the option to render the player display name has been added.
It can be found in the "Score Settings" sidebar of the designer.

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
