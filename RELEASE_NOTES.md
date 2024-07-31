## Release Notes 3.0.5

**Bugfixes**

- **Table Resolving**: The early date format error of the initial 3.0 release led to some corrupt data. The server has been made more robust that these tables are now loaded anyway (and repaired again on update).
- **iScored Subscriptions**: Added multi-selection in this overview, so that tables can be bulk deleted.
- **iScored Subscriptions**: Fixed error in score submissions when "long name inputs" were disabled on iScored site.
- **iScored Subscriptions**: Fixed iScored game room URL validation. You can use the URL with format https://www.iscored.info/Syd or https://www.iScored.info?mode=public&user=Syd, only the first one has been supported so far and the validator is less restrictive now.
- **iScored Subscriptions**: Improved error handling during table resolving: You'll now get an error message when the VPS match could not be resolved for a VPS tagged table from iScored.
- **Table Imports**: Fixed error for the table import dialog when "All VPX Tables" filter has been selected.
- **Preferences** Fixed error resetting existing configuration values back to **null**.
- **Highscore Formatting**: Fixed number separator to be localized, e.g. "123,456,789" for US and "123.456.789" for the rest of the world ;)
- **Tournaments**: Fixed handling of duplicated tables. The input for this is not fixed yet, but at least the backend is robust against that.
- **VPin Mania**: Added avatar caching for player list.