## Release Notes 3.11.3

## Bugfixes

- **Tables / Uploads**: Fixed uploads of DMD packages for tables that are installed into subfolders.


---

## Release Notes 3.11.2

## Bugfixes

- **Tables / Highscore Parsing**: Improved detection of VPReg.stg based highscore entries. The lookup is now completely case-insensitive. Also, an additional lookup is made using the ROM name plus the **_VPX** suffix which some tables use to store the highscore.
- **Emulator Detection**: The way the **nvram** and **rom** folders are read has been changed. For VPX emulators...
  - the **nvram** folder is read from the Windows registry first, instead of simply assuming the default folder of PinMAME which is used as fallback now.
  - the **roms** folder is read from the frontend/Popper first. If the value is empty there or invalid, the Windows registry value for PinMAME roms is used instead.
- **Emulators Detection**: For some reason the launch and exit calls for the VPin Studio Server have never been (automatically) added to the emulator scripts of Future Pinball. As a result, the game status was never set for this emulator. As a result, in-game recording didn't work, because no active game status was found. 
- **Preferences / VPX Monitor**: The VPX monitor has been disabled for now. The task of this monitor was to detect the table that is currently running to provide services like the pause menu also to non-Popper users. We used the VPX window title for this, but since the title does not include the active game anymore. Currently, I see no way to continue the support here.
- **Server**: Fixed issue that DOFLinx was also killed on Popper restart.
- **Tables / Table Data Manager**: Fixed issue that the Table Data Manager dialog did not open because of issues in the PinVol tab (needs to be revisited again).


---

## Release Notes 3.11.1

## Bugfixes

- **Tables / PUP Packs**: Fixed alias lookups.
- **Tables / ALT Sound**: Added "name" field, fixed alias lookups, fixed "Open Folder" button for alias names.
- **Tables / ALT Color**: Added "name" field and fixed alias lookups, fixed "Open Folder" button for alias names.
- **Tables / VPS Updates**: Fixed filtering issues.
- **Tables / Media Recorder**: Added missing sorting for the "Last Update" column.
- **Tables / Universal Uploader**: Improved detection of FlexDMD folders.
- **Tables / Universal Uploader**: Fixed issue importing music files.
- **PinVol Integration**: Fixed label and spinner inputs.
- **Backglasses / Sub-folders**: Fixed extraction of DMD images when backglass is in a sub-folder of tables.

## Release Notes 3.11.0

## Changes
 
- **Tables / PinVol Integration**: VPin Studio integrates deeper with PinVol now. (The integration might need some adjustments since I haven't used it myself yet). 
  - The PinVol system and table volume can be updated via context menu in the table overview. Multi-selection is supported.
  - The PinVol preferences allow to configure the PinVol system sound. An additional validator has been added there that shows potential conflicts with PinUP Popper.
  - The Table Data Manager dialog has also a configuration panel for the PinVol settings now, located under the tab "Settings".  
  <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/pinvol-dialog.png" width="350" />

- **Tables / Backglass Manager**: Tables / Backglass Manager: Added DMD position dialog. Finally, you don't have to search for your mouse cursor to position the DMD anymore.
  - Choose whether the freezy / VPinMAME DMD appears on backglass our full DMD screen
  - Drag and draw with your mouse the dmd position, adjust with handlers or precisely entering numbers or using +/- buttons
  - And don't miss our auto-position option so you don't even have to adjust anything manually.
   <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/dmd-positioner.png" width="500" />

- **Tables / Backglass Manager**: Added .res designer dialog. Create fancy background for your backglass!
  - This new dialog helps creating table res file. It is possible to choose wether the backglass is stretched in the screen or centered.
  - When centered, you can upload or drop an image that is used as a background.
  
   <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/res-editor.png" width="330" /> <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/res-editor-frame.png" width="330" />

- **VPin Studio Launcher**: Added Wake-on-LAN (WOL) functionality using a Magic Packet to power on any VPin Studio Servers that are in sleep mode. To enable WOL, follow these steps:
  1. **Enable Wake-on-LAN in BIOS**:
     - Restart your computer and enter the BIOS/UEFI settings (typically by pressing **DEL** or **F2** during boot).
     - Navigate to the **Advanced Settings** or **Power Management** section.
     - Enable the **Wake-on-LAN** or **WOL** option.
     - Save and exit the BIOS.
     - **Note**: The exact location and name of the WOL setting may vary by motherboard manufacturer. Refer to your motherboard's user manual or the manufacturer's website for specific instructions.
  2. **Configure Wake-on-LAN in Windows**:
     - Open **Device Manager**, find your network adapter, and go to its **Properties**.
     - In the **Power Management** tab:
       - Enable **Allow this device to wake the computer**.
       - Enable **Only allow a magic packet to wake the computer**.
     - In the **Advanced** tab, enable **Wake-on-LAN** and related options (e.g., **Wake on Magic Packet**).
  3. **Adjust Power Settings**:
     - Open **Control Panel > Hardware and Sound > Power Options**.
     - Under your active power plan, go to **Change plan settings > Change advanced power settings**.
     - Expand **Sleep > Allow wake timers** and enable it.

     **Important Note**: Enabling Wake-on-LAN can cause the computer to wake up from sleep due to any network activity. To prevent unintended wake-ups, ensure that the **Only allow a magic packet to wake the computer** option is set in the network adapter's settings. This allows users to remotely wake VPin Studio Servers even if they are in sleep mode while avoiding unintended power-on events.


- **Tables / Media Recorder**: Added playlists to table selection options.
- **Tables / Media Recorder**: Added "Last Update" column to see which table was touched last.
- **Tables / Data Manager Dialog**: Renamed tab "Customizations" to "Settings" and moved some fields to the "Meta Data" tab from there.
- **Tables / Highscore Parsing**: **"Autobots, roll out!"** @marten Added the first highscore parser that combines highscores from different modes into one list, starting with the table **Transformers**.
 
## Bugfixes

- **Tables / Overview**: Fixed critical error of the card generator that resulted in stalling the whole client (again, the last fix did work properly).
- **Tables / Media Recorder**: Increased timeout to wait for an emulator to 60 seconds. The recording was cancelled for some users, because the launch took longer than 30 seconds. 
- **Tables / Media Recorder**: Added hiding of the Windows taskbar for emulator based recordings.
- **Tables / Deletion Dialog**: Switched to a two column layout here to support smaller screens.
- **Preferences / Popper Settings**: Added missing "Watchdog" setting.
- **Tables / Invalid Highscores Filter**: The filter did not check properly the highscore type so the table list contained a lot of false positives.
- **Preferences / DOF Settings**: Fixed installation validation to check the x86 folder, not the x32.