package de.mephisto.vpin.server.preferences;

import de.mephisto.vpin.restclient.assets.AssetType;
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
public class PreferencesService implements InitializingBean {
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
    LOG.info("Saved preferences " + values);

    notifyChangeListeners(values, oldValues);
    return true;
  }

  public boolean savePreference(String key, Object value) throws Exception {
    Map<String, Object> values = new HashMap<>();
    values.put(key, value);
    return savePreference(values);
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

  @Override
  public void afterPropertiesSet() {
    List<Preferences> all = preferencesRepository.findAll();
    if (all.isEmpty()) {
      Preferences prefs = new Preferences();
      preferencesRepository.saveAndFlush(prefs);
      all = preferencesRepository.findAll();
    }
    preferences = all.get(0);
  }
}
