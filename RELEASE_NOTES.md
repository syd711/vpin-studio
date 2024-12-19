## Release Notes 3.12.0

## Changes

- **Tables / PUP Packs**: Added [PupPackScreenTweaker.exe](https://github.com/matiou11/PupPackScreenTweaker) as additional PUP pack editor to the PUP pack section. Note that this editor is only available when working on the cabinet itself.
- **Tables / Table Data Manager**: The auto-naming has no restrictions on VPX files that are located in sub-folders anymore. You can also rename them now.
- **Tables / Highscore Settings**: This section has undergone a revamp:
  - Added support for multi-selection from the table overview.
  - Added bulk operation support for highscore resets.
  - Added bulk operation support for highscore backups.
  - Re-implemented the highscore reset dialog which shows more information about the actual reset, e.g. if a resetted nvram is available.
  - Added reset value input option for the highscore reset dialog.
- **Preferences / Controller Setup**: Added error message that is displayed when "SET FSMODE=EnableTrueFullScreen" is set in the emulator launch script, as this will avoid any VPin Studio overlays from getting the focus.
- **Tables / Overview:** De-cluttering:
  - **Context Menu**: Removing less used entries.
  - **Toolbar**: When switching into asset-view mode, unnecessary actions are hidden.

## Bugfixes

- **Tables / PUP Packs**: The PUP pack data in the PUP pack section is now refreshed on table selection.


