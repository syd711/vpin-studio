## Release Notes


### Changes

**PinUP Popper 1.5 Support**

- Added additional fields to the "PinUP Popper Table Settings" section. You can now configure all new PinUP Popper metadata fields there. The new fields are only shown if you already use version 1.5.
 

**Table Management: DMD**

- Added DMD section.

**Table Management: PinUP Popper Table Settings**

- Added missing "custom2" and "custom3" fields.
- Moved "Keep Screens On" dialog into the regular table data edit dialog.
- Moved other input components ("Status", "Alternative Launcher") into the regular table edit dialog.
- Added support for all new Popper 1.5 fields (including WIP status).
- Reorganized fields into sections.

**Table Management: ALT Color**

- Moved FlexDMD button to new DMD section.

**Competitions: Table Subscription & Discord Competitions**

- Added "Synchronize All" button.
- Improved competition/subscription "Synchronize" action: Instead synchronizing only the latest highscore of a table against a competition or subscription, the full highscore history is "replayed", starting the start-date of a competition or subscription. This way, untracked highscores are pushed into table subscription channels.
- Added offline players to subscription: This way the highscores of other players using your VPin will also be pushed into the subscription. This will also include the initial highscores of a table. You can avoid this using the highscore filter option in the preferences. 

### Bugfixes

- Fixed ignored "enabled" flag when highscores were fetched for generating highscore lists on the dashboard.
- Fixed the "PinUP Popper Custom Preferences" section.

### Known Bugs

- Table sorting is still broken :/