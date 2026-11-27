package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.mcp.McpToolGroup;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.directb2s.DirectB2SResource;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameMediaService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GamesResource;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.vps.VpsService;
import de.mephisto.vpin.server.vpx.VPXService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static de.mephisto.vpin.server.mcp.McpTableTools.prop;
import static de.mephisto.vpin.server.mcp.McpTableTools.schema;

/**
 * The MCP tools that manage tables: data, components, backglasses, highscores, launching and deleting.
 * Except for the reading tools they change the cabinet and are only offered when the read-only mode is off and table
 * changes are allowed.
 * <p>
 * Where the REST resources add side effects (game events, cache invalidation), the resources are called directly so
 * that a change made by the AI behaves exactly like the same change made in the Studio.
 */
@Service
public class McpManagementTools implements McpToolProvider {
  private final static Logger LOG = LoggerFactory.getLogger(McpManagementTools.class);

  static final int DEFAULT_TEXT_LENGTH = 20000;
  static final int MAX_TEXT_LENGTH = 100000;

  private static final List<String> TABLE_DETAIL_FIELDS = List.of("rating", "tags", "displayName", "rom", "highscoreFile", "manufacturer", "year", "version", "author");

  @Autowired
  private GameService gameService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private AltColorService altColorService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private VPXService vpxService;

  @Autowired
  private VpsService vpsService;

  @Lazy
  @Autowired
  private GamesResource gamesResource;

  @Lazy
  @Autowired
  private DirectB2SResource directB2SResource;

