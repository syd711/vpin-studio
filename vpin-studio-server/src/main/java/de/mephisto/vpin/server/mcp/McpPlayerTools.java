package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.mcp.McpToolGroup;
import de.mephisto.vpin.server.achievements.AchievementsQueryService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionResource;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

import static de.mephisto.vpin.server.mcp.McpTableTools.prop;
import static de.mephisto.vpin.server.mcp.McpTableTools.schema;

/**
 * MCP tools for the players of the cabinet, their achievements and the changes of offline competitions.
 */
@Service
public class McpPlayerTools implements McpToolProvider {
  private final static Logger LOG = LoggerFactory.getLogger(McpPlayerTools.class);

  private static final int MAX_TABLES = 100;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private AchievementsQueryService achievementsQueryService;

  @Lazy
  @Autowired
  private CompetitionResource competitionResource;

  @Override
  public List<McpTool> getTools() {
    List<McpTool> tools = new ArrayList<>();
    Map<String, Object> playerId = schema(Map.of("id", prop("integer", "The player id, see list_players.")), List.of("id"));

    tools.add(McpTool.read("list_players", "List players",
        "Lists the players of the cabinet with their id, name, initials, whether they are the administrator, "
            + "whether achievements are tracked for them and whether they are linked with a VPin Mania account.",
        schema(Map.of(), List.of()), args -> playerService.getBuildInPlayers().stream().map(McpPlayerTools::toPlayer).toList()));

    tools.add(McpTool.read("get_player_achievements", "Get player achievements",
        "Returns the achievements of a player: level, unlocked achievements and badges, and the tables with their "
            + "achievement progress. Without an id the overview of all players is returned.",
        schema(Map.of(
            "id", prop("integer", "The player id, see list_players. Without it all players are summarized."),
            "includeTables", prop("boolean", "Also return the achievement progress per table, default false.")), List.of()),
        this::getAchievements));

    Map<String, Object> playerProps = new LinkedHashMap<>();
    playerProps.put("id", prop("integer", "The player id, see list_players."));
    playerProps.put("name", prop("string", "The name of the player."));
    playerProps.put("initials", prop("string", "The three letter initials used in the highscores."));
    playerProps.put("competitionName", prop("string", "The name shown in competitions, an empty string removes it."));
    playerProps.put("achievementsEnabled", prop("boolean", "Track achievements for this player."));
    tools.add(McpTool.write(McpToolGroup.COMPETITIONS, "update_player", "Update player",
        "Changes the name, initials, competition name or the achievement tracking of a player. Only the given fields are changed.",
        schema(playerProps, List.of("id")), this::updatePlayer));

    Map<String, Object> competitionProps = new LinkedHashMap<>();
    competitionProps.put("id", prop("integer", "The competition id, see list_competitions."));
    competitionProps.put("name", prop("string", "Name of the competition, at most 40 characters."));
    competitionProps.put("startDate", prop("string", "Start in local time, \"YYYY-MM-DD\" (midnight) or \"YYYY-MM-DDTHH:MM\". Only for competitions that have not started yet."));
    competitionProps.put("endDate", prop("string", "End in local time, \"YYYY-MM-DD\" (midnight) or \"YYYY-MM-DDTHH:MM\"."));
    competitionProps.put("badge", prop("string", "Badge, see list_competition_badges. An empty string removes it."));
    tools.add(McpTool.write(McpToolGroup.COMPETITIONS, "update_competition", "Update competition",
        "Changes the name, dates or badge of an offline competition that has not finished yet. Only the given fields are changed.",
        schema(competitionProps, List.of("id")), this::updateCompetition));
    return tools;
  }

  //-------------------------------- read -------------------------------------------------------------

  private Object getAchievements(McpArguments args) {
    Integer id = args.getInt("id");
    if (id == null) {
      return achievementsQueryService.getPlayers();
    }
    requirePlayer(id);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("player", achievementsQueryService.getPlayerAchievements(id));
    if (args.getBoolean("includeTables", false)) {
      result.put("tables", achievementsQueryService.getTables(id).stream().limit(MAX_TABLES).toList());
    }
    return result;
  }

  //-------------------------------- write ------------------------------------------------------------

