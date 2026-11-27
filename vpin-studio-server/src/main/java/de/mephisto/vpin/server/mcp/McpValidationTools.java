package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.connectors.vps.model.VPSChange;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.connectors.vps.model.VpsUrl;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mcp.McpToolGroup;
import de.mephisto.vpin.restclient.validation.IgnoredValidationSettings;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vps.VpsService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

import static de.mephisto.vpin.server.mcp.McpTableTools.prop;
import static de.mephisto.vpin.server.mcp.McpTableTools.schema;

/**
 * MCP tools for the validation issues of the tables and the update information of the Virtual Pinball Spreadsheet.
 */
@Service
public class McpValidationTools implements McpToolProvider {
  private final static Logger LOG = LoggerFactory.getLogger(McpValidationTools.class);

  private static final int MAX_VERSIONS = 10;

  @Autowired
  private GameService gameService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private VpsService vpsService;

  @Override
  public List<McpTool> getTools() {
    List<McpTool> tools = new ArrayList<>();

    tools.add(McpTool.read("list_validation_issues", "List validation issues",
        "Counts the current validation issues over all tables, e.g. how many tables have NO_ROM, and lists the issues "
            + "that are ignored for all tables. Use search_tables with \"issue\" to get the tables of one issue.",
        schema(Map.of("emulatorId", prop("integer", "Only count tables of this emulator, see list_emulators.")), List.of()),
        this::listValidationIssues));

    Map<String, Object> ignoreProps = new LinkedHashMap<>();
    ignoreProps.put("issue", prop("string", "The validation issue, e.g. NO_ROM or NO_DIRECTB2S_OR_PUPPACK."));
    ignoreProps.put("tableId", prop("integer", "Only for this table, see search_tables. Without it the issue is ignored for all tables."));
    tools.add(McpTool.write(McpToolGroup.TABLES, "ignore_validation", "Ignore validation issue",
        "Ignores a validation issue for one table or for all tables, so it is no longer reported. Ignoring an issue for "
            + "all tables affects the whole cabinet, confirm it with the user.",
        schema(ignoreProps, List.of("issue")), args -> setIgnored(args, true)));

    tools.add(McpTool.write(McpToolGroup.TABLES, "unignore_validation", "Report validation issue again",
        "Reports an ignored validation issue again, for one table or for all tables.",
        schema(ignoreProps, List.of("issue")), args -> setIgnored(args, false)));

    tools.add(McpTool.write(McpToolGroup.TABLES, "revalidate_table", "Revalidate table",
        "Reloads a table from its files and the frontend and validates it again, e.g. after files were added. "
            + "Returns the current validation issues.",
        schema(Map.of("id", prop("integer", "The table id, see search_tables.")), List.of("id")),
        this::revalidate));

    tools.add(McpTool.read("check_vps_update", "Check VPS update",
        "Returns the Virtual Pinball Spreadsheet (VPS) entry a table is linked with: the installed version, the newer "
            + "table versions with their download links and the reported changes (backglass, PUP pack, ROM, ...). "
            + "Without a VPS link, it searches VPS for the table name so it can be linked with update_table.",
        schema(Map.of("id", prop("integer", "The table id, see search_tables.")), List.of("id")),
        this::checkVpsUpdate));
    return tools;
  }

  //-------------------------------- tools ------------------------------------------------------------

  private Object listValidationIssues(McpArguments args) {
    Integer emulatorId = args.getInt("emulatorId");
    Map<String, Integer> counts = new TreeMap<>();
    int tablesWithIssues = 0;
    int total = 0;
    for (Game game : gameService.getGames()) {
      if (emulatorId != null && game.getEmulatorId() != emulatorId) {
        continue;
      }
      total++;
      List<Integer> codes = McpTableTools.getIssueCodes(game);
      if (!codes.isEmpty()) {
        tablesWithIssues++;
      }
      for (Integer code : codes) {
        counts.merge(McpTableTools.VALIDATION_NAMES.getOrDefault(code, String.valueOf(code)), 1, Integer::sum);
      }
    }
    IgnoredValidationSettings ignored = getIgnoredSettings();
    List<String> ignoredGlobally = new ArrayList<>();
    ignored.getIgnoredValidators().forEach((code, value) -> {
      if (Boolean.TRUE.equals(value)) {
        ignoredGlobally.add(nameOf(code));
      }
    });
    Collections.sort(ignoredGlobally);

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tables", total);
    result.put("tablesWithIssues", tablesWithIssues);
    result.put("issues", counts);
    result.put("ignoredForAllTables", ignoredGlobally);
    return result;
  }

