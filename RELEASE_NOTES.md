## Release Notes

Thanks to @ed209 making the whole competition stuff better!

### Competitions: (Discord) Competitions

**Discord Competition Changes:**

 - Added new table check **Checksum**: This mode checks if the players table matches with the one of the competition. This means that no script modifications are allowed!
 - Improved(?) strict check: the **strict table check** has been changed to 1MB.
 - Added **Score Limit** so that the amount of scores posted for every highscore summary is customizable.
 - Improved error message about **invalid table file size**: the error message shows the actual difference now.
 - Added **Score Limit** and **Score Validation** information to the metadata section of competitions.
 - Added sanity check for new highscores submissions which filters duplicates.
 - Added new button in Discord Competition overview tab called **Synchronize**: If you achieved a new highscore and the internet was broken (or the highscore parsing failed), you can re-evaluate your highscores of the selected competition. This way, highscores can be submitted after technical problems. 
 - Changed competition finished card to show only the winner highscore. But additionally, the full highscore list is posted with this finished message now.

**Redesign of the Discord competitions dialogs:**

 - Added separate sections for the settings of a competition.
 - The **VPS Data** is not a label, not a textfield, which was confusing.
 - Added **Score Limit** field for the new limit option.

### Competitions: Table Subscriptions

- Changed the new score limit parameter subscription with a default value of "10". (Only applied for new subscription channels).
- Merged the code base with the one of online competitions. So they now really work the same, but only use ROM name checking.

### Table Management: Script Details

- Added green open button for EM highscore files (if the text file is available).
- Added new table info value: this table contains the metadata of the VPX files, curated by the table author(s).  (Thanks again to @somatik for sharing his knowledge here!)

### Bugfixes

- Fixed database lock exception: Fixed possible error on competitions creations.
- Fixed table overview refresh error, that happen when a table was selected and the highscore was re-scanned.
- Fixed wrong _ERROR_ message on highscore export, indicating an error that was none.
- Fixed highscore parsing issue when the first place was resetted to an empty value (with no initials at all).
- Fixed missing scrolling in Table Management's POV section.
- Fixed all ROM, Tablename, Emulator and media asset name checks to case-insensitive: we are on Windows, right?
- Fixed strict mode check: for people joining the competition, the file size check was **always** enabled.
- Fixed timing issues with pinned message updates which caused invalid highscore postings.
- Fixed score formatting of Discord messages for lists larger nine entries.
- Fixed **duplicate score submissions** for subscriptions.
