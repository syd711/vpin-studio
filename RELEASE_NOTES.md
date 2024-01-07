## Hotfix Notes

### Bugfixes

- **Fixed card generation (again)**: The filename used for the card generation was still wrong and has been corrected now.
- Increased delay of initial overlay show: There may be a timing issue when the overlay is visible earlier than the PinUP Popper menu and then gets hidden behind it. The incremented delay (of 2 secs) should take care that the overlay now appears after the Popper menu.
- Fixed disabled "Install" button in the System Manager.
- The file type ".mp4" is now allowed for the screens "Info" and "Other" too, including for drag-and-drop.
- Fixed statistics overview: There was a case of negative playtime values (How? I don't know.). This statistics dashboard does handle these value more gracefully now.

### Known Issues

See https://github.com/syd711/vpin-studio/issues