  private Object setIgnored(McpArguments args, boolean ignore) {
    Integer code = McpTableTools.parseIssue(args.getString("issue"));
    if (code == null) {
      throw new McpToolException("Missing argument \"issue\".");
    }
    String name = McpTableTools.VALIDATION_NAMES.get(code);
    Integer tableId = args.getInt("tableId");

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("issue", name);
    result.put("ignored", ignore);
    if (tableId != null) {
      Game game = gameService.getGame(tableId);
      if (game == null) {
        throw new McpToolException("No table found for id " + tableId + ", use search_tables to look up the id.");
      }
      List<Integer> ignoredCodes = new ArrayList<>(game.getIgnoredValidations() != null ? game.getIgnoredValidations() : List.of());
      boolean changed = ignore ? !ignoredCodes.contains(code) && ignoredCodes.add(code) : ignoredCodes.remove(code);
      if (changed) {
        game.setIgnoredValidations(ignoredCodes);
        try {
          gameService.save(game);
        }
        catch (Exception e) {
          throw new McpToolException("Saving the table failed: " + e.getMessage());
        }
        LOG.info("MCP: {} {} for \"{}\"", ignore ? "ignored" : "unignored", name, game.getGameDisplayName());
      }
      result.put("tableId", game.getId());
      result.put("changed", changed);
      if (!ignore && getIgnoredSettings().isIgnored(String.valueOf(code))) {
        result.put("message", "The issue is still ignored for all tables, call unignore_validation without tableId to report it again.");
      }
      return result;
    }

    IgnoredValidationSettings settings = getIgnoredSettings();
    boolean wasIgnored = settings.isIgnored(String.valueOf(code));
    if (wasIgnored != ignore) {
      Map<String, Boolean> validators = new HashMap<>(settings.getIgnoredValidators());
      validators.put(String.valueOf(code), ignore);
      settings.setIgnoredValidators(validators);
      try {
        preferencesService.savePreference(settings);
      }
      catch (Exception e) {
        throw new McpToolException("Saving the ignored validations failed: " + e.getMessage());
      }
      LOG.info("MCP: {} {} for all tables", ignore ? "ignored" : "unignored", name);
    }
    result.put("allTables", true);
    result.put("changed", wasIgnored != ignore);
    return result;
  }

  private Object revalidate(McpArguments args) {
    int id = args.requireInt("id");
    if (gameService.getGame(id) == null) {
      throw new McpToolException("No table found for id " + id + ", use search_tables to look up the id.");
    }
    Game game = gameService.reload(id);
    if (game == null) {
      throw new McpToolException("The table could not be reloaded, see the VPin Studio Server log.");
    }
    LOG.info("MCP: revalidated \"{}\"", game.getGameDisplayName());
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("tableName", game.getGameDisplayName());
    result.put("validations", gameService.validate(game).stream().map(McpTableTools::toValidation).toList());
    return result;
  }

