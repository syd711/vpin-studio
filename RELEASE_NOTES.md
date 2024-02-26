## Release Notes

### Changes

- DOF Preferences: Added second DOF installation folder for people using 32-bit and 64-bit installations in parallel.
- DOF Preferences: Added option to let the VPin Studio server synchronize DOF settings.
- System Manager: Added Serum support. Serum is already part of Freezy, but if you really want to ensure to have the latest and greatest Serum support, you can download the required DLLs this way too.
- Pause Menu: Added preferences checkbox for the tutorials, so that you can skip the rendering of YouTube entries completely (in case you have downloaded all tutorial videos instead).
- Added Remote File Editor: It's a first very basic text editor, but as a first step, you can edit the "DmdDevice.ini" file now remotely (in the System Manager). The editor can be used for future releases to edit other cabinet files too.
- Highscore Card Settings: Added hint about the transparency settings when a Popper menu screen is selected that has no transparency enabled. This setting is required when you want to use transparent highscore cards.
- Added **NVOffset** validator: If a table uses an NVOffset, all tables using the same ROM must have set it in order to work.
- Added **NVOffset** filter option: Filter for tables that have set **NVOffset(<?>)** set in their script. 

### Bugfixes

- Enabled folder selection for VPin Studio installer files.
- Enabled dashboard support for rotated playfields: With long overdue, this should finally work for those of you using rotated playfield screens. I've only supported the default portrait mode of Windows here, no additional rotations.
- Fixed calculating the correct nvram file when table is using **NVOffset**: Somehow, I never got this right.
- Table Data Manager: Fixed error in ROM validation.
- Fixed Highscore Section: The section now always re-scans the highscores and the raw data is taken from this scan, not from a previous database entry. After configuration updates, old values may have been shown there.


### Additional Information

https://vpuniverse.com/forums/topic/8278-nvoffset-another-summary-of-how-it-works/
