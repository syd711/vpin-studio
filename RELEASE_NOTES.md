## Release Notes 3.0.6

**Breaking Change**
- **Highscore Cards Rendering**: I've changes the attributes "Wheel Icon Size" and "Margin Left" for **non-raw/list based** highscore cards. The wheel icon size was calculated before which felt tedious.

**Changes**
- **Highscore Cards**: Added button to only generate the card for the active game selection.
- **Highscore Cards**: Added option to disable the card generation for per table. The checkbox for this is found in the highscores section. The card designer shows these tables in a grey "disabled" state.
- **VPin Mania**: Added VPS table search.
- **VPin Mania**: Added new tab "Player Statistics" where you can see your statistics and search for other players.
- **Preferences / Notifications**: Added "Desktop Mode" checkbox for notifications.
- **Table Overview / Table Data Manager**: Optimized dialog so that it can be displayed for smaller screen resolutions.
- **iScored Integration**: Added info button which shows a summary of your game room settings.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/mania/gameroom-info.png" width="560" />


**Bugfixes**

- **VPin Mania**: The actual highscore timestamp is now used when a highscore is written. So when you synchronize your highscores, the actual creation date will be used instead of the date of submission. This way, the "Recent Highscores" widget shows only the actual recently created highscores.
- **VPin Mania**: Fixed a bunch of sorting and navigation issues.
- **VPS Table Search Input**: Fixed input fields for VPS table searches so that whitespaces are allowed now too.
- **Mac OS**: Fixed wrong OS name detection when editing files.
- **System Manager**: Replaced the component version question marks with the usual tooltip hint that it is not possible to match an installed version against available github releases.