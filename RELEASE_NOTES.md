## 2.19.0

- **VPS Table Matching**: Introduced new VPS table matching. Thanks to @leprinco the VPS table matching has been improved and is using a real matching library now.
- **VPS Table Mappings**: VPS tables without links or broken links are not filtered anymore. These entries have been filtered before, but it makes sense to keep them for the auto-matching and showing them as installed versions.
- **VPS Table Details Section / VPS Overview**: Added table type (VPX/FP) as badge because FP versions are shown now too. 
- **Table Filters**: Splitted missing VPS matching filter option into "Missing VPS Table Mapping" and "Missing VPS Table Version Mapping" options.
- **Table Overview / VPS Status Column**: Reformatted and renamed this column so that users can immediately see if a table is properly mapped and if updates are available.

### Bugfixes

- **Highscore**: Fixed parsing issue. Unfortunately the formatting of the last patch broke some score parsing for some score that already have been formatted.
- **Highscore Parsing**: Fixed special character issue that happened for some users in combination with different locale settings and pinemhi. @leprinco found a nice fix there so that hopefully other users won't be bothered with this issue anymore.
- **MAME Settings**: Skipped some of the superflous MAME reload calls since these very expensive. The MAME cache isn't cleared on table reload anymore, but only when the **Reload** button in the MAME section is pressed or for single tables.
- **VPS Reset**: Fixed broken "VPS Reset" button when used in the **Table Data Manager**. Since the fields have no been cleared on reset, the old value was still persisted.
- **NVOffset Validator***: Improved this validator so that if a mismatch is detected, the other table is shown in the message too. The validator has also been changed that not only a NVOffset must have been set for the other tables, but these also have to differ.
