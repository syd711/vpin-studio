## Release Notes 4.4.0

## Changes

- **Zen Studio and Zaccaria Tables Support**
    - The emulator support for Zaccaria and Zen Emulators have been improved. The table overview does show now more
      accurate table columns and sidepanels depending on the selected emulator.
    - The play button is now shown for these emulators too. It allows launching tables via the Popper frontend or via *
      *Steam**.

      <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/launch-steam.png?raw=true" width="500" />

  - ALT Color management has been enabled for FX and FX3. Note that you need to configure the game itself in order to
    support external DMD providers.
  - The paths information for these emulators are automatically resolved when created, including the Steam path.
  - The Studio comes with integrated .pupgames lists now: When a new emulator is added, related games are
    automatically added to the game library.

    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/system-manager/new-emulator.png?raw=true" width="500" />

- **System Tray**
    - Added "Restart" menu item.

- **Discord Notifications**:
    - Added more logging for related events. No Discord notifications are emitted when the Studio detects a
      highscore for the first time. This should avoid sending update events for initial scores. An additional log
      message has been added to the "Event Log" for this.

- **Table Overview**
    - Replaced the white "checked" icons or .ini, .res and .directb2s files with edit button. These files can now
      directly be edited via the systems default text editor. For backglasses, the corresponding backglass is selected
      in the backglass manager.
    - Switched order of the backglass and the PUP pack column.
    - Removed .ini section since the .ini file can now directly be edited from the table overview.
    - Added edit, upload and delete actions for .ini, .res. and .pov files to the section "Table Data".

      <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/table-files.png?raw=true" width="500" />


## Bugfixes

- **ALT Color**: Fixed deletion of ALT color files.
- **VPS Updates**: The VPS update indicator in the table overview is working correctly now. E.g. the VPS version **1.4**
  and the table version **1.4.0** where detected as identical now which wasn't the case before.  

