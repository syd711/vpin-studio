## Release Notes

### Table Data Management

**Sorry for the wallpaper here, but it's a bigger change and long overdue. So here we go...**

**tltr:** Checkout the brand new "Table Data Manager" (edit table) and the new "Server Settings" in detail with all descriptions!

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/table-manager.png" width="600" />

The **Table Data Manager** is now "the one" dialog to rule them all. It consolidates the 4x existing data sources for table data:
- PinUP Popper table data 
- VPin Studio scanned table data
- Virtual Pinball Spreadsheet data
- VPX file script fields section

**It wasn't obvious where data was stored before and when exactly what value is used or preferred.**
From now on, **the values inside the PinUP Popper database will be the single source of truth and are always preferred over the scanned table values**.
As a result from this, large parts of the "Script Section" have been resolved into the **Table Data Manager** dialog (and moved lower to the bottom) and the "Popper Popper Table Data" section has been re-ordered.

**The VP-spreadsheet table mapping is now stored inside the PinUP Popper database too.** 
Since Popper does not provide special fields for these values, 
you can go to the server setting to change the default mapping there.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/field-mapping.png" width="600" />

Additionally, the copying of VP-spreadsheet table meta-data into PinUP Popper has been moved into **Table Data Manager** dialog too, so that one can see what data will be written.


**Ok, but why is this a breaking change?**

If you had stored invalid values for the Popper fields that are used for the highscore filename or the ROM name,
the highscores of these tables won't be resolved anymore. Even if your table scan resolved the correct ROM name of a table.
The field in Popper must either be empty OR you can apply the correct value so that it is saved there.

Phew, so that was too easy, right? Now the usual additional goodies...

### Other Changes

- Added checkbox to enable fuzzy lookup logic for the backglass server in the "Backglass Server" preferences.
- Improved navigation between the Table Data Manager and the Table Asset Manager, including previous and next button.
- Added tooltips for VPS feature badges.
- Added "Invalidate All" as split-button to the "Invalidate" button in the overview toolbar.
- Added auto-fill rules for the VPX meta-data fields notes and rules, so that these values are written to the "notes" and "details" fields of PinUP Popper 1.5. 
- VPS update flags are now resetted for the following uploads: backglass, table replacements, altsound, altcolor, puppack and pov files/archives.
- The automatic writing from mapped VPS data to PinUP popper has been removed again for: initial scans, table replacements and clones. It is only applied via the **Table Data Manager** or when a **new** table is imported.
- The "Table Import" dialog now provides a progress bar, so that the user gets feedback about the progress when a bunch of tables is installed at once.
- The media preview is now pausing when the window does not have the focus anymore (saving CPU usage here).
- Moved "Scan All" button into a split-menu button since it's not likely to be used that often.
- Re-arranged fields in the "PinUP Popper Table Data" section so that they match with the new Table Data Manager.
- Added option to upload **Music** archives. The upload accepts any zip with audio files (with or without "Music" folder in it.). I think we have all mandatory uploads then, right?
- Added the option to render highscore cards with a transparent background.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/cards/transparent-cards.png" width="600" />

### Bugfixes

- Fixed VPS tables view's sidebar section: All badges of a table are readable now and not abbreviated anymore. 
- Upload menu items sorted alphabetical now.
- Added missing labels for "Audio" and "Audio Launch" in the Popper media section.
- Maximizing the window will now work for the active screen.
- Fixed duplicate table scan during initial installation.
- Fixed VPS search: Unfortunately, only matches have been shown that start with the search term, not include them.

### Miscellaneous

Since the table scans are never a 100% correct, I started a score database which helps to fix table scans by providing 
additional data. So if you find table tables where the scan does not find the correct values, drop a message.
I can fix the scans with additional lookups here (the file will be automatically downloaded by the server):

https://github.com/syd711/vpin-studio/blob/main/resources/scoringdb.json