  @Override
  public List<McpTool> getTools() {
    List<McpTool> tools = new ArrayList<>();
    Map<String, Object> idOnly = schema(Map.of("id", prop("integer", "The table id, see search_tables.")), List.of("id"));

    tools.add(McpTool.read("get_table_backglasses", "Get table backglasses",
        "Returns the backglass files of a table: the active backglass and all other versions found next to it.",
        idOnly, this::getBackglasses));

    tools.add(McpTool.read("get_table_script", "Get table script",
        "Returns the text of a table file: the VBScript of a VPX table, its INI or its POV file. Long texts are cut, "
            + "use offset to read on.",
        schema(Map.of(
            "id", prop("integer", "The table id, see search_tables."),
            "file", enumProp("Which file, default SCRIPT.", List.of("SCRIPT", "INI", "POV")),
            "offset", prop("integer", "Start at this character, default 0."),
            "maxLength", prop("integer", "Maximum number of characters, default " + DEFAULT_TEXT_LENGTH + ", maximum " + MAX_TEXT_LENGTH + ".")), List.of("id")),
        this::getTableScript));

    tools.add(McpTool.write(McpToolGroup.TABLES, "set_table_enabled", "Enable or disable table",
        "Enables or disables a table in the frontend. Disabled tables are hidden from the players but stay installed.",
        schema(Map.of(
            "id", prop("integer", "The table id, see search_tables."),
            "enabled", prop("boolean", "true to enable the table, false to disable it.")), List.of("id", "enabled")),
        this::setEnabled));

    Map<String, Object> updateProps = new LinkedHashMap<>();
    updateProps.put("id", prop("integer", "The table id, see search_tables."));
    updateProps.put("rating", prop("integer", "Rating from 1 to 5, 0 removes the rating."));
    updateProps.put("tags", prop("string", "Comma separated list of tags, replaces the current tags. An empty string removes all tags."));
    updateProps.put("notes", prop("string", "The VPin Studio notes of the table, replaces the current notes."));
    updateProps.put("displayName", prop("string", "The name shown in the frontend."));
    updateProps.put("rom", prop("string", "The ROM name, e.g. \"mm_109c\". An empty string removes it."));
    updateProps.put("highscoreFile", prop("string", "The highscore file name for tables without NVRAM highscores, e.g. \"Medieval.txt\". An empty string removes it."));
    updateProps.put("manufacturer", prop("string", "The manufacturer."));
    updateProps.put("year", prop("integer", "The year of the real machine, 0 removes it."));
    updateProps.put("version", prop("string", "The version of the table file."));
    updateProps.put("author", prop("string", "The author(s) of the table file."));
    updateProps.put("ignoreUpdates", prop("boolean", "true to hide the VPS update notifications of this table."));
    updateProps.put("vpsTableId", prop("string", "Links the table with this Virtual Pinball Spreadsheet table id, see check_vps_update. An empty string removes the link."));
    updateProps.put("vpsVersionId", prop("string", "Links the table with this VPS table version id (only together with vpsTableId)."));
    tools.add(McpTool.write(McpToolGroup.TABLES, "update_table", "Update table",
        "Updates the data of a table: rating, tags, notes, frontend data (display name, ROM, highscore file, "
            + "manufacturer, year, version, author), the VPS link and the update notifications. Only the given fields are changed.",
        schema(updateProps, List.of("id")), this::updateTable));

    tools.add(McpTool.write(McpToolGroup.TABLES, "set_component_enabled", "Enable or disable a table component",
        "Enables or disables the PUP pack, ALT sound or ALT color of a table, or disables its backglass. The files are "
            + "kept. To enable a disabled backglass use set_active_backglass.",
        schema(Map.of(
            "id", prop("integer", "The table id, see search_tables."),
            "component", enumProp("The component.", List.of("PUP_PACK", "ALT_SOUND", "ALT_COLOR", "BACKGLASS")),
            "enabled", prop("boolean", "true to enable, false to disable.")), List.of("id", "component", "enabled")),
        this::setComponentEnabled));

    tools.add(McpTool.write(McpToolGroup.TABLES, "set_active_backglass", "Set active backglass",
        "Makes one of the backglass versions of a table the active backglass. The current one becomes a version.",
        schema(Map.of(
            "id", prop("integer", "The table id, see search_tables."),
            "version", prop("string", "File name of the version, as returned by get_table_backglasses.")), List.of("id", "version")),
        this::setActiveBackglass));

    tools.add(McpTool.write(McpToolGroup.TABLES, "play_table", "Play table",
        "Launches a table on the cabinet. The frontend is closed while the table is running.",
        idOnly, this::playTable));

    tools.add(McpTool.delete(McpToolGroup.TABLES, "reset_highscores", "Reset highscores",
        "Resets the highscores of a table. A backup of the highscores is created first and can be restored in VPin "
            + "Studio, but the current scores are gone from the table. Always confirm with the user first.",
        schema(Map.of(
            "id", prop("integer", "The table id, see search_tables."),
            "score", prop("integer", "The value the scores are reset to, default 0.")), List.of("id")),
        this::resetHighscores));

    Map<String, Object> deleteProps = new LinkedHashMap<>();
    deleteProps.put("id", prop("integer", "The table id, see search_tables."));
    deleteProps.put("deleteBackglass", prop("boolean", "Also delete the backglass files, default true."));
    deleteProps.put("deleteHighscores", prop("boolean", "Also delete the highscores, default true."));
    deleteProps.put("keepMedia", prop("boolean", "Keep the frontend media (wheel, playfield videos, ...), default false."));
    deleteProps.put("deletePupPack", prop("boolean", "Also delete the PUP pack, default false. PUP packs may be shared with other tables."));
    deleteProps.put("deleteMusic", prop("boolean", "Also delete the music files, default false."));
    deleteProps.put("deleteAltSound", prop("boolean", "Also delete the ALT sound, default false."));
    deleteProps.put("deleteAltColor", prop("boolean", "Also delete the ALT color files, default false."));
    deleteProps.put("deleteRom", prop("boolean", "Also delete the ROM, default false. ROMs may be shared with other tables."));
    tools.add(McpTool.delete(McpToolGroup.TABLES, "delete_table", "Delete table",
        "Deletes a table from the cabinet: the table file, its frontend entry and, depending on the options, its "
            + "backglass, highscores, media and other assets. This cannot be undone, so always confirm with the user first.",
        schema(deleteProps, List.of("id")), this::deleteTable));

    tools.add(McpTool.delete(McpToolGroup.TABLES, "delete_backglass", "Delete backglass",
        "Deletes the active backglass of a table, or one of its other versions. This cannot be undone, so always "
            + "confirm with the user first. Use get_table_backglasses to look up the versions.",
        schema(Map.of(
            "id", prop("integer", "The table id, see search_tables."),
            "version", prop("string", "File name of the version to delete, as returned by get_table_backglasses. Without it the active backglass is deleted.")), List.of("id")),
        this::deleteBackglass));

    return tools;
  }

  //-------------------------------- read -------------------------------------------------------------

  private Object getBackglasses(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    DirectB2S b2s = backglassService.getDirectB2SAndVersions(game);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("tableName", game.getGameDisplayName());
    result.put("active", b2s != null && b2s.isEnabled() ? b2s.getFileName() : null);
    result.put("versions", b2s != null ? b2s.getVersions() : List.of());
    return result;
  }

