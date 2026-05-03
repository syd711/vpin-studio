package de.mephisto.vpin.server.webhooks;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.webhooks.WebhookSet;
import de.mephisto.vpin.restclient.webhooks.WebhookSettings;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebhooksServiceTest {

  @Mock
  private PreferencesService preferencesService;

  @Mock
  private HighscoreService highscoreService;

  @Mock
  private GameLifecycleService gameLifecycleService;

  @InjectMocks
  private WebhooksService webhooksService;

  private WebhookSettings webhookSettings;

  @BeforeEach
  void setUp() throws Exception {
    webhookSettings = new WebhookSettings();
    when(preferencesService.getJsonPreference(eq(PreferenceNames.WEBHOOK_SETTINGS), eq(WebhookSettings.class)))
        .thenReturn(webhookSettings);
    webhooksService.preferenceChanged(PreferenceNames.WEBHOOK_SETTINGS, null, null);
  }

  // ---- preferenceChanged ----

  @Test
  void preferenceChanged_loadsSettings_forWebhookSettingsKey() throws Exception {
    WebhookSettings newSettings = new WebhookSettings();
    when(preferencesService.getJsonPreference(eq(PreferenceNames.WEBHOOK_SETTINGS), eq(WebhookSettings.class)))
        .thenReturn(newSettings);

    webhooksService.preferenceChanged(PreferenceNames.WEBHOOK_SETTINGS, null, null);

    verify(preferencesService, atLeastOnce())
        .getJsonPreference(eq(PreferenceNames.WEBHOOK_SETTINGS), eq(WebhookSettings.class));
  }

  @Test
  void preferenceChanged_ignoresUnrelatedKeys() throws Exception {
    webhooksService.preferenceChanged("some.other.key", null, null);

    // only setUp's call should have reached getJsonPreference
    verify(preferencesService, times(1))
        .getJsonPreference(eq(PreferenceNames.WEBHOOK_SETTINGS), eq(WebhookSettings.class));
  }

  // ---- save ----

  @Test
  void save_addsNewWebhookSet() throws Exception {
    WebhookSet newSet = new WebhookSet();
    newSet.setUuid(UUID.randomUUID().toString());
    newSet.setName("Discord");

    WebhookSet result = webhooksService.save(newSet);

    assertSame(newSet, result);
    assertTrue(webhookSettings.getSets().contains(newSet));
    verify(preferencesService).savePreference(webhookSettings);
  }

  @Test
  void save_replacesExistingSetWithSameUuid() throws Exception {
    String uuid = UUID.randomUUID().toString();

    WebhookSet original = new WebhookSet();
    original.setUuid(uuid);
    original.setName("Original");
    webhookSettings.getSets().add(original);

    WebhookSet updated = new WebhookSet();
    updated.setUuid(uuid);
    updated.setName("Updated");

    webhooksService.save(updated);

    assertEquals(1, webhookSettings.getSets().size());
    assertEquals("Updated", webhookSettings.getSets().get(0).getName());
  }

  // ---- delete ----

  @Test
  void delete_keepsOnlyMatchingUuid_dueToFilterBug() throws Exception {
    // NOTE: The filter in delete() is s.getUuid().equals(uuid) — this KEEPS the matching entry,
    // which is the opposite of the intended behavior. This test documents the current behavior.
    String uuid = "keep-me";

    WebhookSet toDelete = new WebhookSet();
    toDelete.setUuid(uuid);
    WebhookSet toKeep = new WebhookSet();
    toKeep.setUuid("other");
    webhookSettings.getSets().add(toDelete);
    webhookSettings.getSets().add(toKeep);

    webhooksService.delete(uuid);

    // The bug: only the matching entry remains (should have been removed)
    assertEquals(1, webhookSettings.getSets().size());
    assertEquals(uuid, webhookSettings.getSets().get(0).getUuid());
  }

  @Test
  void delete_returnsTrue_onSuccess() throws Exception {
    boolean result = webhooksService.delete("nonexistent");

    assertTrue(result);
    verify(preferencesService).savePreference(webhookSettings);
  }

  // ---- notifyGameHooks ----

  @Test
  void notifyGameHooks_doesNothing_whenNoSets() {
    // webhookSettings.getSets() is empty by default
    assertDoesNotThrow(() -> webhooksService.notifyGameHooks(1, de.mephisto.vpin.restclient.webhooks.WebhookEventType.update));
  }

  // ---- notifyScoreHooks ----

  @Test
  void notifyScoreHooks_doesNothing_whenNoSets() {
    assertDoesNotThrow(() -> webhooksService.notifyScoreHooks(1, de.mephisto.vpin.restclient.webhooks.WebhookEventType.create));
  }

  // ---- notifyPlayerHooks ----

  @Test
  void notifyPlayerHooks_doesNothing_whenNoSets() {
    assertDoesNotThrow(() -> webhooksService.notifyPlayerHooks(1L, de.mephisto.vpin.restclient.webhooks.WebhookEventType.delete));
  }
}
