## Release Notes


### Changes

**VPin Studio Connection Handling**

- Added error handler for connection timeouts: If a request failed (e.g. because the wifi connection is not stable), the Studio will exit to the launcher screen.

**Table Management: Multi VPX Emulator Support**

- This was a design flow from the beginning: The table lookup isn't configured through the installation of the VPin Studio anymore, but solely through the data that is found in the PinUP Popper database. So PinUP Popper is the single source of truth regarding the VPX emulator setup now. You can also configure multiple VPX emulators now. The VPin Studio will resolve the affected emulator automatically (if possible), otherwise you will find a new emulator combobox in some of the upload dialogs to determine an upload target. As a result, the server setup only requires the location of PinUP Popper.
- Added emulator name field to **PinUP Popper Table Settings** section.
- Added emulator combo-box for table uploads.
- Added emulator combo-box for ROM uploads.

**Table Overview**

- Added new column "version". With more and more VPX releases coming out, this should help to keep an overview what version of a table is installed. (**Tip**: Use the "Autofill" button on the table settings section to set the version automatically.)
- Added "disabled" status: If a table is disabled, all the row texts have a grey color. The flag can be changed in the table settings section. 

### Bugfixes

- Fixed miscellaneous issues with the bot token preference page and the token validation.
- Fixed table data auto-fill function so that the table version and author are applied, even if no VPS mapping was configured.
- Improved bot configuration help texts (hopefully).
- Fixed version updates after table uploads: If a table is uploaded, replaced or cloned, the version number is reset and an auto-fill is triggered to update PinUP Popper table data to the version stored in the VPX script.
- Fixed reading of player highscores for deleted games.
- Fixed duplicate name check for Discord bots.
- Fixed errors in MAME settings, backglass settings and highscore backups when the ROM name of a table wasn't set.
- Removed ROM alias mapping cache: performance is not a problem so this way manual changes on the original files are always included too.
- Optimized execution time of some Discord operations.
- Fixed accidental "Popper Screens" dialog opening in the table preference section which happend during the PinUP Popper running check.
- Fixed installer so that highscore cards are disabled by default. Otherwise the initial table scan would take much longer.
- Improved initial table scan dialog so that the scanned table names are shown.