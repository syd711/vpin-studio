package de.mephisto.vpin.server.webhooks;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.util.NetworkUtil;
import de.mephisto.vpin.restclient.webhooks.*;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WebhooksService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(WebhooksService.class);

  @Autowired
  private PreferencesService preferencesService;

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

  public void notifyPlayerHooks(int playerId, @NonNull WebhookEventType eventType) {
    List<WebhookSet> sets = webhookSettings.getSets();
    for (WebhookSet set : sets) {
      handleWebhookSet(set, set.getPlayers(), WebhookType.player, eventType, playerId);
    }
  }

  private void handleWebhookSet(@NonNull WebhookSet webhookSet, @NonNull Webhook webhook, @NonNull WebhookType webhookType, @NotNull WebhookEventType eventType, int entityId) {
    if (!NetworkUtil.isValidUrl(webhook.getEndpoint())) {
      LOG.info("{} / {} not fired, no valid endpoint set.", webhookSet.getName(), eventType.name());
      return;
    }

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
  }

  public WebhookSet save(@NonNull WebhookSet webhookSet) throws Exception {
    try {
      List<WebhookSet> collect = new ArrayList<>(webhookSettings.getSets().stream().filter(s -> s.getUuid().equals(webhookSet.getUuid())).collect(Collectors.toList()));
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

  @Override
  public void afterPropertiesSet() throws Exception {
    webhooksRestClient = new WebhooksRestClient();
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.WEBHOOK_SETTINGS, null, null);
  }
}
