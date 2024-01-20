## Release Notes

### Changes

- Removed "ID" column from the tables overview (no need to display this technical value).
- Added column "Date Added" to table overview. Note that this value may be empty because previous version of the Studio did not set this value when new tables have been uploaded.
- Improved "Players" section: Added default background image for player's score list.
- Added fancy splash screen.

### Bugfixes

- Fixed virtual-pinball-spreadsheet change listener: Changes have been pre-filtered by the *updateDate* inside the VPS data, but this field is not necessarily updated when data has changed. So this check has been removed.
- Fixed Studio window size: The size of the launcher was used for the actual Studio window size too, when the "Disconnect" action has been used.
- Blind fix for file uploads: Added a blind fix for issues when using the upload table dialog. It's related to the latest feature of saving the last folder location, but I couldn't reproduce it yet.
- Fixed overlay focus issue: Added a lazy "to-front" call for the overlay window which should fix the problem of the window hidden behind the Popper table selector.
- The video size for playfield and loading videos inside the media view dialog has been improved.   


