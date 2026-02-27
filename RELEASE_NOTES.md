## Release Notes 4.7.0

## Changes

### VPX 10.8.1 Support

With version 4.7.0, we are building the foundation for the upcoming VPX 10.8.1 release, which introduces a completely new folder structure for VPX files and their companion files (table override INI file, backglass file, PuP video folder, DMD colorization, music, etc.).

Please note that not all companion software supports the new folder layout yet. For example, PinUP Popper currently does not look into the actual table folder for assets. Therefore, the transition will take some time, and we are working closely with the VPX team to ensure full support.

**Right now, nothing changes for you.** We needed to implement major server-side changes to support the new format for all available companion assets.

First, we will ensure everything continues to work with the old folder structure. Later, we will enable specific flags in the backup restore process that allow backups to be extracted into the new folder structure.

Further reading:
https://github.com/vpinball/vpinball/blob/master/docs/FileLayout.md

### .vpxz File Support

Support for .vpxz files has been added. Check out the YouTube video to see how you can connect your phone with VPin Studio (https://www.youtube.com/watch?v=A-mzXOkTD7E
) and upload and install .vpxz files on your mobile device.

A huge shoutout to @jsm174 for his awesome VPX app!

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/vpxz/vpxz-view.png?raw=true" width="700" />

### VPin Mania 2.0

VPin Mania has been relaunched with a new registration system and additional features.
Please watch the YouTube video to get an overview of what has changed.

**So, is all my data gone now?**

No! Although you now need to re-register with a real user account, your existing cabinet data will be reused once you link your cabinet to VPin Mania again. If this does not work, you can always perform a complete sync between your cabinet and VPin Mania.

**Table statistics are not affected by this update — rankings and play counts remain intact.**

## Bugfixes

- **Table Scans**: Improved PUP pack detection.