## Release Notes


**Competitions: (Discord) Competitions**

- "Strict Table Checking" is not enabled anymore by default for online competitions.
- Added "Invalid" status for competitions, e.g. if a channel is on longer valid.
- Added validations for competitions and new status type "Invalid". This way, errors can be displayed, e.g. if a competition was created for a game or Discord channel that no longer exists.
- Added "VPS Link" and "Download Link" fields for online competitions. If set, this information will be rendered with the competition card and metadata, so players can download the correct version of the table.
- Added "Channel" column to overview table to have a better overview when multiple competitions are on one server.

**Preferences: Discord Bot**

- Added validation button for the configuration. This one checks if the configured server, channel and category still exist.

**Preferences: Highscores**

- Added miscellaneous new score card titles to the preferences. For some highscore cards the first place is only detected by setting these special labels. Note that this value can be updated in the "Score Format" settings anytime. Please let me know if there are other cards with special titles to improve these defaults! With this update, the *special* titles have been updated for the following tables:
  - Bram Stoker's Dracula
  - Whirlwind
  - Theater of Magic

**Table Management: Script Details**

- Added green open button for EM highscore files (if the text file is available).

**Bugfixes**

- Fixed EM highscore file parsing: tables writing 16 lines highscore files were one line off, e.g. 2001 (Gottlieb). Thanks you for this hint, @rubadub-github!
- Fixed issue in Discord configuration validation.
- Fixed wrong Discord notification text for new competitions (_"started a new!"_). 
- Fixed issues with "Strict Mode" caused by errors in the "Join Competition" dialog.