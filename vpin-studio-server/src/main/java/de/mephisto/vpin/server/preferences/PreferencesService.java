package de.mephisto.vpin.server.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetRepository;
import de.mephisto.vpin.server.system.SystemService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PreferencesService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(PreferencesService.class);

  @Autowired
  private PreferencesRepository preferencesRepository;

  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private SystemService systemService;

  private Preferences preferences;

  private List<PreferenceChangedListener> listeners = new ArrayList<>();

  public Preferences getPreferences() {
    return preferences;
  }

  public void addChangeListener(PreferenceChangedListener listener) {
    this.listeners.add(listener);
  }

  public void removeChangeListener(PreferenceChangedListener listener) {
    this.listeners.remove(listener);
  }

  public void notifyListeners(String key, Object oldValue, Object newValue) throws Exception {
    for (PreferenceChangedListener listener : this.listeners) {
      listener.preferenceChanged(key, oldValue, newValue);
    }
  }

  public Object getPreferenceValue(String key) {
    BeanWrapper bean = new BeanWrapperImpl(preferences);
    return bean.getPropertyValue(key);
  }

  public Object getPreferenceValue(String key, Object defaultValue) {
    BeanWrapper bean = new BeanWrapperImpl(preferences);
    Object value = bean.getPropertyValue(key);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  public Long getPreferenceValueLong(String key, long defaultValue) {
    BeanWrapper bean = new BeanWrapperImpl(preferences);
    Object value = bean.getPropertyValue(key);
    if (value != null && String.valueOf(value).length() > 0) {
      return Long.parseLong(String.valueOf(value));
    }
    return defaultValue;
  }

  public synchronized boolean savePreferenceMap(Map<String, Object> values) throws Exception {
    return savePreferenceMap(values, false);
  }

  public synchronized boolean savePreferenceMap(Map<String, Object> values, boolean silent) throws Exception {
    BeanWrapper bean = new BeanWrapperImpl(preferences);

    Map<String, Object> oldValues = new HashMap<>();
    Set<Map.Entry<String, Object>> entries = values.entrySet();
    for (Map.Entry<String, Object> entry : entries) {
      String key = entry.getKey();
      Object oldValue = bean.getPropertyValue(key);
      oldValues.put(key, oldValue);
      bean.setPropertyValue(entry.getKey(), entry.getValue());
    }
    preferencesRepository.saveAndFlush(preferences);
    if (!silent) {
      notifyChangeListeners(values, oldValues);
    }
    return true;
  }

  public boolean savePreference(String key, Object value, boolean silent) throws Exception {
    Map<String, Object> values = new HashMap<>();
    values.put(key, value);
    return savePreferenceMap(values, silent);
  }

  public boolean savePreference(JsonSettings value) throws Exception {
    return this.savePreference(value, false);
  }

  public boolean savePreference(JsonSettings value, boolean silent) throws Exception {
    if (value != null) {
      String json = value.toJson();
      return savePreference(value.getSettingsName(), json, silent);
    }

    Map<String, Object> values = new HashMap<>();
    values.put(value.getSettingsName(), value);
    return savePreference(value.getSettingsName(), values, silent);
  }

  public Asset saveAvatar(byte[] bytes, String mimeType) {
    Asset avatar = preferences.getAvatar();
    if (avatar != null) {
      avatar.setData(bytes);
      avatar.setMimeType(mimeType);
      assetRepository.saveAndFlush(avatar);
      return avatar;
    }

    Asset newAvatar = new Asset();
    newAvatar.setAssetType(AssetType.VPIN_AVATAR.name());
    newAvatar.setData(bytes);
    newAvatar.setUuid(UUID.randomUUID().toString());
    newAvatar.setMimeType(mimeType);
    LOG.info("Created asset " + newAvatar);

    Asset asset = assetRepository.saveAndFlush(newAvatar);
    preferences.setAvatar(asset);
    Preferences updatedPreferences = preferencesRepository.saveAndFlush(preferences);
    LOG.info("Updates avatar in preferences.");
    return updatedPreferences.getAvatar();
  }

  private void notifyChangeListeners(Map<String, Object> values, Map<String, Object> oldValues) throws Exception {
    //notify change listeners
    for (Map.Entry<String, Object> entry : oldValues.entrySet()) {
      String key = entry.getKey();
      Object oldValue = entry.getValue();
      Object newValue = values.get(key);
      notifyListeners(key, oldValue, newValue);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends JsonSettings> T getJsonPreference(String key) {
    Class<? extends JsonSettings> clazz = PreferenceNames.getClassFromKey(key);
    return (T) getJsonPreference(key, clazz);
  }

  public <T> T getJsonPreference(String key, Class<T> jsonSettings) {
    try {
      Object preferenceValue = getPreferenceValue(key);
      if (preferenceValue != null) {
        return JsonSettings.fromJson(jsonSettings, (String) preferenceValue);
      }
      return jsonSettings.getDeclaredConstructor().newInstance();
    }
    catch (Exception e) {
      LOG.error("Failed to read JSON preferences: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      List<Preferences> all = preferencesRepository.findAll();
      if (all.isEmpty()) {
        Preferences prefs = new Preferences();
        preferencesRepository.saveAndFlush(prefs);
        all = preferencesRepository.findAll();
      }
      preferences = all.get(0);
    }
    catch (Exception e) {
      LOG.error("Preference Service init failed: {}", e.getMessage(), e);
    }

    try {
      ServerSettings serverSettings = getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
      boolean stickyKeysEnabled = systemService.isStickyKeysEnabled();
      if (stickyKeysEnabled && !serverSettings.isStickyKeysEnabled()) {
        serverSettings.setStickyKeysEnabled(true);
        savePreference(serverSettings);
      }
      if (!stickyKeysEnabled && serverSettings.isStickyKeysEnabled()) {
        serverSettings.setStickyKeysEnabled(false);
        savePreference(serverSettings);
      }
    }
    catch (Exception e) {
      LOG.error("Sticky keys init failed: {}", e.getMessage(), e);
    }

    addChangeListener(this);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    try {
      if (propertyName.equals(PreferenceNames.SERVER_SETTINGS)) {
        ServerSettings serverSettings = getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
        systemService.setStickyKeysEnabled(serverSettings.isStickyKeysEnabled());
        LOG.info("Sticky keys enabled: " + serverSettings.isStickyKeysEnabled());
      }
    }
    catch (Exception e) {
      LOG.error("Preferences update failed: {}", e.getMessage(), e);
    }
  }
}