  private Object getTableScript(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    String file = StringUtils.defaultIfBlank(args.getString("file"), "SCRIPT").trim().toUpperCase(Locale.ROOT);
    int offset = Math.max(0, args.getInt("offset", 0));
    int maxLength = Math.max(1, Math.min(MAX_TEXT_LENGTH, args.getInt("maxLength", DEFAULT_TEXT_LENGTH)));

    String text;
    try {
      text = switch (file) {
        case "SCRIPT" -> {
          if (!game.isVpxGame()) {
            throw new McpToolException("The script can only be read from Visual Pinball X tables.");
          }
          yield vpxService.getScript(game);
        }
        case "INI" -> readText(game.getIniFile());
        case "POV" -> readText(game.getPOVFile());
        default -> throw new McpToolException("Unknown file \"" + file + "\", use SCRIPT, INI or POV.");
      };
    }
    catch (McpToolException e) {
      throw e;
    }
    catch (Exception e) {
      throw new McpToolException("The file could not be read: " + e.getMessage());
    }
    if (text == null) {
      throw new McpToolException("\"" + game.getGameDisplayName() + "\" has no " + file + " file.");
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("file", file);
    result.put("totalLength", text.length());
    result.put("offset", offset);
    String part = offset < text.length() ? text.substring(offset, Math.min(text.length(), offset + maxLength)) : "";
    result.put("text", part);
    if (offset + part.length() < text.length()) {
      result.put("nextOffset", offset + part.length());
    }
    return result;
  }

  //-------------------------------- write ------------------------------------------------------------

  private Object setEnabled(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    boolean enabled = args.requireBoolean("enabled");
    TableDetails tableDetails = requireTableDetails(game);
    tableDetails.setStatus(enabled ? 1 : 0);
    gameMediaService.saveTableDetails(tableDetails, game.getId(), true);
    LOG.info("MCP: {} \"{}\"", enabled ? "enabled" : "disabled", game.getGameDisplayName());
    return Map.of("tableId", game.getId(), "enabled", enabled);
  }

  private Object updateTable(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    Integer rating = args.getInt("rating");
    Integer year = args.getInt("year");
    String notes = args.getString("notes");
    Boolean ignoreUpdates = args.getBooleanOrNull("ignoreUpdates");
    String vpsTableId = args.getString("vpsTableId");
    String vpsVersionId = args.getString("vpsVersionId");
    boolean tableDetailChanges = TABLE_DETAIL_FIELDS.stream().anyMatch(args::has);
    if (!tableDetailChanges && notes == null && ignoreUpdates == null && vpsTableId == null && vpsVersionId == null) {
      throw new McpToolException("Nothing to update: pass at least one field.");
    }
    if (rating != null && (rating < 0 || rating > 5)) {
      throw new McpToolException("The rating must be between 0 and 5.");
    }
    if (year != null && year != 0 && (year < 1930 || year > 2100)) {
      throw new McpToolException("The year must be between 1930 and 2100, 0 removes it.");
    }
    if (args.has("displayName") && StringUtils.isBlank(args.getString("displayName"))) {
      throw new McpToolException("The display name must not be empty.");
    }
    if (vpsVersionId != null && vpsTableId == null) {
      throw new McpToolException("Pass the vpsTableId together with the vpsVersionId.");
    }
    String linkedTableId = StringUtils.trimToNull(vpsTableId);
    if (linkedTableId != null && vpsService.getTableById(linkedTableId) == null) {
      throw new McpToolException("No VPS table found for id \"" + linkedTableId + "\".");
    }

    List<String> updated = new ArrayList<>();
    if (tableDetailChanges) {
      TableDetails tableDetails = requireTableDetails(game);
      if (rating != null) {
        tableDetails.setGameRating(rating == 0 ? null : rating);
        updated.add("rating");
      }
      if (args.has("tags")) {
        tableDetails.setTags(normalizeTags(args.getString("tags")));
        updated.add("tags");
      }
      if (args.has("displayName")) {
        tableDetails.setGameDisplayName(args.getString("displayName").trim());
        updated.add("displayName");
      }
      if (args.has("rom")) {
        tableDetails.setRomName(StringUtils.trimToNull(args.getString("rom")));
        updated.add("rom");
      }
      if (args.has("highscoreFile")) {
        tableDetails.setHsFilename(StringUtils.trimToNull(args.getString("highscoreFile")));
        updated.add("highscoreFile");
      }
      if (args.has("manufacturer")) {
        tableDetails.setManufacturer(StringUtils.trimToNull(args.getString("manufacturer")));
        updated.add("manufacturer");
      }
      if (year != null) {
        tableDetails.setGameYear(year == 0 ? null : year);
        updated.add("year");
      }
      if (args.has("version")) {
        tableDetails.setGameVersion(StringUtils.trimToNull(args.getString("version")));
        updated.add("version");
      }
      if (args.has("author")) {
        tableDetails.setAuthor(StringUtils.trimToNull(args.getString("author")));
        updated.add("author");
      }
      gameMediaService.saveTableDetails(tableDetails, game.getId(), true);
    }
    if (vpsTableId != null) {
      gameService.vpsLink(game.getId(), linkedTableId, linkedTableId != null ? StringUtils.trimToNull(vpsVersionId) : null);
      updated.add("vpsLink");
    }
    if (notes != null || ignoreUpdates != null) {
      //re-read the game, the changes above may have replaced the cached instance
      Game current = requireGame(game.getId());
      if (notes != null) {
        current.setComment(StringUtils.trimToNull(notes));
        updated.add("notes");
      }
      if (ignoreUpdates != null) {
        current.setIgnoreUpdates(ignoreUpdates);
        updated.add("ignoreUpdates");
      }
      try {
        gameService.save(current);
      }
      catch (Exception e) {
        throw new McpToolException("Saving the table failed: " + e.getMessage());
      }
    }
    LOG.info("MCP: updated \"{}\": {}", game.getGameDisplayName(), updated);

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("updated", updated);
    return result;
  }

  private Object setComponentEnabled(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    String component = StringUtils.defaultString(args.getString("component")).trim().toUpperCase(Locale.ROOT);
    boolean enabled = args.requireBoolean("enabled");
    switch (component) {
      case "PUP_PACK" -> {
        if (StringUtils.isEmpty(game.getPupPackName())) {
          throw new McpToolException("\"" + game.getGameDisplayName() + "\" has no PUP pack.");
        }
        pupPacksService.setPupPackEnabled(game, enabled);
      }
      case "ALT_SOUND" -> {
        if (!game.isAltSoundAvailable() || StringUtils.isEmpty(game.getRom())) {
          throw new McpToolException("\"" + game.getGameDisplayName() + "\" has no ALT sound.");
        }
        altSoundService.setAltSoundEnabled(game.getRom(), enabled);
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.ALT_SOUND, game.getRom());
      }
      case "ALT_COLOR" -> {
        if (game.getAltColorType() == null) {
          throw new McpToolException("\"" + game.getGameDisplayName() + "\" has no ALT color files.");
        }
        if (!game.isVpxGame()) {
          throw new McpToolException("ALT color can only be switched for Visual Pinball X tables.");
        }
        altColorService.setAltColorEnabled(game, enabled);
      }
      case "BACKGLASS" -> {
        if (enabled) {
          throw new McpToolException("Use set_active_backglass to enable one of the backglass versions.");
        }
        DirectB2S b2s = backglassService.getDirectB2SAndVersions(game);
        if (b2s == null || !b2s.isEnabled()) {
          throw new McpToolException("\"" + game.getGameDisplayName() + "\" has no active backglass.");
        }
        Map<String, Object> values = new HashMap<>();
        values.put("emulatorId", b2s.getEmulatorId());
        values.put("fileName", b2s.getFileName());
        values.put("disable", true);
        try {
          directB2SResource.updateBackglass(values);
        }
        catch (Exception e) {
          throw new McpToolException("Disabling the backglass failed: " + e.getMessage());
        }
      }
      default -> throw new McpToolException("Unknown component \"" + component + "\", use PUP_PACK, ALT_SOUND, ALT_COLOR or BACKGLASS.");
    }
    LOG.info("MCP: {} {} of \"{}\"", enabled ? "enabled" : "disabled", component, game.getGameDisplayName());
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("component", component);
    result.put("enabled", enabled);
    return result;
  }

