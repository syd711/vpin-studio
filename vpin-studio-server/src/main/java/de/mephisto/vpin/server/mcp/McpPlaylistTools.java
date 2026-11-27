package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.frontend.PlaylistGame;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.mcp.McpToolGroup;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameMediaService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.server.playlists.PlaylistService;
import de.mephisto.vpin.server.tagging.TaggingService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static de.mephisto.vpin.server.mcp.McpTableTools.prop;
import static de.mephisto.vpin.server.mcp.McpTableTools.schema;

/**
 * MCP tools for the playlists of the frontend and the tags of the tables.
 */
@Service
public class McpPlaylistTools implements McpToolProvider {
  private final static Logger LOG = LoggerFactory.getLogger(McpPlaylistTools.class);

  private static final int MAX_BULK = 500;

  @Autowired
  private PlaylistService playlistService;

  @Autowired
  private GameService gameService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private TaggingService taggingService;

  @Autowired
  private McpTableTools tableTools;

  @Override
  public List<McpTool> getTools() {
    List<McpTool> tools = new ArrayList<>();
    Map<String, Object> idOnly = schema(Map.of("id", prop("integer", "The playlist id, see list_playlists.")), List.of("id"));
    Map<String, Object> tableIds = prop("array", "The table ids, see search_tables.");
    tableIds.put("items", Map.of("type", "integer"));

    tools.add(McpTool.read("list_playlists", "List playlists",
        "Lists the playlists of the frontend with their id, name, number of tables and whether they are SQL playlists "
            + "(filled by a query, their tables can not be changed).",
        schema(Map.of(), List.of()), args -> listPlaylists()));

    tools.add(McpTool.read("get_playlist", "Get playlist",
        "Returns a playlist with its tables.", idOnly, this::getPlaylist));

    tools.add(McpTool.read("list_tags", "List tags",
        "Lists all tags used by the tables. Use search_tables with \"tag\" to get the tables of a tag.",
        schema(Map.of(), List.of()), args -> taggingService.getTags()));

    tools.add(McpTool.write(McpToolGroup.PLAYLISTS, "create_playlist", "Create playlist",
        "Creates a new playlist in the frontend, optionally with tables.",
        schema(Map.of(
            "name", prop("string", "The name of the playlist."),
            "tableIds", tableIds,
            "emulatorId", prop("integer", "The emulator of the playlist, default the first Visual Pinball X emulator, see list_emulators.")), List.of("name")),
        this::createPlaylist));

    tools.add(McpTool.write(McpToolGroup.PLAYLISTS, "rename_playlist", "Rename playlist",
        "Renames a playlist.",
        schema(Map.of(
            "id", prop("integer", "The playlist id, see list_playlists."),
            "name", prop("string", "The new name.")), List.of("id", "name")),
        this::renamePlaylist));

    tools.add(McpTool.write(McpToolGroup.PLAYLISTS, "add_to_playlist", "Add tables to playlist",
        "Adds tables to a playlist. Tables already in the playlist are skipped.",
        schema(Map.of(
            "id", prop("integer", "The playlist id, see list_playlists."),
            "tableIds", tableIds), List.of("id", "tableIds")),
        args -> changePlaylist(args, true)));

    tools.add(McpTool.write(McpToolGroup.PLAYLISTS, "remove_from_playlist", "Remove tables from playlist",
        "Removes tables from a playlist. The tables stay installed.",
        schema(Map.of(
            "id", prop("integer", "The playlist id, see list_playlists."),
            "tableIds", tableIds), List.of("id", "tableIds")),
        args -> changePlaylist(args, false)));

    tools.add(McpTool.delete(McpToolGroup.PLAYLISTS, "delete_playlist", "Delete playlist",
        "Deletes a playlist from the frontend. The tables stay installed. Confirm with the user first.",
        idOnly, this::deletePlaylist));

    Map<String, Object> bulkProps = new LinkedHashMap<>();
    bulkProps.put("tableIds", tableIds);
    Map<String, Object> filter = prop("object", "Instead of tableIds: the filters of search_tables (query, emulatorId, tag, pupPack, issue, hasBackglass, ...); all matching tables are tagged.");
    filter.put("properties", McpTableTools.searchProperties());
    bulkProps.put("filter", filter);
    bulkProps.put("addTags", prop("string", "Comma separated tags to add."));
    bulkProps.put("removeTags", prop("string", "Comma separated tags to remove."));
    bulkProps.put("dryRun", prop("boolean", "Only return the tables that would change, nothing is saved. Default false."));
    tools.add(McpTool.write(McpToolGroup.PLAYLISTS, "bulk_tag", "Tag many tables",
        "Adds and removes tags on many tables at once, given as table ids or as search_tables filter. "
            + "Run it with dryRun=true first and show the user how many tables change.",
        schema(bulkProps, List.of()), this::bulkTag));
    return tools;
  }

  //-------------------------------- read -------------------------------------------------------------

