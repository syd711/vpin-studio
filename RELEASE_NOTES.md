## Release Notes

### Changes

- Highscore Parsing: Added support for a lot more VPReg.stg based highscore formats.
- Highscore Parsing: Added support for single score tables.
- Pause Menu: Added view option "Style" to the preferences. You can now choose if entries should be rendered as part of the pause menu or if Popper assets of the "Info", "Help" and "Other2" screen should be shown on their configured screen locations. 

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/pause-menu.png" width="600" />

### Bugfixes

- Fixed Studio update issues: Both of the installer .exe files did not set any privileges. So when installed in the **program folder** of Windows, the update did only work when the program was started as administrator. I've never noticed because I did use another folder all the time.
- Fixed Table Uploads: The table versions have been resetted on upload + replace which should not be the case.
