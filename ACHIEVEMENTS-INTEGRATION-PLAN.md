# Achievements Integration Plan (Achievement Watcher → VPin Studio)

Status: plan / analysis — nothing implemented yet. Branch: `aw-port`. Date: 2026-11-22.

Goal: port the Python **VPX Achievement Watcher** (AW, `C:\workspace\vpx-achievement-watcher`, v3.2) to Java,
integrate it into VPin Studio as the new **Achievements** section that replaces the **Dashboard**, reuse the
Studio's existing NVRAM map parsing, vpxtool, VPS database and table lifecycle, and replace the Firebase
player identity with **VPin Mania accounts**. A new *Achievements* page in the VPin Mania preferences lets
users migrate their existing Firebase data to VPin Mania.

Inputs analyzed:

- Visual concept `VPin Studio – Achievements Integration.html` (4 boards, unpacked from the nested bundle):
  A1 Overview, A2 Tables, A3 Badges, A4 Leaderboards.
- AW sources: `core/watcher_core.py`, `core/badges.py`, `core/cloud_sync.py`, `core/cloud_auth.py`,
  `core/watcher_io.py`, `core/config.py`, `app_data/global_achievements.json`, `firebase/database.rules.json`,
  `docs/cloud-anti-cheat.md`, `README.md`.
- Studio: server (`games`, `highscores`, `nvrams`, `mania`, `players`, `vps`, `vpx`, `vpinmame`, `alx`,
  `notifications`, `frontend`), UI (`DashboardController`, `NavigationController`, `mania/*`),
  commons widgets, `java-pinmame-nvmaps` library (`C:\workspace\java-pinmame-nvmaps`),
  `vpin-mania-rest-client` (`C:\workspace\vpin-mania-rest-client`),
  VPin Mania backend (`C:\workspace-php\vpin-mania`: Lumen 10 / PHP 8.1, MySQL with raw SQL, React web client).

---

## 1. Executive summary

- **Everything shown in the concept can be implemented.** Roughly 70 % maps directly onto AW logic, ~20 % is
  already in the Studio, and ~10 % is new logic that AW does not have (progress values for "Closest to
  unlock" and locked badges, "+N this week", table filters, player counts per table, multi-player cabinets).
- **Three repositories change:** the Studio, `vpin-mania-rest-client` (new `AchievementClient`) and the
  VPin Mania backend (`C:\workspace-php\vpin-mania`: new `mania_achievement_*` tables, an
  `AchievementController`/`AchievementService`, delete cascades, optional web view). See §6.
- **The Studio removes most of AW's infrastructure code:** window-title polling + vpxtool ROM detection
  (→ `TableStatusChangeListener` + `Game.getRom()`), the Python NVRAM decoder with its "autofix" heuristics
  (→ `java-pinmame-nvmaps`, which decodes platform memory regions correctly), the VPS-ID mapping UI
  (→ `Game.extTableId/extTableVersionId`), the hard-coded manufacturer table (→ VPS manufacturer),
  the installed-table scan (→ `GameService`), the Firebase identity/anti-cheat layer (→ Mania cabinet token),
  overlays/sounds/themes/hotkeys/tray/updater/backup (→ existing Studio features or out of scope).
- **The core to port is small and well defined:** rule generation (global + per-ROM), rule evaluation,
  level/prestige, 31 badges, rarity tiers, and the cloud data model.
- **Two design decisions need the user** (see §10): how a game session is attributed to one of several
  cabinet players, and how cabinet-wide NVRAM totals are handled for per-player global achievements.

---

## 2. The concept, screen by screen — feasibility

Legend: **AW** = logic exists in the Achievement Watcher, **ST** = exists in Studio, **NEW** = needs new logic,
**MANIA** = needs the new VPin Mania backend API.

### Common header (all four tabs)

| Element | Source | Notes |
|---|---|---|
| Nav item "Achievements" replaces "Dashboard" (2nd position, star icon) | ST | `NavigationItem.Dashboard` → `Achievements`, `scene-dashboard.fxml` → `scene-achievements.fxml`. Not persisted anywhere (only `NavigationController` uses it). "Designer" label already exists. |
| Breadcrumb "/ Achievements", toolbar (VR Mode, jobs, Drop-In, monitor, online, settings) | ST | Existing toolbar, unchanged. |
| Tabs Overview / Tables / Badges / Leaderboards | NEW | `TabPane` like other Studio sections. |
| **Player** selector ("Matthias3 (MFA)") | ST + NEW | View filter over cabinet players with "Track achievements" on (§10.1). Only players linked to a Mania account (`Player.maniaAccountUuid`) get cloud data; others are local-only. |
| **Sync with cloud** button | AW + MANIA | Pushes pending unlocks/progress/summary, refreshes leaderboards + rarity. |

### A1 · Overview

| Element | Source | Notes |
|---|---|---|
| Level circle, level name, prestige stars, progress bar "306 / 400 to Pinball Legend" | AW | `compute_player_level` (10 levels, prestige every 2 000, max 5). Port 1:1. |
| Unlocked count | AW | Unique titles across global + per-ROM unlocks. |
| "+14 this week" | NEW (trivial) | Count unlocks with `ts` ≥ now-7d. AW stores `ts` per unlock already. |
| Cloud rank "#128 of 2,310 players" | AW + MANIA | AW `lb_global` top-N + own rank. Mania endpoint must also return the total player count. |
| Recent unlocks with rarity chip | AW + MANIA | Unlock list sorted by `ts`; rarity tiers from Mania (Common ≥50 %, Uncommon ≥25 %, Rare ≥10 %, Epic ≥5 %, Legendary <5 %). |
| Latest scores | ST | Reuse `WidgetLatestScoresController` (commons) — same widget the Dashboard uses today. |
| Player ranking — **Cabinet** | ST | Reuse `WidgetPlayerRankController` (highscore-based ranking, "24× 1st", points). |
| Player ranking — **Cloud** | AW + MANIA | Global achievement leaderboard (top N + own rank). |
| Badges "12 / 31" mini grid + "All badges" link | AW | 31 badge definitions; link switches to Badges tab. |
| **Closest to unlock** (progress %) | NEW (partly AW) | Global rules: AW `global_tally` progress (nvram_tally, rom_count, rom_complete_set, rom_multi_brand) ✓. Per-ROM session rules (`nvram_delta`) have no persistent progress in AW → **NEW**: store the best session delta per ROM/field and show `best / min`. |

### A2 · Tables

| Element | Source | Notes |
|---|---|---|
| Table list with "unlocked/total" and progress bar | AW + ST | AW keys everything by ROM. List = Studio games (`GameService`) with a ROM, joined to per-ROM progress. Several games sharing one ROM show the same progress. |
| Search "table or ROM" | ST | Filter on `Game.gameDisplayName` and `Game.rom`. |
| Filters "In progress / Completed / Not started / No NVRAM map" with counts | NEW | Computed from progress + `NVRamMapParser.isSupportedRom` (after alias/family resolution, §5.3). |
| Summary: wheel icon, name, "ROM … · VPS-ID matched · last played …" | ST | Wheel from game media, VPS match from `extTableId`, last played from ALX (`AlxService`) or last achievement session. |
| "N / M achievements", %, rarity distribution (Common 14, Uncommon 7, …) | AW + MANIA | Count of unlocked per rarity tier for this ROM. |
| "Open in Tables" / "Play stats in Analytics" | ST | Navigate to `NavigationItem.Tables` with the game selected / the Analytics (ALX) tab. |
| Achievements table: status ✓/🔒, title, unlocked date, rarity; "Show locked" | AW + MANIA | Rules from the ROM's rule set + unlock state. Add a tooltip with the condition (AW's ℹ️ info: "Ramps Made ≥ 10 in one game"). |
| "Cloud Top 5 · this table" + own rank | AW + MANIA | AW `lb/<rom>` top 25 + own rank; show 5 here. |