  private Object setActiveBackglass(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    String version = StringUtils.trimToNull(args.getString("version"));
    DirectB2S b2s = backglassService.getDirectB2SAndVersions(game);
    if (b2s == null || b2s.getVersions() == null || version == null || !b2s.getVersions().contains(version)) {
      throw new McpToolException("\"" + version + "\" is not a backglass version of this table, see get_table_backglasses.");
    }
    Map<String, Object> values = new HashMap<>();
    values.put("emulatorId", b2s.getEmulatorId());
    values.put("fileName", version);
    values.put("setVersionAsDefault", true);
    DirectB2S updated;
    try {
      updated = directB2SResource.updateBackglass(values);
    }
    catch (Exception e) {
      throw new McpToolException("Activating the backglass failed: " + e.getMessage());
    }
    LOG.info("MCP: activated backglass \"{}\" of \"{}\"", version, game.getGameDisplayName());
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("active", updated != null ? updated.getFileName() : version);
    result.put("versions", updated != null ? updated.getVersions() : List.of());
    return result;
  }

  private Object playTable(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    try {
      boolean launched = gamesResource.play(game.getId(), new HashMap<>());
      return Map.of("tableId", game.getId(), "launched", launched);
    }
    catch (ResponseStatusException e) {
      throw new McpToolException(StringUtils.defaultIfEmpty(e.getReason(), e.getMessage()));
    }
  }