  private Object checkVpsUpdate(McpArguments args) {
    int id = args.requireInt("id");
    Game game = gameService.getGame(id);
    if (game == null) {
      throw new McpToolException("No table found for id " + id + ", use search_tables to look up the id.");
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("tableName", game.getGameDisplayName());
    result.put("installedVersion", StringUtils.trimToNull(game.getVersion()));

    VpsTable vpsTable = StringUtils.isNotEmpty(game.getExtTableId()) ? vpsService.getTableById(game.getExtTableId()) : null;
    if (vpsTable == null) {
      result.put("linked", false);
      String term = StringUtils.defaultIfBlank(game.getGameDisplayName(), game.getGameFileName());
      List<Map<String, Object>> candidates = new ArrayList<>();
      for (VpsTable t : vpsService.getTables()) {
        if (candidates.size() >= MAX_VERSIONS) {
          break;
        }
        if (term != null && StringUtils.containsIgnoreCase(t.getName(), baseName(term))) {
          candidates.add(toVpsTable(t));
        }
      }
      result.put("candidates", candidates);
      result.put("message", "The table is not linked with VPS. Pick a candidate and link it with update_table (vpsTableId, vpsVersionId).");
      return result;
    }

    result.put("linked", true);
    result.put("vpsTable", toVpsTable(vpsTable));
    result.put("vpsVersionId", game.getExtTableVersionId());
    result.put("updateAvailable", game.isUpdateAvailable());
    result.put("ignoreUpdates", game.isIgnoreUpdates());
    VPSChanges changes = game.getVpsUpdates();
    if (changes != null && !changes.isEmpty()) {
      List<Map<String, Object>> list = new ArrayList<>();
      for (VPSChange change : changes.getChanges()) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("type", change.getDiffType() != null ? change.getDiffType().name() : null);
        c.put("id", change.getId());
        list.add(c);
      }
      result.put("changes", list);
    }

    String format = game.isVpxGame() ? "VPX" : game.isFpGame() ? "FP" : null;
    List<Map<String, Object>> versions = new ArrayList<>();
    List<VpsTableVersion> tableFiles = vpsTable.getTableFiles() != null ? new ArrayList<>(vpsTable.getTableFiles()) : new ArrayList<>();
    tableFiles.sort(Comparator.comparing((VpsTableVersion v) -> v.getCreatedAt() != null ? v.getCreatedAt() : 0L).reversed());
    for (VpsTableVersion version : tableFiles) {
      if (format != null && version.getTableFormat() != null && !format.equalsIgnoreCase(version.getTableFormat())) {
        continue;
      }
      if (versions.size() >= MAX_VERSIONS) {
        break;
      }
      Map<String, Object> v = new LinkedHashMap<>();
      v.put("id", version.getId());
      v.put("version", version.getVersion());
      v.put("authors", version.getAuthors());
      v.put("format", version.getTableFormat());
      v.put("createdAt", version.getCreatedAt() != null ? Instant.ofEpochMilli(version.getCreatedAt()).toString() : null);
      v.put("installed", version.getId() != null && version.getId().equals(game.getExtTableVersionId()));
      v.put("urls", version.getUrls() != null ? version.getUrls().stream().filter(u -> !u.isBroken()).map(VpsUrl::getUrl).toList() : List.of());
      if (StringUtils.isNotEmpty(version.getComment())) {
        v.put("comment", version.getComment());
      }
      versions.add(v);
    }
    result.put("versions", versions);
    return result;
  }

  //-------------------------------- helpers ----------------------------------------------------------

  private IgnoredValidationSettings getIgnoredSettings() {
    IgnoredValidationSettings settings = preferencesService.getJsonPreference(PreferenceNames.IGNORED_VALIDATION_SETTINGS, IgnoredValidationSettings.class);
    return settings != null ? settings : new IgnoredValidationSettings();
  }

  private static String nameOf(String code) {
    try {
      return McpTableTools.VALIDATION_NAMES.getOrDefault(Integer.parseInt(code), code);
    }
    catch (NumberFormatException e) {
      return code;
    }
  }

  private static String baseName(String term) {
    String cleaned = term.replaceAll("[\\(\\[].*", "").trim();
    return cleaned.isEmpty() ? term : cleaned;
  }

  private static Map<String, Object> toVpsTable(VpsTable t) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", t.getId());
    m.put("name", t.getName());
    m.put("manufacturer", t.getManufacturer());
    m.put("year", t.getYear());
    return m;
  }
}
