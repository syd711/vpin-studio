package de.mephisto.vpin.server.doflinx;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.doflinx.DOFLinxSettings;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.frontend.popper.pupgames.PUPGameImporter;
import de.mephisto.vpin.server.games.BackglassNamingHelper;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DOFLinxService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(DOFLinxService.class);

  private final static String DOFLINX_INI = "DOFLinx.INI";

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  private DOFLinxSettings dofLinxSettings;
  private INIConfiguration iniConfiguration;

  public boolean getDOFLinxAutoStart() {
    return preferencesService.getPreferences().getPinVolAutoStartEnabled();
  }

  public boolean isValid() {
    if (!StringUtils.isEmpty(dofLinxSettings.getInstallationFolder())) {
      File folder = new File(dofLinxSettings.getInstallationFolder());
      return new File(folder, "DOFLinx.exe").exists() && new File(folder, "B2S").exists();
    }
    return false;
  }

  public boolean toggleAutoStart() {
    try {
      dofLinxSettings.setAutostart(!dofLinxSettings.isAutostart());
      preferencesService.savePreference(dofLinxSettings);
      return dofLinxSettings.isAutostart();
    }
    catch (Exception e) {
      LOG.error("Failed to set PinVol autostart flag: {}", e.getMessage(), e);
    }
    return false;
  }

  public boolean isRunning() {
    return systemService.isProcessRunning("DOFLinx");
  }

  public boolean killDOFLinx() {
    File folder = new File(dofLinxSettings.getInstallationFolder());
    SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("DOFLinxMsg", "QUIT"));
    executor.setDir(folder);
    executor.executeCommandAsync();
    return true;
  }

  @Nullable
  public File getInstallationFolder() {
    String installationFolder = dofLinxSettings.getInstallationFolder();
    if (!StringUtils.isEmpty(installationFolder)) {
      return new File(installationFolder);
    }
    return null;
  }

  @NonNull
  public String getGameNameForAltSound(@NonNull Game game) {
    if (game.getEmulator().getType().equals(EmulatorType.ZenFX) || game.getEmulator().getType().equals(EmulatorType.PinballM)) {
      return FilenameUtils.getBaseName(game.getGameName()).replaceAll("Table_", "");
    }

    if (game.getEmulator().getType().equals(EmulatorType.ZenFX3)) {
      GameEmulator emulator = game.getEmulator();
      List<TableDetails> tableDetailList = PUPGameImporter.read(emulator.getType(), emulator.getId());
      Optional<TableDetails> td = tableDetailList.stream().filter(t -> t.getGameName().equals(game.getGameName())).findFirst();
      if (td.isPresent()) {
        return td.get().getManufacturer().toUpperCase() + "_" + game.getGameDisplayName().replaceAll(" ", "_");
      }
    }
    return game.getRom();
  }

  private void startDOFLinx() {
    try {
      if (isValid()) {
        LOG.info("DOFLinx service launches DOFLink.exe");
        File folder = new File(dofLinxSettings.getInstallationFolder());
        SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("DOFLinx.exe"));
        executor.setDir(folder);
        executor.executeCommandAsync();
      }
    }
    catch (Exception e) {
      LOG.error("Failed to launch DOFLinx.exe: {}", e.getMessage(), e);
    }
  }

  @Nullable
  public File getBackglassFile(Game game) {
    GameEmulator emulator = game.getEmulator();
    B2SMapping mapping = BackglassNamingHelper.findBackglassName(game);
    if (mapping != null) {
      File backglassesFolder = getBackglassesFolder(emulator);
      if (backglassesFolder != null && backglassesFolder.exists()) {
        return new File(backglassesFolder, mapping.getDirectb2s());
      }
    }
    return null;
  }

  @Nullable
  public File getBackglassesFolder(@NonNull GameEmulator gameEmulator) {
    if (isValid()) {
      INIConfiguration configuration = getConfiguration();
      String path = null;
      switch (gameEmulator.getType()) {
        case ZenFX3: {
          path = configuration.getString("PATH_FX3_B2S");
          break;
        }
        case ZenFX: {
          path = configuration.getString("PATH_FX_B2S");
          break;
        }
        case PinballM: {
          path = configuration.getString("PATH_LINX_B2S");
          break;
        }
      }

      if (path == null || !new File(path).exists()) {
        return new File(getInstallationFolder(), "B2S");
      }

      return new File(path);
    }
    return null;
  }

  public boolean restart() {
    killDOFLinx();
    startDOFLinx();
    return true;
  }

  public ComponentSummary getComponentSummary() {
    ComponentSummary summary = new ComponentSummary();
    summary.setType(ComponentType.doflinx);

    if (isValid()) {
      summary.addEntry("DOFLinx.INI", getDOFLinxINI().getAbsolutePath());
      summary.addEntry("Last Modified", DateUtil.formatDateTime(new Date(getDOFLinxINI().lastModified())));
    }
    else {
      summary.addEntry("DOFLinx.INI", "-");
      summary.addEntry("Last Modified", "-");
    }

    return summary;
  }

  public File getDOFLinxINI() {
    return new File(getInstallationFolder(), DOFLINX_INI);
  }

  @Nullable
  private INIConfiguration getConfiguration() {
    File dofLinxINI = getDOFLinxINI();
    if (dofLinxINI.exists()) {
      INIConfiguration config = new INIConfiguration();
      config.setSeparatorUsedInOutput("=");
      config.setSeparatorUsedInInput("=");
      config.setCommentLeadingCharsUsedInInput("#");

      try (FileReader fileReader = new FileReader(dofLinxINI, StandardCharsets.UTF_8)) {
        config.read(fileReader);
      }
      catch (Exception e) {
        LOG.error("Failed to read: {}: {}", dofLinxINI.getAbsolutePath(), e.getMessage(), e);
      }

      return config;
    }
    return null;
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (propertyName.equals(PreferenceNames.DOFLINX_SETTINGS)) {
      this.dofLinxSettings = preferencesService.getJsonPreference(PreferenceNames.DOFLINX_SETTINGS, DOFLinxSettings.class);
      this.iniConfiguration = getConfiguration();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      preferencesService.addChangeListener(this);
      preferenceChanged(PreferenceNames.DOFLINX_SETTINGS, null, null);
      if (dofLinxSettings.isAutostart()) {
        startDOFLinx();
        LOG.info("Auto-started DOFLinx");
      }
      else {
        LOG.info("Autostart for DOFLinx not enabled.");
      }
    }
    catch (Exception e) {
      LOG.error("DOFLinx service initialization failed: {}", e.getMessage(), e);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
