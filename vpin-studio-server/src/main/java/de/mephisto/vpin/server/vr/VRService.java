package de.mephisto.vpin.server.vr;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.preferences.VRSettings;
import de.mephisto.vpin.restclient.vr.VRFilesInfo;
import de.mephisto.vpin.server.dmd.DMDDeviceIniService;
import de.mephisto.vpin.server.emulators.EmulatorChangeListener;
import de.mephisto.vpin.server.emulators.EmulatorDetailsService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpx.VPXService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;

@Service
public class VRService implements InitializingBean, PreferenceChangedListener, EmulatorChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private EmulatorDetailsService emulatorDetailsService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private VPXService vpxService;

  @Autowired
  private DMDDeviceIniService dmdDeviceIniService;

  private VRSettings vrSettings;

  public boolean toggleVRMode() {
    if (vrSettings.isEnabled()) {
      return false;
    }

    try {
      LOG.info("Toggling VR Mode: " + !vrSettings.isVrEnabled());
      vrSettings.setVrEnabled(!vrSettings.isVrEnabled());
      preferencesService.savePreference(vrSettings);
      onVRToggle();
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
    GameEmulatorScript vrLaunchScript = emulatorDetailsService.getGameEmulatorVRLaunchScript(emulatorId);
    GameEmulator gameEmulator = emulatorService.getGameEmulator(emulatorId);
    GameEmulatorScript launchScript = gameEmulator.getLaunchScript();

    if (vrLaunchScript == null && launchScript != null) {
      vrLaunchScript = emulatorDetailsService.cloneScript(launchScript);
    }
    return vrLaunchScript;
  }

  public File getVRResourcesFolder(int emulatorId) {
    File resources = new File(RESOURCES, "vr/" + emulatorId + "/");
    resources.mkdirs();
    return resources;
  }

  public GameEmulatorScript saveVRLaunchScript(int emulatorId, GameEmulatorScript script) {
    return emulatorDetailsService.saveEmulatorVRLaunchScript(emulatorId, script);
  }

  private void toggleEmulator(GameEmulator gameEmulator) {
    boolean enabled = vrSettings.isVrEnabled();
    if (gameEmulator.isVpxEmulator()) {
      toggleEmulatorScripts(gameEmulator, enabled);
      toggleVRFiles(gameEmulator, enabled);
    }
  }

  private void toggleVRFiles(GameEmulator gameEmulator, boolean enabled) {
    VRFilesInfo vrFiles = getVRFiles(gameEmulator.getId());
    if (vrFiles == null) {
      return;
    }
    toggleFile(gameEmulator, vrFiles.getDmdDeviceIniFile(), vrFiles.getDmdDeviceIniVrFile(), enabled);
    toggleFile(gameEmulator, vrFiles.getvPinballXIniFile(), vrFiles.getvPinballXIniVrFile(), enabled);
  }

  /**
   * Toggles a single config file between its original and VR version.
   *
   * @param originalFile the file used at runtime (e.g. dmddevice.ini)
   * @param vrSourceFile the VR replacement stored in resources/vr/<emulatorId>/
   * @param enable       true  → backup original and put VR file in place
   *                     false → remove VR file and restore original backup
   */
  private void toggleFile(GameEmulator emulator, File originalFile, File vrSourceFile, boolean enable) {
    if (originalFile == null) {
      return;
    }
    if (enable) {
      enableVRFile(emulator, originalFile, vrSourceFile);
    }
    else {
      disableVRFile(emulator, originalFile);
    }
  }

  private void enableVRFile(GameEmulator emulator, File originalFile, File vrSourceFile) {
    File vrFile = new File(getVRResourcesFolder(emulator.getId()), originalFile.getName());
    File backup = new File(originalFile.getAbsolutePath() + ".vr");

    if (originalFile.exists() && vrFile.exists()) {
      if (backup.exists()) {
        return;
      }

      if (!originalFile.renameTo(backup)) {
        LOG.error("Failed to rename {} to {}", originalFile.getAbsolutePath(), backup.getAbsolutePath());
        return;
      }
      LOG.info("Backed up {} → {}", originalFile.getAbsolutePath(), backup.getAbsolutePath());
      try {
        FileUtils.copyFile(vrSourceFile, originalFile);
        LOG.info("Copied VR file {} → {}", vrSourceFile.getAbsolutePath(), originalFile.getAbsolutePath());
      }
      catch (Exception e) {
        LOG.error("Failed to copy VR file {} to {}: {}", vrSourceFile.getAbsolutePath(), originalFile.getAbsolutePath(), e.getMessage(), e);
      }
    }
  }

  private void disableVRFile(GameEmulator emulator, File originalFile) {
    File backup = new File(originalFile.getAbsolutePath() + ".vr");
    if (backup.exists() && originalFile.exists()) {
      // Remove the currently active (VR) file so we can restore the backup
      if (!originalFile.delete()) {
        LOG.error("Failed to delete active VR file {}", originalFile.getAbsolutePath());
        return;
      }
      LOG.info("Removed active VR file {}", originalFile.getAbsolutePath());
      if (!backup.renameTo(originalFile)) {
        LOG.error("Failed to restore {} → {}", backup.getAbsolutePath(), originalFile.getAbsolutePath());
      }
      else {
        LOG.info("Restored {} → {}", backup.getAbsolutePath(), originalFile.getAbsolutePath());
      }
    }
  }

  private void toggleEmulatorScripts(GameEmulator gameEmulator, boolean enabled) {
    GameEmulatorScript originalLaunchScript = emulatorDetailsService.getGameEmulatorLaunchScript(gameEmulator.getId());
    GameEmulatorScript vrLaunchScript = emulatorDetailsService.getGameEmulatorVRLaunchScript(gameEmulator.getId());

    //initialize VR script
    if (vrLaunchScript == null && originalLaunchScript != null) {
      vrLaunchScript = emulatorDetailsService.cloneScript(originalLaunchScript);
    }

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

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.VR_SETTINGS.equals(propertyName)) {
      vrSettings = preferencesService.getJsonPreference(PreferenceNames.VR_SETTINGS, VRSettings.class);
    }
  }

  public VRFilesInfo getVRFiles(int emulatorId) {
    GameEmulator gameEmulator = emulatorService.getGameEmulator(emulatorId);
    if (gameEmulator != null && gameEmulator.isVpxEmulator()) {
      VRFilesInfo info = new VRFilesInfo();

      //dmd deviceini
      File dmdDeviceIniFile = dmdDeviceIniService.getDmdDeviceIniFile(gameEmulator);
      File dmdDeviceIniFileVr = new File(getVRResourcesFolder(emulatorId), dmdDeviceIniFile.getName());
      info.setDmdDeviceIni(dmdDeviceIniFile.getAbsolutePath());
      info.setDmdDeviceIniVr(dmdDeviceIniFileVr.getAbsolutePath());
      if (dmdDeviceIniFileVr.exists()) {
        info.setDmdDeviceIniVrFile(dmdDeviceIniFileVr);
      }
      if (dmdDeviceIniFile.exists()) {
        info.setDmdDeviceIniFile(dmdDeviceIniFile);
      }

      //vpinballx.ini
      File vpxIniFile = vpxService.getVPXFile();
      File vpxIniFileVr = new File(getVRResourcesFolder(emulatorId), vpxIniFile.getName());
      info.setvPinballXIni(vpxIniFile.getAbsolutePath());
      info.setvPinballXIniVr(vpxIniFileVr.getAbsolutePath());
      if (vpxIniFileVr.exists()) {
        info.setvPinballXIniVrFile(vpxIniFileVr);
      }
      if (vpxIniFile.exists()) {
        info.setvPinballXIniFile(vpxIniFile);
      }
      return info;
    }
    return null;
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
