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

### iScored Subscriptions

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/iScored-logo.png" width="400" />

I'm happy to announce the (first!) **iScored** integration for the VPin Studio. 
Since the VPin Studio server already knows everything about a table highscore and highscore changes, it
can now push these updates out to any **iScored** dashbard.
The configuration for this is super easy thanks to the dashboard scan provided by the VPin Studio client 
competitions for this are setup in no time.

Checkout the video for more details:
https://www.youtube.com/@vpin-studio/videos


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

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/misc/notifications.jpg" width="500" />

Checkout the video to see how to install the VPin Studio App:
https://www.youtube.com/@vpin-studio/videos



### Controller Support

All VPin UI related keybindings have been re-implemented using a new API which also includes controller support.
This is a **breaking change** since you have to re-map all existing bindings from previous versions of the VPin Studio.

  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/controller-bindings.png" width="600" />

### Miscellaneous

- **Table Overview**: You can now directly access the UI settings for the table overview via an preferences button on the tab header. 
- **Table Overview / Table Deletion**: Added option to keep the media assets of a table when it is deleted. This may come in handy if you wish to re-install a table from scratch.
- **Preferences Changes**: Overall, the preference have been changed a lot with some **breaking changes**. You have to re-visit and re-apply some of them.
- **Sidebar**: Added toggling button to the sidebar for a better support of smaller screens. The status of the sidebar is persisted.
- **Filter Section**: Splitted filter preferences for .ini and .pov files.

### Bugfixes

- **Pause Menu**: Fixed auto-play issues of YouTube videos.
- **VPX Monitor**: I disabled this feature for now since when enabled, this VPX process watcher hangs and I haven't found the problem yet.
- **Table Overview / PUP Packs**: Fixed some refresh issues and added hint on how to disable a PUP pack.