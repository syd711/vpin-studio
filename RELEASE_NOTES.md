## Release Notes

### Changes

- **Table Management**: Added support for **loading tables from sub-folders**. Up until now, the Studio did only assume that all VPX files and backglasses are located in the **Tables** folder of the emulator. While Popper supports sub-folders here, the Studio does now too. Because of the complexity, I disabled some additional operations for these tables, like the renaming option of the VPX file. **This required quite a lot of changes, so there might be some aftermath**.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/header-toolbar.png" width="300" />

- Updated pinemhi.

### Bugfixes

**NVOffset**: Fixed problem that pinemhi is not able to read nvram files with offset. I worked around this problem by temporary renaming the .nv files parsing them.
