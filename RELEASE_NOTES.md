## Release Notes 4.5.0

Note that there are breaking changes in the pause menu preferences. Please revisit these settings!

## Changes

- **World Of Virtual Pinball Competitions**
  - The VPin Studio has supports **World Of Virtual Pinball** competitions now (https://worldofvirtualpinball.com/en).
  - A new tab **WOVP Competitions** has been added to the competition section. Note that the tab is only visible after you have added a valid API key on the corresponding preferences section. More details are described on YouTube (https://youtu.be/f8E_v081ciQ).
   
- **Pause Menu**
  - The pause menu supports **desktop environments** too.
  - The **tutorial** video can be shown on a separate frontend screen now (again).
  - The **P key** configuration is now optional. You will lose the "one second" delay on resume, otherwise the focus management should ensure that the game will continue once the pause menu is closed.
  - Added menu entry for the score submission of **World Of Virtual Pinball** competitions.

- **VPin Tournaments**
  - The feature has been removed from the Studio. The focus of the tool will remain the table management and smaller competitions. This will include 3rd party systems and improvements regarding the existing Discord based solutions.


## Bugfixes

- **Discord Table Subscriptions**: Solved the channel limit issue for table subscriptions. Discord allows only 50 channels for every category and up to 500 channels in total. The VPin Studio bot can now automatically create new categories and create additional subscription channels if you have already reached the first 50 entries. Note that you bot needs the **"Manage Channel" permission** for this. So you might need to re-add the bot to your server with more permissions that before. (For help, see: https://github.com/syd711/vpin-studio/wiki/Discord-Integration#discord-permissions-for-adding-a-bot-to-your-server)
- **Discord Bot Settings**: Fixed issues with the issues checker and improved validation.