  private Object listPlaylists() {
    requirePlaylists();
    List<Map<String, Object>> result = new ArrayList<>();
    for (Playlist playlist : playlistService.getPlaylists()) {
      result.add(toSummary(playlist));
    }
    return result;
  }

  private Object getPlaylist(McpArguments args) {
    Playlist playlist = requirePlaylist(args.requireInt("id"));
    Map<String, Object> result = toSummary(playlist);
    if (StringUtils.isNotEmpty(playlist.getPlayListSQL())) {
      result.put("sql", playlist.getPlayListSQL());
    }
    if (StringUtils.isNotEmpty(playlist.getSqlError())) {
      result.put("sqlError", playlist.getSqlError());
    }
    List<Map<String, Object>> tables = new ArrayList<>();
    for (PlaylistGame pg : playlist.getGames()) {
      Map<String, Object> t = new LinkedHashMap<>();
      t.put("tableId", pg.getId());
      Game game = gameService.getGame(pg.getId());
      t.put("name", game != null ? game.getGameDisplayName() : null);
      t.put("favorite", pg.isFav());
      t.put("globalFavorite", pg.isGlobalFav());
      tables.add(t);
    }
    result.put("tables", tables);
    return result;
  }

  //-------------------------------- write ------------------------------------------------------------

  private Object createPlaylist(McpArguments args) {
    requirePlaylistChanges();
    String name = StringUtils.trimToNull(args.getString("name"));
    if (name == null) {
      throw new McpToolException("Missing argument \"name\".");
    }
    if (playlistService.getPlaylists().stream().anyMatch(p -> name.equalsIgnoreCase(p.getName()))) {
      throw new McpToolException("A playlist \"" + name + "\" exists already, see list_playlists.");
    }
    List<Game> games = requireGames(args.getIntList("tableIds"));
    GameEmulator emulator = resolveEmulator(args.getInt("emulatorId"));

    Playlist playlist = new Playlist();
    playlist.setName(name);
    playlist.setUseDefaults(true);
    playlist.setVisible(true);
    playlist.setParentId(-1);
    playlist.setEmulatorId(emulator != null ? emulator.getId() : null);
    playlist.setMenuColor((int) Long.parseLong("FFFFFF", 16));
    playlist.setMediaName("pl_" + FileUtils.replaceWindowsChars(name));
    Playlist saved = playlistService.save(playlist);
    if (saved == null) {
      throw new McpToolException("The playlist could not be created, see the VPin Studio Server log.");
    }
    for (Game game : games) {
      playlistService.addToPlaylist(saved.getId(), game.getId(), 0);
    }
    LOG.info("MCP: created playlist \"{}\" with {} tables", name, games.size());
    return toSummary(playlistService.getPlaylist(saved.getId()));
  }

  private Object renamePlaylist(McpArguments args) {
    requirePlaylistChanges();
    Playlist playlist = requirePlaylist(args.requireInt("id"));
    String name = StringUtils.trimToNull(args.getString("name"));
    if (name == null) {
      throw new McpToolException("Missing argument \"name\".");
    }
    String oldName = playlist.getName();
    playlist.setName(name);
    playlistService.save(playlist);
    LOG.info("MCP: renamed playlist \"{}\" to \"{}\"", oldName, name);
    return Map.of("id", playlist.getId(), "oldName", oldName, "name", name);
  }

  private Object changePlaylist(McpArguments args, boolean add) {
    Playlist playlist = requirePlaylist(args.requireInt("id"));
    if (playlist.isSqlPlayList()) {
      throw new McpToolException("\"" + playlist.getName() + "\" is an SQL playlist, its tables are selected by its query.");
    }
    List<Game> games = requireGames(args.getIntList("tableIds"));
    if (games.isEmpty()) {
      throw new McpToolException("Missing argument \"tableIds\".");
    }
    Set<Integer> current = new HashSet<>();
    playlist.getGames().forEach(pg -> current.add(pg.getId()));
    List<Integer> changed = new ArrayList<>();
    for (Game game : games) {
      if (add && !current.contains(game.getId())) {
        playlistService.addToPlaylist(playlist.getId(), game.getId(), 0);
        changed.add(game.getId());
      }
      else if (!add && current.contains(game.getId())) {
        playlistService.removeFromPlaylist(playlist.getId(), game.getId());
        changed.add(game.getId());
      }
    }
    LOG.info("MCP: {} {} tables {} playlist \"{}\"", add ? "added" : "removed", changed.size(), add ? "to" : "from", playlist.getName());
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("id", playlist.getId());
    result.put("name", playlist.getName());
    result.put(add ? "added" : "removed", changed);
    result.put("skipped", games.size() - changed.size());
    return result;
  }

  private Object deletePlaylist(McpArguments args) {
    requirePlaylistChanges();
    Playlist playlist = requirePlaylist(args.requireInt("id"));
    boolean deleted = playlistService.delete(playlist.getId());
    LOG.info("MCP: deleted playlist \"{}\"", playlist.getName());
    return Map.of("id", playlist.getId(), "name", playlist.getName(), "deleted", deleted);
  }

