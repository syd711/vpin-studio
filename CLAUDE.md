# VPin Studio - Agent Guide

This document provides guidance for AI agents working with the VPin Studio codebase.

## Project Overview

**VPin Studio** is a comprehensive virtual pinball cabinet management system. It provides management of pinball cabinets running PinUP Popper with a client/server architecture supporting multi-machine management.

- **License:** MIT
- **Repository:** github.com/syd711/vpin-studio

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 25 |
| Server | Spring Boot 4.0.6 |
| UI Framework | JavaFX 25 with FXML |
| Build Tool | Maven 3.x |
| Database | SQLite |
| Logging | SLF4J + Logback |
| Testing | JUnit 5, Mockito, AssertJ |

### Key Libraries

- **Spring Data JPA + Hibernate** - ORM
- **JDA 5.0.0** - Discord integration
- **JNA 5.13.0** - Windows native bindings
- **ControlsFX** - Extended JavaFX controls
- **Jackson** - JSON serialization

## Project Structure

```
vpin-studio-dev/
├── vpin-connector-assets/      # Asset retrieval library
├── vpin-connector-discord/     # Discord bot connector
├── vpin-connector-github/      # GitHub release handler
├── vpin-connector-iscored/     # ISCored leaderboard connector
├── vpin-connector-vps/         # Visual Pinball Scenes connector
├── vpin-connector-wovp/        # World of VPin connector
├── vpin-studio-commons/        # Shared utilities, FX components
├── vpin-studio-rest-client/    # REST API client library
├── vpin-studio-server/         # Spring Boot backend (REST API)
├── vpin-studio-ui/             # JavaFX desktop client
├── vpin-studio-app/            # Additional app utilities
├── vpin-tools/                 # Developer tools
├── vps-bot/                    # VPS bot integration
├── documentation/              # User documentation
├── resources/                  # Themes, backgrounds, scripts
└── pom.xml                     # Parent Maven POM
```

### Key Server Packages

Location: `vpin-studio-server/src/main/java/de/mephisto/vpin/server/`

| Package | Purpose |
|---------|---------|
| `games/` | Game/table management |
| `highscores/` | Highscore tracking |
| `competitions/` | Competition management |
| `emulators/` | VPX, FP emulator support |
| `frontend/` | Frontend system integration |
| `preferences/` | User preferences |
| `res/` | REST API endpoints |
| `mania/` | VPin Mania integration |

### Key UI Packages

Location: `vpin-studio-ui/src/main/java/de/mephisto/vpin/ui/`

| Package | Purpose |
|---------|---------|
| `tables/` | Table management UI |
| `competitions/` | Competition UI |
| `preferences/` | Preferences dialog |
| `mania/` | Mania mode UI |
| `launcher/` | Application launcher |

## Build Commands

```bash
# Full build
mvn clean package

# Build specific module
mvn clean install -f vpin-studio-server/pom.xml -DskipTests

# Run tests
mvn test

# Build without tests
mvn clean package -DskipTests
```

### Run Configurations

**Server (Development):**
- Main class: `de.mephisto.vpin.server.VPinStudioServer`
- VM args: `-Dfile.encoding=utf8 -Dspring.profiles.active=dev -Dserver.port=8089`
- Module: `vpin-studio-server`

**UI Client:**
- Main class: `de.mephisto.vpin.ui.Studio`
- Module: `vpin-studio-ui`

## Architecture Patterns

### Client/Server Communication

- Server runs on port `8089` (configurable)
- REST API base path: `api/v1/`
- REST client library in `vpin-studio-rest-client`

### REST API Convention

```java
// Resource classes in: vpin-studio-server/.../res/
@RestController
public class GameResource {
    @GetMapping("/api/v1/games")
    public List<Game> getGames() { ... }
}
```

### Service Layer

```java
// Server services in: vpin-studio-server/.../
@Service
public class GameService { ... }

// REST client in: vpin-studio-rest-client/.../
public class GameServiceClient { ... }
```

### UI Pattern (JavaFX)

- FXML files in: `src/main/resources/de/mephisto/vpin/ui/`
- Controllers: `*Controller.java`
- CSS styling in resources
- **Visual rules (colors, font sizes, buttons, pills, reusable tiles): `UI-STYLEGUIDE.md`** — follow it for every new or
  reworked view; `sm-*` classes live in `vpin-studio-ui/.../ui/stylesheet-system-manager.css`; `stylesheet.css` only `@import`s
  one `stylesheet-<widget>.css` per widget type (import order = cascade order)

```java
// Controller pattern
public class TableOverviewController implements Initializable {
    @FXML private TableView<GameRepresentation> tableView;

    @Override
    public void initialize(URL url, ResourceBundle rb) { ... }
}
```

## Key Conventions

### Package Naming

