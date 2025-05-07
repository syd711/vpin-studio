package de.mephisto.vpin.server.webhooks;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.util.NetworkUtil;
import de.mephisto.vpin.restclient.webhooks.*;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleListener;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerLifecycleListener;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WebhooksService implements InitializingBean, PreferenceChangedListener, HighscoreChangeListener, PlayerLifecycleListener, GameLifecycleListener {
  private final static Logger LOG = LoggerFactory.getLogger(WebhooksService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  private WebhooksRestClient webhooksRestClient;
  private WebhookSettings webhookSettings;

  public void notifyGameHooks(int gameId, @NonNull WebhookEventType eventType) {
    List<WebhookSet> sets = webhookSettings.getSets();
    for (WebhookSet set : sets) {
      handleWebhookSet(set, set.getGames(), WebhookType.game, eventType, gameId);
    }
  }

  public void notifyScoreHooks(int scoreId, @NonNull WebhookEventType eventType) {
    List<WebhookSet> sets = webhookSettings.getSets();
    for (WebhookSet set : sets) {
      handleWebhookSet(set, set.getScores(), WebhookType.score, eventType, scoreId);
    }
  }

  public void notifyPlayerHooks(long playerId, @NonNull WebhookEventType eventType) {
    List<WebhookSet> sets = webhookSettings.getSets();
    for (WebhookSet set : sets) {
      LOG.info("Executing webhook set \"{}\" / {}", set, eventType.name());
      handleWebhookSet(set, set.getPlayers(), WebhookType.player, eventType, playerId);
    }
  }

  private void handleWebhookSet(@NonNull WebhookSet webhookSet, @NonNull Webhook webhook, @NonNull WebhookType webhookType, @NonNull WebhookEventType eventType, long entityId) {
    if (!NetworkUtil.isValidUrl(webhook.getEndpoint())) {
      LOG.info("{} / {} not fired, no valid endpoint set.", webhookSet.getName(), eventType.name());
      return;
    }

    new Thread(() -> {
      Thread.currentThread().setName("WebHook Executer");
      webhook.getParameters().put("id", entityId);
      switch (eventType) {
        case update: {
          webhooksRestClient.onUpdate(webhook.getEndpoint(), webhook.getParameters());
          break;
        }
        case delete: {
          String url = webhook.getEndpoint();
          if (!url.endsWith("/")) {
            url = url + "/" + entityId;
          }
          webhooksRestClient.onDelete(url);
          break;
        }
        case create: {
          webhooksRestClient.onCreate(webhook.getEndpoint(), webhook.getParameters());
          break;
        }
      }
    }).start();
  }

  public WebhookSet save(@NonNull WebhookSet webhookSet) throws Exception {
    try {
      List<WebhookSet> collect = new ArrayList<>(webhookSettings.getSets().stream().filter(s -> !s.getUuid().equals(webhookSet.getUuid())).collect(Collectors.toList()));
      collect.add(webhookSet);
      webhookSettings.setSets(collect);
      preferencesService.savePreference(PreferenceNames.WEBHOOK_SETTINGS, webhookSettings);
      return webhookSet;
    }
    catch (Exception e) {
      LOG.error("Saving webhook set failed: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.WEBHOOK_SETTINGS)) {
      webhookSettings = preferencesService.getJsonPreference(PreferenceNames.WEBHOOK_SETTINGS, WebhookSettings.class);
    }
  }

  public boolean delete(String uuid) throws Exception {
    try {
      List<WebhookSet> collect = new ArrayList<>(webhookSettings.getSets().stream().filter(s -> s.getUuid().equals(uuid)).collect(Collectors.toList()));
      webhookSettings.setSets(collect);
      preferencesService.savePreference(PreferenceNames.WEBHOOK_SETTINGS, webhookSettings);
      return true;
    }
    catch (Exception e) {
      LOG.error("Saving webhook set failed: {}", e.getMessage(), e);
      throw e;
    }
  }

  //----------------------------------- Scores Listener ----------------------------------------------------------------

  @Override
  public void highscoreChanged(@NonNull HighscoreChangeEvent event) {
    notifyScoreHooks(event.getGame().getId(), WebhookEventType.update);
  }

  @Override
  public void highscoreUpdated(@NonNull Game game, @NonNull Highscore highscore) {
    //not used
  }
  //----------------------------------- Player Listener ----------------------------------------------------------------

  @Override
  public void playerCreated(@NonNull Player player) {
    notifyPlayerHooks(player.getId(), WebhookEventType.create);
  }

  @Override
  public void playerUpdated(@NonNull Player player) {
    notifyPlayerHooks(player.getId(), WebhookEventType.update);
  }

  @Override
  public void playerDeleted(@NonNull Player player) {
    notifyPlayerHooks(player.getId(), WebhookEventType.delete);
  }
  //----------------------------------- Games Listener  ----------------------------------------------------------------

  @Override
  public void gameCreated(@NonNull Game game) {
    notifyGameHooks(game.getId(), WebhookEventType.create);
  }

  @Override
  public void gameUpdated(@NonNull Game game) {
    notifyGameHooks(game.getId(), WebhookEventType.update);
  }

  @Override
  public void gameDeleted(@NonNull Game game) {
    notifyGameHooks(game.getId(), WebhookEventType.delete);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    webhooksRestClient = new WebhooksRestClient();

    preferencesService.addChangeListener(this);
    gameLifecycleService.addGameLifecycleListener(this);
    highscoreService.addHighscoreChangeListener(this);

    preferenceChanged(PreferenceNames.WEBHOOK_SETTINGS, null, null);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
