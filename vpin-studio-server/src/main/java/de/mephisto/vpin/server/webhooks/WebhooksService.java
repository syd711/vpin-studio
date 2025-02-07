package de.mephisto.vpin.server.webhooks;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.util.NetworkUtil;
import de.mephisto.vpin.restclient.webhooks.*;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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
public class WebhooksService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(WebhooksService.class);

  @Autowired
  private PreferencesService preferencesService;

  private WebhooksRestClient webhooksRestClient;

  public void notifyGameHooks(@NonNull Game game, @NonNull WebhookEventType eventType) {
    WebhookSettings webhookSettings = preferencesService.getJsonPreference(PreferenceNames.WEBHOOK_SETTINGS, WebhookSettings.class);
    List<WebhookSet> sets = webhookSettings.getSets();
    for (WebhookSet set : sets) {
      handleWebhookSet(set, set.getGames(), WebhookType.game, eventType, game.getId());
    }
  }

  private void handleWebhookSet(@NonNull WebhookSet webhookSet, @NonNull Webhook webhook, @NonNull WebhookType webhookType, @NotNull WebhookEventType eventType, int entityId) {
    if (!NetworkUtil.isValidUrl(webhook.getEndpoint())) {
      LOG.info("{} / {} not fired, no valid endpoint set.", webhookSet.getName(), eventType.name());
      return;
    }

    switch (webhookType) {

    }
    switch (eventType) {
      case update: {
        webhooksRestClient.onGameUpdate(webhook.getEndpoint(), webhook.getParameters(), entityId);
        break;
      }
      case delete: {
        webhooksRestClient.onGameUpdate(webhook.getEndpoint(), webhook.getParameters(), entityId);
        break;
      }
      case create: {
        webhooksRestClient.onGameCreate(webhook.getEndpoint(), webhook.getParameters(), entityId);
        break;
      }
    }
  }

  public WebhookSet save(@NonNull WebhookSet webhookSet) throws Exception {
    try {
      WebhookSettings webhookSettings = preferencesService.getJsonPreference(PreferenceNames.WEBHOOK_SETTINGS, WebhookSettings.class);
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
  public void afterPropertiesSet() throws Exception {
    webhooksRestClient = new WebhooksRestClient();
  }
}
