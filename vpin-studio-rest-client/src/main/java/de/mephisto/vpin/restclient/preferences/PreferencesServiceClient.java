package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

/*********************************************************************************************************************
 * Preferences
 ********************************************************************************************************************/
public class PreferencesServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  private Map<String, Object> jsonSettingsCache = new HashMap<>();

  public PreferencesServiceClient(VPinStudioClient client) {
    super(client);
  }

  private List<PreferenceChangeListener> listeners = new ArrayList<>();

  public void addListener(PreferenceChangeListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(PreferenceChangeListener listener) {
    this.listeners.remove(listener);
  }

  public PreferenceEntryRepresentation getPreference(String key) {
    return getRestClient().get(API + "preferences/" + key, PreferenceEntryRepresentation.class);
  }

  @SuppressWarnings("unchecked")
  public <T> T getJsonPreference(String key, Class<T> clazz) {
    if (!jsonSettingsCache.containsKey(key)) {
      try {
        T settings = getRestClient().get(API + "preferences/json/" + key, clazz);
        jsonSettingsCache.put(key, settings);
      }
      catch (Exception e) {
        LOG.error("Failed to load json preferences " + key + ": " + e.getMessage());
      }
    }
    return (T) jsonSettingsCache.get(key);
  }

  public synchronized boolean setJsonPreference(JsonSettings settings) {
    return setJsonPreference(settings, false);
  }

  public boolean setJsonPreference(JsonSettings settings, boolean silent) {
    try {
      Map<String, Object> data = new HashMap<>();
      data.put("data", settings.toJson());
      boolean result = getRestClient().put(API + "preferences/json/" + settings.getSettingsName(), data);
      jsonSettingsCache.remove(settings.getSettingsName());
      if (!silent) {
        notifyPreferenceChange(settings.getSettingsName(), settings);
      }
      return result;
    }
    catch (Exception e) {
      LOG.error("Failed to set json preferences: " + e.getMessage(), e);
    }
    return false;
  }

  public boolean setPreferences(Map<String, Object> values) {
    try {
      boolean result = getRestClient().put(API + "preferences", values);
      if (result) {
        Set<Map.Entry<String, Object>> entries = values.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
          notifyPreferenceChange(entry.getKey(), entry.getValue());
        }
      }
      return result;
    }
    catch (Exception e) {
      LOG.error("Failed to set preferences: " + e.getMessage(), e);
    }
    return false;
  }

  public void notifyPreferenceChange(String key, Object value) {
    listeners.stream().forEach(listener -> {
      listener.preferencesChanged(key, value);
    });
  }

  public boolean setPreference(String key, Object value) {
    if (value instanceof JsonSettings) {
      throw new UnsupportedOperationException("Use setJsonPreference for JSON settings");
    }

    try {
      Map<String, Object> values = new HashMap<>();
      values.put(key, value);
      return setPreferences(values);
    }
    catch (Exception e) {
      LOG.error("Failed to set preference: " + e.getMessage(), e);
    }
    return false;
  }

  public boolean uploadVPinAvatar(File file) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "preferences/avatar";
      HttpEntity upload = createUpload(file, -1, null, AssetType.VPIN_AVATAR, null);
      new RestTemplate().exchange(url, HttpMethod.POST, upload, Boolean.class);
      finalizeUpload(upload);
      return true;
    }
    catch (Exception e) {
      LOG.error("Background upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public void clearCache() {
    this.jsonSettingsCache.clear();
  }

  public void clearCache(String name) {
    if (jsonSettingsCache.containsKey(name)) {
      this.jsonSettingsCache.remove(name);
    }
  }
}