  private Object bulkTag(McpArguments args) {
    List<String> addTags = TaggingUtil.getTags(args.getString("addTags"));
    List<String> removeTags = TaggingUtil.getTags(args.getString("removeTags"));
    if (addTags.isEmpty() && removeTags.isEmpty()) {
      throw new McpToolException("Pass addTags or removeTags.");
    }
    boolean dryRun = args.getBoolean("dryRun", false);

    List<Game> games;
    if (args.has("tableIds")) {
      games = requireGames(args.getIntList("tableIds"));
    }
    else if (args.has("filter")) {
      Map<String, Object> filter = args.getMap("filter");
      if (filter.isEmpty()) {
        throw new McpToolException("The filter is empty: pass at least one search_tables filter or use tableIds.");
      }
      games = tableTools.findTables(new McpArguments(filter));
    }
    else {
      throw new McpToolException("Pass tableIds or filter.");
    }
    if (games.size() > MAX_BULK) {
      throw new McpToolException(games.size() + " tables match, at most " + MAX_BULK + " can be tagged at once. Narrow the filter down.");
    }

    List<Map<String, Object>> changes = new ArrayList<>();
    for (Game game : games) {
      TableDetails tableDetails = frontendService.getTableDetails(game.getId());
      if (tableDetails == null) {
        continue;
      }
      List<String> before = TaggingUtil.getTags(tableDetails.getTags());
      List<String> after = new ArrayList<>(before);
      after.removeIf(t -> removeTags.stream().anyMatch(t::equalsIgnoreCase));
      for (String tag : addTags) {
        if (after.stream().noneMatch(tag::equalsIgnoreCase)) {
          after.add(tag);
        }
      }
      if (after.equals(before)) {
        continue;
      }
      Map<String, Object> change = new LinkedHashMap<>();
      change.put("tableId", game.getId());
      change.put("name", game.getGameDisplayName());
      change.put("tags", after);
      changes.add(change);
      if (!dryRun) {
        tableDetails.setTags(after.isEmpty() ? null : TaggingUtil.join(after));
        gameMediaService.saveTableDetails(tableDetails, game.getId(), true);
      }
    }
    if (!dryRun) {
      LOG.info("MCP: tagged {} tables (+{} -{})", changes.size(), addTags, removeTags);
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("dryRun", dryRun);
    result.put("matchingTables", games.size());
    result.put("changedTables", changes.size());
    result.put("changes", changes);
    if (dryRun && !changes.isEmpty()) {
      result.put("message", "Nothing saved yet. Call bulk_tag again without dryRun to save the changes.");
    }
    return result;
  }

  //-------------------------------- helpers ----------------------------------------------------------

  private static void requirePlaylists() {
    if (!VPinStudioServer.Features.PLAYLIST_ENABLED) {
      throw new McpToolException("The frontend of this cabinet has no playlists.");
    }
  }

  private static void requirePlaylistChanges() {
    requirePlaylists();
    if (!VPinStudioServer.Features.PLAYLIST_CRUD) {
      throw new McpToolException("Playlists can not be created, renamed or deleted with the frontend of this cabinet.");
    }
  }

  private Playlist requirePlaylist(int id) {
    requirePlaylists();
    Playlist playlist = playlistService.getPlaylist(id);
    if (playlist == null) {
      throw new McpToolException("No playlist found for id " + id + ", use list_playlists to look up the id.");
    }
    return playlist;
  }

  private List<Game> requireGames(List<Integer> ids) {
    List<Game> games = new ArrayList<>();
    List<Integer> unknown = new ArrayList<>();
    for (Integer id : new LinkedHashSet<>(ids)) {
      Game game = gameService.getGame(id);
      if (game == null) {
        unknown.add(id);
      }
      else {
        games.add(game);
      }
    }
    if (!unknown.isEmpty()) {
      throw new McpToolException("No tables found for the ids " + unknown + ", use search_tables to look up the ids.");
    }
    return games;
  }

  private GameEmulator resolveEmulator(Integer emulatorId) {
    if (emulatorId != null) {
      GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
      if (emulator == null) {
        throw new McpToolException("No emulator found for id " + emulatorId + ", see list_emulators.");
      }
      return emulator;
    }
    List<GameEmulator> vpx = emulatorService.getVpxGameEmulators();
    if (!vpx.isEmpty()) {
      return vpx.get(0);
    }
    List<GameEmulator> all = emulatorService.getValidGameEmulators();
    return all.isEmpty() ? null : all.get(0);
  }

  private static Map<String, Object> toSummary(Playlist playlist) {
    Map<String, Object> p = new LinkedHashMap<>();
    p.put("id", playlist.getId());
    p.put("name", playlist.getName());
    p.put("tableCount", playlist.getGames() != null ? playlist.getGames().size() : 0);
    p.put("sqlPlaylist", playlist.isSqlPlayList());
    p.put("visible", playlist.isVisible());
    p.put("emulatorId", playlist.getEmulatorId());
    p.put("parentId", playlist.getParentId());
    return p;
  }
}
