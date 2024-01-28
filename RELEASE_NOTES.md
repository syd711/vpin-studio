## Hotfix

### Changes

- Added "Emulator" column to table overview. The column is hidden by default and must be enabled in the UI settings.
- Increased possible time to show the highscore card on startup to 30 seconds.
- Renamed "Tables" tab to "VPX Tables".
- Replaced tab icon of "Tables" and "VPS Tables" for a better understanding which tables are shown.
- Replaced version info label in the preferences footer with a link button to show the release notes again.
- Color coded buttons:
  - **Green** buttons indicate the start of an external/3rd party program.
  - **Yellow**-ish buttons indicate opening of a Windows file explorer.
  - **Blue** buttons indicate an URL that is opened in the browser.

### Bugfixes

- Fixed size issues and missing scroll bars in the highscore card designer.
- Fixed issue Pinemhi autostart that was accidental started together with PinVol.
- Search terms for Popper assets are not trimmed anymore, so you can search for "24". - Go for it, Jack!
- Tables and VPS Tables: Ignoring special keys on type-ahead (so that you can ALT+TAB out the window without loosing the selection).
- Added hint in the preferences that the pause menu only works with VPX 10.8.
- Removed "P" as possible key mapping for the overlay or pause menu, since this key is pressed by the pause menu itself.
- Added alternative "chrome.exe" lookup path for 64-bit installations.
- Fixed "chrome.exe" system command where possible white spaces have not been escaped (incl. improved logging).
- Fixed "Pause Key" dropdown, that wasn't filled with any values.
- Fixed "Show Overlay on Startup" (Hopefully this time, guys!).
- Fixed possible error in update script of the server restart (thanks @m-oster for this!);