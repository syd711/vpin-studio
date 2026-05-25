## Release Notes 5.0.0

### !!! Important Notice !!!

We are happy to announce that the next major release of VPin Studio 5.0 has been published. While it does not introduce many flashy new features, we have extensively revamped the underlying codebase to ensure the project's long-term maintainability and support future development.
The major drawback:

**You have to reinstall the Studio Client(s) + Server!!! Studio 5.x updates can't be applied with a 4.x version!**

**Use the existing installation folder for this so that all your data remains untouched by this!**.

Download Links:
- Full Installer: https://github.com/syd711/vpin-studio/releases/download/5.0.0/VPin-Studio-Full-Installer-5.0.0.exe
- Client Installer (Windows): https://github.com/syd711/vpin-studio/releases/download/5.0.0/VPin-Studio-Client-Installer-5.0.0.exe  

### Changes

- **Java 25 Migration**
  - Code base has been migrated from **Java 11 forward to Azul Zulu Java 25(!)**, including an update for all 3rd party dependencies. 
- **Competition Wheel Icons**
  - Added icon augmentation to APNG wheels.
- **Splash Screen**
  - Added details to splash screen showing connection steps/attempts.
- **Mac**
  - App Icon now works correctly as dynamic icon with MacOS 26+.
  - Fix splash screen not showing.
  - Added background to DMG.
  - Changed DMG creation to use create-dmg instead of jpackage for more robust options and simpler workflow.
- **Future Pinball**
  - With long overdue, VPin Studio 5 supports Future Pinball highscores now too. The highscore support reflects in the Designer section too, which allows to design highscore cards for Future Pinball now. Note that because of the limited test data a lot of highscore lists might still look broken. Please submit your fpRAM file in that case on our Discord so that we can improve the parsing.

### Breaking Changes

- **Players**: The players "iScored Name" has been renamed to "Competition Name" and is used for Discord too. **You have to reconfigure the name as the old value has been discarded!**
- **Notifications**: The notification settings for iScored have been resetted.

### Bugfixes

- **Table Overview**: Fixed issue that sometimes not all tables have been loaded initially.
- **DMD Screen Capture**: Fixed the DMD capture when dmddevice is set to double or scale2X scaler mode. Used to transfer DMD score in WoVP.
- **Statistics**: Fixed issue analytics not being updated for PinballY in the pause menu.
- **Highscores**: Fixed VPReg.stg file lookup.
- **VPU/VPF**: Fixed login tests.
- **Discord Competitions**: Fixed issue that the player name used being inconsistent for the first and consecutive scores.
- **Player Avatars**: Fixed issue of the white outer avatar ring keep growing with every save.
- **Future Pinball**: Fixed installation of .fpl files.
- **Update Info Dialog**: Fixed size issues for smaller screens.

### VPin Mania

- Added synchronization of Future Pinball highscores.

