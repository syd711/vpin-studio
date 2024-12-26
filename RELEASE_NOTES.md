## Release Notes 3.12.0

## Changes

- **Tables / PUP Packs**: Added [PupPackScreenTweaker.exe](https://github.com/matiou11/PupPackScreenTweaker) as additional PUP pack editor to the PUP pack section. Note that this editor is only available when working on the cabinet itself.
- **Tables / Table Data Manager**: The auto-naming has no restrictions on VPX files that are located in sub-folders anymore. You can also rename them now.
- **Tables / Highscores Section**: This section has undergone a revamp:
  - Added support for multi-selection from the table overview.
  - Added bulk operation support for highscore resets.
  - Added bulk operation support for highscore backups.
  - Re-implemented the highscore reset dialog which shows more information about the actual reset, e.g. if a resetted nvram is available.
  - Added a **reset value** input option for the highscore reset dialog.
- **Preferences / Controller Setup**: Added error message that is displayed when "SET FSMODE=EnableTrueFullScreen" is set in the emulator launch script, as this will avoid any VPin Studio overlays from getting the focus.
- **Tables / Overview:** Added new column "Comment". The colum is hidden by default, not sortable and placed as last table column. 
- **Tables / Overview:** Added context menu item "Edit Comment". 
- **Tables / Filter:** Added filter option "No comment". 
- **Tables / Overview:** De-cluttering:
  - **Context Menu**: Removing less used entries.
  - **Toolbar**: When switching into asset-view mode, unnecessary actions are hidden.
- **Hook Support**: The VPin Studio allows to execute customs scripts from any client. You can add these "hooks" by adding .exe or .bat files into the server installation directory **resources/hooks**. The list of files is picked up and added to the preferences split button of the Studio client and will be executed on click. See also: https://github.com/syd711/vpin-studio/wiki/Hooks

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/hooks.png" width="300" />


## VPin Mania Changes

- **Added Friends**: Added new "Friends" button on the top window toolbar.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/mania/friends-btn.png" width="300" />

  Connecting with friends (cabinets) allows you to see their online status and what table they are playing. Once you connected your cabinet with with your friends, you user players that have been added as **VPin Mania Players** are visible for your friends too, which allows to share highscores with then. You can adjust the visibility of the accounts in the "Friends" menu.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/mania/friends.png" width="670" />

- **Merged Highscore Cards**: Adding friends means you can merge the highscores from your friends accounts with yours. The highscore card template editors comes with a new preference for this. To highlight these, you and choose another color for these scores.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/mania/merged-card.png" width="700" />

- **VPin Mania Preferences**: The preferences section "VPin Mania Preferences" has been split into an "Account" and a "Tournaments" section to separate these two.
- **Open in VPin Mania Action**: For more convenience you can open a VPin Mania table overview directly from
    - the table overview using the corresponding context menu action.
    - the highscore editor by clicking on the VPin Mania icon.
- **Deny Lists**: A list of selected user has additional permissions to add and remove highscores to/from deny lists. These highscores are ignored for all rankings and for merged highscore cards. Default highscores that are posted on VPin Mania can be ignored this way.




## Bugfixes

- **Tables / PUP Packs**: The PUP pack data in the PUP pack section is now refreshed on table selection.
- **Tables / Asset Preview Dialog**: The dialog has been re-implemented to ensure the shown media is properly scaled.


