package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The read-only table tools of the MCP endpoint. They delegate to the existing services and return compact maps,
 * so the AI only receives the fields that are meaningful for it (no internal file handles or caches).
 */
@Service
public class McpTableTools implements McpToolProvider {
  private final static Logger LOG = LoggerFactory.getLogger(McpTableTools.class);

  private static final int DEFAULT_LIMIT = 50;
  private static final int MAX_LIMIT = 500;

  private static final int MAX_FILES = 100;

  static final Map<Integer, String> VALIDATION_NAMES = readValidationNames();

  @Autowired
  private GameService gameService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private FolderLookupService folderLookupService;

  @Autowired
  private MusicService musicService;

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private AltColorService altColorService;

  @Override
  public List<McpTool> getTools() {
    List<McpTool> tools = new ArrayList<>();

    tools.add(McpTool.read("list_emulators", "List emulators",
        "Lists the emulators configured on the cabinet (Visual Pinball X, Future Pinball, MAME, ...) with their id and "
            + "the number of installed tables. Use the id to restrict search_tables to one emulator.",
        schema(Map.of(), List.of()), args -> listEmulators()));

    tools.add(McpTool.read("search_tables", "Search tables",
        "Searches the installed tables of the cabinet. Without any filter all tables are returned (up to the limit). "
            + "The query matches the table name, display name, ROM name and file name (case-insensitive); the other "
            + "filters narrow the result down, e.g. by PUP pack, tag, missing frontend media or validation issue. "
            + "Each result contains the table id needed by the other tools.",
        schema(searchProperties(), List.of()), this::searchTables));

    tools.add(McpTool.read("get_table", "Get table details",
        "Returns all details of a table: files, ROM, highscore configuration, media packs, VPS link, play count, "
            + "rating, tags, notes and the list of current validation issues.",
        schema(Map.of("id", prop("integer", "The table id, see search_tables.")), List.of("id")),
        this::getTable));

    tools.add(McpTool.read("get_table_assets", "Get table assets",
        "Returns all assets used by a table and whether they exist: table file, POV/INI/RES/VBS files, ROM, NVRAM, "
            + "backglass, PUP pack, ALT sound, ALT color, DMD, music files (found and missing) and the frontend media "
            + "of every screen (wheel, playfield, backglass, ...).",
        schema(Map.of("id", prop("integer", "The table id, see search_tables.")), List.of("id")),
        this::getTableAssets));

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
    int limit = Math.max(1, Math.min(MAX_LIMIT, args.getInt("limit", DEFAULT_LIMIT)));
    List<Game> matches = findTables(args);

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("total", matches.size());
    result.put("returned", Math.min(limit, matches.size()));
    result.put("tables", matches.stream().limit(limit).map(this::toSummary).toList());
    return result;
  }

