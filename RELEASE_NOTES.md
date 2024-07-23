## Release Notes 3.0

We are happy to announce that VPin Studio version 3.0 has been released.
Here is what's new:

### PinballX Support

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/pinballx.png" width="400" />


The VPin Studio supports **PinballX** now too! The feature set is a bit more limited than the one for PinUP Popper users
but the basic scoring and asset management functionalities are there.
Because of the larger user base, PinUP Popper will remain the main focus. But depending on the feedback we get, 
the feature set of PinballX will be extended too.

In addition to that, the VPin Studio installer supports a **Standalone** mode too.
This mode does not support media management but all other basic VPX based features.

You can select between the three modes (PinUP Popper, PinballX and Standalone) when installing the Studio.

### VPin Mania (Services + Tournaments)


  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/mania/mania.png" width="150" />

Mentioned several times this year and announced on https://www.vpin-mania.net/, the initial version of the **VPin Mania**
services are now live! The original plan was to launch the service as a website, but because I've always favored 
the Studio maintenance and because web development is more expensive, I decided to integrate the frontend as
part of the VPin Studio.

Interested in what these services have to offer? Check out the video below:

https://www.youtube.com/watch?v=Dq3rra5Gu1I&ab_channel=VPinStudio

or read some documentation: https://github.com/syd711/vpin-studio/wiki/Mania


### iScored Subscriptions

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/iscored.png" width="400" />

I'm happy to announce the (first!) **iScored** integration for the VPin Studio. 
Since the VPin Studio server already knows everything about a table highscore and highscore changes, it
can now push these updates out to any **iScored** dashbard.
The configuration for this is super easy thanks to the dashboard scan provided by the VPin Studio client 
competitions for this are setup in no time.

Checkout the video for more details:
https://www.youtube.com/watch?v=Dq3rra5Gu1I&ab_channel=VPinStudio


### Notifications

Having highscore cards automatically updated is a nice feature, but I often get the question: "Ok, but when is the card updated?"
Checking if a highscore has been updated has always bothered me, so the Studio will support notifications now that pop up for different events and only when no emulator is currently running.
You can configure the available notifications on the corresponding preferences page.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/notifications.jpg" width="500" />

### VPin Studio App

The dashboard overlay is one of first features that came with the VPin Studio.
Unfortunately, buttons to bind it are rare on some cabinets. That's why the backend
now comes with an additional .exe file **VPin Studio App** which can be added to your VPin frontend and launched as any other game.
This app will show the dashboard and may be extended in the future with additional features, like additional screens (?) and widgets.

<img src="https://github.com/syd711/vpin-studio/blob/main/documentation/dashboard/dashboard-popper-app.png?raw=true" width="600" />

Checkout the video to see how to install the VPin Studio App:
https://www.youtube.com/watch?v=WeAqw0ojBvU&ab_channel=VPinStudio



### Controller Support

All VPin UI related keybindings have been re-implemented using a new API which also includes controller support.
This is a **breaking change** since you have to re-map all existing bindings from previous versions of the VPin Studio.


  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/controller-bindings.png" width="600" />

### We have also been busy with...

