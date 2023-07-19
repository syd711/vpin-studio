## Release Notes

**Script Details Section**

- Added green "Open" button on the header which will directly open the script in VPX.
- Added additional "Scan" button on the Script section toolbar for convenience.
- Added alias mapping editor.
- Added alias mapping delete button.
- Added alias mapping help.
- Added nvoffset help.
- Added script view: This way you can have a quick look on how the actual VPX script looks like. The view works way faster than the old script export functionality. Unfortunately I'm not able to save the script yet.

**PUP Pack Section**

- Added option to disable PUP packs.

**Bug Fixes**

- Changed most ROM read accesses to the effective ROM name: E.g. when an alias was defined and the script does contain this alias, the ROM name from the script was taken to look up ALTColor, ALTSound, etc.. Instead the actual mapped ROM name is used now.
- Fixed table refresh on MAME setting changes.