  /**
   * @return all tables matching the search_tables filters of the arguments, sorted by name
   */
  List<Game> findTables(McpArguments args) {
    String query = StringUtils.trimToNull(args.getString("query"));
    Integer emulatorId = args.getInt("emulatorId");
    boolean onlyWithIssues = args.getBoolean("onlyWithIssues", false);
    boolean onlyDisabled = args.getBoolean("onlyDisabled", false);
    boolean onlyUpdateAvailable = args.getBoolean("onlyUpdateAvailable", false);

    String tag = StringUtils.trimToNull(args.getString("tag"));
    String pupPack = StringUtils.trimToNull(args.getString("pupPack"));
    Integer issueCode = parseIssue(args.getString("issue"));
    VPinScreen missingMedia = parseScreen(args.getString("missingMedia"));

    List<Predicate<Game>> filters = new ArrayList<>();
    filters.add(g -> emulatorId == null || g.getEmulatorId() == emulatorId);
    filters.add(g -> query == null || matches(g, query));
    filters.add(g -> !onlyWithIssues || hasIssue(g));
    filters.add(g -> !onlyDisabled || g.isDisabled());
    filters.add(g -> !onlyUpdateAvailable || g.isUpdateAvailable());
    filters.add(g -> tag == null || (g.getTags() != null && g.getTags().stream().anyMatch(tag::equalsIgnoreCase)));
    filters.add(g -> pupPack == null || StringUtils.containsIgnoreCase(g.getPupPackName(), pupPack));
    filters.add(g -> issueCode == null || getIssueCodes(g).contains(issueCode));
    addFlagFilter(filters, args.getBooleanOrNull("hasBackglass"), g -> g.getDirectB2SPath() != null);
    addFlagFilter(filters, args.getBooleanOrNull("hasPupPack"), g -> StringUtils.isNotEmpty(g.getPupPackName()));
    addFlagFilter(filters, args.getBooleanOrNull("hasAltSound"), Game::isAltSoundAvailable);
    addFlagFilter(filters, args.getBooleanOrNull("hasAltColor"), g -> g.getAltColorType() != null);
    //the media check reads the file system, so it runs last on the remaining tables
    filters.add(g -> missingMedia == null || frontendService.getMediaFiles(g, missingMedia).isEmpty());

    Predicate<Game> filter = filters.stream().reduce(g -> true, Predicate::and);
    return gameService.getGames().stream()
        .filter(filter)
        .sorted(Comparator.comparing(g -> StringUtils.defaultString(g.getGameDisplayName()), String.CASE_INSENSITIVE_ORDER))
        .toList();
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

  private Object getTableAssets(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    Map<String, Object> a = new LinkedHashMap<>();
    a.put("tableId", game.getId());
    a.put("tableName", game.getGameDisplayName());
    a.put("tableFile", file(game.getGameFile()));

    Map<String, Object> files = new LinkedHashMap<>();
    files.put("pov", safe(() -> file(game.getPOVFile())));
    files.put("ini", safe(() -> file(game.getIniFile())));
    files.put("vrIni", safe(() -> file(game.getVRIniFile())));
    files.put("res", safe(() -> file(game.getResFile())));
    files.put("vbs", safe(() -> file(game.getVBSFile())));
    a.put("tableFiles", files);

    Map<String, Object> rom = new LinkedHashMap<>();
    rom.put("name", StringUtils.trimToNull(game.getRom()));
    rom.put("alias", StringUtils.trimToNull(game.getRomAlias()));
    if (StringUtils.isNotEmpty(game.getRom())) {
      rom.put("romFile", safe(() -> file(folderLookupService.getRomFile(game))));
      rom.put("nvram", safe(() -> file(folderLookupService.getNvRamFile(game))));
      rom.put("cfg", safe(() -> file(folderLookupService.getCfgFile(game))));
    }
    a.put("rom", rom);

    Map<String, Object> backglass = new LinkedHashMap<>();
    backglass.put("file", safe(() -> file(game.getDirectB2SFile())));
    backglass.put("versions", game.getDirectB2SVersions());
    a.put("backglass", backglass);

    a.put("pupPack", safe(() -> {
      PupPack pack = pupPacksService.getPupPackCached(game);
      if (pack == null) {
        return null;
      }
      Map<String, Object> p = new LinkedHashMap<>();
      p.put("name", pack.getName());
      p.put("folder", pack.getPupPackFolder() != null ? pack.getPupPackFolder().getAbsolutePath() : null);
      p.put("disabled", game.isPupPackDisabled());
      p.put("selectedOption", pack.getSelectedOption());
      if (pack.getMissingResources() != null && !pack.getMissingResources().isEmpty()) {
        p.put("missingResources", pack.getMissingResources());
      }
      return p;
    }));

    a.put("altSound", safe(() -> {
      if (!altSoundService.isAltSoundAvailable(game)) {
        return null;
      }
      File folder = altSoundService.getAltSoundFolder(game);
      return file(folder);
    }));

    a.put("altColor", safe(() -> {
      if (game.getAltColorType() == null) {
        return null;
      }
      Map<String, Object> c = new LinkedHashMap<>();
      c.put("type", game.getAltColorType().name());
      c.put("folders", altColorService.getAltColorFolders(game).stream().filter(File::exists).map(File::getAbsolutePath).toList());
      return c;
    }));

    if (game.getDMDType() != null || game.getDMDProjectFolder() != null) {
      Map<String, Object> dmd = new LinkedHashMap<>();
      dmd.put("type", game.getDMDType() != null ? game.getDMDType().name() : null);
      dmd.put("folder", game.getDMDProjectFolder());
      a.put("dmd", dmd);
    }

    a.put("music", safe(() -> {
      if (StringUtils.isEmpty(game.getAssets())) {
        return null;
      }
      Map<String, Object> m = new LinkedHashMap<>();
      File folder = folderLookupService.getGameMusicFolder(game);
      m.put("folder", folder != null ? folder.getAbsolutePath() : null);
      m.put("scriptReferences", Arrays.stream(game.getAssets().split("\\|")).filter(StringUtils::isNotBlank).toList());
      List<File> found = musicService.getMp3Files(game);
      m.put("foundCount", found.size());
      m.put("found", found.stream().limit(MAX_FILES).map(File::getAbsolutePath).toList());
      m.put("missing", musicService.getMissingMp3Files(game));
      return m;
    }));

    a.put("frontendMedia", safe(() -> {
      Map<String, Object> media = new LinkedHashMap<>();
      for (VPinScreen screen : frontendService.getFrontend().getSupportedScreens()) {
        media.put(screen.name(), frontendService.getMediaFiles(game, screen).stream().map(File::getName).toList());
      }
      return media;
    }));
    return a;
  }

  //-------------------------------- helpers ----------------------------------------------------------

  static Map<String, Object> searchProperties() {
    Map<String, Object> p = new LinkedHashMap<>();
    p.put("query", prop("string", "Text to search for, e.g. \"Medieval\" or a ROM name like \"mm_109c\"."));
    p.put("emulatorId", prop("integer", "Only return tables of this emulator, see list_emulators."));
    p.put("onlyWithIssues", prop("boolean", "Only return tables that have a validation issue."));
    p.put("issue", prop("string", "Only return tables with this validation issue, e.g. NO_ROM, NO_DIRECTB2S_OR_PUPPACK or NO_WHEEL_IMAGE."));
    p.put("onlyDisabled", prop("boolean", "Only return tables that are disabled in the frontend."));
    p.put("onlyUpdateAvailable", prop("boolean", "Only return tables for which a newer version is listed on the Virtual Pinball Spreadsheet (VPS)."));
    p.put("tag", prop("string", "Only return tables with this tag."));
    p.put("pupPack", prop("string", "Only return tables using a PUP pack whose name contains this text."));
    p.put("hasBackglass", prop("boolean", "true: only tables with a backglass file, false: only tables without one."));
    p.put("hasPupPack", prop("boolean", "true: only tables with a PUP pack, false: only tables without one."));
    p.put("hasAltSound", prop("boolean", "true: only tables with an ALT sound, false: only tables without one."));
    p.put("hasAltColor", prop("boolean", "true: only tables with ALT color files, false: only tables without them."));
    Map<String, Object> screen = prop("string", "Only return tables without frontend media for this screen.");
    screen.put("enum", Arrays.stream(VPinScreen.values()).map(Enum::name).toList());
    p.put("missingMedia", screen);
    p.put("limit", prop("integer", "Maximum number of results, default " + DEFAULT_LIMIT + ", maximum " + MAX_LIMIT + "."));
    return p;
  }

  private static void addFlagFilter(List<Predicate<Game>> filters, Boolean expected, Predicate<Game> flag) {
    if (expected != null) {
      filters.add(g -> flag.test(g) == expected);
    }
  }

  static List<Integer> getIssueCodes(Game game) {
    if (game.getIssueTypes() != null && !game.getIssueTypes().isEmpty()) {
      return game.getIssueTypes();
    }
    return hasIssue(game) ? List.of(game.getValidationState().getCode()) : List.of();
  }

  static Integer parseIssue(String issue) {
    String name = StringUtils.trimToNull(issue);
    if (name == null) {
      return null;
    }
    String normalized = name.toUpperCase(Locale.ROOT).replaceFirst("^CODE_", "");
    for (Map.Entry<Integer, String> entry : VALIDATION_NAMES.entrySet()) {
      if (entry.getValue().equals(normalized)) {
        return entry.getKey();
      }
    }
    throw new McpToolException("Unknown validation issue \"" + issue + "\". Known issues: " + new TreeSet<>(VALIDATION_NAMES.values()));
  }

  private VPinScreen parseScreen(String value) {
    String name = StringUtils.trimToNull(value);
    if (name == null) {
      return null;
    }
    for (VPinScreen screen : frontendService.getFrontend().getSupportedScreens()) {
      if (screen.name().equalsIgnoreCase(name) || screen.getSegment().equalsIgnoreCase(name)) {
        return screen;
      }
    }
    throw new McpToolException("Unknown or unsupported screen \"" + value + "\". The frontend supports: "
        + frontendService.getFrontend().getSupportedScreens());
  }

  private static Map<String, Object> file(File file) {
    if (file == null) {
      return null;
    }
    Map<String, Object> f = new LinkedHashMap<>();
    f.put("path", file.getAbsolutePath());
    f.put("exists", file.exists());
    return f;
  }

  /**
   * One asset that can not be resolved must not fail the whole tool.
   */
  private static Object safe(Supplier<Object> supplier) {
    try {
      return supplier.get();
    }
    catch (Exception e) {
      LOG.warn("MCP asset lookup failed: {}", e.getMessage());
      return Map.of("error", String.valueOf(e.getMessage()));
    }
  }


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
    List<String> issues = getIssueCodes(game).stream().map(c -> VALIDATION_NAMES.getOrDefault(c, String.valueOf(c))).toList();
    t.put("issues", issues.isEmpty() ? null : issues);
    return t;
  }

  static Map<String, Object> toValidation(ValidationState state) {
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
