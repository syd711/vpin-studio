## Release Notes 3.0.3

- Re-enabled the VPX monitor again. Thanks @leprinco!

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/vpxmonitor.png" width="700" />

- Added separate "VPin Mania Name" field to the player edit dialog. This name will be used for all VPin Mania based stuff. You can rename your existing accounts too.

### Bugfixes

- **VPin Mania**: Optimized wheel icon lookup with a persistent cache, so that when browsing the tables by letter in VPin Mania everything is loaded faster.
- **VPin Mania**: Fixed joining tournaments.
- **VPin Mania**: Fixed wrong status of iScored checkbox "Score submissions enabled" which was always unchecked for existing tournaments.
- **VPin Mania**: Fixed duplicated entries issue in the players list. 
- **VPin Mania**: Fixed filtering of tournament highscore submissions when no matching player has been found. 
- **iScored**: Fixed filtering of highscores submission to iScored where on matching player has been found. 
- **Table Overview / ALT Sound**: Fixed ALT sound editor not showing when "Edit" was pressed.
- **Table Overview / VPS Updates**: Fixed filtering for VPS updates according to the settings.
- **Highscore Cards**: Added "Reset Default Asset" button to the default asset preview.
