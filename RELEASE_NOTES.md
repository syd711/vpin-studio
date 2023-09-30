## Release Notes

**The first version with PinUP Popper media support.**

The media can be configured as part of the new asset manager, available in the "PinUP Popper Media" section.

### Changes

**Tables Management: Overview**

- Added new toolbar button: Asset Manager. The Asset Manager lets you select assets for the different screens available in PinUP Popper and shows the already installed assets for the selected screen.
- Removed renaming buttons: these actions are now part of the "Table Settings" dialog. The table name and display name can be changed there.

**Tables Management: PinUP Popper Media**

- Replace "on-hover" upload button with the new asset manager button. The button initializes the asset manager with the selected table and screen.

**Table Management: PinUP Popper Table Data**

- Changed this section name to "PinUP Popper Table Settings"
- Added green "open" button which opens VPX's tables folder.
- Added "Game Type", "Game Version (File Version)" and "Game Theme" fields.
- Added "Auto-Fill" button which fills missing data by looking them up from the table metadata and VPS.
- Added "Launcher" combo-box which allows to select from the list of available VPX exe files combined with the alt-launcher entries from PinUP Popper. The green launcher icon on the table overview toolbar will use this selection!

**Table Management: Backglass**

- Added "real" backglass preview: The previous image was just a plain image export while the new preview will hide the grill if the corresponding configuration is found in the backglass. Also, the DMD image is shown if part of the directb2s file. I'm still not 100% sure if I got everything right there, so additional changes are likely there. Note that not all EM tables are supported.

**Table Management: Highscores**

- Aligned layout of the "Reset Highscore" dialog.

**Table Management: POV**

- Added green "open" button which opens VPX's tables folder.

**Table Management: Script Details**

- Removed table file rename button: this action is now part of the "Table Settings" dialog. The table name and display name can be changed there.

**Table Management: ALT Sound**

- Added AltSound 2.0 support: The "ALT Sound" section supports now both formats to be edited: "g-sound" and "altsound". If you have an altsound package installed that is set to format "g-sound", the new editor for this format will be used. 

**Preferences: Bot Preferences**

- Added "Bot Name" field which should help to identify the bot that is configured in case you are using different ones.

**Competitions**

- Added rules section for every competition dialog.
- Switch Discord competition dialogs to a two column layout, so that they still fit on HD screen.

### Bugfixes

- Removed table data field "ROM Url".
- Unified some "Edit" icons.
- Fixed missing **default message emitting** for new highscores: these have been canceled if there was at least one subscription.
- Fixed wrong score limit for initial highscore posts of subscriptions and Discord competitions.
- Fixed automatic resetted nvram downloading (if you have nvram files in the installation folder of the VPin Studio, just delete them).
- Fixed asset lookup: Instead of the game filename, the game name is used which caused various error in looking up media, deleting and renaming tables. This also affected the upload and clone/replace table actions.
- Added fallback for ROM scanning: if no ROM name could be resolved, an additional lookup is made into the table details "ROM Name" field of PinUP Popper as a fallback.