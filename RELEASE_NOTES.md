## Release Notes 3.7.2

**Note that a progress dialog will come up once you update is finished, see notes below!!!**

## Bugfixes

- **Media Cache**: For generating highscore cards and backglass previews, the Studio extracts the data from backglasses and other sources. Unfortunately the generation of these assets were stored with non-unique names, so this index must be regenerated. **You can do that manually in the server settings but the Studio will also regenerate it once after showing the release notes dialog.**

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/media-cache.png" width="650" />

- **VPin Studio Server Tray**: Fixed the "Launch Studio" action from the context menu of the tray icon.
- **Table Parser**: Fixed issue in the VPX table analyzer which caused the missing resolving for highscore textfile names.
- **CSV Exporter: Tables**: Added missing escaping of delimiters.
- **CSV Exporter: Backglasses**: Fixed export errors caused by the wrong media index and improved the performance there.
- **Highscore Parsing**: Increased support of VPReg.stg file based highscores. A few more highscore patterns are supported now, mainly used from seventies EM tables. 
- **Table Overview / Table Importer Dialog**: Fixed initialization of the emulator combobox. 
- **Table Overview / Table Uploads**: Fixed issue that newly uploaded tables do not appear for the "Just Added" playlist. 
- **Table Overview / Table Uploads**: Fixed issue that media has been duplicated instead of replaced when "replace" was selected as upload option. 
- **Drop-in Folder**: The delete action moves files to the trash-bin now, instead of deleting them irrecoverably.
- **Table Uploads**: Fixed issue that nvram files have not been extracted when uploaded as part of a bundle, e.g. "Big Bang Bar".


  
