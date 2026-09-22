## Release Notes 5.3.2

### Changes

- **Theming**: Added theming support for the pause menu and notifications. For both components a proper documentation with sample files have been added (https://github.com/syd711/vpin-studio/wiki/Theming).
- **WOVP Competitions**: Disabled tables are ignored now during the synchronization process with the WOVP server. Also, newer table files of the same version are preferred now.

---

## Release Notes 5.3.1

### Changes

- **WOVP Competitions**: The pause menu entry to submit scores to WOVP contains an optional text input now. It is disabled by default, but can be enabled in the WOVP settings and be used to submit additional information about the score or screenshot.
- **Media Recorder**: Enabled recording notifications again. These had been disabled because some users experienced focus issues, but the settings were never removed—they were simply ignored. The old behaviour has been restored for now.
- **iScored Integration**: Added tagging support for iScored game rooms. This means you can auto-tag tables that are matched against one of the Game Rooms. The configuration dialog for a Game Room has an additional "Tags" field now. These table tags are added or removed when the game room is synchronized. E.g. you can add the tag _Syd711_ and create a new SQL playlist with _SELECT * FROM Games WHERE visible=1 AND EMUID = 1 AND tags like '%Syd711%' AND TourneyID like '%iscored%' ORDER BY GameDisplay_. This allows you to create separate playlists for every iScored Game Room. (https://github.com/syd711/vpin-studio/wiki/iScored#iscored-playlists)
- **Uploader**: Fixed big performance issue for 7z based archives. E.g. PUP packs like for Superman LE are extracted way faster now.

---


## Release Notes 5.3.0

### Changes

Hi there!
Olá!
¡Hola!
Hallo!
Bonjour!
Ciao!

The Studio finally supports localization, starting with the languages: English, Portuguese, Spanish, German, French, and Italian.

**VERY IMPORTANT!!!**

The localization required a review of all(!) components, dialogs, preference pages, and settings to support dynamic layouts that handle different text lengths.
If any components look broken or have missing translations, please drop a screenshot on my Discord! Also: **A lot of dialogs may look broken. Please reset them here:**

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/reset-dialogs.png?raw=true" width="600" />


### Changes

- **Cabinet Monitor**
  - Added new **Streaming View** mode: a two-column layout that shows the PlayField screen in portrait orientation on the left and all other screens stacked vertically on the right. Both columns scale proportionally with the window size.
- **Backglass Manager — .res File Generator**
  - Added **Custom Width** display option alongside the existing Stretch and Center modes. When selected, a pixel spinner lets you pin the backglass to an exact width; the height is derived proportionally and the result is centred on the screen. The preview updates live and the value is restored correctly when an existing .res file is loaded.
- **Future Pinball**
  - Enabled table scan with PUP pack detection (thanks @select.nl!!!).
- **Table Overview**
  - Fixed issues with .directb2s file detection and refreshes.
- **Notifications**
  - Added option to rotate notifications, which is required when they are not shown on the playfield.
  - A server restart is no longer required when the target screen for notifications is changed.
- **Move/Copy Tables**
  - Added bulk action support.
  - Added option to move/copy tables into subfolders. 

### Bugfixes

- **Notifications**
  - Fixed notification tests.
- **Media Recorder**
  - Added NVIDIA detection so that the ffmpeg command can use the GPU for video encoding. This was a significant gap between the Studio and Popper and should improve recording performance.
- **Wheel Designer**
  - Fixed an issue where the wheel icon had not been generated for some tables.
- **Java Runtime**
  - Fixed a JDK issue by adding JVM parameters to prevent it from recurring. This fix cannot be applied through an update — only by reinstalling. If you experience regular crashes of the server or client, you can reinstall the Studio over the existing installation to rewrite the affected settings.
- **ALT Color**
  - Fixed issue with aliased cROMc-based ALT color files.
- **Updater**
  - Improved integrity check of downloaded updates, resulting in more graceful errors when failing.
- **PUP Packs**
  - Added lookup check for FP-based tables.
