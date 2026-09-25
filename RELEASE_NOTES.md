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

