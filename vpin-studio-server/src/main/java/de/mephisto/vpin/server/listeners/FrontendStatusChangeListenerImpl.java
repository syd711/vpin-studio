package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.notifications.Notification;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.frontend.FrontendStatusChangeListener;
import de.mephisto.vpin.server.frontend.GameMediaItem;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import de.mephisto.vpin.server.games.TableStatusChangedOrigin;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.notifications.NotificationService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FrontendStatusChangeListenerImpl implements InitializingBean, FrontendStatusChangeListener, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendStatusChangeListenerImpl.class);
  public static final int EXIT_DELAY = 6000;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private NotificationService notificationService;

  private NotificationSettings notificationSettings;

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    Game game = event.getGame();

    boolean pupPackDisabled = pupPacksService.isPupPackDisabled(game);
    if (pupPackDisabled && event.getOrigin().equals(TableStatusChangedOrigin.ORIGIN_POPPER)) {
      pupPacksService.writePUPHideNext(game);
    }

    discordService.setActivity(game.getGameDisplayName());
    highscoreService.scanScore(game);

    try {
      //rescan highscore of last game in case Discord was offline
      int activeTableId = (int) preferencesService.getPreferenceValue(PreferenceNames.ACTIVE_GAME);
      if (activeTableId >= 0) {
        Game lastGamePlayed = gameService.getGame(activeTableId);
        if (lastGamePlayed != null && lastGamePlayed.getId() != game.getId()) {
          highscoreService.scanScore(game);
        }
        preferencesService.savePreference(PreferenceNames.ACTIVE_GAME, game.getId());
      }
    }
    catch (Exception e) {
      LOG.info("Failed to refresh game: " + e.getMessage(), e);
    }

    //this only works for Popper since for other methods VPX is already running
    if (event.getOrigin().equals(TableStatusChangedOrigin.ORIGIN_POPPER)) {
      showTableStartupScreens(game);
    }
  }

  private void showTableStartupScreens(Game game) {
    try {
      PreferenceEntryRepresentation preference = ServerFX.client.getPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS);
      String value = preference.getValue();
      CardSettings cardSettings = JsonSettings.fromJson(CardSettings.class, value);

      String popperScreen = cardSettings.getPopperScreen();
      if (popperScreen != null && !popperScreen.isEmpty()) {
        VPinScreen screen = VPinScreen.valueOf(popperScreen);
        GameMediaItem defaultMediaItem = game.getGameMedia().getDefaultMediaItem(screen);
        if (defaultMediaItem != null && defaultMediaItem.getFile().exists()) {
          Platform.runLater(() -> {
            FrontendPlayerDisplay pupPlayerDisplay = null;
            if (cardSettings.isNotificationOnPopperScreen()) {
              pupPlayerDisplay = frontendStatusService.getPupPlayerDisplay(screen);
            }
            ServerFX.getInstance().showHighscoreCard(cardSettings, pupPlayerDisplay, defaultMediaItem.getMimeType(), defaultMediaItem.getFile());
          });
        }
      }
    }
    catch (Exception e) {
      LOG.info("Error showing highscore card: " + e.getMessage(), e);
    }
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    Game game = event.getGame();
    LOG.info("Executing table exit commands for '" + game + "'");
    discordService.setActivity(null);
    new Thread(() -> {
      LOG.info("Starting " + EXIT_DELAY + "ms update delay before updating highscores.");
      try {
        Thread.sleep(EXIT_DELAY);
      }
      catch (InterruptedException e) {
        //ignore
      }
      LOG.info("Finished " + EXIT_DELAY + "ms update delay, updating highscores.");
      highscoreService.scanScore(game);

      if (notificationSettings.isHighscoreCheckedNotification()) {
        Notification notification = new Notification();
        notification.setImage(game.getWheelImage());
        notification.setTitle1(game.getGameDisplayName());
        notification.setTitle2("Highscore scan finished!");
        notificationService.showNotification(notification);
      }
    }).start();
  }

  @Override
  public void frontendLaunched() {
    LOG.info("Frontend launch event");
    int activeTableId = (int) preferencesService.getPreferenceValue(PreferenceNames.ACTIVE_GAME);
    if (activeTableId >= 0) {
      Game game = gameService.getGame(activeTableId);
      if (game != null) {
        highscoreService.scanScore(game);
      }
    }
  }

  @Override
  public void frontendExited() {
    LOG.info("Frontend exit event");
    discordService.setActivity(null);
  }

  @Override
  public void frontendRestarted() {
    LOG.info("Frontend restarted event");
    discordService.setActivity(null);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    frontendStatusService.addFrontendStatusChangeListener(this);
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.NOTIFICATION_SETTINGS, null, null);
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.NOTIFICATION_SETTINGS)) {
      notificationSettings = preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
    }
  }
}