### A3 · Badges

| Element | Source | Notes |
|---|---|---|
| Player level card + level table (Lvl / Name / From) + prestige explanation | AW | `LEVEL_TABLE`, `PRESTIGE_THRESHOLD=2000`, `MAX_PRESTIGE=5`. Emoji in AW names are dropped (concept uses plain names). |
| Badge filter All / Earned / Locked | NEW (UI) | — |
| Badge groups: Milestones, Prestige, Exploration, Playtime, Special, Rarity, Cloud & Level | AW | Exactly AW's 31 `BADGE_DEFINITIONS` and order. Category per badge is implicit in AW (comments) → make it explicit in the Java enum. Icons/colors per category from the concept (SVG paths in the concept file). |
| Locked badge progress text ("306 / 500", "38 / 50 h", "best: 41 min", "best: 4", "not yet", "Level 6 / 10") | NEW | AW `BADGE_CHECKS` return booleans only. The Java port returns `BadgeProgress {current, target, unit}` per badge from the same stats. |
| Selected badge detail + "Show next to my name" | AW + MANIA | AW `selected_badge`; stored in the Mania achievement summary, shown in leaderboards. |

### A4 · Leaderboards

| Element | Source | Notes |
|---|---|---|
| Global: top 50 by number of different achievements, rank medals, level, badge dot, own row, "2,310 players" | AW + MANIA | AW `lb_global` (n, t, b) + level from `players/<pid>/achievements/player_level`. Mania returns it in one call. |
| Per table: table/ROM search, recent-table chips, top 25 by progress | AW + MANIA | AW `lb/<rom>` (n, u, t, p, b, ts). Search over local games plus a Mania search endpoint for boards that exist in the cloud. Chips = recently played tables. |
| Table header: ROM, total achievements, **players**, your rank, your progress, "VPS entry" link | AW + MANIA + ST | Players = number of accounts with progress on that ROM (AW used `rarity/<rom>/players`). VPS link via existing VPS utilities. |
| Rows: medal rank, avatar/initials, name, badge, progress bar "n / total (x %)", date | MANIA + ST | Mania accounts have avatars (`AccountClient.getAvatarUrl`) — can replace the initials circles. |

**Conclusion:** no concept element is blocked. Items marked NEW are small; items marked MANIA depend on the
backend work in §6.

