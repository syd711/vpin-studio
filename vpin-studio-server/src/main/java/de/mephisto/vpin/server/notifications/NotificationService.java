package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.connectors.discord.DiscordWebhook;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionChangeListener;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.TableStatusChangeListener;
import de.mephisto.vpin.server.popper.TableStatusChangedEvent;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService implements InitializingBean, HighscoreChangeListener, CompetitionChangeListener, TableStatusChangeListener {
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

  public void notifyPopperRestart() {
    discordService.setStatus(null);
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    Game game = event.getGame();
    discordService.setStatus(game.getGameDisplayName());
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
      String message = NotificationFactory.createDiscordHighscoreMessage(event);
      DiscordWebhook.call(webhookUrl, message);
      LOG.info("Called Discord webhook for update of score " + event.getNewHighscore());
    }
  }

  @Override
  public void competitionCreated(Competition competition) {
    Game game = gameService.getGame(competition.getGameId());

    if (competition.isCustomizeMedia()) {
      popperService.augmentWheel(game, competition.getBadge());
    }

    if (competition.isDiscordNotifications() && competition.isActive()) {
      String webhookUrl = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_WEBHOOK_URL);
      if (!StringUtils.isEmpty(webhookUrl)) {
        String message = NotificationFactory.createDiscordCompetitionCreatedMessage(competition, game);
        DiscordWebhook.call(webhookUrl, message);
        LOG.info("Called Discord webhook for creation of " + competition);
      }
    }
  }

  @Override
  public void competitionFinished(Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    popperService.deAugmentWheel(game);

    if (competition.isDiscordNotifications()) {
      String webhookUrl = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_WEBHOOK_URL);
      if (!StringUtils.isEmpty(webhookUrl)) {
        ScoreSummary summary = highscoreService.getHighscores(competition.getGameId());

        if (!summary.getScores().isEmpty()) {
          String message = NotificationFactory.createDiscordCompetitionFinishedMessage(competition, game, summary);
          DiscordWebhook.call(webhookUrl, message);
          LOG.info("Called Discord webhook for completion of " + competition);
        }
        else {
          LOG.warn("Skipped calling Discord webhook for completion of " + competition + ", game has no highscore.");
        }
      }
    }
    runAugmentationCheck();
  }

  @Override
  public void competitionDeleted(Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    popperService.deAugmentWheel(game);

    if (competition.isDiscordNotifications() && competition.isActive()) {
      String webhookUrl = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_WEBHOOK_URL);
      if (!StringUtils.isEmpty(webhookUrl)) {
        String message = NotificationFactory.createDiscordCompetitionCancelledMessage(competition);
        DiscordWebhook.call(webhookUrl, message);
        LOG.info("Called Discord webhook for cancellation of " + competition);
      }
    }
  }

  @Override
  public void competitionChanged(Competition competition) {
    Game game = gameService.getGame(competition.getGameId());

    boolean customizeMedia = competition.isCustomizeMedia();
    if (customizeMedia) {
      popperService.augmentWheel(game, competition.getBadge());
    }
    else {
      popperService.deAugmentWheel(game);
    }
    runAugmentationCheck();
  }

  private void runAugmentationCheck() {
    List<Integer> competedGameIds = competitionService.getActiveCompetitions().stream().map(Competition::getGameId).collect(Collectors.toList());

    List<Game> games = gameService.getGames();
    for (Game game : games) {
      if(!competedGameIds.contains(game.getId())) {
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
  }
}
