package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.iscored.Settings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mcp.McpToolGroup;
import de.mephisto.vpin.restclient.competitions.CompetitionScore;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionResource;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.competitions.iscored.IScoredCompetitionSynchronizer;
import de.mephisto.vpin.server.competitions.mania.ManiaChallengeCompetitionSynchronizer;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.wovp.WovpService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;

import static de.mephisto.vpin.server.mcp.McpTableTools.prop;
import static de.mephisto.vpin.server.mcp.McpTableTools.schema;

/**
 * MCP tools for all competition types: offline competitions, iScored game rooms, World of Virtual Pinball weekly
 * challenges and VPin Mania challenges.
 */
@Service
public class McpCompetitionTools implements McpToolProvider {
  private final static Logger LOG = LoggerFactory.getLogger(McpCompetitionTools.class);

  private static final List<String> TYPES = Arrays.stream(CompetitionType.values()).map(Enum::name).toList();
  private static final String TYPE_DESCRIPTION = "OFFLINE (local competitions on this cabinet), ISCORED (tables of subscribed iScored game rooms), "
      + "WEEKLY (World of Virtual Pinball weekly challenges) or MANIA_CHALLENGE (VPin Mania challenges).";

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private WovpService wovpService;

  @Lazy
  @Autowired
  private IScoredCompetitionSynchronizer iScoredSynchronizer;

  @Lazy
  @Autowired
  private ManiaChallengeCompetitionSynchronizer maniaChallengeSynchronizer;

  @Lazy
  @Autowired
  private CompetitionResource competitionResource;

  @Override
  public List<McpTool> getTools() {
    List<McpTool> tools = new ArrayList<>();
    Map<String, Object> idOnly = schema(Map.of("id", prop("integer", "The competition id, see list_competitions.")), List.of("id"));

    tools.add(McpTool.read("list_competitions", "List competitions",
        "Lists the competitions of the cabinet: " + TYPE_DESCRIPTION + " Each entry contains the competition id, "
            + "the table, the time span, whether it is active and the winner of finished competitions.",
        schema(Map.of(
            "type", enumProp("Only competitions of this type: " + TYPE_DESCRIPTION, TYPES),
            "onlyActive", prop("boolean", "Only competitions that are running now.")), List.of()),
        this::listCompetitions));

    tools.add(McpTool.read("get_competition", "Get competition",
        "Returns the details and the current scores of a competition. For iScored and World of Virtual Pinball the "
            + "scores are loaded from the online service.",
        idOnly, this::getCompetition));

    tools.add(McpTool.read("get_iscored_status", "Get iScored status",
        "Checks the iScored setup: whether iScored is enabled, the configured game rooms, whether each game room is "
            + "reachable and accepts public score entries, and for every game of the room whether it has a VPS tag and "
            + "which installed table it is linked with. Use it to find out why scores are not submitted.",
        schema(Map.of(), List.of()), args -> getIScoredStatus()));

    tools.add(McpTool.read("list_competition_badges", "List competition badges",
        "Lists the badges that can be assigned to offline competitions and iScored game rooms.",
        schema(Map.of(), List.of()), args -> systemService.getCompetitionBadges()));

    tools.add(McpTool.write(McpToolGroup.COMPETITIONS, "create_offline_competition", "Create offline competition",
        "Creates an offline competition for one table of the cabinet. The best score between start and end wins.",
        schema(Map.of(
            "name", prop("string", "Name of the competition, at most 40 characters."),
            "tableId", prop("integer", "The table id, see search_tables."),
            "startDate", prop("string", "Start in local time, \"YYYY-MM-DD\" (midnight) or \"YYYY-MM-DDTHH:MM\". Default: today."),
            "endDate", prop("string", "End in local time, \"YYYY-MM-DD\" (midnight) or \"YYYY-MM-DDTHH:MM\"."),
            "badge", prop("string", "Optional badge, see list_competition_badges."),
            "resetHighscores", prop("boolean", "Reset the highscores of the table when the competition starts, default false.")),
            List.of("name", "tableId", "endDate")),
        this::createOfflineCompetition));

    tools.add(McpTool.write(McpToolGroup.COMPETITIONS, "finish_competition", "Finish competition",
        "Finishes a running offline competition now and determines the winner.",
        idOnly, this::finishCompetition));

    tools.add(McpTool.delete(McpToolGroup.COMPETITIONS, "delete_competition", "Delete competition",
        "Deletes an offline competition. This cannot be undone, so always confirm with the user first. "
            + "Competitions of the other types are synchronized with their online service and can not be deleted.",
        idOnly, this::deleteCompetition));

    tools.add(McpTool.write(McpToolGroup.COMPETITIONS, "add_iscored_game_room", "Add iScored game room",
        "Adds an iScored game room and enables iScored. The games of the room are then linked with the installed "
            + "tables through the VPS links in their tags, and new highscores are submitted to the room.",
        schema(Map.of(
            "url", prop("string", "The public URL of the game room, e.g. \"https://www.iscored.info/?mode=public&user=myroom\"."),
            "synchronize", prop("boolean", "Subscribe to all games of the room automatically, default true."),
            "resetHighscores", prop("boolean", "Reset the highscores of a table when a new game is subscribed, default true."),
            "ignoreHidden", prop("boolean", "Skip games that are hidden in the game room, default true."),
            "badge", prop("string", "Optional badge, see list_competition_badges.")), List.of("url")),
        this::addGameRoom));

    tools.add(McpTool.delete(McpToolGroup.COMPETITIONS, "remove_iscored_game_room", "Remove iScored game room",
        "Removes an iScored game room and its subscriptions. Scores are no longer submitted to it. Confirm with the user first.",
        schema(Map.of("url", prop("string", "The URL of the game room, see get_iscored_status.")), List.of("url")),
        this::removeGameRoom));

    tools.add(McpTool.write(McpToolGroup.COMPETITIONS, "synchronize_competitions", "Synchronize competitions",
        "Synchronizes the online competitions with their service: iScored game rooms, World of Virtual Pinball weekly "
            + "challenges and VPin Mania challenges.",
        schema(Map.of("type", enumProp("Only synchronize this type, default all.", List.of("ISCORED", "WEEKLY", "MANIA_CHALLENGE"))), List.of()),
        this::synchronize));
    return tools;
  }