- Base package: `de.mephisto.vpin.*`
- REST controllers: `*Resource.java`
- Services: `*Service.java` or `*ServiceClient.java`
- DTOs: Plain objects in `restclient` package

### Database

- SQLite database at `./resources/vpin-studio.db`
- Spring Data JPA repositories
- Hibernate with SQLiteDialect

### Configuration

| File | Purpose |
|------|---------|
| `application.properties` | Server configuration |
| `application-dev.properties` | Dev profile overrides |
| `resources/system.properties` | Installation-specific settings |

### Logging

```java
private static final Logger LOG = LoggerFactory.getLogger(MyClass.class);
```

- Log level: INFO (default)
- Log file: `vpin-studio-server.log`

## Common Development Tasks

### Adding a New REST Endpoint

1. Create/update resource class in `vpin-studio-server/.../res/`
2. Add service method in `*Service.java`
3. Add client method in `vpin-studio-rest-client/.../`
4. Update DTO if needed

### Adding UI Feature

1. Create FXML in `vpin-studio-ui/src/main/resources/de/mephisto/vpin/ui/`
2. Create controller in `vpin-studio-ui/src/main/java/de/mephisto/vpin/ui/`
3. Wire up in parent controller or navigation

### Adding External Connector

1. Create new module `vpin-connector-*`
2. Add to parent `pom.xml`
3. Implement connector interface
4. Integrate in server module

## Testing

Tests are located in `src/test/java/` mirroring source structure.

```bash
# Run all tests
mvn test

# Run specific module tests
mvn test -f vpin-connector-vps/pom.xml
```

Key test files:
- `vpin-connector-vps/` - VPS matching tests
- `vpin-connector-discord/` - Discord integration tests
- `vpin-studio-commons/` - Utility tests

## Important Files

| File | Purpose |
|------|---------|
| `VPinStudioServer.java` | Server bootstrap |
| `Studio.java` | UI application bootstrap |
| `PreferenceNames.java` | Preference key constants |
| `VPinStudioClient.java` | Main REST client |
| `FeaturesInfo.java` | Feature flags |

## Development Setup

1. Install JDK 25 with JavaFX (Zulu `jdk+fx` recommended)
2. Clone repository
3. Windows: copy `resources/jvm/jinput-dx8_64.dll` to JDK's bin folder
4. Create `resources/system.properties` with PinUP System path
5. Build: `mvn clean install -DskipTests`
6. Use IDEA run configurations in `.run/`

### Linux

The server runs on Linux in Standalone mode, see `LINUX-SERVER.md`. Windows-only code is guarded with
`OSUtil.isWindows()`; build with `mvn -Plinux` to skip the Windows launcher.

## External Integrations

- **PinUP Popper** - Primary frontend system
- **Visual Pinball X** - Main emulator
- **Discord** - Bot and webhook integration
- **VPS** - Visual Pinball Scenes database
- **ISCored** - Leaderboard system
- **MAME** - Arcade emulator support

## Branches

- `main` - Stable release branch
- Feature branches for development
- Current: `mania-claude` (Mania mode development)

## MP3 Asset Handling

VPX tables reference mp3 files in their embedded VBScript. VPin Studio scans, stores, and resolves these references across several classes.

### Scanning — `VPXFileScanner#lineSearchMp3FileName`

Uses `MP3_EXPRESSION_PATTERN` to match both simple quoted filenames and VBScript string-concat expressions:

```
(?:"[^"]*"|\w+)(?:\s*&\s*(?:"[^"]*"|\w+))*
```

`buildMp3Wildcard` converts each match: quoted parts become literal path segments, variable/identifier tokens become `*` wildcards. Backslashes are normalized to forward slashes. Deduplication via `addAsset()`: the path-qualified version (`sounds/intro.mp3`) wins over a plain filename (`intro.mp3`) for the same basename.

Examples:
- `"intro.mp3"` → `intro.mp3`
- `"MFDOOM\Attract" & i & ".mp3"` → `MFDOOM/Attract*.mp3`

### Storage — `GameDetails.assets` / `Game.assets`

Pipe-delimited string written by `GameCachingService`:
```java
gameDetails.setAssets(StringUtils.join(scanResult.getAssets(), "|"));
// e.g. "MFDOOM/Attract*.mp3|intro.mp3|sounds/theme.mp3"
```

### Music Folder Resolution — `FolderLookupService#getGameMusicFolder`

Derives the game-specific music subfolder from the stored asset paths:

1. Extract unique directory parts from all asset paths (`MFDOOM` from `MFDOOM/Attract*.mp3`)
2. No folder parts → return music root (assets at root level)
3. One folder → use it
4. Multiple folders → prefer the one whose last component matches the ROM name (case-insensitive)
5. Multiple folders, no ROM match → use the deepest one (most path components)
6. No assets at all → fallback: `musicRoot/<rom>`

