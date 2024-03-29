## Release Notes

### Changes

- **VPS Updates**: The VPS update calculation has been re-implemented so that less updates are fired. The new differencing returns the details of the updates now too. Additionally, you can see update markers in the VPS section now.  

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/vps/update-markers.png" width="300" />

- Updated pinemhi.

### Bugfixes

**NVOffsets**: Fixed problem that pinemhi is not able to read nvram files with offset. I worked around this problem by temporary renaming the .nv files parsing them.