  //-------------------------------- read -------------------------------------------------------------

  private Object listCompetitions(McpArguments args) {
    String type = StringUtils.trimToNull(args.getString("type"));
    if (type != null && !TYPES.contains(type.toUpperCase(Locale.ROOT))) {
      throw new McpToolException("Unknown type \"" + type + "\", use one of " + TYPES + ".");
    }
    boolean onlyActive = args.getBoolean("onlyActive", false);
    return getAllCompetitions().stream()
        .filter(c -> type == null || type.equalsIgnoreCase(c.getType()))
        .filter(c -> !onlyActive || isRunning(c))
        .map(this::toSummary)
        .toList();
  }

  private Object getCompetition(McpArguments args) {
    Competition competition = requireCompetition(args.requireInt("id"));
    Map<String, Object> result = toSummary(competition);
    result.put("url", competition.getUrl());
    result.put("badge", competition.getBadge());
    result.put("resetHighscores", competition.isHighscoreReset());
    result.put("vpsTableId", competition.getVpsTableId());
    result.put("vpsVersionId", competition.getVpsTableVersionId());
    if (StringUtils.isNotEmpty(competition.getIssues())) {
      result.put("issues", competition.getIssues());
    }
    try {
      result.put("scores", getScores(competition));
    }
    catch (Exception e) {
      LOG.warn("Failed to load scores of competition {}: {}", competition.getId(), e.getMessage());
      result.put("scores", List.of());
      result.put("scoresError", "The scores could not be loaded: " + e.getMessage());
    }
    return result;
  }

