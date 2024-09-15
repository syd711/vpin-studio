## Release Notes 3.5.4

### Bugfixes

- **Table Overview / Comments**: Comment types are not case-sensitive anymore. This may have been irritating when filtering for comment types and the type was not detected because if was written lower-case.
- **Table Overview / Filters**: Fixed filter restoring on startup. This one got lost during the last refactoring.
- **Table Statistics**: Fixed duplicated tiles on initial load.