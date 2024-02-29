## Release Notes

### Changes

- DOF Preferences: Added second DOF installation folder for people using 32-bit and 64-bit installations in parallel.
- DOF Preferences: Added option to let the VPin Studio server synchronize DOF settings.
- System Manager: Added **Serum** support. Serum is already part of Freezy, but if you really want to ensure to have the latest and greatest Serum support, you can download the required DLLs this way too.
- Pause Menu: Added preferences checkbox for the tutorials, so that you can skip the rendering of YouTube entries completely (in case you have downloaded all tutorial videos instead).
- Highscore Card Settings: Added hint about the transparency settings when a Popper menu screen is selected that has no transparency enabled. This setting is required when you want to use transparent highscore cards.
- Table Management: Added **NVOffset** validator. If a table uses an NVOffset, all tables using the same ROM must have set it in order to work.
- Table Management: **NVOffset filter** option to filter for tables that have set **NVOffset(<?>)** set in their script.
- Table Management: **Alias filter** option to filter for tables that have set an entry in the **VPMAlias.txt**.
- Table Script Section: Moved Alias mapping stuff into the **VPin MAME** section, because that's where is belongs.
- Table Script Section: This section now supports reading the **Screenshot** information out of the VPX file:

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/table-screenshot.png" width="600" />

- Table VPin MAME Section: The moved **VPin MAME** section has been re-worked and you can edit the **VPMAlias.txt** directly now.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/mame.png" width="600" />

- Added Remote File Editor: It's a first very basic text editor, but as a first step, you can edit the **DmdDevice.ini** and the **VPMAlias.txt** now remotely. (The editor can't do much since text editing with Java is like driving Formula 1 with Fiat Panda).

### Bugfixes

- Enabled folder selection for VPin Studio installer files.
- Enabled dashboard support for **rotated playfields**: With long overdue, this should finally work for those of you using rotated playfield screens. I've only supported the default portrait mode of Windows here, no additional rotations.
- Fixed calculating the correct nvram file when table is using **NVOffset**: Somehow, I never got this right.
- Fixed **ROM alias** calculation for tables. Yes, I never got this right too.
- Table Data Manager: Fixed error in ROM validation.
- Fixed Highscore Section: The section now always re-scans the highscores and the raw data is taken from this scan, not from a previous database entry. After configuration updates, old values may have been shown there.


### Additional Information

https://vpuniverse.com/forums/topic/8278-nvoffset-another-summary-of-how-it-works/