  private List<Map<String, Object>> getScores(Competition competition) {
    CompetitionType type = CompetitionType.valueOf(competition.getType());
    switch (type) {
      case OFFLINE -> {
        ScoreList scoreList = competitionService.getCompetitionScores(competition.getId());
        ScoreSummary latest = scoreList != null ? scoreList.getLatestScore() : null;
        return latest != null ? latest.getScores().stream().map(McpCompetitionTools::toScore).toList() : List.of();
      }
      case WEEKLY -> {
        List<Map<String, Object>> scores = new ArrayList<>();
        for (CompetitionScore score : wovpService.getWeeklyScores(competition.getUuid())) {
          Map<String, Object> s = new LinkedHashMap<>();
          s.put("rank", score.getRank());
          s.put("player", score.getParticipantName());
          s.put("country", score.getParticipantCountryCode());
          s.put("score", (long) score.getScore());
          s.put("pending", score.isPending());
          s.put("myScore", score.isMyScore());
          s.put("date", toString(score.getCreationDate()));
          scores.add(s);
        }
        return scores;
      }
      case ISCORED -> {
        GameRoom gameRoom = IScored.getGameRoom(competition.getUrl(), false);
        IScoredGame game = gameRoom != null ? gameRoom.getGameByVps(competition.getVpsTableId(), competition.getVpsTableVersionId()) : null;
        if (game == null) {
          throw new McpToolException("The game could not be found in the iScored game room " + competition.getUrl());
        }
        List<Map<String, Object>> scores = new ArrayList<>();
        int rank = 1;
        for (de.mephisto.vpin.connectors.iscored.Score score : game.getScores()) {
          Map<String, Object> s = new LinkedHashMap<>();
          s.put("rank", rank++);
          s.put("player", score.getName());
          s.put("score", score.getScore());
          s.put("date", toString(score.getDate()));
          scores.add(s);
        }
        return scores;
      }
      default -> {
        //the scores of the table on this cabinet
        ScoreSummary summary = competitionService.getCompetitionScore(competition.getId());
        return summary.getScores().stream().map(McpCompetitionTools::toScore).toList();
      }
    }
  }

  private Object getIScoredStatus() {
    IScoredSettings settings = preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    List<Competition> subscriptions = competitionService.getIScoredSubscriptions();

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("enabled", settings.isEnabled());
    result.put("documentation", WikiHelpIndex.WIKI_URL + "/iScored");
    List<Map<String, Object>> rooms = new ArrayList<>();
    for (IScoredGameRoom room : settings.getGameRooms()) {
      Map<String, Object> r = new LinkedHashMap<>();
      r.put("url", room.getUrl());
      r.put("synchronize", room.isSynchronize());
      r.put("resetHighscores", room.isScoreReset());
      r.put("ignoreHidden", room.isIgnoreHidden());
      r.put("badge", room.getBadge());
      r.put("subscriptions", subscriptions.stream().filter(c -> room.getUrl().equals(c.getUrl())).count());

      List<String> problems = new ArrayList<>();
      GameRoom gameRoom = IScored.getGameRoom(room.getUrl(), true);
      if (gameRoom == null) {
        problems.add("The game room could not be loaded. Check the URL and that the room is public.");
      }
      else {
        r.put("name", gameRoom.getName());
        Settings roomSettings = gameRoom.getSettings();
        if (roomSettings != null) {
          r.put("publicScoreEntry", roomSettings.isPublicScoreEnteringEnabled());
          r.put("publicScoresReadable", roomSettings.isPublicScoresReadingEnabled());
          r.put("adminApproval", roomSettings.isAdminApprovalEnabled());
          if (!roomSettings.isPublicScoreEnteringEnabled()) {
            problems.add("Public score entries are disabled in the game room settings, so VPin Studio can not submit scores.");
          }
          if (!roomSettings.isPublicScoresReadingEnabled()) {
            problems.add("Public score reading is disabled in the game room settings.");
          }
        }
        r.put("games", gameRoom.getGames().stream().map(g -> toRoomGame(g, room)).toList());
      }
      r.put("problems", problems);
      rooms.add(r);
    }
    result.put("gameRooms", rooms);
    if (rooms.isEmpty()) {
      result.put("message", "No game room configured. Add one with add_iscored_game_room or in the iScored preferences.");
    }
    else if (!settings.isEnabled()) {
      result.put("message", "iScored is disabled in the preferences, no scores are submitted.");
    }
    return result;
  }

