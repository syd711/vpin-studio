## Release Notes 3.11.0

## Changes
 
- **Tables / Media Recorder**: Added playlists to table selection options.
- **Tables / Highscore Parsing**: **"Autobots, roll out!"** @marten Added the first highscore parser that combines highscores from different modes into one list, starting with the table **Transformers**.
- **Tables / Backglass Manager**: Added .res designer dialog.

    <img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/tables/res-editor.png" width="600" />

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

**Important Note**: Enabling Wake-on-LAN can cause the computer to wake up from sleep due to any network activity. To prevent unintended wake-ups, ensure that the **Only allow a magic packet to wake the computer** option is set in the network adapter's settings.

This allows users to remotely wake VPin Studio Servers even if they are in sleep mode while avoiding unintended power-on events.


## Bugfixes

- **Tables / Media Recorder**: Increased timeout to wait for an emulator to 60 seconds. The recording was cancelled for some users, because the launch took longer than 30 seconds. 
- **Tables / Media Recorder**: Added hiding of the Windows taskbar for emulator based recordings. 