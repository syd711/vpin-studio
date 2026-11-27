# AI Assistants (MCP)

The VPin Studio Server can offer an [MCP](https://modelcontextprotocol.io) (Model Context Protocol) endpoint. AI
assistants like Claude connect to it and can then answer questions about your cabinet, explain VPin Studio from this
documentation and, if you allow it, make changes for you.

## Setup

1. Open **Preferences → AI Assistants (MCP)** and enable the endpoint.
2. Copy the command shown there and run it in a terminal, e.g.
   `claude mcp add --transport http vpin-studio http://localhost:8089/mcp`.
   If Claude runs on another computer, replace `localhost` with the network name or IP address of the cabinet.
3. Reconnect the assistant after changing any of these options (`/mcp` in Claude Code).

Like the VPin Studio REST API, the endpoint has no password: everyone in your network can use it while it is enabled.
Requests from web browsers are rejected.

## Permissions

| Option | Default | Effect |
|--------|---------|--------|
| Read-only | on | The assistant can only look up data. |
| Tables | on | Table data, component switching, validation issues, highscore resets, launching and deleting tables and backglasses. |
| Competitions and players | on | Offline competitions, iScored game rooms, synchronizing competitions, changing players. |
| Playlists and tags | on | Creating, changing and deleting playlists, tagging many tables at once. |
| Media | on | Downloading and deleting frontend media, recording screens, cancelling jobs. |
| Settings | off | Changing VPin Studio settings. |
| System | off | Frontend restart/stop, table backups and restores, server restart, cabinet shutdown. |

The groups only apply when read-only mode is off. Tools of groups that are not allowed are not offered to the assistant.

**Secrets:** passwords, API keys, tokens, the GameEx account and webhook endpoints and parameters are masked
(`********`) when the assistant reads settings, and it can never change them. The assistant can also never read or
change the AI Assistants settings or the authentication settings, so it cannot widen its own access.

**Confirmation:** the assistant is told to ask you before calling a destructive tool (deleting, resetting highscores,
restoring backups, restarting the server, shutting down) and to preview settings changes and bulk tagging with a dry
run first.

**Change log:** every change an assistant makes is listed in the AI Assistants preferences under *Changes Made by
Assistants* (tool, arguments, result, preview/error). The list is kept until the server restarts; all changes are also
written to the server log.

## Tools

Reading tools are always available.

### Tables

| Tool | Changes | Description |
|------|---------|-------------|
| `search_tables` | | Search by name, ROM, emulator, tag, PUP pack, validation issue, missing media, VPS updates, ... |
| `get_table`, `get_table_assets` | | Table details, validation issues and all files and media of a table |
| `get_table_backglasses` | | Active backglass and its versions |
| `get_table_script` | | VBScript, INI or POV of a table |
| `get_table_highscores`, `get_recent_highscores` | | Highscores |
| `list_emulators` | | Emulators and their table counts |
| `list_validation_issues` | | Issue counts over all tables and globally ignored issues |
| `check_vps_update` | | VPS entry, newer versions with download links, reported changes; link candidates for unlinked tables |
| `update_table` | Tables | Rating, tags, notes, display name, ROM, highscore file, manufacturer, year, version, author, VPS link, update notifications |
| `set_table_enabled` | Tables | Enable or disable a table in the frontend |
| `set_component_enabled` | Tables | Enable or disable PUP pack, ALT sound, ALT color; disable the backglass |
| `set_active_backglass` | Tables | Make a backglass version the active one |
| `ignore_validation`, `unignore_validation` | Tables | Ignore an issue for one table or all tables |
| `revalidate_table` | Tables | Reload and validate a table again |
| `play_table` | Tables | Launch a table |
| `reset_highscores` | Tables (destructive) | Reset the highscores (a highscore backup is created first) |
| `delete_table`, `delete_backglass` | Tables (destructive) | Delete a table or backglass |

### Competitions and players

| Tool | Changes | Description |
|------|---------|-------------|
| `list_competitions`, `get_competition` | | All competition types with scores |
| `get_iscored_status` | | Checks the iScored setup |
| `list_competition_badges` | | Badges for competitions |
| `list_players`, `get_player_achievements` | | Players and their achievements |
| `create_offline_competition`, `update_competition`, `finish_competition` | Competitions | Offline competitions |
| `add_iscored_game_room`, `synchronize_competitions` | Competitions | iScored and online competitions |
| `update_player` | Competitions | Name, initials, competition name, achievement tracking |
| `delete_competition`, `remove_iscored_game_room` | Competitions (destructive) | |

### Playlists and tags

| Tool | Changes | Description |
|------|---------|-------------|
| `list_playlists`, `get_playlist`, `list_tags` | | |
| `create_playlist`, `rename_playlist`, `add_to_playlist`, `remove_from_playlist` | Playlists | |
| `bulk_tag` | Playlists | Add and remove tags on many tables (by ids or `search_tables` filter), with dry run |
| `delete_playlist` | Playlists (destructive) | |

### Media and jobs

| Tool | Changes | Description |
|------|---------|-------------|
| `search_media_sources` | | Searches the media sources for a table and screen |
| `list_jobs`, `get_job` | | Background jobs and their progress |
| `download_media` | Media | Adds a found media file to a table |
| `start_recording` | Media | Records screens of tables |
| `cancel_job` | Media | Cancels a background job |
| `delete_media` | Media (destructive) | Deletes a frontend media file |

### Settings

| Tool | Changes | Description |
|------|---------|-------------|
| `list_settings`, `get_settings` | | The VPin Studio settings, secrets masked |
| `update_settings` | Settings | Changes some fields of one settings key, with dry run |

### System

| Tool | Changes | Description |
|------|---------|-------------|
| `get_system_info` | | Version, OS, frontend, uptime, memory, free disk space |
| `check_studio_update` | | Newer VPin Studio release on GitHub |
| `list_backups` | | Backup sources and table backups |
| `restart_frontend`, `kill_frontend` | System | |
| `create_backup`, `create_settings_backup` | System | Table backups, settings backup file |
| `restore_backup`, `restart_server`, `shutdown_cabinet` | System (destructive) | |

### Documentation

`search_help` and `get_help_page` search and read the [VPin Studio wiki](https://github.com/syd711/vpin-studio/wiki).
