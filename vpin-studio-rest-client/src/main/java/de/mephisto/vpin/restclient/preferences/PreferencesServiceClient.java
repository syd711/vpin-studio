package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

/*********************************************************************************************************************
 * Preferences
 ********************************************************************************************************************/
public class PreferencesServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public PreferencesServiceClient(VPinStudioClient client) {
    super(client);
  }

  private List<PreferenceChangeListener> listeners = new ArrayList<>();

  public void addListener(PreferenceChangeListener listener) {
    this.listeners.add(listener);
  }

  public PreferenceEntryRepresentation getPreference(String key) {
    return getRestClient().get(API + "preferences/" + key, PreferenceEntryRepresentation.class);
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
    } catch (Exception e) {
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
    try {
      Map<String, Object> values = new HashMap<>();
      values.put(key, value);
      return setPreferences(values);
    } catch (Exception e) {
      LOG.error("Failed to set preference: " + e.getMessage(), e);
    }
    return false;
  }

  public boolean uploadVPinAvatar(File file) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "preferences/avatar";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.VPIN_AVATAR, null), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Background upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

}
