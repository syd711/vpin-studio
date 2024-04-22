## Hotfix 2.17.3

- **Table Overview**: Fixed context menu item "Upload Backglass".
- **Color Picker**: Disabled **custom** color picking, since the dialog seems to be broken.
- **Table Imports**: Fixed **DateAdded** data field which had the wrong date format and wasn't picked up by SQL playlists this way.
- **Table Data Manager**: The Popper field "Notes" remains untouched from the auto-fill function now.
- **Studio Window**: Added (fixed) Studio version in the window title.
- **Preferences**: Added **Help & Support** section, including a download option which zips and downloads all log files for ticket submissions.
- **Highscore Cards**: Added **Render Positions** flag available for non-raw highscore templates. The padding value will be used as additional separator between the initials and the score value.
- **Overlay & Card Settings:** Added **Input Debounce** configuration that should avoid possible duplicated input events (experimental).

## Hotfix 2.17.2

- Fixed playlist display errors. (Thanks to @spaceangel6998 helping out here!).

## Hotfix 2.17.1

- Fixed "Open Folder" on-hover buttons (color and icon) in the media section.

## Release Notes 2.17.0

### Changes

- **PrOPPER Naming**: Added PrOPPER Naming to the Table Data Manager dialog. I also restructured the dialog, added the **Score Data** into a separate tab and moved the VPS assigment to the first tab with the new PrOPPER Naming input fields.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/propper-naming.png" width="700" />

- **Playlists**: Added **Favorites** and **Global Favorites** options to playlist section.  
- **Playlists**: Added **Favorites** icon to the **playlist column** in the table overview. The icon is shown if the table has been marked as favorite or global favorite on a playlist.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/favorites-icon.png" width="500" />
  
- **VPS Tables View**: Added list of all table versions to the VPS table details section.

### Bugfixes

- **Card Generator**: Fixed template assignment. Embarrassing, but the actual template assignment did never worked when the actual cards have been generated :-/.
- **Card Generator**: Fixed miscellaneous calculations regarding the positioning of items.
- **Card Generator**: Added new **margins** options for a better positioning. Note that this fix will affect your card design.
- **Card Generator**: Added new **wheel size** option to determine the actual target size of the wheel image. Note that this fix will affect your card design.
- **Card Generator**: Redesigned the input options and added more tooltips.
- **VPS**: Fixed VPS deeplinks.
- **VPS**: Improved logging. All update messages for VPS changes are now logged into a separate log file (will make life easier support-wise).
- **Backglass Manager**: Some label and tooltip improvements.

