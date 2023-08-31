## Release Notes


**Competitions: Discord Competitions**

- Fixed Competitions button refresh: After applying a bot token, the button are now enabled (no restart required anymore).
- Fixed authentication check: Discord API did not return *true* for regular permission checks for bots with admin rights (why???).
- Improved invalid bot name error message in "Discord Competition" dialogs: It should be more clear now what action to take to fix it.

**Preferences: Bot Settings**

**Problem:** If every VPin Studio bot on a server has every permission, everyones highscore for every table could be posted (spammed) into text channels and everyone could create subscriptions.

The problem has been solved filtering servers by their bot "administration" permissions.
This means:

- Only player bots with **administrator** rights can create a Discord competition.
- Only player bots with **administrator** rights can create a channel subscription.
- Only player bots with **administrator** rights can can post "casual" highscore notifications into a text channel.


**Regular** player bots, that have been added by the server administrator can only
- join a Discord competition and post updates there.
- join a table subscription and post updates there.

A Discord server administrator has to ensure that only bots with **regular** rights are added to the server.

Details are documented here:

Tired of reading? Watch a summary about this here: 
