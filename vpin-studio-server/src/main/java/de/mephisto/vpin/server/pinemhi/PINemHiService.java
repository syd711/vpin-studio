package de.mephisto.vpin.server.pinemhi;

import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Service
public class PINemHiService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PINemHiService.class);

  private final static String PROCESS_NAME = "pinemhi_rom_monitor";

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  private boolean enabled = false;

  public boolean getAutoStart() {
    return preferencesService.getPreferences().getPinemhiAutoStartEnabled();
  }

  public boolean toggleAutoStart() {
    try {
      this.enabled = !enabled;
      preferencesService.savePreference(PreferenceNames.PINEMHI_AUTOSTART_ENABLED, enabled);

      if (!enabled) {
        kill();
      }
      else {
        startMonitor();
      }

      return enabled;
    } catch (Exception e) {
      LOG.error("Failed to set PINemHi autostart flag: " + e.getMessage(), e);
    }
    return false;
  }

  public boolean isRunning() {
    return systemService.isProcessRunning(PROCESS_NAME);
  }

  public boolean kill() {
    return systemService.killProcesses(PROCESS_NAME);
  }

  private static void startMonitor() {
    File exe = new File("resources", PROCESS_NAME + ".exe");
    List<String> commands = Arrays.asList("start", "/min", exe.getAbsolutePath());
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("resources"));
    executor.executeCommandAsync();
    LOG.info("Executed " + PROCESS_NAME + " command: " + String.join(" ", commands));
  }

  public boolean restart() {
    kill();
    startMonitor();
    return true;
  }

  public HashMap<Object, Object> getSettings() {
    PropertiesStore store = PropertiesStore.create(new File("resources/pinemhi", "pinemhi.ini"));
    Properties properties = store.getProperties();
    return new HashMap<>(properties);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.enabled = getAutoStart();
    if (enabled) {
      startMonitor();
      LOG.info("Auto-started " + PROCESS_NAME);
    }
  }
}
