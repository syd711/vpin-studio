package de.mephisto.vpin.server.preferences;

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

  public Preferences getPreferences() {
    return preferences;
  }

  public Object getPreferenceValue(String key) {
    BeanWrapper bean = new BeanWrapperImpl(preferences);
    return bean.getPropertyValue(key);
  }

  public boolean savePreference(Map<String, Object> values) {
    BeanWrapper bean = new BeanWrapperImpl(preferences);
    Set<Map.Entry<String, Object>> entries = values.entrySet();
    for (Map.Entry<String, Object> entry : entries) {
      bean.setPropertyValue(entry.getKey(), entry.getValue());
    }
    preferencesRepository.saveAndFlush(preferences);
    LOG.info("Saved preferences " + values);
    return true;
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

  public String saveAvatar(byte[] bytes, String mimeType) {
    Asset newAvatar = new Asset();
    newAvatar.setUuid(UUID.randomUUID().toString());

    String avatarUuid = preferences.getAvatarUuid();
    Optional<Asset> byUuid = assetRepository.findByUuid(avatarUuid);
    if(byUuid.isPresent()) {
      newAvatar = byUuid.get();
    }

    newAvatar.setData(bytes);
    newAvatar.setMimeType(mimeType);
    LOG.info("Updated asset " + newAvatar);

    Asset asset = assetRepository.saveAndFlush(newAvatar);
    preferences.setAvatarUuid(asset.getUuid());
    preferencesRepository.saveAndFlush(preferences);
    LOG.info("Updates avatar in preferences.");
    return asset.getUuid();
  }
}
