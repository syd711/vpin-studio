package de.mephisto.vpin.server.pinemhi;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

  private boolean enabled = false;
  private Ini ini;

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

        Collection<Profile.Section> sections = ini.values();
        for (Profile.Section section : sections) {
          if (section.containsKey(key)) {
            changeCounter++;
            section.put(key, entry.getValue());
            break;
          }
        }
      }

      ini.store();
      LOG.info("Written " + changeCounter + " entries to pinemhi.ini.");
    } catch (IOException e) {
      LOG.error("Failed to save pinemhi.ini: " + e.getMessage(), e);
    }
    return settings;
  }

  public Map<String, Object> loadSettings() {
    try {
      File pinEmHiIni = new File(PINEMHI_FOLDER, PINEMHI_INI);

      ini = new Ini(pinEmHiIni);
      Map<String, Object> entries = new HashMap<>();
      Collection<Profile.Section> values = ini.values();
      for (Profile.Section value : values) {
        Set<Map.Entry<String, String>> entrySet = value.entrySet();
        for (Map.Entry<String, String> e : entrySet) {
          String v = e.getValue();
          if (!v.endsWith(".nv")) {
            entries.put(e.getKey(), e.getValue());
          }
        }
      }
      return entries;
    } catch (IOException e) {
      LOG.error("Failed to load pinemhi.ini: " + e.getMessage(), e);
    }
    return Collections.emptyMap();
  }


  @Override
  public void afterPropertiesSet() throws Exception {
    initPinemHiFolders();

    Config.getGlobal().setEscape(false);
    Config.getGlobal().setStrictOperator(true);
    Config.getGlobal().setEmptySection(true);
    Config.getGlobal().setHeaderComment(true);

    loadSettings();

    File vpPath = new File(ini.get("paths", "VP"));
    if (!vpPath.exists()) {
      ini.get("paths").put("VP", systemService.getNvramFolder().getAbsolutePath());
      ini.store();
    }

    loadSettings();

    this.enabled = getAutoStart();
    if (enabled) {
      startMonitor();
      LOG.info("Auto-started " + PROCESS_NAME);
    }
  }

  private void initPinemHiFolders() throws VPinStudioException {
    try {
      File file = new File(PINEMHI_FOLDER, PINEMHI_INI);
      if (!file.exists()) {
        throw new FileNotFoundException("pinemhi.ini file (" + file.getAbsolutePath() + ") not found.");
      }

      FileInputStream fileInputStream = new FileInputStream(file);
      java.util.List<String> lines = IOUtils.readLines(fileInputStream, StandardCharsets.UTF_8);
      fileInputStream.close();

      boolean writeUpdates = false;
      List<String> updatedLines = new ArrayList<>();
      for (String line : lines) {
        if (line.startsWith("VP=")) {
          String vpValue = line.split("=")[1];
          File pinemhiNvRamFolder = new File(vpValue);
          if (!pinemhiNvRamFolder.exists() || line.endsWith("\\")) {
            LOG.info("Found errorneous VP path entry in pinemhi.ini, updating file.");
            pinemhiNvRamFolder = systemService.getNvramFolder();
            line = "VP=" + pinemhiNvRamFolder.getAbsolutePath();
            writeUpdates = true;
          }
        }

        if (line.startsWith("FP=")) {
          String fpValue = line.split("=")[1];
          if (line.endsWith("\\")) {
            LOG.info("Found errorneous FP path entry in pinemhi.ini, updating file.");
            line = "FP=" + new File(fpValue).getAbsolutePath();
            writeUpdates = true;
          }
        }
        updatedLines.add(line);
      }

      if (writeUpdates) {
        FileOutputStream out = new FileOutputStream(file);
        IOUtils.writeLines(updatedLines, "\n", out, StandardCharsets.UTF_8);
        out.close();
        LOG.info("Written updates to " + file.getAbsolutePath());
      }

      LOG.info("Finished pinemhi installation check.");
    } catch (Exception e) {
      String msg = "Failed to run installation for pinemhi: " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    }
  }
}