  private Object resetHighscores(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    int score = args.getInt("score", 0);
    if (score < 0) {
      throw new McpToolException("The score must not be negative.");
    }
    LOG.info("MCP: resetting highscores of \"{}\"", game.getGameDisplayName());
    boolean reset = gameService.resetGame(game.getId(), score);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("reset", reset);
    if (!reset) {
      result.put("message", "The highscores could not be reset, e.g. because the highscore backup failed. See the VPin Studio Server log.");
    }
    return result;
  }

  private Object deleteTable(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    DeleteDescriptor descriptor = new DeleteDescriptor();
    descriptor.setGameIds(List.of(game.getId()));
    descriptor.setDeleteDirectB2s(args.getBoolean("deleteBackglass", true));
    descriptor.setDeleteHighscores(args.getBoolean("deleteHighscores", true));
    descriptor.setKeepAssets(args.getBoolean("keepMedia", false));
    descriptor.setDeletePupPack(args.getBoolean("deletePupPack", false));
    descriptor.setDeleteMusic(args.getBoolean("deleteMusic", false));
    descriptor.setDeleteAltSound(args.getBoolean("deleteAltSound", false));
    descriptor.setDeleteAltColor(args.getBoolean("deleteAltColor", false));
    descriptor.setDeleteRom(args.getBoolean("deleteRom", false));

    String name = game.getGameDisplayName();
    LOG.info("MCP: deleting \"{}\" (id {})", name, game.getId());
    boolean success = gamesResource.delete(descriptor);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("tableName", name);
    result.put("deleted", success);
    if (!success) {
      result.put("message", "Not all files could be deleted, see the VPin Studio Server log for details.");
    }
    return result;
  }

  private Object deleteBackglass(McpArguments args) {
    Game game = requireGame(args.requireInt("id"));
    String version = StringUtils.trimToNull(args.getString("version"));
    DirectB2S b2s = backglassService.getDirectB2SAndVersions(game);
    if (b2s == null) {
      throw new McpToolException("\"" + game.getGameDisplayName() + "\" has no backglass.");
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    if (version == null) {
      if (!b2s.isEnabled()) {
        throw new McpToolException("\"" + game.getGameDisplayName() + "\" has no active backglass.");
      }
      LOG.info("MCP: deleting backglass \"{}\"", b2s.getFileName());
      result.put("deleted", directB2SResource.deleteBackglass(b2s.getEmulatorId(), b2s.getFileName()));
      result.put("fileName", b2s.getFileName());
    }
    else {
      if (b2s.getVersions() == null || !b2s.getVersions().contains(version)) {
        throw new McpToolException("\"" + version + "\" is not a backglass version of this table, see get_table_backglasses.");
      }
      LOG.info("MCP: deleting backglass version \"{}\"", version);
      Map<String, Object> values = new HashMap<>();
      values.put("emulatorId", b2s.getEmulatorId());
      values.put("fileName", version);
      values.put("deleteVersion", true);
      try {
        directB2SResource.updateBackglass(values);
      }
      catch (Exception e) {
        throw new McpToolException("Deleting the backglass version failed: " + e.getMessage());
      }
      result.put("deleted", true);
      result.put("fileName", version);
    }
    return result;
  }

  //-------------------------------- helpers ----------------------------------------------------------

  private Game requireGame(int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      throw new McpToolException("No table found for id " + id + ", use search_tables to look up the id.");
    }
    return game;
  }

  private TableDetails requireTableDetails(Game game) {
    TableDetails tableDetails = frontendService.getTableDetails(game.getId());
    if (tableDetails == null) {
      throw new McpToolException("The frontend has no entry for \"" + game.getGameDisplayName() + "\".");
    }
    return tableDetails;
  }

  private static String readText(File file) throws Exception {
    if (file == null || !file.exists()) {
      return null;
    }
    return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
  }

  static Map<String, Object> enumProp(String description, List<String> values) {
    Map<String, Object> p = prop("string", description);
    p.put("enum", values);
    return p;
  }

  static String normalizeTags(String tags) {
    List<String> values = TaggingUtil.getTags(tags);
    return values.isEmpty() ? null : TaggingUtil.join(values);
  }
}
