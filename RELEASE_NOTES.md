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

The localization required a review at all(!) components, dialogs, preference pages and stuff to support dynamic layouts that deal with different text length.
So if components look broken, please drop a screenshot on my Discord! Also: **A lot of dialogs may look broken. Please reset them here:**

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/reset-dialogs.png?raw=true" width="600" />


### Bugfixes

- **Notifications**
  - Fixed notification tests.
- **Media Recorder**
  - Added NVIDIA detection so that the ffmpeg command can use the GPU for the video encoding. This was a huge gap between the Studio and Popper and should improve the recording performance.
- **Wheel Designer**:
  - Fixed issue that for some tables the wheel icon has not been generated.
- **Java Runtime**:
  - Fixed JDK issue by adding some JVM parameters that should avoid that. This fix can't be applied through an update, only by a reinstallation. If you experience regular crashes of the server or client you can re-install the Studio over the existing installation to rewrite the affected settings.
- **Move/Copy Tables**
  - Added bulk action support.
  - Added option to move/copy tables into subfolders.
- **ALT Color**
  - Fixed issue with aliased cROMc based ALT color files.
- **Updater**
  - Improved integrity check of downloaded updates, resulting is more graceful errors when failing.
- **PUP Packs**
  - Added lookup check for FP based tables.
