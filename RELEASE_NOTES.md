## Release Notes

**UI**
 - The **Tables** administration is now the default entry screen when you open the UI. The dashboard has been moved below as it has just a lower relevance. 

**Script Details Section**

- Added green "Open" button on the header which will directly open the script in VPX.
- Added additional "Scan" button on the Script section toolbar for convenience.
- Added alias mapping editor.
- Added alias mapping delete button.
- Added alias mapping help.
- Added nvoffset help.
- Added **script viewer**: This way you can have a quick look on how the actual VPX script looks like. The view works way faster than the old script export functionality. Unfortunately I'm not able to save the script yet.
- Added button to open **VPSaveEdit.exe**: It's an external tool that was shipped anyway and it's useful to modify some table data that are not part of the script.
- Added validation error box inside the "Script Details" section which displays related validation issues.

**PUP Pack Section**

- Added option to disable PUP packs. This is solved through an existing PINUp Popper feature, see https://www.nailbuster.com/wikipinup/doku.php?id=pinup_optional#option_2note_this_requires_pinupmenu_v141_to_work_correcty

**PinVol Support**

- Added PinVol support: The exe file shipped together with the server and can be enabled/disabled in the preferences of the VPin Studio. I did not build separate UI for it since the key binding may get too error prone. So you can  only show the PinVol UI when actually working on the cabinet.

**PINemHi**

- It took a while, but since I already ship the **PINemHi Leaderboard**, it was time to build the integration for it too. You'll find the configuration for you _pinemhi.ini_ in the preferences section. I hope everything works as expected as there was **a lot** of fields to configure. Note that when running, the key bindings are intercepted may disturb working with the VPin Studio when launched on the cab. For the leaderboard users out there: let me know if something is missing or not working as expected!

**Linux/MacOS Client Support (experimental)**

- The release build comes with a **VPin-Studio-Client.zip** now that **should** be executeable on Linux and Mac:
  - Unzip the file.
  - Make super you have a Coretto 11 JDK/JRE installed (https://docs.aws.amazon.com/de_de/corretto/latest/corretto-11-ug/downloads-list.html).
  - Mark the *VPin-Studio.sh* file as executeable and run it.
- Note that this client cannot be automatically updated. But only the jar file has to be replaced manually.

**Bug Fixes**

- Fixed table refresh on MAME setting changes.
- Fixed wrong nvram read for aliased ROMs.
- Fixed ignoring ignored MAME validators.
- Add "ROM"-based table refresh: Some table changes, like the MAME settings, result in changes for multiple tables. All tables which are affected by these type of changes are refreshed now.
- **Client starts bigger than screen size** (https://github.com/syd711/vpin-studio/issues/2): Fixed initial window size for smaller resolution while checking also the windows scaling. There is still room for improvement, but right now the window shouldn't exceed the actual screen size anymore.
- **Rom alias seen as missing rom** (https://github.com/syd711/vpin-studio/issues/3): Changed most ROM read accesses to the effective ROM name: E.g. when an alias was defined and the script does contain this alias, the ROM name from the script was taken to look up ALTColor, ALTSound, etc.. Instead the actual mapped ROM name is used now.
- **Date added not set when cloning table** (https://github.com/syd711/vpin-studio/issues/4) : "Date Added" field value is now set in PinUP Popper when new entries have been added through VPin Studio uploads. The field has also been added to the "PinUP Popper Table Data" section.
- **Complains about loading video missing if check disabled** (https://github.com/syd711/vpin-studio/issues/5): Fixed mixed up validator ids between the loading screen validator and the playfield screen validator.
- **Cloned table gets 0 rating instead of null** (https://github.com/syd711/vpin-studio/issues/6): Fixed numeric table metadata values which have been defaulted to "0" while cloning. 
- **Loading screen size wrong after cloning a table** (https://github.com/syd711/vpin-studio/issues/7): Fixed errorneous cloning of PinUP Popper media files. The cloning did only copy the default media which resulted in broken fullscreen loading videos. 
