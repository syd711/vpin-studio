## Release Notes 3.5.3

### Bugfixes

- **Studio Window Exit**: Fixed proper closing of the Studio client window. ALT-F4 or other actions than the "close" button from the toolbar resulted in a "Zulu" zombie process. The process is properly terminated now.
- **Studio Update Error**: Fixed issue that the server update process didn't work (and showed some scripting error). For some users, the update batch file was executed in the wrong folder. **You have to install this update manually so that the automatic update works again for the next update!!!**.
- **Highscore Parsing**: Added support for "Route 66".
- **Table Overview / Asset Management View**: Fixed "scroll into view" issue when the screen-assets button was clicked. This auto-scrolling was not intended for these actions.
- **Table Overview / Asset Management View**: Fixed wrong asset search when switching between the different screen buttons in the table overview.