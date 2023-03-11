# VPin Studio

The VPin Studio is a util to manage VPin users, tables, competitions and highscores.
It depends on PinUP Popper and gives the user an overview about installed tables
and their media configuration.

## Installation

Download the latest installer from the [releases section](https://github.com/syd711/vpin-studio/releases). 

In general, the VPin Studio has a client/server architecture,
supporting the configuration of multiple VPins with one client.
That's why every release comes with two different installers:
- Full Installer: this installer must always be installed on the actual VPin and contains the server and the client UI.
- Client Installer: this installer only contains the UI client which let's you connect from any other Windows PC to your VPin.

## Documentation

For more details see [Documentation](./documentation/).
If your questions aren't answered there, you always can you our Discord server.

## Discord

You have feedback or want to report a bug? Join the VPin Studio Discord server:
https://discord.com/invite/69YqHYd3wD

## Features

The given overview shows the key features and roadmap of the VPin Studio


#### Highscores
  - extraction and versioning of different highscores
  - highscore card generator (including style editing)
  - highscore backup, reset and restore

#### Table Management
  - table validators (PinUP Popper media validation, general config validation, ...)
  - POV export & editing
  - table script preview
  - table volume control
  - table upload
  - rom upload
  - directb2s upload
  - table backup and restore (including highscores)

#### User Management
  - build in user management
  - Discord user management

#### Discord Integration
  - Discord channel to VPin mapping (1:1 connection) (when a group of people using 1x VPin for "offline" competitions)
  - Discord channel to VPin**s** mapping (1:n connection) (when different VPin owners want to compete against each other on one Discord server)
  - Discord user name to highscore mapping
  - BOT support:
    - request table highscore
    - request player archievements
    - request player rankings
    - request active competition
    
#### Competition Management
 - Offline Competitions (these are used for when multiple people using one VPin)
 - Online Competitions (see Discord integration)

#### Dashboard Support
 - VPin dashboard support for UHD, WQHD and HD resolutions
 - "Recent Scores Widget": shows the latest _n_ highscores that have been created with player name info
 - "Active Online Competition Widget": shows the status of the active online competition 
 - "Active Offline Competition Widget": shows the status of the active offline competition 
 - "Player Ranking Widget": shows a leader board of all players

#### Highscore Card Generation
  - updates highscore cards on table-exit with the latest highscore
  - editor support for styling highscore cards
  - directb2s background image support

#### Miscellaneous
- Automatic shutdown support for VPins
- PinUP Popper reset support


## Sample Screenshots

A sample log generated from on the VPin bot:
<img src="./documentation/discord-sample.png" width="800"><br/>
_Discord Integration_

<img src="./documentation/tables1.png" width="800"><br/>
_Table Overview_

<img src="./documentation/tables-validation.png" width="800"><br/>
_Validation Issue Example_


## Third Party Licenses and Resources

For license texts have a look at [Third-Party Libraries](./documentation/third-party-licenses/)

### Icons
<a href="https://www.flaticon.com/free-icons/trophy" title="trophy icons">Trophy icons created by Freepik - Flaticon</a><br/>
<a href="https://www.flaticon.com/free-icons/medal" title="medal icons">Medal icons created by Freepik - Flaticon</a><br/>

### Fonts
Fonts: https://www.1001fonts.com/digital-7-font.html#license

### 7zip
https://7-zip.org/

### Sounds 
Sound Effect by <a href="https://pixabay.com/users/edr-1177074/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=8325">EdR</a> from <a href="https://pixabay.com//?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=8325">Pixabay</a>

Sound Effect from <a href="https://pixabay.com/sound-effects/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=92097">Pixabay</a>
