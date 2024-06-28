## Release Notes 2.21.2
 
- **Playlist**: With the help of @Ltek, the playlists have been fixed so that the usage of favorites and global favorites makes more sense. In addition to that, letter icons are rendered for the common default Popper playlists. The "Local Favorite" and "Global Favorite" checkboxes can now only be checked for SQL playlists. Note that (same as in Popper), the game has to be played once before it can be selected for one of these lists.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/playlists.png" width="600" />

### Bugfixes

- **Installer**: Added .net check before server installation. Yes, this should be part of the installer, but this was the easier fix.
- **Universal Uploader**: Fixed detection of DMD packages (containing a whitespace, e.g "Metal Slug.UltraDMD").
- **SevenZip Issues**: Studio does not terminate itself anymore in case there are issues with the SevenZip (.rar support) library.
- **VPin MAME Settings**: Fixed "Sound Mode" option from a boolean value to a numeric values, since values from 0-3 are allowed here. The VPin MAME preferenes have been changed accordingly.