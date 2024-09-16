## Release Notes 3.5.4

### Changes

- Updated to pinemhi version 3.6.2.

### Bugfixes

- **Table Overview / Comments**: Comment types are not case-sensitive anymore. This may have been irritating when filtering for comment types and the type was not detected because if was written lower-case.
- **Table Overview / Filters**: Fixed filter restoring on startup. This one got lost during the last refactoring.
- **Table Statistics**: Fixed duplicated tiles on initial load.
- **VPS Tables**: Fixed mix-up between the PUP-packs and the directb2s column.
- **VPin Studio App**: Fixed initialization of the **VPin-Studio-App.exe** which did not refresh any of the widgets.