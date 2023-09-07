## Release Notes


**Competitions: (Discord) Competitions**

- Redesign of the Discord competitions dialog:
  - Added separate sections for the settings.
  - The VPS data is not a label, not a textfield, which was confusing.
  - Added "Score Limit" field: this fields defines the amount of scores to store on a channel and to post with every update.

**Table Management: Script Details**

- Added green open button for EM highscore files (if the text file is available).
- Added new table info value: this table contains the metadata of the VPX files, curated by the table author(s).  (Thanks to @somatik for sharing his knowledge here!)

**Bugfixes**

- Fixed database lock exception: Fixed possible error on competitions creations.
- Fixed table overview refresh error, that happen when a table was selected and the highscore was re-scanned.
- Fixed wrong ERROR message on highscore exports.
- Fixed highscore parsing issue when the first place was resetted to an empty value (no name at all). (Thanks to @ed209 making the whole competition stuff better!)
- Fixed missing scrolling in Table Management's POV section.