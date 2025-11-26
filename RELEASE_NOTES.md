## Release Notes 4.4.3

## Changes

- Fixed PUP Pack Tweaker menu entry so that it is only enable when working on the cab.
- Fixed **updater dialog progress bars** that had a height of 0 pixels. They are now visible again.
- Fixed the wrong error message that setting a highscore screen is required when generating a wheel icon from the wheel designer.
- Disabled **"Force Stereo" validation** when the game has an ALT sound package installed.
- Added **option to hide the green table marker icon**. The icon is now hidden by default. This visual declutter cleans up the visual information of the table overview a bit. If you feel nostalgic, you can enable them again in the client preferences.
- Added **additional "Wheel" column to the Designer** that shows if a table has an existing wheel icon.
- Added additional **default media item preview in the Designer** that shows which asset is currently used by the corresponding screen and would be overwritten.


---

## Release Notes 4.4.2

## Changes

- Fixed saving the view state of the Table Data Manager dialog.
- Fixed **competition summary widget** which did not render values from finished competitions.
- Fixed reading and writing of backglass settings that use an aliased ROM name. 
- Fixed highscore parsing for "King Kong".
- Fixed default selection of the VPX emulator in the table overview (removed the sorting by name here).
- Fixed **critical data access issue** where it might have happened that duplicate game details were saved in the VPin Studio database and lead to consecutive errors.  
- Re-enabled PUP pack editor and the 3rd party tool **PUPPackTweaker** (https://github.com/mat1111x/PupPackScreenTweaker). The tools are visible in the PUP packs side section. Shout out to @YabbaDabbaDoo who provided an updated version build from the latest sources.
- Added missing frontend-running check for table uploads which will fail when the database is locked by the frontend management tool, e.g. PinUP Popper Setup.
- Added quick-edit button to the title of the **Script Details** section. This way, you can quickly open the table script without the need to expand the section. Note that always the default system editor for .vbs files is used here!
- Added **automatic fill-up of highscore lists**: For older pinball tables (especially EM tables) often only 1x highscore entry was shown on highscore cards. By default, the Studio increases the list with **up to five values** which are taking from the highscore history of the table.
- Added preview checkbox for the media upload dialog. The status is persisted and the checkbox is enabled by default. 

---

## Release Notes 4.4.1

## Changes

- Added some blind fixes for possible errors in the "Table Subscriptions" view.
- Added missing logo index file (which fixes empty search results for logos).

---

## Release Notes 4.4.0

## Changes

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

      <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/table-buttons.png?raw=true" width="600" />

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
      for highscore cards or wheel designs.

      <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/logo-source.png?raw=true" width="600" />

- **Backglass Server Preferences**

  - Added option **Extended "B2STableSettings.xml" Search** which allows to configure where to look for the B2STableSettings.xml file.

- **Deletion Dialog**
  - General revamp of the dialog to improve the user experience. 
  - Added option to delete
    - entries from the DMDDevice.ini file.
    - entries from the B2STableSettings.xml file.
    - entries from the VPMAlias.txt file.
    - the ROM file.
  
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/tables/table-delete.png?raw=true" width="500" />

## Bugfixes

- **ALT Color**: Fixed deletion of ALT color files.
- **VPS Updates**: The VPS update indicator in the table overview is working correctly now. E.g. the VPS version **1.4**
  and the table version **1.4.0** where detected as identical now which wasn't the case before.  
- **Backups**: Added custom B2STableSettings.xml to backups when the file is part of a separate table folder. 
- **Backups**: The backup is now written into a temporary local file and then copied to the target folder. This should solve issues when creating backus for a NAS device. 

