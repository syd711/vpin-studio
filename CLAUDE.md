# VPin Studio - Agent Guide

This document provides guidance for AI agents working with the VPin Studio codebase.

## Project Overview

**VPin Studio** is a comprehensive virtual pinball cabinet management system. It provides management of pinball cabinets running PinUP Popper with a client/server architecture supporting multi-machine management.

- **Version:** 4.6.3
- **License:** MIT
- **Repository:** github.com/syd711/vpin-studio

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 11 |
| Server | Spring Boot 2.7.4 |
| UI Framework | JavaFX 11/17 with FXML |
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

1. Install JDK 11 (Zulu with JavaFX recommended)
2. Clone repository
3. Copy `resources/jvm/jinput-dx8_64.dll` to JDK's bin folder
4. Create `resources/system.properties` with PinUP System path
5. Build: `mvn clean install -DskipTests`
6. Use IDEA run configurations in `.run/`

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

## Additional Resources

- `README.md` - Project overview
- `GETSTARTED.md` - Developer setup guide
- `RELEASE_NOTES.md` - Version history
- `documentation/` - User documentation
