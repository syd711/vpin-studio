## Release Notes 3.0.8

## Bugfixes

- **Highscore Parser**: Fixed highscore parsing where the numbers were separated by whitespaces (hopefully).
- **Table Overview / Table Imports**: Added missing cache reset when existing tables are imported.
- **Table Overview / Backglass**: Added missing cache after uploads.
- **Table Overview / Backglass**: Added reload button to invalidate backglass cache in case an update was made manually.
- **Table Overview / Table Data Manager**: I've reports that the Table Data Manager can not be opened. I've added more logging to figure out the root cause.
- **Table Overview / Script Details**: Added drop-down menu for the script editing so that you can use the embedded editor in case you have set no system default editor for .vbs files.
- **VPin Mania**: Added duplicated scores filter to tournament score submissions.