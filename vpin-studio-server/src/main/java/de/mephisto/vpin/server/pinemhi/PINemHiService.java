package de.mephisto.vpin.server.pinemhi;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Service
public class PINemHiService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PINemHiService.class);

  private final static String PROCESS_NAME = "pinemhi_rom_monitor";

  public final static String PINEMHI_FOLDER = SystemService.RESOURCES + "pinemhi";
  public final static String PINEMHI_COMMAND = "PINemHi.exe";
  public final static String PINEMHI_INI = "pinemhi.ini";

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private PinUPConnector pinUPConnector;

  private boolean enabled = false;
  private INIConfiguration iniConfiguration;

  public boolean getAutoStart() {
    return preferencesService.getPreferences().getPinemhiAutoStartEnabled();
  }

  public boolean toggleAutoStart() {
    try {
      this.enabled = !enabled;
      preferencesService.savePreference(PreferenceNames.PINEMHI_AUTOSTART_ENABLED, enabled);
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
    File exe = new File("resources/pinemhi", PROCESS_NAME + ".exe");
    List<String> commands = Arrays.asList("start", "/min", exe.getAbsolutePath());
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("resources/pinemhi"));
    executor.executeCommandAsync();
    LOG.info("Executed " + PROCESS_NAME + " command: " + String.join(" ", commands));
  }

  public boolean restart() {
    kill();
    startMonitor();
    loadSettings();
    return true;
  }

  public Map<String, Object> save(Map<String, Object> settings) {
    try {
      int changeCounter = 0;
      Set<Map.Entry<String, Object>> entries = settings.entrySet();
      for (Map.Entry<String, Object> entry : entries) {
        String key = entry.getKey();

        Set<String> sections = iniConfiguration.getSections();
        for (String section : sections) {
          if ("romfind".equals(section)) {
            continue;
          }

          SubnodeConfiguration s = iniConfiguration.getSection(section);
          if (s.containsKey(key)) {
            changeCounter++;
            s.setProperty(key, entry.getValue());
            break;
          }
        }
      }

      saveIni();
    } catch (Exception e) {
      LOG.error("Failed to save pinemhi.ini: " + e.getMessage(), e);
    }
    return settings;
  }

  private void saveIni() throws IOException, ConfigurationException {
    File pinEmHiIni = new File(PINEMHI_FOLDER, PINEMHI_INI);
    FileWriter fileWriter = new FileWriter(pinEmHiIni);
    iniConfiguration.write(fileWriter);
    fileWriter.close();
  }

  public Map<String, Object> loadSettings() {
    try {
      iniConfiguration = new INIConfiguration();
      iniConfiguration.setCommentLeadingCharsUsedInInput(";");
      iniConfiguration.setSeparatorUsedInOutput("=");
      iniConfiguration.setSeparatorUsedInInput("=");

      File pinEmHiIni = new File(PINEMHI_FOLDER, PINEMHI_INI);

      try (FileReader fileReader = new FileReader(pinEmHiIni)) {
        iniConfiguration.read(fileReader);
      }


      Map<String, Object> entries = new HashMap<>();
      Set<String> sections = iniConfiguration.getSections();
      for (String section : sections) {
        if ("romfind".equals(section)) {
          continue;
        }

        SubnodeConfiguration s = iniConfiguration.getSection(section);
        Iterator<String> keys = s.getKeys();
        while (keys.hasNext()) {
          String key = keys.next();
          if (!key.endsWith(".nv")) {
            entries.put(key, s.getString(key));
          }
        }
      }
      return entries;
    } catch (Exception e) {
      LOG.error("Failed to load pinemhi.ini: " + e.getMessage(), e);
    }
    return Collections.emptyMap();
  }


  @Override
  public void afterPropertiesSet() throws Exception {
    loadSettings();
    this.enabled = getAutoStart();
    if (enabled) {
      startMonitor();
      LOG.info("Auto-started " + PROCESS_NAME);
    }

    try {
      String vpPath = (String) iniConfiguration.getSection("paths").getProperty("VP");
      File vp = new File(vpPath);
      if (!vp.exists() || !vpPath.endsWith("/")) {
        vp = new File(pinUPConnector.getDefaultGameEmulator().getNvramFolder().getAbsolutePath());
        iniConfiguration.getSection("paths").setProperty("VP", vp.getAbsolutePath().replaceAll("\\\\", "/") + "/");
        saveIni();
        LOG.info("Changed VP path to " + vp.getAbsolutePath());
      }
    } catch (Exception e) {
      LOG.error("Failed to update VP path in pinemhi.ini: " + e.getMessage(), e);
    }
  }
}
