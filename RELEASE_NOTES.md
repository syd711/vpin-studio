## Release Notes 5.4.2

### Changes

- **Media Recorder**: Added option to disabled NVIDIA hardware acceleration support.
- **Highscore Cards**: Invalidated PinUP Popper display cache before showing highscore cards on table launches to ensure the correct display location is used.
- **System Manager**: The target folder of components (e.g. Visual Pinball, VPinMAME) can now be set at any time, also for installed components, and is persisted on the server. A new "Reset" button restores the automatically detected folder.
- **Visual Pinball / VPinMAME**: A configured target folder is now respected for version checks, modification date and the VPinMAME folder resolution. This fixes portable or non-default installations, where no ".vpx" file association exists to detect the folder from.
- **VPinMAME**: If no VPinMAME folder can be detected, the folder configured for a VPX emulator in the frontend is used as fallback.
- **Serum / VNI**: The legacy "altcolor" folder is now resolved from the table emulator's VPinMAME folder.
- **WOVP Challanges**: The dashboard highscore list (outliner on the right) now scrolls automatically to the position of your score.
- **MacOS**: Changes to allow Mac build on a Rosetta free machine.

---

## Release Notes 5.4.1

### Changes

- **Highscore Cards**: Added "Topper" for target screen selection.
- **Studio Client**: Fixed Remote OS resolving.

**For those who had updated to 5.4.0 with remote connection issues: Just download the VPin-Studio.zip from the release artifacts and replace the one on your remote PC. You should be able to connect again then.**

---

## Release Notes 5.4.0

### Changes

- **Linux Support (experimental)**: The VPin Studio server can now run on Linux in Standalone mode, next to a standalone build of Visual Pinball X (10.8.1) and any frontend. The Studio client connects to it like it would to a Windows cabinet. Please note that this feature is **experimental**.
  - Supported: table management (scanning, uploads, VPS matching, backglasses, ROMs, nvram, cloning, deleting), launching tables from the client, highscores from nvram and `VPReg.stg`, and the pause menu and overlay.
  - Not available: features depending on Windows tools or APIs, e.g. the media recorder, DOF, PinVol, PINemHi, Popper/PinballX/PinballY, and the highscore monitor.
  - Installation instructions: https://github.com/syd711/vpin-studio/wiki/Linux-Server-Installation-Instructions
- **Theming**: Added theming support for the pause menu and notifications. For both components a proper documentation with sample files have been added (https://github.com/syd711/vpin-studio/wiki/Theming). **If you actually use this, please share some screenshots on my Discord!**
- **WOVP Competitions**: 
  - Disabled tables are ignored now during the synchronization process with the WOVP server. Also, newer table files of the same version are preferred now.
  - API keys are sortable now. This allows to modify the order of players selectable in the pause menu. 
- **Media Recorder**: Fixed various issues and added error reporting to the client.
- **Misc**: 
  - Fixed typos.
  - Fixed the overall drag-n-drop and resize handler which stuck several times.