---

## 3. AW features that are *not* in the concept

| AW feature | Recommendation |
|---|---|
| Achievement **toasts**, level-up and badge-unlocked notifications on the cabinet | **Include (phase 2)** via the existing `NotificationService` (on-cabinet notifications with title1-3, image, duration, `showOnEmulatorExit`). Cheap and important for the experience. |
| End-of-game **in-game overlay** (3 pages: highlights & score, achievement progress, cloud top 25) | **Later (phase 4).** Candidate: a Pause-Menu page / post-exit overlay in the Studio's existing overlay stack. |
| Session **highlights** (Power/Precision/Fun events from NVRAM deltas, Best Ball) | Later, together with the overlay. Port `HIGHLIGHT_RULES`/`EVENT_KEYWORDS` then. |
| **Multiplayer** (score-server plugin + patched PinMAME plugin for VPX 10.8.1, up to 4 players, live per-game rounds) | **Later (phase 4).** Phase 1 mitigation: read `game_state.player_count` from the map at table exit and skip achievements when > 1 (AW also awards nothing in multiplayer games). |
| **Live unlocks** during the game (10.8.1 rounds protocol) | Later (needs the score-server plugin). Phase 1 evaluates at table exit — same as AW on VPX < 10.8.1. |
| **AWEditor** (custom achievements for tables without a ROM: script analysis, `FireAchievement` VBScript injection, `.trigger` files, `progress_cat`/`lb_cat`) | **Out of scope** for the first release. The data model keeps an `origin`/`key` field so custom tables can be added later. |
| Stats tab: global NVRAM dumps, player session deltas | Optional. The session table (§5.4) stores start/end audits and deltas; a simple "Session details" dialog can be added later. |
| Dashboard "Setup status" checklist and notifications (rank beaten, missing VPS-IDs, updates) | Partly: show hints in the Achievements view (e.g. "12 tables have no VPS match — cloud upload disabled for them", "Player not linked to a Mania account"). Studio already has validators for VPS matching. |
| Score **duels / tournaments** | Out of scope (already hidden in AW 3.2; Studio has its own competitions). |
| Appearance (themes, fonts, overlay placement), 20 sound packs, effects, hotkeys/joystick, tray, setup wizard, auto-update, ZIP backup, recovery key | Not needed — Studio has its own equivalents (or they are Firebase-specific). |
| Local anti-tamper **signatures** and Firebase anti-cheat rules | Replaced by server-side validation in VPin Mania (cabinet token, plausibility checks, rate limits). The signature code is only needed to *read* AW files during migration (§7). |

---

## 4. What the Studio already provides (reuse map)

| Need | AW implementation | Studio reuse |
|---|---|---|
| Detect table start/end | Poll `Visual Pinball - [..]` window title every 1-2 s, resolve `.vpx`, run `vpxtool romname` | `TableStatusChangeListener.tableLaunched/tableExited` (`FrontendStatusService`) with `Game` and origin. Skip `ORIGIN_STUDIO` test launches (same rule as `ManiaService`). |
| ROM of a table | vpxtool | `Game.getRom()` (already scanned); vpxtool available via `VPXUtil.runVpxTool` if ever needed. |
| ROM alias (`afm_113Serum` → `afm_113b`) | `VPMAlias.txt` + prefix heuristics | `VPinMameRomAliasService.getRomForAlias(emulator, alias)`; port only the family fallback (§5.3). |
| `.nv` file location | `NVRAM_DIR/<rom>.nv` | `FolderLookupService.getNvRamFile(game)` / `HighscoreResolver.getNvRamFile(game)`. |
| NVRAM map files + updates | Download tomlogic maps from GitHub on start | `resources/maps` (index.json, romnames.json, maps/), updated via `ServerUpdatePreProcessing` (`maps/main.zip`). |
| Decode audits | Own Python decoder (raw file offsets, BCD/int, "autofix" guesses encoding/size by plausibility) | `net.nvrams.mapping.map.NVRamMapParser`: `getMap(rom)`, `getMemory(map, bytes)`, `map.getAudits()` → `NVRamMappings` → `NVRamMapping.getValue(memory)`. Platform-aware (memory regions, nibbles, endianness, checksums) → more correct than AW. `map.getGameState().getPlayerCount()` for multiplayer detection. |
| VPS-ID of a table | Own mapping file + auto-match + Popper import | `Game.extTableId` / `extTableVersionId`, `VpsService` (auto-match, table lookup, version info). |
| Manufacturer of a ROM | Hard-coded `MANUFACTURER_MAP` prefixes | `VpsService.getTableById(game.extTableId).getManufacturer()`, fallback to the ported prefix map for unmatched games. |
| Installed tables per manufacturer (`rom_complete_set`) | vpxtool scan of the tables folder | `GameService.getGames()` + VPS manufacturer. |
| Player identity | 4-char Player ID + name, Firebase anonymous auth, recovery key | Cabinet `Player` (name, initials, avatar) linked to a Mania `Account` via `Player.maniaAccountUuid`. |
| Cloud | Firebase RTDB REST | `ManiaService` / `VPinManiaClient` (cabinet token auth), extended by an `AchievementClient` (§6). |
| Playtime | Sum of session durations from summary files | ALX total playtime (`AlxService`, §10.3); Endurance from the own session table. |
| Notifications on the cabinet | Qt toasts | `NotificationService.showNotification(Notification)`. |
| Latest scores / cabinet ranking widgets | — | `WidgetLatestScoresController`, `WidgetPlayerRankController` (commons). |
| Mania preference pages | — | `ManiaSettingsController` + `ManiaSettingsPage` pattern (account, privacy, discord pages). |
| Settings storage | `config.json` | `PreferencesService` JSON preferences (`PreferenceNames`). |
| Persistence | Signed JSON files | SQLite via Spring Data JPA. |

