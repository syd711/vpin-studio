package de.mephisto.vpin.server.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PreferencesServiceTest extends AbstractVPinServerTest {

  @Autowired
  private PreferencesService preferencesService;

  @BeforeAll
  public void setup() {
    setupSystem();
  }

  @Test
  public void testGetPreferenceValue() {
    // systemName is a valid bean property on Preferences
    Object value = preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_NAME);
    // may be null if never set, but method should not throw
  }

  @Test
  public void testGetPreferenceValueWithDefault() {
    // use a valid property that may not be set
    Object value = preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_NAME, "default");
    assertNotNull(value);
  }

  @Test
  public void testSavePreference() throws Exception {
    // save and read back a valid bean property
    String originalValue = (String) preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_NAME);

    try {
      boolean saved = preferencesService.savePreference(PreferenceNames.SYSTEM_NAME, "TestSystemName", true);
      assertTrue(saved);

      Object retrieved = preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_NAME);
      assertEquals("TestSystemName", retrieved);
    }
    finally {
      // restore original value
      preferencesService.savePreference(PreferenceNames.SYSTEM_NAME, originalValue != null ? originalValue : "", true);
    }
  }

  @Test
  public void testSavePreferenceMap() throws Exception {
    String originalSystemName = (String) preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_NAME);
    String originalWebhook = (String) preferencesService.getPreferenceValue("discordWebHookUrl");

    try {
      Map<String, Object> values = new HashMap<>();
      values.put(PreferenceNames.SYSTEM_NAME, "MapTestSystem");
      values.put("discordWebHookUrl", "http://test.webhook.url");

      boolean saved = preferencesService.savePreferenceMap(values, true);
      assertTrue(saved);

      assertEquals("MapTestSystem", preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_NAME));
      assertEquals("http://test.webhook.url", preferencesService.getPreferenceValue("discordWebHookUrl"));
    }
    finally {
      Map<String, Object> cleanup = new HashMap<>();
      cleanup.put(PreferenceNames.SYSTEM_NAME, originalSystemName != null ? originalSystemName : "");
      cleanup.put("discordWebHookUrl", originalWebhook != null ? originalWebhook : "");
      preferencesService.savePreferenceMap(cleanup, true);
    }
  }

  @Test
  public void testGetPreferences() {
    Preferences prefs = preferencesService.getPreferences();
    assertNotNull(prefs);
  }

  @Test
  public void testGetPreferenceValueLong() {
    // idleTimeout is a valid int property on Preferences
    long value = preferencesService.getPreferenceValueLong(PreferenceNames.IDLE_TIMEOUT, 42L);
    // should return existing value or the default
    assertTrue(value >= 0 || value == 42L);
  }
}
