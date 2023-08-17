## Release Notes


**Preferences: Bot Preferences**

- Added option to disable bot commands.
- Added more user-friendly UI for the user allow list creation.

**Preferences: PinUP Popper Custom Options**

Added a new preferences section for the custom options of PinUP Popper, because... why not?

**Table Management**

 Added file drop support for the section: DirectB2, PUP Pack, ALT Color, ALT Sound.

**Competitions: Table Subscription**

The competitions section has a new tab now: "Table Subscriptions".
These combine the best of both worlds: offline competitions and Discord competitions.
Table subscriptions allow you to create a separate Discord channel for a table, depending on the ROM name of it.
New highscores will be posted there so you can always check your current score of all tables via Discord.

**Also**, other players can subscribe to the channel. By doing this, their highscores will compete with yours.
More details about this can be found here: https://www.youtube.com/@vpin-studio/videos

**Maintenance Mode**

Added maintenance mode when VPin Studio is used from remote. If enabled, a full screen window will
open, indicating the maintenance mode. When entered, all VPX and PinUP processes will be terminated.
When the maintenance mode is ended, Popper will be restarted automatically.
The background image used here can be customized (see https://github.com/syd711/vpin-studio/wiki/FAQ).

**Bug Fixes**

- Updated JavaFX: I noted that this update resulted in a more reliable playback of videos. Note that videos with an alpha channel (like used in some loading videos) are still not supported.
- Fixed erroneous time field initialization for existing competition dialogs.  
- Re-enabled and fixed pinemhi settings.
- Fixed wrong ALT color validation message text.
- Fixed and improved default image extraction.
- Fixed modal mode of all upload dialogs.
- Fixed refresh/reload issues with PUP packs, ALT sound and ALT color.