package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.BotCommand;
import de.mephisto.vpin.connectors.discord.BotCommandResponse;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.RankedPlayer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

  @Override
  public BotCommandResponse onBotCommand(BotCommand cmd) {
    String name = cmd.getCommand();
    switch (name) {
      case BotCommand.CMD_COMPETITIONS: {
        List<Competition> activeCompetitions = competitionService.getActiveCompetitions();
        if (activeCompetitions.isEmpty()) {
          return () -> "No active competitions found.";
        }

        StringBuilder builder = new StringBuilder();
        for (Competition activeCompetition : activeCompetitions) {
          Game game = gameService.getGame(activeCompetition.getGameId());
          if (game != null) {
            ScoreSummary highscores = highscoreService.getScoreSummary(cmd.getServerId(), game.getId(), game.getGameDisplayName());
            String msg = DiscordBotCommandResponseFactory.createActiveCompetitionMessage(activeCompetition, game, highscores);
            builder.append(msg);
          }
        }
        return builder::toString;
      }
      case BotCommand.CMD_HS: {
        if (cmd.getParameter() != null) {
          List<Game> games = gameService.getGames();
          for (Game game : games) {
            if (game.getGameDisplayName().toLowerCase().contains(cmd.getParameter())) {
              HighscoreMetadata metadata = highscoreService.updateHighscore(game);
              if (StringUtils.isEmpty(metadata.getRaw()) && !StringUtils.isEmpty(metadata.getStatus())) {
                return () -> "Highscore for '" + game.getGameDisplayName() + "' retrieval failed: " + metadata.getStatus();
              }
              ScoreSummary highscores = highscoreService.getScoreSummary(cmd.getServerId(), game.getId(), game.getGameDisplayName());
              return () -> DiscordBotCommandResponseFactory.createHighscoreMessage(game, highscores);
            }
          }
          LOG.info("No matching game found for '" + cmd);
        }
        return null;
      }
      case BotCommand.CMD_RANKS: {
        List<RankedPlayer> playersByRanks = highscoreService.getPlayersByRanks();
        return () -> DiscordBotCommandResponseFactory.createRanksMessage(playersByRanks);
      }
      case BotCommand.CMD_PLAYER: {
        Player player = playerService.getPlayerForInitials(cmd.getServerId(), cmd.getParameter());
        if (player != null) {
          ScoreSummary highscores = highscoreService.getAllHighscoresForPlayer(cmd.getServerId(), cmd.getParameter());
          return () -> DiscordBotCommandResponseFactory.createRanksMessageFor(gameService, player, highscores);
        }
        return () -> "No player found with initials '" + cmd.getParameter().toUpperCase() + "'";
      }
    }
//    return () -> "Unknown bot command '" + cmd.getContent() + "'";
    return () -> null;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    discordService.setBotCommandListener(this);
  }
}
