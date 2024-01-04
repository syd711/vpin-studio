## Release Notes

### Changes

- Added DOF Preferences: You can synchronize your DOF settings via API key now. Note that this synchronization is not performed during the server startup, but only added for convenience in the preferences.
- Enabled Remote VPX Installations in the "System Manager": Since a VPX update does not need any additional steps, the installer button is now enabled also when the Studio client has a remote connection to the server.

### Bugfixes

- Improved VPX emulator lookup from Popper database (there may have been issues when the VPX emulator was renamed).
- Fixed "hanging" Studio UI closing: When the server is offline and the Studio client is still open, you don't have to kill the process anymore.
- Fixed "Release Notes" info dialog: I've always shown the latest from the main branch, so you may have read the updates for the next version already. The dialog shows the correct version release notes now.

### Known Issues

See https://github.com/syd711/vpin-studio/issues
