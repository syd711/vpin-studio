## Release Notes

### Changes

- Added DOF Preferences: You can synchronize your DOF settings via API key now. Note that this synchronization is not performed during the server startup, but only added for convenience in the preferences.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/preferences/dof.png" width="500" />

- Enabled Remote VPX Installations in the "System Manager": Since a VPX update does not need any additional steps, the installer button is now enabled also when the Studio client has a remote connection to the server.
- System Manager: For the VPX installations, no additional post-processing it executed anymore (renaming of the .exe files). So the VPX zip files is extracted "as-is" and the user can take care of the **VisualPinballX.exe** that should be used in the emulator scripts of PinUP Popper.
- Table Launches: If a table is launched from the Studio, the **VPXEXE=....** configured in the launch script of the default emulator in PinUP Popper is used (It was always the **VisualPinballX.exe** before).
- Add update VPS indicators for table columns: Instead of showing only the one update column, an additional update icon with tooltip is shown for assets that are shown in the table overview and received an update.

<img src="https://raw.githubusercontent.com/syd711/vpin-studio/main/documentation/vps/update-colum.png" width="700" />


### Bugfixes

- **Fixed card generation**: The wrong filename was chosen for the card generation which lead to missing updates. (Because I'm usually too lazy to rename anything, I never noticed before.)
- VPX emulator lookup: Improved lookup from Popper database (there may have been issues when the VPX emulator was renamed).
- Release Notes Info Dialog: I've always shown the latest from the main branch, so you may have read the updates for the next version already. The dialog shows the correct version release notes now.
- Studio UI: Fixed "hanging" Studio process problem. When the server is offline and the Studio client is still open, you don't have to kill the process anymore.
- Studio UI: Fixed issues with the initial window size.
- Studio UI: Fullscreen mode now takes care about the insets caused by the location of the Windows toolbar.
- System Manager: The combo box for the release artifacts has not been filled properly. For VPX, the "GL" artifacts are now also selectable for installation.

### Known Issues

See https://github.com/syd711/vpin-studio/issues
