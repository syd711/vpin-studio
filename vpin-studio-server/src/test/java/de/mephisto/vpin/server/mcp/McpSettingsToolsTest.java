package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class McpSettingsToolsTest {

  @Mock
  private PreferencesService preferencesService;

  @InjectMocks
  private McpSettingsTools tools;

  private final NotificationSettings notifications = new NotificationSettings();
  private final ManiaSettings mania = new ManiaSettings();

  @BeforeEach
  void setUp() {
    mania.setApiKey("secret-key");
    when(preferencesService.getJsonPreference(eq(PreferenceNames.NOTIFICATION_SETTINGS), any())).thenReturn(notifications);
    when(preferencesService.getJsonPreference(eq(PreferenceNames.MANIA_SETTINGS), any())).thenReturn(mania);
  }

  @Test
  void neverOffersOwnOrAuthenticationSettings() {
    assertThat(McpSettingsTools.SETTINGS).doesNotContainKeys(PreferenceNames.MCP_SETTINGS, PreferenceNames.AUTHENTICATION_SETTINGS);
    assertThatThrownBy(() -> call("get_settings", Map.of("key", PreferenceNames.MCP_SETTINGS)))
        .isInstanceOf(McpToolException.class).hasMessageContaining("not available");
    assertThatThrownBy(() -> call("update_settings", Map.of("key", PreferenceNames.AUTHENTICATION_SETTINGS, "changes", Map.of("token", "x"))))
        .isInstanceOf(McpToolException.class).hasMessageContaining("not available");
  }

  @Test
  void everyOfferedKeyHasASettingsClass() {
    for (String key : McpSettingsTools.SETTINGS.keySet()) {
      assertThat(PreferenceNames.getClassFromKey(key)).as(key).isNotNull();
    }
  }

  @Test
  void getMasksSecrets() {
    String json = String.valueOf(call("get_settings", Map.of("key", PreferenceNames.MANIA_SETTINGS)));
    assertThat(json).doesNotContain("secret-key").contains(McpSecrets.MASK);
  }

  @Test
  void dryRunDoesNotSave() throws Exception {
    boolean before = notifications.isStartupNotification();
    Map<?, ?> result = (Map<?, ?>) call("update_settings", Map.of("key", PreferenceNames.NOTIFICATION_SETTINGS,
        "changes", Map.of("startupNotification", !before), "dryRun", true));
    assertThat((List<?>) result.get("changes")).hasSize(1);
    verify(preferencesService, never()).savePreference(any(JsonSettings.class));
  }

  @Test
  void updateMergesAndSaves() throws Exception {
    boolean before = notifications.isStartupNotification();
    int duration = notifications.getDurationSec();
    call("update_settings", Map.of("key", PreferenceNames.NOTIFICATION_SETTINGS, "changes", Map.of("startupNotification", !before)));

    ArgumentCaptor<JsonSettings> saved = ArgumentCaptor.forClass(JsonSettings.class);
    verify(preferencesService).savePreference(saved.capture());
    NotificationSettings updated = (NotificationSettings) saved.getValue();
    assertThat(updated.isStartupNotification()).isEqualTo(!before);
    assertThat(updated.getDurationSec()).isEqualTo(duration);
  }

  @Test
  void rejectsUnknownFields() {
    assertThatThrownBy(() -> call("update_settings", Map.of("key", PreferenceNames.NOTIFICATION_SETTINGS, "changes", Map.of("nope", true))))
        .isInstanceOf(McpToolException.class).hasMessageContaining("Unknown fields [nope]");
  }

  @Test
  void rejectsSecretChanges() throws Exception {
    assertThatThrownBy(() -> call("update_settings", Map.of("key", PreferenceNames.MANIA_SETTINGS, "changes", Map.of("apiKey", "stolen"))))
        .isInstanceOf(McpToolException.class).hasMessageContaining("[apiKey]");
    assertThatThrownBy(() -> call("update_settings", Map.of("key", PreferenceNames.MANIA_SETTINGS, "changes", Map.of("apiKey", McpSecrets.MASK))))
        .isInstanceOf(McpToolException.class).hasMessageContaining("[apiKey]");
    verify(preferencesService, never()).savePreference(any(JsonSettings.class));
  }

  @Test
  void otherFieldsOfSettingsWithSecretsCanBeChanged() throws Exception {
    boolean before = mania.isSubmitRatings();
    call("update_settings", Map.of("key", PreferenceNames.MANIA_SETTINGS, "changes", Map.of("submitRatings", !before)));
    ArgumentCaptor<JsonSettings> saved = ArgumentCaptor.forClass(JsonSettings.class);
    verify(preferencesService).savePreference(saved.capture());
    ManiaSettings updated = (ManiaSettings) saved.getValue();
    assertThat(updated.getApiKey()).isEqualTo("secret-key");
    assertThat(updated.isSubmitRatings()).isEqualTo(!before);
  }

  private Object call(String name, Map<String, Object> arguments) {
    McpTool tool = tools.getTools().stream().filter(t -> t.name().equals(name)).findFirst().orElseThrow();
    return tool.handler().apply(new McpArguments(arguments));
  }
}
