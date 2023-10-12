## Release Notes


### Changes

**VPin Studio Connection Handling**

- Added error handler for connection timeouts: If a request failed (e.g. because the Wifi connection is not stable), the Studio will exit to the launcher screen.

**Table Management: Multi VPX Emulator Support**

- This was a design flow from the beginning. The table lookup isn't configured through the installation of the VPin Studio anymore, but solely through the data that is found in the PinUP Popper database. So PinUP Popper is the single source of truth regarding the VPX emulator setup now. You can also configure multiple VPX emulators know. The VPin Studio will resolve the affected emulator automatically (if possible), otherwise you will find a new emulator combobox in some of the upload dialogs to determine an upload target. 

### Bugfixes

- Re