- **Documentation**: Indeed, we sometimes do that! The wiki has been updated (https://github.com/syd711/vpin-studio/wiki) and YouTube videos have been (re-)recorded.
- **Navigation**: The navigation supports deep links now. E.g. you can jump from the **Table Overview / Highscores** section to the corresponding table in the **Highscore Cards** view (and back!).
- **Preferences Changes (breaking)**: Overall, the preference have been changed a lot with some **breaking changes**. You have to re-visit and re-apply some of them.
- **Backglass Manager**: The window is resizeable now and the size and position is saved.
- **Backglass Manager / Backglass Section**: Added checkbox for the new backglass server option **Hide Backglass**, which is also part of the filter now.
- **Table Asset Manager**: Added support for **Playlist** assets.
- **Table Data Manager**: Added support for **Lookup** fields. Since you can configure custom value lists for some table fields, e.g. **manufacturer** or **game type**, these lists are now available for selection in the Table Data Manager dialog too. Only the list of **Alternate Launcher .exe** is ignored since this list is still loaded from the actual VPX installation folder.
- **Table Data Manager / VPS matching**: The automatching to VPS has been improved and now exploit all information in game filename to better determine the VPS version. Also the table version when empty and detected during the matching process is automatically filled in. 
- **Table Overview**: You can now directly access the UI settings for the table overview via a preferences button on the tab header.
- **Table Overview / Emulators**: Enabled import and delete actions for the **Future Pinball** emulator. 
- **Table Overview / Sidebar**: Added toggling button to the sidebar for a better support of smaller screens. The status of the sidebar is persisted.
- **Table Overview / Filter Section**: Splitted filter preferences for .ini and .pov files.
- **Table Overview / Filter Section**: The status of the filter is saved and restored on start.
- **Table Overview / Table Deletion**: Added option to keep the media assets of a table when it is deleted. This may come in handy if you wish to re-install a table from scratch.
- **Table Overview / Status Comments**: Added "OUTDATED" as an additional comment type.
- **Table Overview / VPin MAME**: Added apply defaults button to delete existing registry entry, as resetting a table may help sometimes to fix table problems.
- **Table Overview / VPin MAME**: Applying the MAME defaults is only possible now, when the table has existing settings / has been played once.
- **Table Overview / Highscore Section**: Added highscore card preview image shown in the lower part of the section.
- **Table Overview / Highscore Section**: The "Open Card" button has been replaced with and "Edit Card" button which jumps into the Highscore Card designer, selecting the table's card.
- **Table Overview / Replace or Append Media**: On media upload, when a media already exists, a confirmation pops up to ask whether the media file should replace existing one or be appended to the list of media files.
- **Table Overview / .res File Support**: Added .res file support which includes displaying the availability of the file in a column, uploading and filtering. 
- **Table Overview / .ini File Support**: Added .ini file support which includes a separate section for viewing/editing the existing properties of an .ini file. Note that the section only shows existing fields of the .ini. Additional entries can't be added. This way, we stay compatible with future releases of VPX.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/ini.png" width="600" />
  
- **Table Overview / Validation (breaking)**: Removed the "Outdated Recordings" validators. Since the modification date of the table is changed on edit (same for the backglass), this validator was simply too fuzzy. 
- **Table Overview / Ignored Validation (breaking)**: Changed the way ignored validators are persisted. This way, new validators can be ignored by default and for new users more exotic validators are disabled from the start.


- **Table Overview / Auto-Fill**: Added **preference dialog for the auto-fill** function. The dialog allows to select the fields that should be overwritten. The settings button for this is next to every auto-fill menu drop-down (inside the Table Data Manager and the Table Data section).

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/auto-fill.png" width="500" />

### Bugfixes

- **Pause Menu**: Fixed auto-play issues of YouTube videos.
- **VPX Monitor**: I disabled this feature for now since when enabled, this VPX process watcher hangs and I haven't found the problem yet.
- **Table Overview / ALT Sound**: Fixed missing refresh after uploading a new ALT sound package.
- **Table Overview / ALT Sound**: Removed "Enabled" checkbox. This was a leftover from the previous fixed where the audio setting was a boolean flag and set a numeric value. 
- **Table Overview / ALT Color**: VPin MAME settings "Use external DMD" and "Colorize DMD" and enabled after asset uploads now.
- **Table Overview / PUP Packs**: Fixed some refresh issues and added hint on how to disable a PUP pack.
- **Table Overview / Highscores**: Fixed out-dated help text on how to setup highscore resolving. 
- **Component Manager**: Fixed Discord server link.
- **Backglass Manager**: Fixed "Start as EXE" flag so that the actual server default is used when unchecked. 
- **Table Overview / Table Uploads**: The asset view mode is turned off after table asset uploads automatically.
- **VPS Updates**: The VPS updates were broken **(approx. for the last two month)**. The updates are now detected again, update indicators will be shown again in the table overview (if enabled).