# MCP Server – Expansion Plan

Status as of 2026-11-27. The code lives in `vpin-studio-server/.../server/mcp/`.

## Implementation status (2026-11-27): all phases implemented

66 tools in total. User docs: `documentation/mcp/README.md`. Tests: `McpResourceTest`, `McpSecretsTest`, `McpSettingsToolsTest`.

- Permission groups: `McpToolGroup` (TABLES, COMPETITIONS, PLAYLISTS, MEDIA, SETTINGS, SYSTEM) with `McpSettings.allow*`;
  SETTINGS and SYSTEM default off, the others default on (keeps the behaviour of existing installations).
- Secrets: `@Secret` annotation (rest-client) on ManiaSettings.apiKey, WOVPSettings.apiKey1-5, AuthenticationSettings.token,
  VPU/VPFSettings.password, PinballXSettings.gameExMail/gameExPassword, DOFSettings.apiKey, Webhook.endpoint/parameters;
  plus a name heuristic (password/secret/token/apikey/credential) in `McpSecrets` as safety net.
- Audit log: in memory (`McpAuditLog`, 200 entries), REST `api/v1/mcp/audit`, shown in the AI Assistants preferences.
- Deviations: playlists also got `list_tags`; validation and VPS tools live in `McpValidationTools`; players,
  achievements and `update_competition` in `McpPlayerTools`; `create_settings_backup` writes the system backup JSON to
  `resources/backups`. Restoring a settings backup is not offered (needs the UI's restore descriptor).
- Fixed on the way: `PreferenceNames.getClassFromKey(ASSET_SOURCES_SETTINGS)` returned `AuthenticationSettings`.
- Open: the wiki page (copy `documentation/mcp/README.md` to the GitHub wiki so `search_help` finds it); a live test
  against a real cabinet.

## Current state

| Group | Read tools | Write tools (only when `McpSettings.readOnly = false`) |
|-------|-----------|--------------------------------------------------------|
| Tables | `search_tables`, `get_table`, `get_table_assets`, `get_table_backglasses`, `list_emulators` | `set_table_enabled`, `update_table` (rating/tags/notes), `play_table`, `delete_table`, `delete_backglass` |
| Highscores | `get_table_highscores`, `get_recent_highscores` | – |
| Competitions | `list_competitions`, `get_competition`, `get_iscored_status`, `list_competition_badges` | `create_offline_competition`, `finish_competition`, `delete_competition`, `add_iscored_game_room`, `remove_iscored_game_room`, `synchronize_competitions` |
| Help | `search_help`, `get_help_page` | – |

Settings, preferences, media, playlists, backups, the frontend and system control have no tools yet.

## Guiding rules (apply to every phase)

1. **Never let the AI widen its own access.** `mcpSettings` is never readable or writable through MCP. The same goes
   for `authenticationSettings`, and every token, password and API key (Discord bot token, Mania keys, webhook URLs,
   WOVP and iScored credentials).
2. **The endpoint has no authentication** (like the REST API). Each new write tool adds attack surface on the LAN,
   so every write tool stays behind `readOnly`. For settings and system writes, consider a second opt-in flag
   (see Phase 6).
3. **Prefer typed tools over a generic setter.** A tool like `set_highscore_card_settings` with a real JSON schema
   gives the AI a precise description and gives us validation. A generic `set_setting(key, json)` is only acceptable
   for an allow-listed set of simple keys.
4. Reuse the existing services and resources; the MCP layer only maps arguments to them (as `McpManagementTools` already does).
5. Destructive tools use `McpTool.delete(...)` so `destructiveHint` is set, and the instructions require confirmation.
6. Log every write as `LOG.info("MCP: …")`, as the existing tools do. Add a test in `McpResourceTest` for each tool.

---

## Phase 1 – Settings & preferences (read first, then write)

**Goal:** "Why are my highscore cards not showing?", "Turn on the pause menu", "Switch notifications off at night".

| Tool | Kind | Notes |
|------|------|-------|
| `list_settings` | read | Allow-listed setting keys, each with a one-line description and the name of its preference page. |
| `get_settings(key)` | read | Returns the JSON of one allow-listed `*Settings` object, with secret fields masked. |
| `update_settings(key, changes)` | write | Partial update: merge `changes` into the current JSON, validate by deserializing into the settings class, then save through `PreferencesService`. Return the old and new values of the changed fields. |

Allow-list candidates (from `PreferenceNames`): `highscoreCardSettings`, `pauseMenuSettings`, `overlaySettings`,
`notificationSettings`, `validationSettings`, `ignoredValidations`, `vpsSettings`, `taggingSettings`,
`recorderSettings`, `backupSettings`, `monitoringSettings`, `dofSettings`, `achievementsSettings`, `serverSettings`
(selected fields only).
Never exposed: `mcpSettings`, `authenticationSettings`, `discordClientSettings`, `maniaSettings`, `webhookSettings`,
`wovpSettings`, plus the credential fields of `iScoredSettings`.

Open questions:
- Field-level masking: annotate secret fields, e.g. a new `@Secret` on the `JsonSettings` subclasses, instead of keeping a hard-coded list.
- Do some settings need a service reload after saving (overlay, DOF, monitoring)? Check whether `PreferenceChangedListener` covers it.

## Phase 2 – Validation workflow

**Goal:** "Fix or silence the warnings on my tables."

| Tool | Kind | Notes |
|------|------|-------|
| `list_validation_issues` | read | Counts per issue code across the cabinet (a summary of `search_tables onlyWithIssues`). |
| `ignore_validation(tableId?, code)` | write | Ignore one issue for one table, or globally through `ignoredValidations`. |
| `unignore_validation(tableId?, code)` | write | Reverts the above. |
| `revalidate_table(id)` | write | Re-runs the scan and validation of one table. |

## Phase 3 – Table metadata & components

**Goal:** Fix things the AI can diagnose today but can't change.

| Tool | Kind | Notes |
|------|------|-------|
| `update_table` (extend) | write | Add the ROM name, table name, highscore type/file, VPS table/version link and display name. |
| `set_component_enabled(id, component, enabled)` | write | Enable or disable the PUP pack, ALT sound, ALT color or backglass (these toggles exist in the UI sidebars). |
| `set_active_backglass(id, version)` | write | Switch to another backglass version from `get_table_backglasses`. |
| `reset_highscores(id)` | delete | Clears NVRAM/highscores; needs confirmation. |
| `get_table_script` / `get_table_ini` | read | Read the VBS / table INI / POV; writes come later, if ever. |
| `check_vps_update(id)` / `list_vps_updates` | read | Changelog and download links for newer VPS versions. |

## Phase 4 – Playlists & tags

| Tool | Kind |
|------|------|
| `list_playlists`, `get_playlist(id)` | read |
| `create_playlist(name, tableIds?)`, `add_to_playlist`, `remove_from_playlist`, `rename_playlist` | write |
| `delete_playlist` | delete |
| `list_tags` | read |
| `bulk_tag(query/filters, addTags, removeTags)` | write: tags many tables at once, using the same filters as `search_tables` |

Example: "Make a playlist of all 90s Williams tables I've played more than 5 times."

## Phase 5 – Media & assets

| Tool | Kind | Notes |
|------|------|-------|
| `search_media_sources(id, screen)` | read | Asset search across the configured asset sources (`vpin-connector-assets`). |
| `download_media(id, screen, assetId)` | write | Downloads a found asset into the frontend media folder. |
| `delete_media(id, screen, file)` | delete | |
| `start_recording(ids, screens)` | write | Media recorder jobs; returns a job id. |

These run as long jobs, so this phase also needs `list_jobs` / `get_job(id)` (read) and `cancel_job(id)` (write),
backed by the existing `jobs/` package.

## Phase 6 – System & frontend control (separate opt-in)

Higher risk, so add a second flag `McpSettings.allowSystemControl` (default false) in the AI Assistants preferences.

| Tool | Kind |
|------|------|
| `get_system_info` | read: versions, disk space, frontend type, server uptime |
| `restart_frontend`, `kill_frontend` | write |
| `create_backup`, `list_backups` | write / read (`backups/`) |
| `restore_backup` | delete |
| `check_studio_update` | read |
| `restart_server`, `shutdown_cabinet` | delete-level; maybe never |

## Phase 7 – Competitions & players (fill the gaps)

- `list_players`, `get_player` (read), `update_player` (write: name, initials, achievements flag)
- `update_competition` (write: dates, badge, Discord channel)
- Achievements: `get_achievements_overview`, `get_player_achievements` (read)

---

## Cross-cutting work

- **Preferences UI**: the AI Assistants page (`McpPreferencesController`) gets a per-group checklist ("Allow: tables / settings /
  playlists / media / system") instead of the single read-only switch. Tools are then filtered by group, not
  only by `readOnly`.
- **Audit log**: keep the last N MCP write calls (tool, arguments, result, timestamp) and show them in the
  preferences page, so the user can see what the AI changed.
- **Dry run**: an optional `dryRun: true` argument on bulk and settings writes that returns the diff without saving.
- **Instructions**: add guidance on the new tool groups and on confirming bulk changes to `INSTRUCTIONS_WRITE`.
- **Docs**: a wiki page "AI Assistants / MCP" listing all tools, with `WikiHelpIndex` picking it up.

## Suggested order

1. Phase 1 read tools (`list_settings`, `get_settings`) with secret masking: low risk, high value for diagnosis.
2. Phase 2 (validation): small, and builds directly on existing data.
3. Phase 1 `update_settings` + audit log.
4. Phases 3 and 4.
5. Per-group permissions UI, then Phases 5–7.
