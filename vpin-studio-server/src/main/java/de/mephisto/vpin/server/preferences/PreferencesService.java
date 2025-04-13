package de.mephisto.vpin.server.preferences;

import de.mephisto.vpin.commons.utils.WinRegistry;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.doflinx.DOFLinxSettings;
import de.mephisto.vpin.restclient.frontend.pinballx.PinballXSettings;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.monitor.MonitoringSettings;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.restclient.preferences.BackupSettings;
import de.mephisto.vpin.restclient.preferences.OverlaySettings;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.validation.IgnoredValidationSettings;
import de.mephisto.vpin.restclient.validation.ValidationSettings;
import de.mephisto.vpin.restclient.vpf.VPFSettings;
import de.mephisto.vpin.restclient.vpu.VPUSettings;
import de.mephisto.vpin.restclient.webhooks.WebhookSettings;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetRepository;
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

  public synchronized boolean savePreference(Map<String, Object> values) throws Exception {
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
    notifyChangeListeners(values, oldValues);
    return true;
  }

  public boolean savePreference(String key, Object value) throws Exception {
    Map<String, Object> values = new HashMap<>();
    values.put(key, value);
    return savePreference(values);
  }

  public boolean savePreference(String key, JsonSettings value) throws Exception {
    if (value != null) {
      String json = value.toJson();
      return savePreference(key, json);
    }

    Map<String, Object> values = new HashMap<>();
    values.put(key, value);
    return savePreference(key, values);
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
  public <T> T getJsonPreference(String key) {
    try {
      switch (key) {
        case PreferenceNames.UI_SETTINGS: {
          return (T) getJsonPreference(key, UISettings.class);
        }
        case PreferenceNames.SERVER_SETTINGS: {
          return (T) getJsonPreference(key, ServerSettings.class);
        }
        case PreferenceNames.HIGHSCORE_CARD_SETTINGS: {
          return (T) getJsonPreference(key, CardSettings.class);
        }
        case PreferenceNames.MANIA_SETTINGS: {
          return (T) getJsonPreference(key, ManiaSettings.class);
        }
        case PreferenceNames.DOF_SETTINGS: {
          return (T) getJsonPreference(key, DOFSettings.class);
        }
        case PreferenceNames.DOFLINX_SETTINGS: {
          return (T) getJsonPreference(key, DOFLinxSettings.class);
        }
        case PreferenceNames.PAUSE_MENU_SETTINGS: {
          return (T) getJsonPreference(key, PauseMenuSettings.class);
        }
        case PreferenceNames.VALIDATION_SETTINGS: {
          return (T) getJsonPreference(key, ValidationSettings.class);
        }
        case PreferenceNames.IGNORED_VALIDATION_SETTINGS: {
          return (T) getJsonPreference(key, IgnoredValidationSettings.class);
        }
        case PreferenceNames.NOTIFICATION_SETTINGS: {
          return (T) getJsonPreference(key, NotificationSettings.class);
        }
        case PreferenceNames.PINBALLX_SETTINGS: {
          return (T) getJsonPreference(key, PinballXSettings.class);
        }
        case PreferenceNames.FILTER_SETTINGS: {
          return (T) getJsonPreference(key, FilterSettings.class);
        }
        case PreferenceNames.VPU_SETTINGS: {
          return (T) getJsonPreference(key, VPUSettings.class);
        }
        case PreferenceNames.OVERLAY_SETTINGS: {
          return (T) getJsonPreference(key, OverlaySettings.class);
        }
        case PreferenceNames.VPF_SETTINGS: {
          return (T) getJsonPreference(key, VPFSettings.class);
        }
        case PreferenceNames.BACKUP_SETTINGS: {
          return (T) getJsonPreference(key, BackupSettings.class);
        }
        case PreferenceNames.RECORDER_SETTINGS: {
          return (T) getJsonPreference(key, RecorderSettings.class);
        }
        case PreferenceNames.MONITORING_SETTINGS: {
          return (T) getJsonPreference(key, MonitoringSettings.class);
        }
        case PreferenceNames.WEBHOOK_SETTINGS: {
          return (T) getJsonPreference(key, WebhookSettings.class);
        }
        case PreferenceNames.ISCORED_SETTINGS: {
          return (T) getJsonPreference(key, IScoredSettings.class);
        }
        default: {
          throw new UnsupportedOperationException("JSON format not supported for preference '" + key + "'");
        }
      }
    }
    catch(ClassCastException cce) {
      throw new UnsupportedOperationException("Cannot return settings in expected format for preference '" + key + "'");
    }
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
      boolean stickyKeysEnabled = WinRegistry.isStickyKeysEnabled();
      if (stickyKeysEnabled && !serverSettings.isStickyKeysEnabled()) {
        serverSettings.setStickyKeysEnabled(true);
        savePreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
      }
      if (!stickyKeysEnabled && serverSettings.isStickyKeysEnabled()) {
        serverSettings.setStickyKeysEnabled(false);
        savePreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
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
        WinRegistry.setStickyKeysEnabled(serverSettings.isStickyKeysEnabled());
      }
    }
    catch (Exception e) {
      LOG.error("Preferences update failed: {}", e.getMessage(), e);
    }
  }
}
