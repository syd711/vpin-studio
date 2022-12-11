package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.connectors.discord.DiscordWebhook;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionChangeListener;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService implements InitializingBean, HighscoreChangeListener, CompetitionChangeListener {
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

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    try {
      Game game = event.getGame();
      cardService.generateCard(game, false);
    } catch (Exception e) {
      LOG.error("Error updating card after highscore change event: " + e.getMessage(), e);
    }
  }

  @Override
  public void competitionCreated(Competition competition) {
    if (competition.isDiscordNotifications() && competition.isActive()) {
      String webhookUrl = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_WEBHOOK_URL);
      if (!StringUtils.isEmpty(webhookUrl)) {
        Game game = gameService.getGame(competition.getGameId());
        String message = NotificationFactory.createDiscordCompetitionCreatedMessage(competition, game);
        DiscordWebhook.call(webhookUrl, message);
        LOG.info("Called Discord webhook for creation of " + competition);
        if(competition.isCustomizeMedia()) {
          popperService.augmentWheel(game, competition.getBadge());
        }
      }
    }
  }

  @Override
  public void competitionFinished(Competition competition) {
    if (competition.isDiscordNotifications()) {
      String webhookUrl = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_WEBHOOK_URL);
      if (!StringUtils.isEmpty(webhookUrl)) {
        Game game = gameService.getGame(competition.getGameId());
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
  }

  @Override
  public void competitionDeleted(Competition competition) {
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
    String winner = competition.getWinnerInitials();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    highscoreService.addHighscoreChangeListener(this);
    competitionService.addCompetitionChangeListener(this);
  }
}
