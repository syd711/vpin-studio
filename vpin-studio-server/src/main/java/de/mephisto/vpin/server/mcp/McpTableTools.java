package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Score;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The read-only table tools of the MCP endpoint. They delegate to the existing services and return compact maps,
 * so the AI only receives the fields that are meaningful for it (no internal file handles or caches).
 */
@Service
public class McpTableTools {

  private static final int DEFAULT_LIMIT = 50;
  private static final int MAX_LIMIT = 500;

  private static final Map<Integer, String> VALIDATION_NAMES = readValidationNames();

  @Autowired
  private GameService gameService;

  @Autowired
  private EmulatorService emulatorService;

  public List<McpTool> getTools() {
    List<McpTool> tools = new ArrayList<>();

    tools.add(McpTool.read("list_emulators", "List emulators",
        "Lists the emulators configured on the cabinet (Visual Pinball X, Future Pinball, MAME, ...) with their id and "
            + "the number of installed tables. Use the id to restrict search_tables to one emulator.",
        schema(Map.of(), List.of()), args -> listEmulators()));

    tools.add(McpTool.read("search_tables", "Search tables",
        "Searches the installed tables of the cabinet. Without a query all tables are returned (up to the limit). "
            + "The query matches the table name, display name, ROM name and file name (case-insensitive). "
            + "Each result contains the table id needed by the other tools.",
        schema(Map.of(
                "query", prop("string", "Text to search for, e.g. \"Medieval\" or a ROM name like \"mm_109c\"."),
                "emulatorId", prop("integer", "Only return tables of this emulator, see list_emulators."),
                "onlyWithIssues", prop("boolean", "Only return tables that have a validation issue."),
                "onlyDisabled", prop("boolean", "Only return tables that are disabled in the frontend."),
                "onlyUpdateAvailable", prop("boolean", "Only return tables for which a newer version is listed on the Virtual Pinball Spreadsheet (VPS)."),
                "limit", prop("integer", "Maximum number of results, default " + DEFAULT_LIMIT + ", maximum " + MAX_LIMIT + ".")),
            List.of()), this::searchTables));

    tools.add(McpTool.read("get_table", "Get table details",
        "Returns all details of a table: files, ROM, highscore configuration, media packs, VPS link, play count, "
            + "rating, tags, notes and the list of current validation issues.",
        schema(Map.of("id", prop("integer", "The table id, see search_tables.")), List.of("id")),
        this::getTable));

    tools.add(McpTool.read("get_table_highscores", "Get table highscores",
        "Returns the current highscore list of a table.",
        schema(Map.of("id", prop("integer", "The table id, see search_tables.")), List.of("id")),
        this::getHighscores));

    tools.add(McpTool.read("get_recent_highscores", "Get recent highscores",
        "Returns the most recent new highscores across all tables of the cabinet.",
        schema(Map.of("count", prop("integer", "Number of highscores to return, default 10.")), List.of()),
        this::getRecentHighscores));

    return tools;
  }

  //-------------------------------- tools ------------------------------------------------------------

  private Object listEmulators() {
    Map<Integer, Long> counts = gameService.getGames().stream().collect(Collectors.groupingBy(Game::getEmulatorId, Collectors.counting()));
    List<Map<String, Object>> result = new ArrayList<>();
    for (GameEmulator emulator : emulatorService.getValidGameEmulators()) {
      Map<String, Object> e = new LinkedHashMap<>();
      e.put("id", emulator.getId());
      e.put("name", emulator.getName());
      e.put("type", emulator.getType() != null ? emulator.getType().name() : null);
      e.put("tableCount", counts.getOrDefault(emulator.getId(), 0L));
      result.add(e);
    }
    return result;
  }

  private Object searchTables(McpArguments args) {
    String query = StringUtils.trimToNull(args.getString("query"));
    Integer emulatorId = args.getInt("emulatorId");
    boolean onlyWithIssues = args.getBoolean("onlyWithIssues", false);
    boolean onlyDisabled = args.getBoolean("onlyDisabled", false);
    boolean onlyUpdateAvailable = args.getBoolean("onlyUpdateAvailable", false);
    int limit = Math.max(1, Math.min(MAX_LIMIT, args.getInt("limit", DEFAULT_LIMIT)));

    List<Game> matches = gameService.getGames().stream()
        .filter(g -> emulatorId == null || g.getEmulatorId() == emulatorId)
        .filter(g -> query == null || matches(g, query))
        .filter(g -> !onlyWithIssues || hasIssue(g))
        .filter(g -> !onlyDisabled || g.isDisabled())
        .filter(g -> !onlyUpdateAvailable || g.isUpdateAvailable())
        .sorted(Comparator.comparing(g -> StringUtils.defaultString(g.getGameDisplayName()), String.CASE_INSENSITIVE_ORDER))
        .toList();

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("total", matches.size());
    result.put("returned", Math.min(limit, matches.size()));
    result.put("tables", matches.stream().limit(limit).map(this::toSummary).toList());
    return result;
  }

