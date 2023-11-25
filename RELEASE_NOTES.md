## Release Notes


### Changes

**PinUP Popper 1.5 Support**

Added additional fields to the "PinUP Popper Table Settings" section. You can now configure all new PinUP Popper metadata fields there. The new fields are only shown if you already use version 1.5.

**Added New "System Manager"**

Added main navigation item "System Manager". You can use the System Manager to check if your VPin software stack is up-to-date.
The integrated installation simulator allows to to check if you want to to use the support automatic update.
The System Manager uses the latest Github releases from various projects and compares these against your local files.
Note that there is always the possibility that the released artifacts of these projects may change in an unexpected way.


### Other Changes

**Navigation**

- Switched to MUI icons.

**Overlay**

- Added new overlay style that can embed an external page. This way you can embed your iscored.info dashboard.

**Preferences**

- Combined "Service Info" and "Service Options" to "Service" entry and moved this to the Studio settings section.
- Renamed section to "VPin System Preferences"
- Added reset button for "Do not show again" dialogs.
- Added configuration parameters for overlay widget "External Page".

<img src="https://github.com/syd711/vpin-studio/blob/update-installer/documentation/releases/2.3.0/ui-prefs.png?raw=true" width="400" />


**Table Management: Overview**

- Added "VPS" toolbar button for a quicker access to missing resources.
- Added "Edit Popper Table Data" toolbar button for a quicker access to the table data.

**Table Management: PinUP Popper Table Settings**

- Added missing "custom2" and "custom3" fields.
- Moved "Keep Screens On" dialog into the regular table data edit dialog.
- Moved other input components ("Status", "Alternative Launcher") into the regular table edit dialog.
- Added support for all new Popper 1.5 fields (including WIP status).
- Reorganized fields into sections.

**Table Management: DMD**

- Added DMD section for uploading Flex- or UltraDMD packages.

**Table Management: ALT Color**

- Moved FlexDMD button to DMD section.

**Competitions: Table Subscription & Discord Competitions**

- Improved competition/subscription "Synchronize" action: Instead synchronizing only the latest highscore of a table against a competition or subscription, the full highscore history is "replayed", starting the start-date of a competition or subscription. This way, untracked highscores are pushed into table subscription channels.
- Added "Synchronize All" buttons.
- Added offline players to subscription: This way the highscores of other players using your VPin will also be pushed into the subscription. This will also include the initial highscores of a table. You can avoid this using the highscore filter option in the preferences.


### Bugfixes

- Fixed error handling for broken VPX files.
- Fixed ignored "enabled" flag when highscores were fetched for generating highscore lists on the dashboard.
- Fixed the "PinUP Popper Custom Preferences" section.
- Improved text file highscore parsing: If the highscore text filename is not set, the resolved alternativ ROM name/tablename is used as a fallback (E.g. the Alt. ROM name has value \"Apache-playmatic1975\", the server will search also the highscore file **User\Apache-playmatic1975.txt**. 
- Fixed error in table scanning that led to unnecessary deep scans of table files (parsing with binaries). So the overall table scan is faster now.
- Fixed table's table sorting - wuhu!