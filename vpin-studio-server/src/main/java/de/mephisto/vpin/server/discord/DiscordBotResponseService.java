package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.BotCommand;
import de.mephisto.vpin.connectors.discord.BotCommandResponse;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.RankedPlayer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.parsing.HighscoreParsingService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DiscordBotResponseService implements DiscordBotCommandListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordBotResponseService.class);

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private HighscoreParsingService highscoreParser;

  @Override
  public BotCommandResponse onBotCommand(BotCommand cmd) {
    String name = cmd.getCommand();
    switch (name) {
      case BotCommand.CMD_COMMANDS: {
        return () -> DiscordBotCommandResponseFactory.COMMAND_SUMMARY;
      }
      case BotCommand.CMD_HELP: {
        return () -> DiscordBotCommandResponseFactory.COMMAND_SUMMARY;
      }
      case BotCommand.CMD_COMPETITIONS: {
        List<Competition> activeCompetitions = competitionService.getActiveCompetitions();
        if (activeCompetitions.isEmpty()) {
          return () -> "No active competitions found.";
        }

        StringBuilder builder = new StringBuilder();
        for (Competition activeCompetition : activeCompetitions) {
          if (!StringUtils.isEmpty(activeCompetition.getWinnerInitials()) || !activeCompetition.isStarted()) {
            continue;
          }

          Game game = gameService.getGame(activeCompetition.getGameId());
          if (game != null) {
            if (activeCompetition.getType().equals(CompetitionType.DISCORD.name())) {
              ScoreSummary scoreSummary = discordService.getScoreSummary(highscoreParser, activeCompetition.getUuid(), activeCompetition.getDiscordServerId(), activeCompetition.getDiscordChannelId());
              String msg = DiscordBotCommandResponseFactory.createActiveCompetitionMessage(activeCompetition, game, scoreSummary);
              builder.append(msg);
            }
            else {
              ScoreSummary highscores = highscoreService.getScoreSummary(cmd.getServerId(), game);
              String msg = DiscordBotCommandResponseFactory.createActiveCompetitionMessage(activeCompetition, game, highscores);
              builder.append(msg);
            }
          }
        }

        String result = builder.toString();
        if (StringUtils.isEmpty(result)) {
          return () -> "No active competitions found.";
        }

        return () -> result;
      }
      case BotCommand.CMD_RECENT: {
        ScoreSummary recentHighscores = gameService.getRecentHighscores(10);
        String msg = DiscordBotCommandResponseFactory.createRecentHighscoresMesssage(gameService, recentHighscores);
        return () -> msg;
      }
      case BotCommand.CMD_HS: {
        if (!StringUtils.isEmpty(cmd.getParameter())) {
          List<Game> games = gameService.getKnownGames(-1);
          for (Game game : games) {
            if (game.getGameDisplayName().toLowerCase().contains(cmd.getParameter()) || String.valueOf(game.getId()).equals(cmd.getParameter().trim())) {
              HighscoreMetadata metadata = highscoreService.scanScore(game, EventOrigin.BOT_CMD);
              if (metadata == null || (StringUtils.isEmpty(metadata.getRaw()) && !StringUtils.isEmpty(metadata.getStatus()))) {
                return () -> "Highscore for '" + game.getGameDisplayName() + "' retrieval failed: " + metadata.getStatus();
              }
              ScoreSummary highscores = highscoreService.getScoreSummary(cmd.getServerId(), game);
              return () -> DiscordBotCommandResponseFactory.createHighscoreMessage(game, highscores);
            }
          }
          return () -> "No matching table found for \"" + cmd.getParameter() + "\"";
        }
        return () -> "Missing search parameter for \"hs\" command.";
      }
      case BotCommand.CMD_FIND: {
        if (cmd.getParameter() != null) {
          List<Game> games = gameService.getKnownGames(-1);
          List<Game> matches = new ArrayList<>();
          for (Game game : games) {
            if (game.getGameDisplayName().toLowerCase().contains(cmd.getParameter()) || String.valueOf(game.getId()).equals(cmd.getParameter().trim())) {
              matches.add(game);
            }

            if (matches.size() == 10) {
              break;
            }
          }

          if (matches.isEmpty()) {
            return () -> "No matching table found for \"" + cmd.getParameter() + "\"";
          }

          StringBuilder builder = new StringBuilder();
          matches.forEach(g -> builder.append(g.getGameDisplayName() + " [ID " + g.getId() + "]\n"));
          return builder::toString;
        }
        return () -> "Missing search parameter for \"find\" command.";
      }
      case BotCommand.CMD_RANKS: {
        List<RankedPlayer> playersByRanks = highscoreService.getPlayersByRanks();
        return () -> DiscordBotCommandResponseFactory.createRanksMessage(playersByRanks);
      }
      case BotCommand.CMD_PLAYER: {
        Player player = playerService.getPlayerForInitials(cmd.getServerId(), cmd.getParameter());
        if (player != null) {
          List<Competition> offlineCompetitions = competitionService.getWonCompetitions(CompetitionType.OFFLINE, player.getInitials());
          List<Competition> onlineCompetitions = competitionService.getWonCompetitions(CompetitionType.DISCORD, player.getInitials());
          ScoreSummary highscores = highscoreService.getAllHighscoresForPlayer(cmd.getServerId(), cmd.getParameter());
          return () -> DiscordBotCommandResponseFactory.createRanksMessageFor(gameService, player, highscores, offlineCompetitions, onlineCompetitions);
        }
        return () -> "No player found with initials '" + cmd.getParameter().toUpperCase() + "'";
      }
    }
    return () -> "Unknown bot command \"" + cmd.getContent() + "\"\n\n" + DiscordBotCommandResponseFactory.COMMAND_SUMMARY;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    discordService.setBotCommandListener(this);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
