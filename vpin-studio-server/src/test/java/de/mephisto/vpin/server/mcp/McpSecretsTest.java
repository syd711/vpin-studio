package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.frontend.pinballx.PinballXSettings;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.vpu.VPUSettings;
import de.mephisto.vpin.restclient.webhooks.Webhook;
import de.mephisto.vpin.restclient.webhooks.WebhookSet;
import de.mephisto.vpin.restclient.webhooks.WebhookSettings;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class McpSecretsTest {

  @Test
  void masksAnnotatedFields() {
    ManiaSettings mania = new ManiaSettings();
    mania.setApiKey("very-secret");
    JsonNode masked = McpSecrets.mask(mania);
    assertThat(masked.get("apiKey").stringValue()).isEqualTo(McpSecrets.MASK);
    assertThat(masked.toString()).doesNotContain("very-secret");
  }

  @Test
  void leavesUnsetSecretsEmpty() {
    JsonNode masked = McpSecrets.mask(new ManiaSettings());
    assertThat(masked.get("apiKey").isNull()).isTrue();
  }

  @Test
  void keepsFlagsReadable() {
    WOVPSettings wovp = new WOVPSettings();
    wovp.setApiKey1("key-1");
    JsonNode masked = McpSecrets.mask(wovp);
    assertThat(masked.get("apiKey1").stringValue()).isEqualTo(McpSecrets.MASK);
    assertThat(masked.get("apiKey1Enabled").booleanValue()).isTrue();
  }

  @Test
  void masksPasswordsAndMail() {
    VPUSettings vpu = new VPUSettings();
    vpu.setLogin("me");
    vpu.setPassword("pw");
    JsonNode masked = McpSecrets.mask(vpu);
    assertThat(masked.get("password").stringValue()).isEqualTo(McpSecrets.MASK);
    assertThat(masked.get("login").stringValue()).isEqualTo("me");

    PinballXSettings pinballX = new PinballXSettings();
    pinballX.setGameExMail("me@example.com");
    pinballX.setGameExPassword("pw");
    masked = McpSecrets.mask(pinballX);
    assertThat(masked.toString()).doesNotContain("me@example.com").doesNotContain("\"pw\"");
  }

  @Test
  void masksNestedWebhooks() {
    WebhookSettings settings = webhooks("https://discord.com/api/webhooks/123/token");
    JsonNode masked = McpSecrets.mask(settings);
    assertThat(masked.toString()).doesNotContain("discord.com").doesNotContain("Bearer");
    assertThat(masked.get("sets").get(0).get("name").stringValue()).isEqualTo("My set");
    assertThat(masked.get("sets").get(0).get("scores").get("endpoint").stringValue()).isEqualTo(McpSecrets.MASK);
  }

  @Test
  void detectsChangedSecrets() {
    WebhookSettings before = webhooks("https://a.example");
    WebhookSettings after = webhooks("https://b.example");
    assertThat(McpSecrets.getChangedSecrets(before, after)).containsExactly("sets[0].scores.endpoint");
    assertThat(McpSecrets.getChangedSecrets(before, webhooks("https://a.example"))).isEmpty();

    ManiaSettings mania = new ManiaSettings();
    ManiaSettings masked = new ManiaSettings();
    masked.setApiKey(McpSecrets.MASK);
    assertThat(McpSecrets.getChangedSecrets(mania, masked)).containsExactly("apiKey");
  }

  @Test
  void namesLookingLikeCredentialsAreSecretWithoutAnnotation() {
    Map<String, Object> value = new HashMap<>();
    value.put("serverToken", "abc");
    value.put("name", "visible");
    UnannotatedSettings settings = new UnannotatedSettings();
    settings.setNested(value);
    settings.setClientSecret("hidden");
    JsonNode masked = McpSecrets.mask(settings);
    assertThat(masked.toString()).doesNotContain("abc").doesNotContain("hidden").contains("visible");
  }

  private static WebhookSettings webhooks(String endpoint) {
    Webhook webhook = new Webhook();
    webhook.setEndpoint(endpoint);
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("Authorization", "Bearer xyz");
    webhook.setParameters(parameters);
    WebhookSet set = new WebhookSet();
    set.setName("My set");
    set.setScores(webhook);
    WebhookSettings settings = new WebhookSettings();
    settings.setSets(List.of(set));
    return settings;
  }

  public static class UnannotatedSettings {
    private String clientSecret;
    private Map<String, Object> nested;

    public String getClientSecret() {
      return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
    }

    public Map<String, Object> getNested() {
      return nested;
    }

    public void setNested(Map<String, Object> nested) {
      this.nested = nested;
    }
  }
}
