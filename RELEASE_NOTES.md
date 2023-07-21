## Release Notes

**Script Details Section**

- Added green "Open" button on the header which will directly open the script in VPX.
- Added additional "Scan" button on the Script section toolbar for convenience.
- Added alias mapping editor.
- Added alias mapping delete button.
- Added alias mapping help.
- Added nvoffset help.
- Added script view: This way you can have a quick look on how the actual VPX script looks like. The view works way faster than the old script export functionality. Unfortunately I'm not able to save the script yet.
- Added button to open **VPSaveEdit.exe**: It's an external tool that was shipped anyway and is useful to modify some table data that are not part of the script.
- Added validation error box inside the "Script Details" section which displays related validation issues.

**PUP Pack Section**

- Added option to disable PUP packs.

**Bug Fixes**

- Changed most ROM read accesses to the effective ROM name: E.g. when an alias was defined and the script does contain this alias, the ROM name from the script was taken to look up ALTColor, ALTSound, etc.. Instead the actual mapped ROM name is used now.
- Fixed table refresh on MAME setting changes.
- Fixed wrong nvram read for aliased ROMs.
- Fixed ignoring ignored MAME validators.
- Add "ROM"-based table refresh: Some table changes, like the MAME settings, result in changes for multiple tables. All tables which are affected by these type of changes are refreshed now.