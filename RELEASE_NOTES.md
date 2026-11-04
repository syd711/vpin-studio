## Release Notes 5.7.0

### Changes

- **Localization**: Added a lot of missing translation, especially for tooltips and progress dialogs.
- **Competitions**: Removed Discord competitions and Discord subscriptions.

### Bugfixes

- **Backglasses / .res File Generator**: Fixed frame images of different tables overwriting each other when they had the same filename. Uploaded frames were stored under their original filename in the shared "_Frames" folder, so every table using a frame with that name showed the frame uploaded last. Uploaded frames are now stored with the table name as prefix. Re-select the frame and regenerate the .res file of affected tables.