## Release Notes


### Changes

**Discord Messages**

- Added message filtering by the allow list, configurable in the Highscores settings.

**Preferences**

- Renamed "PinUP Popper Validators" to "Screen Validators".
- Renamed "VPX Validators" to "Configuration Validators".

**Table Management: Spreadsheet Data**

- Added auto-resolving for table version. Not only the table itself is resolved automatically (when the view is selected), but also the table version will be resolved. Note that this auto-assignment does not work a 100%, so its recommended to check these manually too.

**Table Management: Overview**

- Renamed highscore type "EM" to "Text" (see bugfixes).
- 

### Bugfixes

- Fixed initial game scan: All VPX emulators are taken into account.
- Fixed table (re)loading: Tables that are detected during a reload are not scanned "on-the-fly" anymore, but in a separate progress dialog. 
- Increased icon size of table overview buttons toolbar.
- Fixed update progress bar showing progress.
- Removed "EM" table hint for the highscore filename field in the "Script Details" section: Since not only EM tables, but also original tables use text-files for storing highscores, the "EM" hint was wrong.
- Fixed highscore parsing for "SpongeBob's Bikini Bottom Pinball VPW". The table has a custom highscore format that was not supported yet.
- Improved performance of recent highscores (which should fill up the corresponding dashboard widget faster).
- Improved update button: The update button is now shown together with the target version.
- Improved updates: The update button is now also shown when the server is older than the client, so that the update dialog can be invoked too.
- Fixed auto-shutdown: When jobs are running, e.g. table exports, the shutdown is prohibited until all jobs are finished.