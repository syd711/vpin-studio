## Release Notes

### Breaking Change

Since the preferences are growing, I had to change the data format to be more flexible in the future.
As a result, your existing **"UI Settings" and "Server Settings"** in the preferences view have been resetted.
**Please revisit your settings there!**

### Changes

- Table Overview: Changed hover states so that the font remains white (not blue anymore), including a row hover styling.
- Studio Window: The size and location of the Studio window is now persisted and restored on the next launch.
- File Dialogs: The last used folder location is now stored and used as initial directory for every new file selection dialog.

### Bugfixes

- Overlay: Fixed issue that when the overlay is shown on startup, it does not hide on key-press when no hotkey is defined.