  private Object updatePlayer(McpArguments args) {
    Player player = requirePlayer(args.requireInt("id"));
    List<String> updated = new ArrayList<>();
    if (args.has("name")) {
      String name = StringUtils.trimToNull(args.getString("name"));
      if (name == null) {
        throw new McpToolException("The name must not be empty.");
      }
      player.setName(name);
      updated.add("name");
    }
    if (args.has("initials")) {
      String initials = StringUtils.trimToEmpty(args.getString("initials")).toUpperCase(Locale.ROOT);
      if (initials.length() != 3) {
        throw new McpToolException("The initials must have exactly three characters.");
      }
      Optional<Player> duplicate = playerService.getBuildInPlayers().stream()
          .filter(p -> !Objects.equals(p.getId(), player.getId()) && initials.equalsIgnoreCase(p.getInitials())).findFirst();
      if (duplicate.isPresent()) {
        throw new McpToolException("The initials " + initials + " are already used by " + duplicate.get().getName() + ".");
      }
      player.setInitials(initials);
      updated.add("initials");
    }
    if (args.has("competitionName")) {
      player.setCompetitionName(StringUtils.trimToNull(args.getString("competitionName")));
      updated.add("competitionName");
    }
    Boolean achievements = args.getBooleanOrNull("achievementsEnabled");
    if (achievements != null) {
      player.setAchievementsEnabled(achievements);
      updated.add("achievementsEnabled");
    }
    if (updated.isEmpty()) {
      throw new McpToolException("Nothing to update: pass name, initials, competitionName or achievementsEnabled.");
    }
    Player saved = playerService.save(player);
    LOG.info("MCP: updated player \"{}\": {}", saved.getName(), updated);
    Map<String, Object> result = toPlayer(saved);
    result.put("updated", updated);
    return result;
  }

  private Object updateCompetition(McpArguments args) {
    int id = args.requireInt("id");
    Competition competition = competitionService.getCompetition(id);
    if (competition == null) {
      throw new McpToolException("No competition found for id " + id + ", use list_competitions to look up the id.");
    }
    if (!CompetitionType.OFFLINE.name().equals(competition.getType())) {
      throw new McpToolException("Only offline competitions can be changed. " + competition.getType()
          + " competitions are synchronized with their online service.");
    }
    if (competition.getWinnerInitials() != null) {
      throw new McpToolException("The competition is already finished.");
    }

    List<String> updated = new ArrayList<>();
    if (args.has("name")) {
      String name = StringUtils.trimToNull(args.getString("name"));
      if (name == null || name.length() > 40) {
        throw new McpToolException("The name must not be empty or longer than 40 characters.");
      }
      competition.setName(name);
      updated.add("name");
    }
    Instant now = Instant.now();
    if (args.has("startDate")) {
      if (competition.getStartDate() != null && !competition.getStartDate().isAfter(now)) {
        throw new McpToolException("The competition has already started, its start date can not be changed.");
      }
      competition.setStartDate(McpCompetitionTools.parseDate(args.getString("startDate"), "startDate"));
      updated.add("startDate");
    }
    if (args.has("endDate")) {
      competition.setEndDate(McpCompetitionTools.parseDate(args.getString("endDate"), "endDate"));
      updated.add("endDate");
    }
    if (competition.getStartDate() != null && competition.getEndDate() != null && !competition.getStartDate().isBefore(competition.getEndDate())) {
      throw new McpToolException("The end date must be after the start date.");
    }
    if (args.has("endDate") && !competition.getEndDate().isAfter(now)) {
      throw new McpToolException("The end date must be in the future. Use finish_competition to end it now.");
    }
    if (args.has("badge")) {
      String badge = StringUtils.trimToNull(args.getString("badge"));
      if (badge != null && !systemService.getCompetitionBadges().contains(badge)) {
        throw new McpToolException("Unknown badge \"" + badge + "\", see list_competition_badges.");
      }
      competition.setBadge(badge);
      updated.add("badge");
    }
    if (updated.isEmpty()) {
      throw new McpToolException("Nothing to update: pass name, startDate, endDate or badge.");
    }
    Competition saved = competitionResource.save(competition);
    LOG.info("MCP: updated competition \"{}\": {}", saved.getName(), updated);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("id", saved.getId());
    result.put("name", saved.getName());
    result.put("start", saved.getStartDate() != null ? saved.getStartDate().toString() : null);
    result.put("end", saved.getEndDate() != null ? saved.getEndDate().toString() : null);
    result.put("badge", saved.getBadge());
    result.put("updated", updated);
    return result;
  }

  //-------------------------------- helpers ----------------------------------------------------------

  private Player requirePlayer(long id) {
    Player player = playerService.getBuildInPlayer(id);
    if (player == null) {
      throw new McpToolException("No player found for id " + id + ", use list_players to look up the id.");
    }
    return player;
  }

  private static Map<String, Object> toPlayer(Player p) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", p.getId());
    m.put("name", p.getName());
    m.put("initials", p.getInitials());
    m.put("competitionName", p.getCompetitionName());
    m.put("administrator", p.isAdministrative());
    m.put("achievementsEnabled", p.isAchievementsEnabled());
    m.put("maniaAccountLinked", StringUtils.isNotEmpty(p.getManiaAccountUuid()));
    return m;
  }
}
