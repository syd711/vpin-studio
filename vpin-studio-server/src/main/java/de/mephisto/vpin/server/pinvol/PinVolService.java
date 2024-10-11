package de.mephisto.vpin.server.pinvol;

import de.mephisto.vpin.commons.utils.NirCmd;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
public class PinVolService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PinVolService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  private boolean enabled = false;

  public boolean getPinVolAutoStart() {
    return preferencesService.getPreferences().getPinVolAutoStartEnabled();
  }

  public boolean toggleAutoStart() {
    try {
      this.enabled = !enabled;
      preferencesService.savePreference(PreferenceNames.PINVOL_AUTOSTART_ENABLED, enabled);
      return enabled;
    }
    catch (Exception e) {
      LOG.error("Failed to set PinVol autostart flag: " + e.getMessage(), e);
    }
    return false;
  }

  public boolean isRunning() {
    return systemService.isProcessRunning("PinVol");
  }

  public boolean killPinVol() {
    return systemService.killProcesses("PinVol");
  }

  private static void startPinVol() {
    try {
      de.mephisto.vpin.commons.utils.FileUtils.writeBatch("./resources/PinVol.bat", "cd /d %~dp0\ncd resources\nstart /min PinVol.exe\nexit\n");
      List<String> commands = Arrays.asList("cmd", "/c", "start", "PinVol.bat");
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(new File("./resources"));
      executor.executeCommandAsync();
      LOG.info("Executed PinVol command: " + String.join(" ", commands));
    }
    catch (Exception e) {
      LOG.error("Failed to launch PinVol.exe: " + e.getMessage(), e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    setSystemVolume();
    this.enabled = getPinVolAutoStart();
    if (enabled) {
      startPinVol();
      LOG.info("Auto-started PinVol");
    }
  }

  public boolean setSystemVolume() {
    ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    if (serverSettings.getVolume() > 0) {
      NirCmd.setVolume(serverSettings.getVolume());
      LOG.info("Applied initial system volume.");
      return true;
    }
    return false;
  }

  public boolean restart() {
    killPinVol();
    startPinVol();
    return true;
  }
}