  private Map<String, Object> toRoomGame(IScoredGame game, IScoredGameRoom room) {
    Map<String, Object> g = new LinkedHashMap<>();
    g.put("name", game.getName());
    g.put("hidden", game.isGameHidden());
    g.put("locked", game.isGameLocked());
    g.put("vpsTableId", game.getVpsTableId());
    g.put("vpsVersionId", game.getVpsTableVersionId());
    g.put("allVersions", game.isAllVersionsEnabled());

    String status;
    if (game.isGameHidden() && room.isIgnoreHidden()) {
      status = "IGNORED_HIDDEN";
    }
    else if (!game.isVpsTagged()) {
      status = "NO_VPS_TAG";
    }
    else {
      Game table = game.isAllVersionsEnabled()
          ? gameService.getGameByVpsTable(game.getVpsTableId(), null)
          : gameService.getGameByVpsTable(game.getVpsTableId(), game.getVpsTableVersionId());
      if (table == null) {
        status = "NOT_INSTALLED";
      }
      else {
        status = "LINKED";
        g.put("tableId", table.getId());
        g.put("tableName", table.getGameDisplayName());
      }
    }
    g.put("status", status);
    return g;
  }

  //-------------------------------- write ------------------------------------------------------------

  private Object createOfflineCompetition(McpArguments args) {
    String name = StringUtils.trimToNull(args.getString("name"));
    if (name == null) {
      throw new McpToolException("Missing argument \"name\".");
    }
    if (name.length() > 40) {
      throw new McpToolException("The name must not be longer than 40 characters.");
    }
    Game game = gameService.getGame(args.requireInt("tableId"));
    if (game == null) {
      throw new McpToolException("No table found for id " + args.getInt("tableId") + ", use search_tables to look up the id.");
    }
    Instant start = args.getString("startDate") != null ? parseDate(args.getString("startDate"), "startDate")
        : LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant end = parseDate(args.getString("endDate"), "endDate");
    if (!start.isBefore(end)) {
      throw new McpToolException("The end date must be after the start date.");
    }
    if (!end.isAfter(Instant.now())) {
      throw new McpToolException("The end date must be in the future.");
    }
    String badge = StringUtils.trimToNull(args.getString("badge"));
    if (badge != null && !systemService.getCompetitionBadges().contains(badge)) {
      throw new McpToolException("Unknown badge \"" + badge + "\", see list_competition_badges.");
    }

    Competition competition = new Competition();
    competition.setType(CompetitionType.OFFLINE.name());
    competition.setUuid(UUID.randomUUID().toString());
    competition.setName(name);
    competition.setGameId(game.getId());
    competition.setStartDate(start);
    competition.setEndDate(end);
    competition.setBadge(badge);
    competition.setHighscoreReset(args.getBoolean("resetHighscores", false));
    Competition saved = competitionResource.save(competition);
    LOG.info("MCP: created offline competition \"{}\" for \"{}\"", name, game.getGameDisplayName());
    return toSummary(saved);
  }

  private Object finishCompetition(McpArguments args) {
    Competition competition = requireCompetition(args.requireInt("id"));
    requireOffline(competition, "finished");
    if (competition.getWinnerInitials() != null) {
      throw new McpToolException("The competition is already finished, the winner is " + competition.getWinnerInitials() + ".");
    }
    competitionResource.finishCompetition(competition.getId().intValue());
    LOG.info("MCP: finished competition \"{}\"", competition.getName());
    return toSummary(requireCompetition(competition.getId().intValue()));
  }

