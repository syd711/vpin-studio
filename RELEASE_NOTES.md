## Release Notes 2.21.0

- **Preference Menu**: Added button to restart PinUP Popper.
- **Header Toolbar**: Added button to start PinUP Popper Config.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/header-toolbar.png" width="400" />
  
- **Preferences / Client Settings**: Added option to **select emulators** to be shown in VPin Studio.
 
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/emulators.png" width="500" />

- **Highscore Card Designer**: The Popper screen preview has been removed from the lower left section. Instead, the new **overlay** feature has been added to the card designer. This mode assumes you have your highscore screen laying above the Topper, Backglass or FullDMD screen (in 16:9 format) **with transparency**. This allows you to design your card while seeing the effecting background video and adept the layout accordingly. (I hope we will see some demo screenshots here soon to visualize this.)  
- **Universal Uploader**: Added .rar support (wuhuu, finally!).


### Bugfixes

- **VPX Monitor**: Fixed wrong table resolving for the VPX monitor, was a bit hasty there.
- **Universal Uploader:** Fixed issue that the table overview was stuck in "Loading" mode until the **Table Data Manager** dialog was closed.
- **Universal Uploader / Table Upload Dialog:** Fixed hidden upload of PUP packs. For some assets, e.g. PUP packs the corresponding checkbox was not displayed.
- **Universal Uploader:** Fixed various extraction issues for some PUP packs in combination with Popper assets and DMD bundles.
- **Performance Optimization**: Added caching for ALTSounds.
- **Performance Optimization**: Highscores are not re-scanned on table overview reload. This should save same time and since the data is fetched when the highscore section is opened, it should make no difference.
- **Table Overview**: Fixed keeping the table selection on reload and other actions.