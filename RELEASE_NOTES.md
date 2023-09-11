## Release Notes

Thanks to @ed209, @Meatballsaucey, @TryToTilt and his server members making the whole competition stuff better!

### Competitions: Discord Competitions

**Discord Competition Changes:**

 - Added new table check **Checksum**: This mode checks if the players table matches with the one of the competition. This means that no script modifications are allowed!
 - Improved(?) strict check: the **strict table check** has been changed to 1MB.
 - Added **Score Limit** so that the amount of scores posted for every highscore summary is customizable.
 - Improved error message about **invalid table file size**: the error message shows the actual difference now.
 - Added **Score Limit** and **Score Validation** information to the metadata section of competitions.
 - Added sanity check for new highscores submissions which filters duplicates.
 - Added new button in Discord Competition overview tab called **Synchronize**: If you achieved a new highscore and the internet was broken (or the highscore parsing failed), you can re-evaluate your highscores of the selected competition. This way, highscores can be submitted after technical problems. 
 - Changed competition finished card to show only the winner highscore. But additionally, the full highscore list is posted with this finished message now.
 - Started building up a repository with **empty nvrams**. (this is work in progress, they are not utilized yet)

**Redesign of the Discord competitions dialogs:**

 - Added separate sections for the settings of a competition.
 - The **VPS Data** is a label now, not a textfield anymore which was confusing.
 - Added **Score Limit** field for the new limit option (default value is **5**).

### Competitions: Table Subscriptions

- Added the new score limit parameter to subscriptions with a default value of **10**. (Only applied for new subscription channels).
- Merged the code base with the one of online competitions. So they now really work the same, but only use ROM name checking.
- Added competition validation here too, so that when a subscription was deleted by the administrator, error messages will be shown for other members.

### Table Management: Script Details

- Added green open button for EM highscore files (if the EM text file is available).
- Added new table info value: this table contains the metadata of the VPX files, curated by the table author(s).  (Thanks again to @somatik for sharing his knowledge here!)

### Bugfixes

Here it comes...

**Table Management: ALT Sound**

- The altsound editor is not opened as a dialog anymore. Instead, the view is embedded as an overlay in the main area.
