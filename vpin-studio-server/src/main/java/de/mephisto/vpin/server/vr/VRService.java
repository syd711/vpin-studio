package de.mephisto.vpin.server.vr;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.preferences.VRSettings;
import de.mephisto.vpin.server.emulators.EmulatorChangeListener;
import de.mephisto.vpin.server.emulators.EmulatorDetailsService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class VRService implements InitializingBean, PreferenceChangedListener, EmulatorChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(VRResource.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private EmulatorDetailsService emulatorDetailsService;

  @Autowired
  private EmulatorService emulatorService;

  private VRSettings vrSettings;

  public boolean toggleVRMode() {
    try {
      vrSettings.setVrEnabled(!vrSettings.isVrEnabled());
      preferencesService.savePreference(vrSettings);
    }
    catch (Exception e) {
      LOG.error("Failed to toggle VR mode: {}", e.getMessage(), e);
    }
    return vrSettings.isVrEnabled();
  }

  private void onVRToggle() {
    Collection<GameEmulator> gameEmulators = new ArrayList<>(emulatorService.getGameEmulators());
    for (GameEmulator gameEmulator : gameEmulators) {
      toggleEmulator(gameEmulator);
    }
    emulatorService.clearCache();
  }

  public GameEmulatorScript getEmulatorVRLaunchScript(int emulatorId) {
    return emulatorDetailsService.getGameEmulatorVRLaunchScript(emulatorId);
  }

  public GameEmulatorScript saveVRLaunchScript(int emulatorId, GameEmulatorScript script) {
    return emulatorDetailsService.saveEmulatorVRLaunchScript(emulatorId, script);
  }

  private void toggleEmulator(GameEmulator gameEmulator) {
    boolean enabled = vrSettings.isEnabled();
    if (gameEmulator.isVpxEmulator()) {
      GameEmulatorScript originalLaunchScript = emulatorDetailsService.getGameEmulatorLaunchScript(gameEmulator.getId());
      GameEmulatorScript vrLaunchScript = emulatorDetailsService.getGameEmulatorVRLaunchScript(gameEmulator.getId());

      if (enabled) {
        if (originalLaunchScript != null && vrLaunchScript != null && !StringUtils.isEmpty(vrLaunchScript.getScript())) {
          emulatorDetailsService.saveEmulatorLaunchScript(gameEmulator.getId(), originalLaunchScript);
          gameEmulator.setLaunchScript(vrLaunchScript);
        }
      }
      else {
        if (originalLaunchScript != null && vrLaunchScript != null && !StringUtils.isEmpty(originalLaunchScript.getScript())) {
          emulatorDetailsService.saveEmulatorVRLaunchScript(gameEmulator.getId(), vrLaunchScript);
          gameEmulator.setLaunchScript(originalLaunchScript);
        }
      }

      emulatorService.removeEmulatorChangeListener(this);
      emulatorService.save(gameEmulator);
      emulatorService.addEmulatorChangeListener(this);
    }
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.VR_SETTINGS.equals(propertyName)) {
      vrSettings = preferencesService.getJsonPreference(PreferenceNames.VR_SETTINGS, VRSettings.class);
      onVRToggle();
    }
  }

  @Override
  public void emulatorChanged(int emulatorId) {
    GameEmulator gameEmulator = emulatorService.getGameEmulator(emulatorId);
//    toggleEmulator(gameEmulator);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    vrSettings = preferencesService.getJsonPreference(PreferenceNames.VR_SETTINGS, VRSettings.class);

    preferencesService.addChangeListener(this);
    emulatorService.addEmulatorChangeListener(this);
  }
}
