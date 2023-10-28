## Release Notes


### Changes

**Discord Messages**

- Added message filtering by an allow list. They are configurable in the highscores settings.

**Overlay**

- Added dashboard variants in the preferences section. All variants support HD, WQHD and 4K resolutions. 

**Preferences**

- Reorganized and renamed preferences sections (still figuring out the best grouping here).
- Added "Virtual Pinball Spreadsheet" validator: The new validator checks if the table has been mapped against the VPS. This way, version updates can be detected and displayed in the table overview.

**Table Management: Table Settings**

- Added bulk operation to execute the "Auto-fill" for all tables.


**Table Management: PinUP Popper Table Settings**

- Added "Fix Version" button. If selected, the version of the selected VPS table will be written into PinUP Popper. As a result, the update indicator in the table overview is not shown anymore.

**Table Management: Highscores**

- Added bulk operation to execute highscore scans for all tables.
- Changed green open button for this section so that the folder opens for the given highscore format.
- Changed status display: when the Studio has a problem reading the highscore, only the error message is shown now (well, this is actually more of a bugfix since some errors were not displayed at all). 

**Table Management: Spreadsheet Data**

- Added auto-resolving for table version. Not only the table itself is resolved automatically (when the view is selected), but also the table version will be resolved. Note that this auto-assignment does not work a 100%, so its recommended to check these manually too.
- Added auto-fill button which executes the automatic table assignment.
- Added bulk auto-fill button which executes the automatic table assignment for all tables.
- The table assignment is not executed automatically anymore when the view is opened for a table selection. The auto-fill button or textfield and combo-box must be used.

**Table Management: Script Details**

- Renamed "Tablename" to "Alt. ROM Name".

**Table Management: Overview**

- Renamed highscore type "EM" to "Text" (see bugfixes).
- Added version icon: If the table has been matched against a version of the Virtual Pinball Spreadsheet and the version stored in PinUP Popper does not match against it, an update icon is shown in front of the version number now. The feature can be disabled in the UI settings.

### Bugfixes

- Fixed initial game scan: All VPX emulators are taken into account.
- Fixed table (re)loading: Tables that are detected during a reload are not scanned "on-the-fly" anymore, but in a separate progress dialog. 
- Increased icon size of table overview buttons toolbar.
- Fixed update dialog: The update progress bar showing progress again (for the next update of cause).
- Improved update button: The update button is now shown together with the target version.
- Improved updates: The update button is now also shown when the server is older than the client, so that the update dialog can be invoked too.
- Removed "EM" table hint for the highscore filename field in the "Script Details" section: Since not only EM tables, but also original tables use text-files for storing highscores, the "EM" hint was wrong.
- Improved performance of recent highscores (which should fill up the corresponding dashboard widget faster).
- Fixed auto-shutdown: When jobs are running, e.g. table exports, the shutdown is prohibited until all jobs are finished.
- Fixed highscore parsing for "SpongeBob's Bikini Bottom Pinball VPW". The table has a custom highscore format that was not supported yet.
- Fixed highscore parsing for EM highscore textfiles with 12 or 14 lines (e.g. "Apache Playmatic").
- Fixed highscore parsing for older SS tables which only stored one score (e.g. "Algar").
- Changed table-scanning so that when only a "tablename", but ROM name is found, the resolved "tablename" value is used as ROM name.

### Known Bugs

- Table sorting is broken :/