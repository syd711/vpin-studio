package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.connectors.discord.BotCommand;
import de.mephisto.vpin.connectors.discord.BotCommandResponse;
import de.mephisto.vpin.connectors.discord.DiscordWebhook;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.competitions.*;
import de.mephisto.vpin.server.discord.DiscordBotCommandListener;
import de.mephisto.vpin.server.discord.DiscordBotCommandResponseFactory;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.discord.DiscordWebhookMessageFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.TableStatusChangeListener;
import de.mephisto.vpin.server.popper.TableStatusChangedEvent;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService implements InitializingBean, HighscoreChangeListener, CompetitionChangeListener, TableStatusChangeListener, DiscordBotCommandListener {
  private final static Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private CardService cardService;

  @Autowired
  private GameService gameService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private PopperService popperService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private PlayerService playerService;

  public void notifyPopperRestart() {
    discordService.setStatus(null);
  }

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
            ScoreSummary highscores = highscoreService.getHighscores(game.getId(), game.getGameDisplayName());
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
              ScoreSummary highscores = highscoreService.getHighscores(game.getId(), game.getGameDisplayName());
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
        Optional<Player> playerForInitials = playerService.getPlayerForInitials(cmd.getParameter());
        if (playerForInitials.isPresent()) {
          ScoreSummary highscores = highscoreService.getHighscores(cmd.getParameter());
          return () -> DiscordBotCommandResponseFactory.createRanksMessageFor(playerForInitials.get(), highscores);
        }
        return () -> "No player found with initials '" + cmd.getParameter().toUpperCase() + "'";
      }
    }
//    return () -> "Unknown bot command '" + cmd.getContent() + "'";
    return () -> null;
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    Game game = event.getGame();
    discordService.setStatus(game.getGameDisplayName());
    highscoreService.updateHighscore(game);
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    Game game = event.getGame();
    LOG.info("Executing table exit commands for '" + game + "'");
    discordService.setStatus(null);
    new Thread(() -> {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        //ignore
      }
      LOG.info("Finished 5 second update delay, updating highscores.");
      highscoreService.updateHighscore(game);
    }).start();
  }

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    try {
      Game game = event.getGame();
      cardService.generateCard(game, false);
    } catch (Exception e) {
      LOG.error("Error updating card after highscore change event: " + e.getMessage(), e);
    }

    String webhookUrl = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_WEBHOOK_URL);
    if (!StringUtils.isEmpty(webhookUrl)) {
      String message = DiscordWebhookMessageFactory.createHighscoreCreatedMessage(event);
      DiscordWebhook.call(webhookUrl, message);
      LOG.info("Called Discord webhook for update of score " + event.getNewHighscore());
    }
  }

  @Override
  public void competitionCreated(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      if (competition.isCustomizeMedia()) {
        popperService.augmentWheel(game, competition.getBadge());
      }

      if (competition.isDiscordNotifications() && competition.isActive()) {
        String webhookUrl = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_WEBHOOK_URL);
        if (!StringUtils.isEmpty(webhookUrl)) {
          String message = DiscordWebhookMessageFactory.createCompetitionCreatedMessage(competition, game);
          DiscordWebhook.call(webhookUrl, message);
          LOG.info("Called Discord webhook for creation of " + competition);
        }
      }
    }
  }

  @Override
  public void competitionFinished(@NonNull Competition competition, @Nullable Player winner) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      LOG.info("Finishing " + competition);
      popperService.deAugmentWheel(game);

      if (competition.isDiscordNotifications()) {
        String webhookUrl = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_WEBHOOK_URL);
        if (!StringUtils.isEmpty(webhookUrl)) {
          ScoreSummary summary = highscoreService.getHighscores(competition.getGameId(), game.getGameDisplayName());

          if (!summary.getScores().isEmpty()) {
            String message = DiscordWebhookMessageFactory.createCompetitionFinishedMessage(competition, winner, game, summary);
            DiscordWebhook.call(webhookUrl, message);
            LOG.info("Called Discord webhook for completion of " + competition);
          }
          else {
            LOG.warn("Skipped calling Discord webhook for completion of " + competition + ", game has no highscore.");
          }
        }
      }
    }
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      popperService.deAugmentWheel(game);

      if (competition.isDiscordNotifications() && competition.isActive()) {
        String webhookUrl = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_WEBHOOK_URL);
        if (!StringUtils.isEmpty(webhookUrl)) {
          String message = DiscordWebhookMessageFactory.createCompetitionCancelledMessage(competition);
          DiscordWebhook.call(webhookUrl, message);
          LOG.info("Called Discord webhook for cancellation of " + competition);
        }
      }
    }
  }

  @Override
  public void competitionChanged(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      boolean customizeMedia = competition.isCustomizeMedia();
      if (customizeMedia) {
        popperService.augmentWheel(game, competition.getBadge());
      }
      else {
        popperService.deAugmentWheel(game);
      }
    }
    runAugmentationCheck();
  }

  private void runAugmentationCheck() {
    List<Integer> competedGameIds = competitionService.getActiveCompetitions().stream().map(Competition::getGameId).collect(Collectors.toList());

    List<Game> games = gameService.getGames();
    for (Game game : games) {
      if (!competedGameIds.contains(game.getId())) {
        popperService.deAugmentWheel(game);
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    highscoreService.addHighscoreChangeListener(this);
    competitionService.addCompetitionChangeListener(this);
    popperService.addTableStatusChangeListener(this);
    discordService.setStatus(null);
    discordService.setBotCommandListener(this);
  }
}