  private Object deleteCompetition(McpArguments args) {
    Competition competition = requireCompetition(args.requireInt("id"));
    requireOffline(competition, "deleted");
    boolean deleted = competitionService.delete(competition.getId());
    LOG.info("MCP: deleted competition \"{}\"", competition.getName());
    return Map.of("id", competition.getId(), "name", competition.getName(), "deleted", deleted);
  }

  private Object addGameRoom(McpArguments args) {
    String url = StringUtils.trimToNull(args.getString("url"));
    if (url == null || !IScored.isIScoredGameRoomUrl(url)) {
      throw new McpToolException("\"" + url + "\" is not the URL of an iScored game room. Copy the public URL of the room from iScored.");
    }
    IScoredSettings settings = preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    if (settings.getGameRooms().stream().anyMatch(r -> url.equalsIgnoreCase(r.getUrl()))) {
      throw new McpToolException("The game room is already configured.");
    }
    GameRoom gameRoom = IScored.getGameRoom(url, true);
    if (gameRoom == null) {
      throw new McpToolException("The game room could not be loaded. Check the URL and that the room is public.");
    }
    String badge = StringUtils.trimToNull(args.getString("badge"));
    if (badge != null && !systemService.getCompetitionBadges().contains(badge)) {
      throw new McpToolException("Unknown badge \"" + badge + "\", see list_competition_badges.");
    }

    IScoredGameRoom room = new IScoredGameRoom();
    room.setUuid(UUID.randomUUID().toString());
    room.setUrl(url);
    room.setSynchronize(args.getBoolean("synchronize", true));
    room.setScoreReset(args.getBoolean("resetHighscores", true));
    room.setIgnoreHidden(args.getBoolean("ignoreHidden", true));
    room.setBadge(badge);
    List<IScoredGameRoom> rooms = new ArrayList<>(settings.getGameRooms());
    rooms.add(room);
    settings.setGameRooms(rooms);
    settings.setEnabled(true);
    saveIScoredSettings(settings);
    LOG.info("MCP: added iScored game room {}", url);

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("url", url);
    result.put("name", gameRoom.getName());
    result.put("added", true);
    result.put("message", "The game room has been synchronized, use get_iscored_status to check the linked tables.");
    return result;
  }

  private Object removeGameRoom(McpArguments args) {
    String url = StringUtils.trimToNull(args.getString("url"));
    IScoredSettings settings = preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    List<IScoredGameRoom> rooms = new ArrayList<>(settings.getGameRooms());
    if (url == null || !rooms.removeIf(r -> url.equalsIgnoreCase(r.getUrl()))) {
      throw new McpToolException("No game room configured for \"" + url + "\", see get_iscored_status.");
    }
    settings.setGameRooms(rooms);
    saveIScoredSettings(settings);
    if (!settings.isEnabled()) {
      //an enabled iScored is synchronized by the preference listener; the synchronization deletes the subscriptions
      //of rooms that are no longer configured
      iScoredSynchronizer.synchronizeGameRooms();
    }
    LOG.info("MCP: removed iScored game room {}", url);
    return Map.of("url", url, "removed", true);
  }

  private Object synchronize(McpArguments args) {
    String type = StringUtils.defaultIfEmpty(args.getString("type"), "ALL").toUpperCase(Locale.ROOT);
    Map<String, Object> result = new LinkedHashMap<>();
    if (type.equals("ALL") || type.equals("ISCORED")) {
      result.put("ISCORED", iScoredSynchronizer.synchronizeGameRooms());
    }
    if (type.equals("ALL") || type.equals("WEEKLY")) {
      result.put("WEEKLY", wovpService.synchronize(true));
    }
    if (type.equals("ALL") || type.equals("MANIA_CHALLENGE")) {
      result.put("MANIA_CHALLENGE", maniaChallengeSynchronizer.synchronizeChallenges(true));
    }
    if (result.isEmpty()) {
      throw new McpToolException("Unknown type \"" + type + "\", use ISCORED, WEEKLY or MANIA_CHALLENGE.");
    }
    return result;
  }

