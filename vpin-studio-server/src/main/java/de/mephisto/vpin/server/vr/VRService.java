package de.mephisto.vpin.server.vr;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.preferences.VRSettings;
import de.mephisto.vpin.restclient.vr.VRFilesInfo;
import de.mephisto.vpin.server.dmd.DMDDeviceIniService;
import de.mephisto.vpin.server.emulators.EmulatorChangeListener;
import de.mephisto.vpin.server.emulators.EmulatorDetailsService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendStatusChangeListener;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpx.VPXService;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;

@Service
public class VRService implements InitializingBean, PreferenceChangedListener, EmulatorChangeListener, TableStatusChangeListener, FrontendStatusChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final static String TABLE_INI_SWAP_JOURNAL = "vr/active-table-ini-swaps.txt";

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

  @Autowired
  private FrontendStatusService frontendStatusService;

  private VRSettings vrSettings;

  private VRTableIniSwapper tableIniSwapper;

  /**
   * VR mode is active when the VR feature is enabled and the user has toggled into VR.
   */
  public boolean isVRModeActive() {
    return vrSettings != null && vrSettings.isEnabled() && vrSettings.isVrEnabled();
  }

  public boolean toggleVRMode() {
    if (!vrSettings.isEnabled()) {
      LOG.info("Skipped VR mode toggle, the VR mode is not enabled.");
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

    //table ini files are swapped on table launch, leaving VR must not leave any of them behind
    if (!vrSettings.isVrEnabled()) {
      tableIniSwapper.restoreAll();
    }
  }

  public GameEmulatorScript getEmulatorVRLaunchScript(int emulatorId) {
    GameEmulatorScript vrLaunchScript = emulatorDetailsService.getGameEmulatorVRLaunchScript(emulatorId);
    GameEmulator gameEmulator = emulatorService.getGameEmulator(emulatorId);
    if (gameEmulator == null) {
      return null;
    }

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
      LOG.info("Toggling VR mode {} for emulator \"{}\"", enabled ? "on" : "off", gameEmulator.getName());
      toggleEmulatorScripts(gameEmulator, enabled);
      toggleVRFiles(gameEmulator, enabled);
    }
    else {
      LOG.info("Skipped VR mode toggle for emulator \"{}\", it is not a VPX emulator.", gameEmulator.getName());
    }
  }

  private void toggleVRFiles(GameEmulator gameEmulator, boolean enabled) {
    VRFilesInfo vrFiles = getVRFiles(gameEmulator.getId());
    if (vrFiles == null) {
      LOG.info("No VR files found for emulator \"{}\"", gameEmulator.getName());
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
      LOG.info("Skipped VR file toggle for emulator \"{}\", the original file does not exist.", emulator.getName());
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

    if (!vrFile.exists()) {
      LOG.info("No VR file {} found, keeping {}", vrFile.getAbsolutePath(), originalFile.getAbsolutePath());
      return;
    }

    if (originalFile.exists()) {
      if (backup.exists()) {
        LOG.info("Backup {} already exists, the VR file {} is already active.", backup.getAbsolutePath(), originalFile.getAbsolutePath());
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
    if (!backup.exists()) {
      LOG.info("No backup {} found, {} is not a VR file, nothing to restore.", backup.getAbsolutePath(), originalFile.getAbsolutePath());
      return;
    }
    if (originalFile.exists()) {
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
    GameEmulatorScript launchScript = gameEmulator.getLaunchScript();
    GameEmulatorScript launchScriptBackup = emulatorDetailsService.getGameEmulatorLaunchScript(gameEmulator.getId());
    GameEmulatorScript vrLaunchScript = emulatorDetailsService.getGameEmulatorVRLaunchScript(gameEmulator.getId());

    if (enabled) {
      //create a backup of the original script only when enabling VR
      if (launchScript != null) {
        launchScriptBackup = emulatorDetailsService.cloneScript(launchScript);
        emulatorDetailsService.saveEmulatorLaunchScript(gameEmulator.getId(), launchScriptBackup);
        LOG.info("Backed up launch script of emulator \"{}\"", gameEmulator.getName());
      }

      if (vrLaunchScript != null) {
        gameEmulator.setLaunchScript(vrLaunchScript);
        LOG.info("Applied VR launch script to emulator \"{}\"", gameEmulator.getName());
      }
      else {
        LOG.info("No VR launch script found for emulator \"{}\", keeping the launch script.", gameEmulator.getName());
      }
    }
    else {
      if (launchScriptBackup != null) {
        gameEmulator.setLaunchScript(launchScriptBackup);
        LOG.info("Restored launch script of emulator \"{}\"", gameEmulator.getName());
      }
      else {
        LOG.info("No launch script backup found for emulator \"{}\", keeping the launch script.", gameEmulator.getName());
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

  //-------------------- Table VR ini ---------------------------------------------------------------------------------

  /**
   * Creates the VR ini of the table as a copy of the table ini, or as an empty file if the table has no ini.
   */
  public boolean createTableVRIni(@NonNull Game game) {
    File iniFile = game.getIniFile();
    File vrIniFile = game.getVRIniFile();
    if (tableIniSwapper.isSwapped(iniFile)) {
      LOG.warn("Cannot create VR ini {}, the VR ini is currently in use.", vrIniFile.getAbsolutePath());
      return false;
    }
    if (vrIniFile.exists()) {
      LOG.info("VR ini {} already exists.", vrIniFile.getAbsolutePath());
      return true;
    }

    try {
      if (iniFile.exists()) {
        FileUtils.copyFile(iniFile, vrIniFile);
        LOG.info("Created VR ini {} → {}", iniFile.getAbsolutePath(), vrIniFile.getAbsolutePath());
      }
      else {
        FileUtils.touch(vrIniFile);
        LOG.info("Created empty VR ini {}", vrIniFile.getAbsolutePath());
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to create VR ini {}: {}", vrIniFile.getAbsolutePath(), e.getMessage(), e);
      return false;
    }
  }

  /**
   * @return true while the VR ini of the table is in place of the table ini
   */
  public boolean isTableIniSwapped(@NonNull Game game) {
    return tableIniSwapper.isSwapped(game.getIniFile());
  }

  public boolean deleteTableVRIni(@NonNull Game game) {
    File vrIniFile = game.getVRIniFile();
    if (tableIniSwapper.isSwapped(game.getIniFile())) {
      LOG.warn("Cannot delete VR ini {}, the VR ini is currently in use.", vrIniFile.getAbsolutePath());
      return false;
    }
    if (vrIniFile.exists() && vrIniFile.delete()) {
      LOG.info("Deleted VR ini {}", vrIniFile.getAbsolutePath());
      return true;
    }
    return false;
  }

  //-------------------- TableStatusChangeListener ---------------------------------------------------------------------

  /**
   * Puts the VR ini of the table in place. This runs synchronously before the emulator is started.
   */
  @Override
  public void tableLaunching(@NonNull Game game) {
    if (!isVRModeActive()) {
      return;
    }
    if (!game.isVpxGame()) {
      LOG.info("Skipped VR ini swap for \"{}\", it is not a VPX table.", game.getGameDisplayName());
      return;
    }

    //swaps of tables that never received an exit event, e.g. after a launch from the Studio
    File iniFile = game.getIniFile();
    for (File activeSwap : tableIniSwapper.getActiveSwaps()) {
      if (!activeSwap.getAbsolutePath().equals(iniFile.getAbsolutePath())) {
        tableIniSwapper.swapOut(activeSwap);
      }
    }

    LOG.info("VR mode is active, preparing VR ini for \"{}\"", game.getGameDisplayName());
    tableIniSwapper.swapIn(iniFile);
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    //the swap is done in tableLaunching() before the emulator is started
  }

  /**
   * Restores the desktop ini. This is not bound to the VR mode, so a swap is never left behind.
   */
  @Override
  public void tableExited(TableStatusChangedEvent event) {
    Game game = event.getGame();
    if (tableIniSwapper.isSwapped(game.getIniFile())) {
      LOG.info("Table \"{}\" exited, restoring desktop ini", game.getGameDisplayName());
      tableIniSwapper.swapOut(game.getIniFile());
    }
  }

  /**
   * The desktop ini must be back before any other listener reads table files.
   */
  @Override
  public int getPriority() {
    return 1000;
  }

  //-------------------- FrontendStatusChangeListener ------------------------------------------------------------------

  @Override
  public void frontendLaunched() {
    restoreDanglingTableIniSwaps();
  }

  @Override
  public void frontendExited() {
    restoreDanglingTableIniSwaps();
  }

  @Override
  public void frontendRestarted() {
    restoreDanglingTableIniSwaps();
  }

  /**
   * Restores swaps that have not been reverted by an exit event, as long as no table is running.
   */
  private void restoreDanglingTableIniSwaps() {
    if (tableIniSwapper.getActiveSwaps().isEmpty()) {
      return;
    }
    GameStatus status = frontendStatusService.getGameStatus();
    if (status != null && status.isActive()) {
      LOG.info("Skipped restoring VR ini swaps, a table is still running.");
      return;
    }
    tableIniSwapper.restoreAll();
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
    frontendStatusService.addTableStatusChangeListener(this);
    frontendStatusService.addFrontendStatusChangeListener(this);

    tableIniSwapper = new VRTableIniSwapper(new File(RESOURCES, TABLE_INI_SWAP_JOURNAL));
    //no table can be running yet, so leftovers of a crash or server restart are restored
    tableIniSwapper.restoreAll();
  }
}
