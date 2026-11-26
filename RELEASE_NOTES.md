## Release Notes 5.8.0

### Achievements

The **VPX Achievement Watcher** is now built into VPin Studio. The new **Achievements** section replaces the
Dashboard. 

A huge shoutout to @Solters for building the original tool and helping with this migration!

- **Automatic tracking**: Achievements are evaluated from the NVRAM audits after every game of a ROM-based table.
  More than 120 global achievements plus generated achievements for every supported ROM. Rule sets for more than
  1,500 ROMs ship with the release.
- **Overview, Tables, Badges and Leaderboards tabs**: player level and prestige, achievements closest to
  unlocking, per-table progress, 31 badges (one can be shown next to your name), global and per-table
  leaderboards with rarity information.
- **Players**: The player dialog has a new "Track achievements" option (on by default). All tracked players of
  the cabinet are credited for a game. Multiplayer games only count towards global achievements.
- **Played games**: Shows the tracked games of a table with session highlights and the unlocks of each game.
- **Notifications**: Pop-ups for achievements, badges and level-ups (can be switched off in the Notifications
  preferences).
- **Pause menu**: A new "Achievements" entry shows the table progress, the last game and the next achievements
  (can be switched off in the Pause Menu preferences).
- **VPin Mania**: Achievements are synced to your VPin Mania account and power the cloud rank and
  leaderboards. The cabinet must be registered for VPin Mania for the cloud features.
- **Migration**: Existing Achievement Watcher data (cloud Player ID + Recovery Key, or a local Watcher
  installation) can be imported in the new "Achievements" page of the preferences. See the
  [migration guide](https://github.com/syd711/vpin-studio/wiki/Achievements#migration-from-the-achievement-watcher).
- **Live tracking (optional, VPX 10.8.1)**: When enabled, VPin Studio installs a score-server plugin for VPX that
  checks achievements while you play and records your best ball.

### AI Assistants (MCP)

The VPin Studio Server can now offer an **MCP (Model Context Protocol)** endpoint, so AI assistants like Claude can
answer questions about your cabinet, e.g. "Which tables have no ROM?" or "Which tables have updates on VPS?".

- **Preferences**: Enable the endpoint in the new "AI Assistants (MCP)" page of the preferences. It is disabled by
  default. The page shows the endpoint URL and the command to add the cabinet to Claude Code:
  `claude mcp add --transport http vpin-studio http://<cabinet>:8089/mcp`
- **Tools**: List emulators, search tables (by name or ROM, only with issues, disabled or with VPS updates), table
  details including validation issues, backglass versions, the highscores of a table and the latest highscores of
  the cabinet.
- **Read-only mode**: On by default, the assistant can then only look up data. When switched off, it can also
  enable or disable tables, update ratings, tags and notes, launch tables and delete tables or backglasses. The
  assistant is told to ask for confirmation before deleting anything.
- Like the REST API, the endpoint has no password, so everyone in your network can use it while it is enabled.

### Changes

- **Players**: Removed the "Discord Players" tab. The players view now only shows the built-in players.
- **System Manager**: The Visual Pinball updates tab has a new emulator selection when multiple VPX emulators are
  configured. Release checks, simulations and installations as well as the "VPinballX.ini" button work on the
  selected emulator, so every VPX installation can be updated separately.
- **System Manager**: The overview lists the installed version of every configured VPX emulator.