  //-------------------------------- helpers ----------------------------------------------------------

  private List<Competition> getAllCompetitions() {
    List<Competition> all = new ArrayList<>();
    all.addAll(competitionService.getOfflineCompetitions());
    all.addAll(competitionService.getIScoredSubscriptions());
    all.addAll(competitionService.getWeeklyCompetitions());
    all.addAll(competitionService.getManiaChallengeCompetitions());
    return all;
  }

  private Competition requireCompetition(int id) {
    Competition competition = competitionService.getCompetition(id);
    if (competition == null) {
      throw new McpToolException("No competition found for id " + id + ", use list_competitions to look up the id.");
    }
    return competition;
  }

  private static void requireOffline(Competition competition, String action) {
    if (!CompetitionType.OFFLINE.name().equals(competition.getType())) {
      throw new McpToolException("Only offline competitions can be " + action + ". " + competition.getType()
          + " competitions are synchronized with their online service.");
    }
  }

  private void saveIScoredSettings(IScoredSettings settings) {
    try {
      //the iScored synchronizer listens to this preference and synchronizes the game rooms
      preferencesService.savePreference(settings);
    }
    catch (Exception e) {
      throw new McpToolException("Saving the iScored settings failed: " + e.getMessage());
    }
  }

  private static boolean isRunning(Competition c) {
    Instant now = Instant.now();
    return c.getWinnerInitials() == null && (c.getStartDate() == null || !c.getStartDate().isAfter(now)) && (c.getEndDate() == null || c.getEndDate().isAfter(now));
  }

  private Map<String, Object> toSummary(Competition c) {
    Map<String, Object> s = new LinkedHashMap<>();
    s.put("id", c.getId());
    s.put("type", c.getType());
    s.put("name", c.getName());
    s.put("tableId", c.getGameId());
    Game game = c.getGameId() > 0 ? gameService.getGame(c.getGameId()) : null;
    s.put("tableName", game != null ? game.getGameDisplayName() : null);
    s.put("start", toString(c.getStartDate()));
    s.put("end", toString(c.getEndDate()));
    s.put("active", isRunning(c));
    s.put("winner", c.getWinnerInitials());
    if (StringUtils.isNotEmpty(c.getWinnerInitials()) && StringUtils.isNotEmpty(c.getScore())) {
      s.put("winningScore", c.getScore());
    }
    if (c.getValidationState() != null && c.getValidationState().getCode() > 0) {
      s.put("validationCode", c.getValidationState().getCode());
    }
    return s;
  }

  private static Map<String, Object> toScore(Score score) {
    Map<String, Object> s = new LinkedHashMap<>();
    s.put("rank", score.getPosition());
    s.put("player", score.getPlayerInitials());
    s.put("score", score.getFormattedScore());
    s.put("date", toString(score.getCreatedAt()));
    return s;
  }

  static Instant parseDate(String value, String argument) {
    if (StringUtils.isBlank(value)) {
      throw new McpToolException("Missing argument \"" + argument + "\".");
    }
    String v = value.trim();
    try {
      if (v.length() <= 10) {
        return LocalDate.parse(v).atStartOfDay(ZoneId.systemDefault()).toInstant();
      }
      return LocalDateTime.parse(v.replace(' ', 'T')).atZone(ZoneId.systemDefault()).toInstant();
    }
    catch (DateTimeParseException e) {
      throw new McpToolException("\"" + argument + "\" must be \"YYYY-MM-DD\" or \"YYYY-MM-DDTHH:MM\", got \"" + value + "\".");
    }
  }

  private static Map<String, Object> enumProp(String description, List<String> values) {
    Map<String, Object> p = prop("string", description);
    p.put("enum", values);
    return p;
  }

  private static String toString(Object date) {
    return date != null ? date.toString() : null;
  }
}
