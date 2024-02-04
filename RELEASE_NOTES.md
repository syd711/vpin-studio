## Hotfix

### Changes

- VPS update flags are now resetted for the following uploads: backglass, table replacements, altsound, altcolor, puppack and pov files/archives.
- The "Table Import" dialog now provides a progress bar, so that the user gets feedback about the progress when a bunch of tables is installed at once.
- Moved "Scan All" button into a split-menu button since it not likely to be used that often.
- Simplified "ROM" and "Alt. ROM Name" fields (hopefully): 
  - When these values are available in Popper, Popper values are always preferred.
  - When these values are empty in Popper, the scanned value will be used as fallback.
  - When one of these values is edited, the "Table Data" dialog opens now (instead of a simple input field). The dialog will show if the value is "only" scanned or saved in Popper. The values can be applied with a separate "Apply" button.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/apply-rom-name.png" width="300" />

- Added the option to render highscore cards with a transparent background.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/cards/transparent-cards.png" width="600" />

### Bugfixes

- Fixed initial breadcrumb path which was showing the initial VPS tables selection, not the one from the table overview.
- Fixed window layout issue where the minimize/maximize/close buttons have been moved out of the toolbar.
- Corrected error message text when a client is connecting to a server with a newer version.
- Fixed possible error for some backglasses preventing them from being uploaded.
- Fixed MAME deletion: The registry key are not deleted (finally) and the "Mame" section is disabled when no registry entry is available for the table.  
 