---

## 5. Architecture of the port

### 5.1 Modules and packages

```
vpin-studio-server/  de.mephisto.vpin.server.achievements
  AchievementsService            orchestration, TableStatusChangeListener, Initializing
  AchievementsResource           REST  /api/v1/achievements/...
  session/AchievementSessionTracker   start/end snapshot, duration, player count
  nvram/NvramAuditReader         audits Map<String,Long> for a ROM + .nv file (wraps NVRamMapParser)
  nvram/RomMapResolver           exact → alias → romnames parent → family fallback
  rules/AchievementRule, RuleCondition (sealed: NvramDelta, SessionTime, NvramTally, NvramOverall,
        RomCount, RomCompleteSet, RomMultiBrand)
  rules/GlobalRules              loads bundled global_achievements.json (122 rules)
  rules/RomRuleGenerator         port of _ensure_rom_specific (+ milestone tables, NOISE_REGEX, excluded fields)
  rules/RuleEvaluator            port of _evaluate_achievements + _evaluate_player_session_achievements
  progression/LevelCalculator    port of compute_player_level
  progression/Badge (enum, 31), BadgeEvaluator (earned + progress)
  progression/RarityTier
  ManufacturerResolver           VPS manufacturer → fallback prefix map
  cloud/AchievementsCloudService upload queue (unlocks, progress, summary), retry on failure, pull for restore
  migration/FirebaseLegacyClient anonymous sign-up, claim(pid, key), read players/<pid>/{achievements,progress,vps_mapping}
  migration/LocalWatcherImporter reads <AW BASE>/Achievements/achievements_state.json + cloud_identity.json
  migration/AchievementsMigrationService preview + import
  model/ (JPA entities + repositories, §5.4)

vpin-studio-rest-client/  de.mephisto.vpin.restclient.achievements
  AchievementsServiceClient, AchievementsSettings,
  representations: PlayerAchievementsSummary, LevelInfo, AchievementEntry, TableAchievementsProgress,
  BadgeEntry, ClosestUnlock, MigrationSource/MigrationPreview/MigrationResult

vpin-studio-ui/  de.mephisto.vpin.ui.achievements
  AchievementsController (+ scene-achievements.fxml)       replaces DashboardController
  AchievementsOverviewController / TablesController / BadgesController / LeaderboardsController (+ fxml)
vpin-studio-ui/  de.mephisto.vpin.ui.mania
  ManiaAchievementsSettingsController (+ mania-achievements-settings.fxml)
  dialogs/AchievementsMigrationDialogController (+ fxml)
```

The UI reads Mania leaderboards directly through `Studio.maniaClient` (as other Mania views already do);
all writes go through the Studio server so they also work when the UI is not running.

### 5.2 Session lifecycle (replaces AW's watcher thread)

1. `tableLaunched(event)`: ignore if achievements are disabled, origin is Studio, game has no ROM, or no map.
   Skip if no player has "Track achievements" enabled. Resolve ROM (§5.3), read **start audits** from the
   `.nv` file, create one `AchievementSession` per game (credited to all tracked players, §10.1).
   Ensure the ROM rule file exists (generate on first play, like AW).
2. `tableExited(event)`: wait until the `.nv` file is flushed (debounce on mtime; `HighscoreMonitoringService`
   already watches the nvram folder and can signal the change), read **end audits**, compute deltas
   (negative → 0), duration, player count. Ignore sessions < 60 s (AW `ROUND_MIN_SEC`).
3. If `player_count > 1` → skip per-game ROM rules (multiplayer); global tallies still count.
4. For each tracked player: evaluate global rules + ROM session rules against that player's state, persist
   unlocks, update tallies/best deltas, `roms_played`, badges, level; queue cloud upload (linked players);
   fire notifications (phase 2).
5. Session start/end is single-threaded per server; a Studio restart during a game loses the start snapshot
   → session discarded (log it).

### 5.3 ROM / map resolution

