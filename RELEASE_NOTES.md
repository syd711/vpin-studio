## Maintenance Release 2.3.2

### Bugfixes

- Added missing 7z.dll file to the installation. Mostly 7zip is already installed on machines, but in case not, updates don't seem to work or the 7-zip installation is limited to another user? Hopefully this fixes the issue. The file will only be added for fresh installations.
- Added missing highscore backup on highscore reset.
- Fixed restoring highscores from "The Addam's Family". (Why only this table? Because it's the only table I know there the ROM name is stored uppercase in the script and the actual ROM file is lower case :-/ )