`getMusicFolder(Game)` returns the root: legacy layout uses `<installationFolder>/Music/`, modern uses `<gameFolder>/music/`.

### Lookup / Validation — `MusicService`

- `getMp3Files(Game)` — returns all `File` objects matching the stored asset patterns. Exact names use `File.exists()`; wildcard patterns use `Files.walk` + `FilenameUtils.wildcardMatch` on relativized paths.
- `getMissingMp3Files(Game)` — same loop, collects patterns for which no matching file was found. Bogus patterns like `/*.mp3` (pure-variable paths with no literal prefix) are skipped.

## Achievements

Java port of the Python **VPX Achievement Watcher** (AW). The "Achievements" navigation item replaced the
Dashboard. Player identity is the VPin Mania account; Firebase is only a one-time migration source.
User docs: `documentation/achievements/README.md`.

### Code locations

- Server `server/achievements/`: `AchievementsService` (TableStatusChangeListener, session evaluation,
  notifications), `AchievementsResource` (`/api/v1/achievements/...`), `AchievementsQueryService`,
  `AchievementRuleStore`, `AchievementGameIndex`
  - `nvram/` NvramAuditReader, RomMapResolver (exact → alias → romnames parent → family), MapFieldExtractor
  - `rules/` AW-compatible rule model, RomRuleGenerator, RuleEvaluator; global rules bundled as
    `achievements/global_achievements.json`
  - `progression/` LevelCalculator, Badge/BadgeEvaluator, RarityTier, SessionHighlights
  - `bundle/` release-bundled per-ROM rule sets (see release step below)
  - `cloud/` AchievementsCloudService (sync queue to Mania, restore, rarity cache)
  - `migration/` FirebaseLegacyClient (claim once, then read), LocalWatcherImporter, AchievementsMigrationService
  - `scoreserver/` live tracking via the VPX 10.8.1 score-server plugin (WebSocket `ws://127.0.0.1:3131`),
    VpxPluginInstaller
  - `model/` JPA entities: AchievementUnlock, AchievementSession, AchievementPlayerState
- REST client `restclient/achievements/` (AchievementsServiceClient, AchievementsSettings, DTOs);
  setting key `PreferenceNames.ACHIEVEMENTS_SETTINGS`; per-player flag `Player.achievementsEnabled`
- UI `ui/achievements/` (Overview/Tables/Badges/Leaderboards tabs, Played games + migration dialogs),
  prefs `preferences/preference-achievements.fxml`; pause menu entry `commons/fx/pausemenu/AchievementsMenuItemController`
- Mania backend: `C:\workspace-php\vpin-mania` (`AchievementService.php`, `AchievementController.php`, schema at
  the end of `server/sql/create_schema.sql`); client `vpin-mania-rest-client` `AchievementClient` (≥ 2.1.0).
  The UI reads Mania leaderboards directly via `Studio.maniaClient`; all writes go through the Studio server.
- Plugin build: `.github/workflows/build-vpx-plugins.yml` + `.github/vpx-plugins/*.py` (pre-releases
  `vpx-plugins-*`, never "latest" because the Studio updater uses releases/latest).

### Design decisions

- **Attribution:** every game is credited to all players with "Track achievements" on; no active-player concept.
- **Tallies** are deltas since tracking started (no absolute audit sums); playtime comes from ALX.
- **Multiplayer games** (player count > 1) skip per-ROM rules; global tallies still count. With live tracking
  (opt-in, `liveTrackingEnabled`) single-player games are checked live from ball 2.
- **Rule titles are the identity** and must stay AW-compatible (migration). Golden tests in
  `src/test/resources/achievements/golden` guard RomRuleGenerator; rules only depend on the NVRAM map.
- Players without a Mania account are tracked locally; unlocks upload once linked. Merged Mania players
  share the union of unlocks.
- Known AW bugs intentionally not ported (rarity badges, hot streak, night owl, tally over absolute audits).
- Levels do not cycle after a prestige (AW restarts at Rookie): VPX Elite is kept, stars still come every 2000.

### Release step

Before each release run `RuleBundleBuilder <pinmame-nvram-maps checkout> <commit>` from the repo root and
commit the added files. It only adds ROMs; "held back" ROMs (whose current map would change their rules) are
kept on purpose — changing them needs a deliberate rule migration.

### Testing notes

Spring tests (e.g. `AchievementsServiceIntegrationTest`) modify `testsystem/vpin-studio.db` and
`testsystem/vPinball/PinUPSystem/PUPDatabase.db` — restore with `git checkout -- testsystem/`.

## Additional Resources

- `README.md` - Project overview
- `GETSTARTED.md` - Developer setup guide
- `RELEASE_NOTES.md` - Version history
- `documentation/` - User documentation