  private Object getTable(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    Map<String, Object> t = toSummary(game);
    t.put("tableName", game.getTableName());
    t.put("gameFile", game.getGameFilePath());
    t.put("gameFileSize", game.getGameFileSize());
    t.put("directB2S", game.getDirectB2SPath());
    t.put("romAlias", game.getRomAlias());
    t.put("scannedRom", game.getScannedRom());
    t.put("highscoreType", game.getHighscoreType() != null ? game.getHighscoreType().name() : null);
    t.put("highscoreFile", game.getHsFileName());
    t.put("validScoreConfiguration", game.isValidScoreConfiguration());
    t.put("pupPack", game.getPupPackName());
    t.put("pupPackDisabled", game.isPupPackDisabled());
    t.put("altSound", game.isAltSoundAvailable());
    t.put("altColor", game.getAltColorType() != null ? game.getAltColorType().name() : null);
    t.put("dmdType", game.getDMDType() != null ? game.getDMDType().name() : null);
    t.put("vpsVersionId", game.getExtTableVersionId());
    t.put("vpsVersion", game.getExtVersion());
    t.put("patchVersion", game.getPatchVersion());
    t.put("ignoreUpdates", game.isIgnoreUpdates());
    t.put("vrRoomSupport", game.isVrRoomSupport());
    t.put("dateAdded", toString(game.getDateAdded()));
    t.put("dateUpdated", toString(game.getDateUpdated()));
    t.put("fileModified", toString(game.getModified()));
    t.put("notes", game.getComment());
    t.put("validations", gameService.validate(game).stream().map(McpTableTools::toValidation).toList());
    return t;
  }

  private Object getHighscores(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    ScoreSummary summary = gameService.getScores(game.getId());
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("tableName", game.getGameDisplayName());
    result.put("scores", summary != null ? summary.getScores().stream().map(s -> toScore(s, false)).toList() : List.of());
    return result;
  }

  private Object getRecentHighscores(McpArguments args) {
    int count = Math.max(1, Math.min(100, args.getInt("count", 10)));
    ScoreSummary summary = gameService.getRecentHighscores(count);
    return summary != null ? summary.getScores().stream().map(s -> toScore(s, true)).toList() : List.of();
  }

  //-------------------------------- helpers ----------------------------------------------------------

  private Game requireGame(int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      throw new McpToolException("No table found for id " + id + ", use search_tables to look up the id.");
    }
    return game;
  }

  private static boolean matches(Game game, String query) {
    return StringUtils.containsIgnoreCase(game.getGameDisplayName(), query)
        || StringUtils.containsIgnoreCase(game.getTableName(), query)
        || StringUtils.containsIgnoreCase(game.getGameName(), query)
        || StringUtils.containsIgnoreCase(game.getGameFileName(), query)
        || StringUtils.containsIgnoreCase(game.getRom(), query);
  }

  private static boolean hasIssue(Game game) {
    ValidationState state = game.getValidationState();
    return state != null && state.getCode() > 0;
  }

  private Map<String, Object> toSummary(Game game) {
    Map<String, Object> t = new LinkedHashMap<>();
    t.put("id", game.getId());
    t.put("name", game.getGameDisplayName());
    t.put("emulatorId", game.getEmulatorId());
    GameEmulator emulator = game.getEmulator();
    t.put("emulator", emulator != null ? emulator.getName() : null);
    t.put("fileName", game.getGameFileName());
    t.put("rom", StringUtils.trimToNull(game.getRom()));
    t.put("version", StringUtils.trimToNull(game.getVersion()));
    t.put("disabled", game.isDisabled());
    t.put("rating", game.getRating());
    t.put("timesPlayed", game.getNumberPlayed());
    t.put("tags", game.getTags());
    t.put("vpsTableId", game.getExtTableId());
    t.put("updateAvailable", game.isUpdateAvailable());
    t.put("missingAssets", game.isHasMissingAssets());
    t.put("issue", hasIssue(game) ? toValidation(game.getValidationState()) : null);
    return t;
  }

  private static Map<String, Object> toValidation(ValidationState state) {
    Map<String, Object> v = new LinkedHashMap<>();
    v.put("code", state.getCode());
    v.put("name", VALIDATION_NAMES.getOrDefault(state.getCode(), "UNKNOWN"));
    if (state.getOptions() != null && !state.getOptions().isEmpty()) {
      v.put("details", state.getOptions());
    }
    return v;
  }

  private Map<String, Object> toScore(Score score, boolean withTable) {
    Map<String, Object> s = new LinkedHashMap<>();
    if (withTable) {
      Game game = gameService.getGame(score.getGameId());
      s.put("tableId", score.getGameId());
      s.put("tableName", game != null ? game.getGameDisplayName() : null);
    }
    s.put("position", score.getPosition());
    s.put("initials", score.getPlayerInitials());
    s.put("score", score.getFormattedScore());
    if (StringUtils.isNotEmpty(score.getLabel())) {
      s.put("label", score.getLabel());
    }
    s.put("date", toString(score.getCreatedAt()));
    return s;
  }

  private static String toString(Object date) {
    return date != null ? date.toString() : null;
  }

  static Map<String, Object> prop(String type, String description) {
    Map<String, Object> p = new LinkedHashMap<>();
    p.put("type", type);
    p.put("description", description);
    return p;
  }

  static Map<String, Object> schema(Map<String, Object> properties, List<String> required) {
    Map<String, Object> s = new LinkedHashMap<>();
    s.put("type", "object");
    s.put("properties", new TreeMap<>(properties));
    if (!required.isEmpty()) {
      s.put("required", required);
    }
    return s;
  }

  /**
   * Maps the validation codes to their constant names (CODE_NO_ROM -> NO_ROM), which are self explaining for the AI.
   */
  private static Map<Integer, String> readValidationNames() {
    Map<Integer, String> names = new HashMap<>();
    for (Field field : GameValidationCode.class.getFields()) {
      if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class && field.getName().startsWith("CODE_")) {
        try {
          names.putIfAbsent(field.getInt(null), field.getName().substring("CODE_".length()));
        }
        catch (IllegalAccessException e) {
          //ignore
        }
      }
    }
    return names;
  }
}
