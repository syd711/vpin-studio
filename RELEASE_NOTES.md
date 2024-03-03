## Release Notes

### Changes

- Pause Menu: Added view option "Style" to the preferences. You can now choose if entries should be rendered as part of the pause menu or if Popper assets of the "Info", "Help" and "Other2" screen should be shown on their configured screen locations. 

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/pause-menu.png" width="600" />

- Added Remote File Editor: It's a first very basic text editor, but as a first step, you can edit the **DmdDevice.ini** and the **VPMAlias.txt** now remotely. (The editor can't do much since text editing with Java is like driving Formula 1 with a Fiat Panda).

### Bugfixes

- Fixed Studio update issues: Both of the installer .exe files did not set any privileges. So when installed in the **program folder** of Windows, the update did only work when the program was started as administrator. I've never noticed because I did use another folder all the time. 
