package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.directb2s.DirectB2SResource;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameMediaService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GamesResource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static de.mephisto.vpin.server.mcp.McpTableTools.prop;
import static de.mephisto.vpin.server.mcp.McpTableTools.schema;

/**
 * The MCP tools that manage tables: backglasses, enabling, ratings and notes, launching and deleting.
 * Except for get_table_backglasses they change the cabinet and are only offered when the read-only mode is off.
 * <p>
 * Where the REST resources add side effects (game events, cache invalidation), the resources are called directly so
 * that a change made by the AI behaves exactly like the same change made in the Studio.
 */
@Service
public class McpManagementTools implements McpToolProvider {
  private final static Logger LOG = LoggerFactory.getLogger(McpManagementTools.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private BackglassService backglassService;

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

    tools.add(McpTool.write("set_table_enabled", "Enable or disable table",
        "Enables or disables a table in the frontend. Disabled tables are hidden from the players but stay installed.",
        schema(Map.of(
            "id", prop("integer", "The table id, see search_tables."),
            "enabled", prop("boolean", "true to enable the table, false to disable it.")), List.of("id", "enabled")),
        this::setEnabled));

    tools.add(McpTool.write("update_table", "Update table",
        "Updates the rating, tags or notes of a table. Only the given fields are changed.",
        schema(Map.of(
            "id", prop("integer", "The table id, see search_tables."),
            "rating", prop("integer", "Rating from 1 to 5, 0 removes the rating."),
            "tags", prop("string", "Comma separated list of tags, replaces the current tags. An empty string removes all tags."),
            "notes", prop("string", "The VPin Studio notes of the table, replaces the current notes.")), List.of("id")),
        this::updateTable));

    tools.add(McpTool.write("play_table", "Play table",
        "Launches a table on the cabinet. The frontend is closed while the table is running.",
        idOnly, this::playTable));

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
    tools.add(McpTool.delete("delete_table", "Delete table",
        "Deletes a table from the cabinet: the table file, its frontend entry and, depending on the options, its "
            + "backglass, highscores, media and other assets. This cannot be undone, so always confirm with the user first.",
        schema(deleteProps, List.of("id")), this::deleteTable));

    tools.add(McpTool.delete("delete_backglass", "Delete backglass",
        "Deletes the active backglass of a table, or one of its other versions. This cannot be undone, so always "
            + "confirm with the user first. Use get_table_backglasses to look up the versions.",
        schema(Map.of(
            "id", prop("integer", "The table id, see search_tables."),
            "version", prop("string", "File name of the version to delete, as returned by get_table_backglasses. Without it the active backglass is deleted.")), List.of("id")),
        this::deleteBackglass));

    return tools;
  }

  //-------------------------------- tools ------------------------------------------------------------

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
    String tags = args.getString("tags");
    String notes = args.getString("notes");
    if (rating == null && tags == null && notes == null) {
      throw new McpToolException("Nothing to update: pass rating, tags or notes.");
    }
    if (rating != null && (rating < 0 || rating > 5)) {
      throw new McpToolException("The rating must be between 0 and 5.");
    }

    if (rating != null || tags != null) {
      TableDetails tableDetails = requireTableDetails(game);
      if (rating != null) {
        tableDetails.setGameRating(rating == 0 ? null : rating);
      }
      if (tags != null) {
        tableDetails.setTags(normalizeTags(tags));
      }
      gameMediaService.saveTableDetails(tableDetails, game.getId(), true);
    }
    if (notes != null) {
      game.setComment(StringUtils.trimToNull(notes));
      try {
        gameService.save(game);
      }
      catch (Exception e) {
        throw new McpToolException("Saving the notes failed: " + e.getMessage());
      }
    }
    LOG.info("MCP: updated \"{}\"", game.getGameDisplayName());

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("tableId", game.getId());
    result.put("updated", true);
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

  static String normalizeTags(String tags) {
    List<String> values = TaggingUtil.getTags(tags);
    return values.isEmpty() ? null : TaggingUtil.join(values);
  }
}