`NVRamMapParser` only does exact index lookups. Port AW's candidate chain into `RomMapResolver`:
exact ROM → `VPinMameRomAliasService` → `romnames.json` parent → family candidates (`afm_113b` → `afm_113`,
`afm`, prefix scan of index keys) — accept a candidate only if it has a map **and** an existing `.nv` file
(AW #880 rule). Cache results per ROM.

### 5.4 Local data model (SQLite / JPA)

| Table | Columns (main) | Notes |
|---|---|---|
| `achievement_unlocks` | id, playerId, rom, title, achKey (sha1(title)[:12]), scope (`global`/`rom`), origin, gameId?, vpsTableId?, vpsVersionId?, unlockedAt, sessionId?, source (`studio`/`firebase`/`local-import`), syncedAt? | Unique (playerId, title). Title stays the identity for AW compatibility; `achKey` matches AW's rarity hash. `local-import` rows are never uploaded (§10.7). |
| `achievement_sessions` | id, gameId, rom, startedAt, endedAt, durationSec, playerCount, startAudits (json), endAudits (json), deltas (json) | One row per game; the players credited are those tracked at that time (`achievement_session_players`: sessionId, playerId, unlockCount). Source for Endurance, night owl, hot streak, speed demon, last played. |
| `achievement_player_state` | playerId (PK), trackingSince, badges (json), selectedBadge, globalTally (json), romsPlayed (json), cloudUploadDone, migratedFirebasePid?, migratedAt? | One row per cabinet player. |
| `Player.achievementsEnabled` | new column on the existing players table | "Track achievements" flag (§10.1), default true. |
| `achievement_field_best` | playerId, rom, field, bestDelta | NEW — enables "Closest to unlock" for per-ROM rules. |

Rule definitions are files, not DB rows: `resources/achievements/global_achievements.json` (bundled default,
copied on first start) and `resources/achievements/rom/<rom>.ach.json` (generated on first play — same
format as AW, so AW files can be imported as-is during migration).

### 5.5 Settings

`PreferenceNames.ACHIEVEMENTS_SETTINGS` (JSON): enabled, cloudSyncEnabled, notificationsEnabled,
minSessionSeconds (60). Per-player tracking is the `Player.achievementsEnabled` flag (§10.1).

---

## 6. VPin Mania backend (`C:\workspace-php\vpin-mania`) + client

### 6.1 Backend facts that shape the design

- Lumen 10, raw SQL via `DB::select/insert/update` (no Eloquent), schema in `server/sql/create_schema.sql`
  (no migrations — new `CREATE TABLE` blocks are appended and must be run on production **before** deploying
  code that uses them), all routes in `server/routes/web.php`, PascalCase columns, `mania_` prefix.
- Auth (`RequestValidator`): `VPin-Token` header = `mania_users.Token` **or** `ApiKey` → `VPin-UserId`.
  Public GET routes are whitelisted explicitly in the middleware.
- `mania_accounts` (Id, UUID, CabinetId, DisplayName, Initials, Visibility, Status) = Studio players linked
  via `Player.maniaAccountUuid`. Leaderboards filter `Visibility = "searchable"` and `Status != "blocked"`.
- `HighscoreController@insertOrUpdateHighscore` does **not** check that the account belongs to the caller's
  cabinet. Achievements will check it (`account.CabinetId → cabinet.UserId = VPin-UserId`).
- `PlayerMergeService` ("Merge players" user setting): accounts with the same initials on different cabinets
  of one user are one player in leaderboards (`canonicalAccountIds`, `withMergedAccountIds`).
- Deletes are hand-written cascades: `AccountController@deleteAccount`, `CabinetController` (cabinet delete,
  line ~233), `UserController@deleteUser`/`unlinkCabinet` delete highscores, ranking rows, Discord data.
- Global ranking is a precomputed table (`mania_global_ranking_list`, `RanksController::regenerateRankings`);
  achievements can compute on the fly with indexes (data per account is small) and add a cache if needed.
- Web client (React/MUI) has `content/PlayerDetails` (route `players/:playerUuid`) and `TableDetails`
  (route `table/:vpsTableId`) — natural places for an optional achievements view.

### 6.2 New tables (append to `create_schema.sql`)

```sql
CREATE TABLE mania_achievement_unlocks (
  Id int NOT NULL AUTO_INCREMENT,
  AccountId int NOT NULL,
  AchKey char(12) NOT NULL,          -- sha1(title)[:12], same hash AW uses for rarity
  Title varchar(200) NOT NULL,
  Rom varchar(40) NULL,              -- NULL for global achievements
  Scope varchar(8) NOT NULL,         -- 'global' | 'rom'
  VPSTableId varchar(16) NULL, VPSVersionId varchar(16) NULL,
  UnlockedAt DATETIME NOT NULL, Source varchar(16) NOT NULL, -- 'studio' | 'firebase'
  PRIMARY KEY (Id), UNIQUE KEY (AccountId, AchKey), KEY (Rom, AchKey));
CREATE TABLE mania_achievement_progress (
  AccountId int NOT NULL, Rom varchar(40) NOT NULL,
  Unlocked int NOT NULL, Total int NOT NULL, Percentage decimal(5,1) NOT NULL,
  VPSTableId varchar(16) NULL, VPSVersionId varchar(16) NULL, TableName varchar(200) NULL,
  UpdatedAt DATETIME NOT NULL, PRIMARY KEY (AccountId, Rom), KEY (Rom, Percentage));
CREATE TABLE mania_achievement_summary (
  AccountId int NOT NULL, UniqueCount int NOT NULL, Level int NOT NULL, Prestige int NOT NULL,
  Badges varchar(1024) NULL, SelectedBadge varchar(64) NULL, UpdatedAt DATETIME NOT NULL,
  PRIMARY KEY (AccountId), KEY (UniqueCount));
CREATE TABLE mania_achievement_imports (
  FirebasePid varchar(8) NOT NULL, AccountId int NOT NULL, ImportedAt DATETIME NOT NULL,
  PRIMARY KEY (FirebasePid));
```

Rarity is computed, not stored: `COUNT(DISTINCT AccountId) GROUP BY AchKey WHERE Rom = ?` against
`COUNT(DISTINCT AccountId) WHERE Rom = ?` (AW semantics: players with ≥ 1 unlock on that ROM), visible and
non-blocked accounts only, merged players counted once.

### 6.3 Routes (`AchievementController` + `App\Services\AchievementService`)

| Route | Auth | Purpose | Replaces Firebase |
|---|---|---|---|
| `POST api/cabinet/{cabinetId}/achievements/{accountUuid}/sync` | token, owns account | One batch per sync: new unlocks (idempotent on AchKey), progress rows, summary. Validates `0 ≤ unlocked ≤ total ≤ 5000`, computes percentage, length limits, rate limit. | `players/<pid>/*`, `lb/*`, `lb_global/*`, rarity counters |
| `GET api/achievements/account/{accountUuid}` | public | Unlocks + progress + summary (restore, web view). Hidden/blocked → 404. | restore_from_cloud |
| `GET api/achievements/leaderboard/global?top=50&account=` | public | Entries (name, initials, avatar uuid, level, badge, count), ownRank, ownEntry, totalPlayers | `lb_global` |
| `GET api/achievements/leaderboard/rom/{rom}?top=25&account=` | public | Entries (progress, date, badge), ownRank, players, total | `lb/<rom>` |
| `GET api/achievements/leaderboard/search?term=` | public | ROMs/table names that have boards | — |
| `GET api/achievements/rarity/{rom}` | public | players + count per AchKey (client maps to tiers) | `rarity/<rom>` |
| `POST api/cabinet/{cabinetId}/achievements/{accountUuid}/import` | token, owns account | Firebase migration (§7). Rejects an already imported pid. | — |

Also: whitelist `api/achievements/*` GETs in `RequestValidator`; add the new tables to the delete cascades
(account, cabinet, user delete); merged players via `PlayerMergeService` in leaderboards/rarity (union of unlocks, §10.5).

### 6.4 `vpin-mania-rest-client`

New `AchievementClient` (registered in `VPinManiaClient`) + models `AchievementSync`, `AchievementUnlock`,
`AchievementProgress`, `AchievementSummary`, `AchievementLeaderboard(+Entry)`, `AchievementRarity`,
`AchievementImportRequest/Result`; version bump (currently 2.0.1) in the root and server `pom.xml`.

---

## 7. Firebase → VPin Mania migration (Mania preferences → new "Achievements" page)

Principle (§10.6): Firebase is only read, once per player, to restore data. The single write is the
ownership claim (two small records, no achievement data). Nothing else is ever pushed to Firebase, and the
Studio has no ongoing Firebase connection.

Page content:

- Achievements on/off, cloud sync on/off, cabinet notifications on/off.
- Per cabinet player: "Track achievements" flag and Mania link status (cloud sync only for linked players).
- **Migrate from Achievement Watcher** button → wizard dialog.

Wizard:

1. **Target**: choose the cabinet player. Cloud imports need a Mania-linked player (offer to link);
   local-only imports work for any tracked player.
2. **Source** (two options):
   - **Local Watcher installation** (default when found; default path `C:\vPinball\VPX Achievement Watcher`,
     else read `BASE` from AW's `config.json`): read `Achievements/achievements_state.json`
     (+ `.bak` fallback), `cloud_identity.json` (pid + recovery key), `tools/vps/vps_id_mapping.json`,
     `Achievements/rom_specific_achievements/*.ach.json`. Verify AW's signature
     (sha256 of `json.dumps(sort_keys, separators=(',',':'))` + salt, legacy salt variant) and warn on
     mismatch. The salts are public, so a valid signature only detects casual edits — it is **not** proof of
     ownership. The Player ID + recovery key from `cloud_identity.json` are used for the claim. If the claim
     succeeds, the import is uploaded like a cloud import; if not (never synced, key missing), the data is
     imported **locally only** (`source = local-import`, never uploaded — §10.7).
   - **Achievement Watcher cloud**: enter Player ID + Recovery Key (AW System → General, or from the ZIP
     backup). The Studio server signs in anonymously to Firebase (Identity Toolkit `accounts:signUp`) and
     performs AW's `claim(pid, key)` (`owners/<pid>` + `uids/<uid>` multi-path write). Success proves
     ownership. Then read `players/<pid>/achievements`, `.../achievements/session`, `.../progress`,
     `.../vps_mapping` (all publicly readable).
3. **Preview**: name, level, prestige, unique achievements, ROMs played, badges, selected badge, number of
   ROMs whose VPS mapping differs from the Studio's game matching.
4. **Import**: write `achievement_unlocks` (source=`migrated`, keep original `ts` and `vps_id`),
   `achievement_player_state` (badges, selected badge, roms_played, global_tally, cloudUploadDone), copy
   ROM rule files that do not exist yet, then upload to Mania via the import endpoint. Store
   `migratedFirebasePid`; the Mania server rejects a second import of the same pid.
5. **Result** + note: *the claim moves the Firebase Player ID to the Studio; the old Watcher can no longer
   upload. Stop the Watcher afterwards, otherwise both track the same games.*

Notes:

- Rarity counters cannot be migrated per player (they are aggregates). Rarity is recomputed from Mania data;
  cloud leaderboards/rarity are thin until users migrate. Optional seeding: `lb_global` is publicly readable
  and enumerable, so a one-time bulk import of legacy players as "unclaimed" entries is possible — needs a
  privacy decision, not planned.
- Unlocked titles that do not exist in the Studio's generated rule set (e.g. rule files AW generated
  differently) are kept: they count for level/badges and appear as "legacy" in the table view.

---

## 8. Porting details and AW issues to fix

**Port 1:1 (must match for migration compatibility):**

- Global rule titles (`app_data/global_achievements.json`: 92 `nvram_tally`, 19 `rom_count`,
  9 `rom_complete_set`, 2 `rom_multi_brand`) — bundle the JSON as-is.
- ROM rule generation `_ensure_rom_specific`: priority fields (sections containing feature/champion/mode),
  `ok_label` filter, `NOISE_REGEX`, excluded fields, category round-robin, session milestones and caps,
  `session_time` rules (5/10/15/20/30/45 min), `_unique_title` (" #2"). Titles have the form
  `"<rom> – <field>: <n>"` (en dash). Label extraction must match AW's `parse_map/_extract_nested`
  (including nested labels like "… Score"/"… Initials" and adjustments/high_scores/mode_champions fields).
  → **Golden tests**: generate `.ach.json` files with the Python code for ~20 representative ROMs (WPC,
  Data East, Sega, Stern Whitestar/SAM, Gottlieb System 3, Bally 6803) and compare titles.
- "Highest stage per field per session" rule for non-live evaluation (`best_per_field`).
- Level table, prestige, rarity tiers, badge ids/names/descriptions, `selected_badge`.

**Decoder difference (accepted):** AW decodes raw file offsets with plausibility "autofix"; the Java library
decodes per platform memory layout. Values become more correct; unlock thresholds are unchanged, so stored
unlocks stay valid. Watch for labels/fields AW decoded that the library skips (covered by golden tests).

**AW bugs not to port:**

- Rarity badges (`rare_finder`, `epic_hunter`, `legendary_hunter`) check whether *any* achievement in the
  rarity cache has that tier, not whether the player unlocked one — and `evaluate_badges` is called without
  a rarity cache after a session, so they are effectively never awarded. Port: tier of the player's own
  unlocked achievements.
- `hot_streak` uses the total unlock count per ROM over all time instead of per session. Port: per session.
- `night_owl` only sees the latest summary per ROM (files are overwritten). Port: all session start times.
- Playtime is summed from summary files that are overwritten per ROM. Port: ALX total (§10.3), Endurance
  from the session table.
- `nvram_tally` uses `max(tally, sum of absolute audits across all played ROMs)` — absolute audits are
  cabinet-wide history. Port: deltas since tracking started only (§10.2).

---

## 9. Implementation phases

### Phase 0 — Groundwork (no UI)
1. Golden-test fixtures from the Python AW (rule files for ~20 ROMs, `compute_player_level`, badge cases).
2. `NvramAuditReader` + `RomMapResolver` on top of `NVRamMapParser` (expose a shared parser bean instead of
   creating a second instance; `NvRamParsingConfiguration` already creates one).
3. Rule model, `GlobalRules`, `RomRuleGenerator`, `RuleEvaluator`, `LevelCalculator`, `BadgeEvaluator`,
   `ManufacturerResolver` with unit tests (JUnit 5/AssertJ, like the existing nvram tests).

### Phase 1 — Local tracking + Achievements UI (replaces Dashboard)
4. JPA entities/repositories (§5.4), settings preference, `Player.achievementsEnabled` (+ checkbox in the
   player dialog and the players table).
5. `AchievementsService` as `TableStatusChangeListener` (§5.2) crediting all tracked players, multiplayer
   handling, min session length, ALX playtime.
6. REST resource + `AchievementsServiceClient` + representations.
7. UI: `NavigationItem.Achievements`, `AchievementsController` with tabs; Overview (local parts + reused
   widgets), Tables, Badges; remove `DashboardController`/`scene-dashboard.fxml`; i18n keys in all
   `messages*.properties`; documentation page under `documentation/` replacing `dashboard`.

### Phase 2 — Notifications
8. Achievement / badge / level-up notifications via `NotificationService` (respect settings).

### Phase 3 — VPin Mania cloud + migration
9a. Backend (`C:\workspace-php\vpin-mania`): schema block (§6.2), `AchievementController` +
    `AchievementService` with ownership check, validation, merged-player union, visibility/blocked filters,
    public GET whitelist, delete cascades (account/cabinet/user). Deployment note: run the SQL first.
9. `AchievementClient` in `vpin-mania-rest-client`, version bump.
10. `AchievementsCloudService`: upload queue with retry, "Sync with cloud", restore from Mania for a player.
11. Cloud parts of the UI: rarity chips/distribution, cloud rank, cloud ranking, Cloud Top 5, Leaderboards tab,
    "Show next to my name".
12. Mania preferences "Achievements" page + migration wizard (§7), `FirebaseLegacyClient`,
    `LocalWatcherImporter` (+ signature verification tests with real AW files).

### Phase 4 — Optional / later
13. Post-game overlay pages and session highlights; session details dialog.
14. VPX 10.8.1 score-server plugin: multiplayer per-player results and live unlocks.
15. Custom achievements for ROM-less tables (AWEditor port).
16. Mania web app: achievements on PlayerDetails / TableDetails (§10.8).

---

## 10. Decisions (2026-11-22)

1. **Attribution — all tracked players.** Every played game is credited to **every cabinet player whose new
   per-player flag "Track achievements" is on** (new `Player.achievementsEnabled`, checkbox in the player
   dialog, default on). No "active player" setting. Consequence: tracked players on one cabinet progress
   together; they differ by when tracking started, migrated data and their flag.
2. **NVRAM totals — per player, own games.** `nvram_tally` progress = sum of deltas of tracked games since the
   player's tracking started (no absolute audit sums). Migrated tallies are the starting value.
   With decision 1 this is effectively "cabinet games since tracking started".
3. **Playtime — include frontend history.** Total playtime for Dedicated/Marathon/Addict = ALX total
   (`AlxService`, seconds played over all games; ALX already contains tracked games, so nothing is added on
   top). It is cabinet-wide, so all tracked players share it. Endurance (single game ≥ 60 min) needs
   per-session durations, which ALX does not have → tracked sessions only.
4. **Players without a Mania account — tracked locally.** Cloud parts hidden with a hint to link the player;
   pending unlocks are uploaded once the player is linked (`syncedAt` is null until then).
5. **Merged players (Mania "Merge players") — union of unlocks.** Merged accounts appear once in leaderboards
   and rarity; an achievement counts once over all merged accounts; level/badge count from the union.
   Implemented server-side with `PlayerMergeService::withMergedAccountIds/canonicalAccountIds`.
6. **Firebase is a one-time, read-only restore source — "claim once, then read".** Per player, once: the
   Studio server signs in anonymously, performs the claim (the only write: `owners/<pid>` + `uids/<uid>`, two
   tiny ownership records, **no achievement data**) to prove the Recovery Key, reads the player's data, and
   uploads it to Mania's import endpoint. The Studio **never** writes unlocks, progress, leaderboard entries
   or rarity counters to Firebase and never contacts Firebase again after the import. Side effect of the
   claim: the old Watcher can no longer upload for that Player ID. Mania trusts the cabinet's payload (same
   trust model as highscores) but validates plausibility and stores the pid once.
