## Release Notes 4.4.0

## Changes
  
- **Zen Studio and Zaccaria Tables Support**
  - The emulator support for Zaccaria and Zen Emulators have been improved. The table overview does show now more accurate table columns and sidepanels depending on the selected emulator.
  - The play button is now shown for these emulators too. It allows launching tables via the Popper frontend or via **Steam**.
  - ALT Color management has been enabled for FX and FX3. Note that you need to configure the game itself in order to support external DMD providers.  
  - The paths information for these emulators are automatically resolved when created, including the Steam path.
  - The Studio comes with integrated .pupgames lists now: When a new emulator is added, related games are automatically added to the game library.
    
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/backups/indicator.png?raw=true" width="500" />

- **System Tray**
  - Added "Restart" menu item.

- **Discord Notifications**: 
  - Added additional logging for initial events. No Discord notifications are emitted when the Studio detects a highscore for the first time. This should avoid sending update events for initial scores. An additional log message has been added to the "Event Log" for this.

 
## Bugfixes

- **ALT Color**: Fixed deletion of ALT color files.
- **VPS Updates**: The VPS update indicator in the table overview is working correctly now. E.g. the VPS version **1.4** and the table version **1.4.0** where detected as identical now which wasn't the case before.  

