## Release Notes

Thanks to @ed209 making the whole competition stuff better!

**Competitions: (Discord) Competitions**

Discord Competition Changes:
 - Added new table check "Checksum": This mode checks if the players table matches with the one of the competition. This means that no script modifications are allowed!
 - Improved(?) strict check: the strict table check has been changed to 1MB.
 - Added "Score Limit" so that the amount of scores posted for every highscore summary is customizeable.
 - Improved error message about invalid table filesize: the message shows the actual difference now.


Redesign of the Discord competitions dialogs:
 - Added separate sections for the settings.
 - The VPS data is not a label, not a textfield, which was confusing.
 - Added "Rules" header which explains the basic rules of online competitions. 
 - Added "Score Limit" field for the new limit option.
  

**Table Management: Script Details**

- Added green open button for EM highscore files (if the text file is available).
- Added new table info value: this table contains the metadata of the VPX files, curated by the table author(s).  (Thanks to @somatik for sharing his knowledge here!)

**Bugfixes**

- Fixed database lock exception: Fixed possible error on competitions creations.
- Fixed table overview refresh error, that happen when a table was selected and the highscore was re-scanned.
- Fixed wrong ERROR message on highscore exports.
- Fixed highscore parsing issue when the first place was resetted to an empty value (with no initials at all).
- Fixed missing scrolling in Table Management's POV section.
- Fixed all ROM, Tablename, Emulator and media asset name checks to case-insensitive: we are on Windows, right?
- Fixed strict mode check: for people joining the competition, the file size check was **always** enabled.