7. **Local Watcher files — local only.** Importable into the cabinet's local achievements; uploaded to Mania
   only when a Firebase claim for that Player ID succeeds (keys from `cloud_identity.json`). Unclaimed
   imports are marked `source = local-import` and never uploaded.
8. **Mania web app — later.** Backend API only in this project; PlayerDetails/TableDetails views are a
   follow-up.

Defaults taken without asking (say if you disagree):

- **Multiplayer games** (`game_state.player_count > 1`): per-game ROM achievements are skipped (the NVRAM
  deltas mix all players and would make them too easy); global tallies and playtime still count.
- **Firebase after migration**: no shutdown plan in this project; admin-assisted migration for lost recovery
  keys is out of scope.

---

## 11. Risks

| Risk | Mitigation |
|---|---|
| Generated ROM rule titles differ from AW → migrated unlocks don't match table progress | Golden tests against Python output; import AW's own `.ach.json` files when migrating from a local installation; keep unmatched titles as "legacy". |
| `.nv` file not yet flushed at `tableExited` | Debounce on mtime / use `HighscoreMonitoringService` file-change signal, timeout fallback. |
| Multiplayer games inflate per-game achievements | `player_count` check (phase 1), score-server plugin (phase 4). |
| Leaderboards fill up with identical rows of players from one cabinet (all tracked players progress together) | Expected with §10.1; users can switch tracking off per player; merged players collapse across cabinets. |
| Mania schema deployed after the code | Deployment checklist: run the §6.2 SQL on production first (same rule as the Discord tables). |
| Firebase claim side effect (old Watcher loses upload rights) | Clear warning in the wizard; it is also the intended end state (one tracker per player). |
| Fake imports (edited local files) | Ownership via Firebase claim; Mania stores the imported pid once; server-side plausibility checks. |
| Load on Mania (rarity/leaderboards per view) | Server-side aggregation + caching (`ManiaServiceCache` pattern), client caches per view session. |
