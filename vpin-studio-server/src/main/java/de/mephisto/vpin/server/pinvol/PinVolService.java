package de.mephisto.vpin.server.pinvol;

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
import java.util.List;

@Service
public class PinVolService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PinVolService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  public boolean getPinVolAutoStart() {
    return preferencesService.getPreferences().getPinVolAutoStartEnabled();
  }

  public boolean toggleAutoStart() {
    try {
      boolean autoStart = getPinVolAutoStart();
      preferencesService.savePreference(PreferenceNames.PINVOL_AUTOSTART_ENABLED, !autoStart);
      return !autoStart;
    } catch (Exception e) {
      LOG.error("Failed to set PinVol autostart flag: " + e.getMessage(), e);
    }
    return false;
  }

  public boolean killPinVol() {
    return systemService.killProcesses("PinVol");
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    boolean autoStart = getPinVolAutoStart();
    if (autoStart) {
      File exe = new File("resources", "PinVol.exe");

      List<String> commands = Arrays.asList("start", "/min", exe.getAbsolutePath());
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(new File("resources"));
      executor.executeCommandAsync();
      LOG.info("Auto-started PinVol");
    }
  }
}
