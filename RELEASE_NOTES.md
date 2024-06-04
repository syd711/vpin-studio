## Release Notes 2.20.1

- **Added VPX Monitor**: The server contains a new service called "VPX Table Monitor", which checks if VPX is started and with which table. **Why is this important?** This monitor also emits the table launch and exit events which cause e.g. the Discord BOT status update and the updates of highscore cards on table exit. So you can simply launch a table without using Popper (e.g. if you are a **VR player or using PinballX**) and the automatic highscore update will now work too. The service is **disabled by default** and can be enabled in the **Server Preferences**. 
- **Highscore Card Resolutions**: Added option to change the resolution of the highscore cards. Note that changing the highscore card size will re-crop every the default background of every table so that the best possible image quality is achieved. This option has been introduced for users showing their highscore cards on a FullDMD screen (which often use HD resolutions).


### Bugfixes

- **VPin MAME Service**: Optimized pre-caching when reading MAME settings from the Windows registry.