package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.connectors.discord.BotCommand;
import de.mephisto.vpin.connectors.discord.BotCommandResponse;
import de.mephisto.vpin.connectors.discord.DiscordWebhook;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.competitions.*;
import de.mephisto.vpin.server.discord.*;
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
    Game game = event.getGame();
    try {
      cardService.generateCard(game, false);
    } catch (Exception e) {
      LOG.error("Error updating card after highscore change event: " + e.getMessage(), e);
    }

    boolean highscoreNotificationSent = false;
    List<Competition> competitionForGame = competitionService.findCompetitionForGame(game.getId());
    for (Competition competition : competitionForGame) {
      if (competition.getDiscordChannelId() > 0 && competition.isActive()) {
        long discordChannelId = competition.getDiscordChannelId();
        discordService.sendMessage(discordChannelId, DiscordChannelMessageFactory.createCompetitionHighscoreCreatedMessage(competition, event));
        highscoreNotificationSent = true;
      }
    }

    //no notification was sent, try at least the webhook
    if (!highscoreNotificationSent) {
      String webhookUrl = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_WEBHOOK_URL);
      if (!StringUtils.isEmpty(webhookUrl)) {
        String message = DiscordWebhookMessageFactory.createHighscoreCreatedMessage(event);
        DiscordWebhook.call(webhookUrl, message);
        LOG.info("Called Discord webhook for update of score " + event.getNewHighscore());
      }
    }
  }

  @Override
  public void competitionCreated(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      if (competition.getBadge() != null) {
        popperService.augmentWheel(game, competition.getBadge());
      }

      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        ScoreSummary highscores = highscoreService.getHighscores(game.getId());
        discordService.saveCompetitionData(competition, game, highscores);
      }

      if (competition.getDiscordChannelId() > 0 && competition.isActive()) {
        long discordChannelId = competition.getDiscordChannelId();
        discordService.sendMessage(discordChannelId, DiscordChannelMessageFactory.createCompetitionCreatedMessage(competition, game));
      }
    }
  }

  @Override
  public void competitionFinished(@NonNull Competition competition, @Nullable Player winner) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      LOG.info("Finishing " + competition);
      popperService.deAugmentWheel(game);

      if (competition.getDiscordChannelId() > 0) {
        long discordChannelId = competition.getDiscordChannelId();
        ScoreSummary summary = discordService.getScoreSummary(discordChannelId);
        if (summary != null) {
          discordService.sendMessage(discordChannelId, DiscordChannelMessageFactory.createCompetitionFinishedMessage(competition, winner, game, summary));
        }
        else {
          LOG.warn("Failed to finished " + competition + " properly, unable to resolve scoring from topic.");
        }
      }

      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        discordService.resetCompetition(competition.getDiscordChannelId());
      }
    }
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      popperService.deAugmentWheel(game);

      if (competition.getDiscordChannelId() > 0 && competition.isActive()) {
        long discordChannelId = competition.getDiscordChannelId();
        String message = DiscordChannelMessageFactory.createCompetitionCancelledMessage(competition);
        discordService.sendMessage(discordChannelId, message);
      }

      if (competition.getType().equals(CompetitionType.DISCORD.name())) {
        discordService.resetCompetition(competition.getDiscordChannelId());
      }
    }
  }

  @Override
  public void competitionChanged(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game != null) {
      if (competition.getBadge() != null) {
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
