## Release Notes 4.4.0

## Changes

- **Tag Management**
    - The Studio comes with a comprehensive tag management now. Tags are displayed as part of the Table Data Manager
      dialog and are also visible in the Table Data section. The tagging support contains the following features:
        - Managing table tags in the Table Data Manager dialog.
        - Remove tags or filter by tags by clicking on them when shown in the Table Data section.
        - Tag filtering support for the filter section.
        - Auto-tagging support for new table uploads (see Preferences -> Table Validators).
        - Auto-tagging support for new backglass uploads (see Preferences -> Backglass Validators).
        - Auto-tagging support for media asset changes (see Preferences -> Screen Validators).
        - Bulk tag adding/removing via context menu action for the selected tables.

      <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/tagging.png?raw=true" width="600" />

- **Zen Studio and Zaccaria Tables Support**
    - The emulator support for Zaccaria and Zen Emulators have been improved. The table overview does show now more
      accurate table columns and sidepanels depending on the selected emulator.
    - The play button is now shown for these emulators too. It allows launching tables via the Popper frontend or via
      **Steam**.

      <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/launch-steam.png?raw=true" width="500" />

    - **ALT Color** management has been enabled for FX and FX3. Note that you need to configure the game itself in order
      to
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
    - Detection of included VBS scripts when parsing the table script and analysis of these scripts when scanning game.
    - Saved VP-spreadsheet ID in XML database file for PinballX / PinballY.
    - Added VPX validator, triggered when an included script is not present in the **scripts** folder.
    - Added **Save globally** button to store DMD position globally.
    - Added detection of dual Backglasses and support of mode in backglass setup.
    - Removed .ini section since the .ini file can now directly be edited from the table overview.
    - Added edit, upload and delete actions for .ini, .res. and .pov files to the section "Table Data".

      <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/table-files.png?raw=true" width="500" />

- **Media Management**
    - **Added a new media type: Logo**. It generally corresponds to a small horizontal image. Like a wheel, it helps
      to identify a table visually but is simpler, generally the name of the table written with the font of the table.
    - Added zones to upload and search logos in MediaManager and in the Table Media sidebar.
    - Added checkbox in media preference to add a validator that check presence of logo.
    - For PinballX and PinballY, auto-invert playfield assets when copied from non PinballX assets source.
    - For PinballY, added search section on Superhac, Kongedam tutorials and personal assets sources.

        <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/logo-asset.png?raw=true" width="600" />

    - Added new default asset source: Table logos. These assets are solely for the "Logo" screen and can be used
      for the highscore card or wheel design.

      <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/logo-source.png?raw=true" width="600" />

- **Highscore Cards / Wheel Designer**
    - Added wheel generator, default template generates Tarcissio styled-wheels.
    - The **Highscore Card** tab has been renamed to **Designer** accordingly.
    - In the frame sidebar section, the option to upload frame images has been added. A Tarcissio wheel frame and a
      black wheel frame are provided by default.
    - The **logo** media from the **Logo** media source can be used in your wheel, it is accessible in **Other Media**
      section of the designer. This is even the main reason behind its introduction.
    - Added possibility to backup existing assets when generating cards. An option has been added to the highscores card
      preferences.
    - Refactored how the VPin Studio finds a previously generated card. If backup is activated, existing cards not being tagged,
      the server will generate new highscore cards in addition to the existing one. Old ones can be deleted as obsolete.
      The new ones will be updated automatically.

      <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/cards/wheel-designer.png?raw=true" width="700" />

- **Backglass Server Preferences**

  - Added option **Extended "B2STableSettings.xml" Search** which allows to configure where to look for the B2STableSettings.xml file.

## Bugfixes

- **ALT Color**: Fixed deletion of ALT color files.
- **VPS Updates**: The VPS update indicator in the table overview is working correctly now. E.g. the VPS version **1.4**
  and the table version **1.4.0** where detected as identical now which wasn't the case before.  
- **Backups**: Added custom B2STableSettings.xml to backups when the file is part of a separate table folder. 

