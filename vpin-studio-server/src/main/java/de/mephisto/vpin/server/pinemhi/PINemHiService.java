package de.mephisto.vpin.server.pinemhi;

import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
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
  private MameService mameService;


  private boolean enabled = false;
  private List<String> romList = new ArrayList<>();

  public boolean getAutoStart() {
    return preferencesService.getPreferences().getPinemhiAutoStartEnabled();
  }

  public boolean toggleAutoStart() {
    try {
      this.enabled = !enabled;
      preferencesService.savePreference(PreferenceNames.PINEMHI_AUTOSTART_ENABLED, enabled, false);
      return enabled;
    }
    catch (Exception e) {
      LOG.error("Failed to set PINemHi autostart flag: " + e.getMessage(), e);
    }
    return false;
  }

  public List<String> getRomList() {
    return romList;
  }

  public boolean isRunning() {
    return systemService.isProcessRunning(PROCESS_NAME);
  }

  public boolean kill() {
    return systemService.killProcesses(PROCESS_NAME);
  }

  private static void startMonitor() {
    File exe = new File(PINEMHI_FOLDER, PROCESS_NAME + ".exe");
    List<String> commands = Arrays.asList("start", "/min", exe.getAbsolutePath());
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File(PINEMHI_FOLDER));
    executor.executeCommandAsync();
    LOG.info("Executed " + PROCESS_NAME + " command: " + String.join(" ", commands));
  }

  public boolean restart() {
    kill();
    startMonitor();
    return true;
  }

  //----------------------

  public Map<String, Object> saveSettings(Map<String, Object> settings) {
    try {
      File ini = getPinemhiIni();
      //int changeCounter = 0;
      INIConfiguration iniConfiguration = loadIni(ini);

      Set<Map.Entry<String, Object>> entries = settings.entrySet();
      for (Map.Entry<String, Object> entry : entries) {
        String key = entry.getKey();

        Set<String> sections = iniConfiguration.getSections();
        for (String section : sections) {
          SubnodeConfiguration s = iniConfiguration.getSection(section);
          if (s.containsKey(key)) {
            //changeCounter++;
            s.setProperty(key, entry.getValue());
            break;
          }
        }
      }

      saveIni(ini, iniConfiguration);
    }
    catch (Exception e) {
      LOG.error("Failed to save pinemhi.ini: " + e.getMessage(), e);
    }
    return settings;
  }

  private static void saveIni(File ini, INIConfiguration iniConfiguration) throws IOException, ConfigurationException {
    try (FileWriter fileWriter = new FileWriter(ini)) {
      iniConfiguration.write(fileWriter);
    }
  }

  private static INIConfiguration loadIni(File ini) throws IOException, ConfigurationException {
    INIConfiguration iniConfiguration = new INIConfiguration();
    iniConfiguration.setCommentLeadingCharsUsedInInput(";");
    iniConfiguration.setSeparatorUsedInOutput("=");
    iniConfiguration.setSeparatorUsedInInput("=");

    try (FileReader fileReader = new FileReader(ini)) {
      iniConfiguration.read(fileReader);
    }
    return iniConfiguration;
  }

  public Map<String, Object> loadSettings() {
    try {
      INIConfiguration iniConfiguration = loadIni(getPinemhiIni());

      Map<String, Object> entries = new HashMap<>();
      Set<String> sections = iniConfiguration.getSections();
      for (String section : sections) {
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
    }
    catch (Exception e) {
      LOG.error("Failed to load pinemhi.ini: " + e.getMessage(), e);
    }
    return Collections.emptyMap();
  }

  public static final File getPinemhiIni() {
    return new File(PINEMHI_FOLDER, PINEMHI_INI);
  }

  //----------------------

  private void checkForUpdates() {
    try {
      if (!new File("resources/pinemhi").exists()) {
        LOG.info("Skipped PINemHi update check, wrong folder.");
        return;
      }

      List<String> commands = Arrays.asList(PINEMHI_COMMAND, "-v");
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(new File(PINEMHI_FOLDER));
      executor.executeCommand();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      String[] split = standardOutputFromCommand.toString().split("\n");
      for (String s : split) {
        if (s.contains("Version")) {
          String version = s.trim().split(" ")[1];
          String pinemhiVersion = systemService.getScoringDatabase().getPinemhiVersion();
          if (version.equals(pinemhiVersion)) {
            LOG.info("Using latest version of PINemHi (" + version + ")");
            break;
          }
          else {
            LOG.info("PINemHi is outdated (" + version + " vs. " + pinemhiVersion + "), checking for updates.");
            List<String> resources = Arrays.asList("PINemHi.exe", "pinemhi_rom_monitor.exe", "PINemHi_Leaderboard.exe");
            for (String resource : resources) {
              File check = new File(PINEMHI_FOLDER, resource);
              LOG.info("Downloading PINemHi file " + check.getAbsolutePath());
              Updater.downloadAndOverwrite("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/pinemhi/" + resource, check, true);
            }

            File nvramFolder = mameService.getNvRamFolder();
            if (nvramFolder != null && nvramFolder.exists()) {
              adjustVPPathForEmulator(nvramFolder, getPinemhiIni(), true);
            }
          }
        }
      }

    }
    catch (Exception e) {
      LOG.error("Failed to check for pinemhi updates: " + e.getMessage(), e);
    }
  }

  /**
   * Load pinhemi.ini, update the VP path with the nvRam foldr of the emulator
   * and save
   *
   * @param emulator The GameEmulator to get the path
   */
  public static void adjustVPPathForEmulator(File nvRamFolder, File ini, boolean forcePath) {
    if (nvRamFolder.exists()) {
      try {
        INIConfiguration iniConfiguration = loadIni(ini);
        String vpPath = (String) iniConfiguration.getSection("paths").getProperty("VP");
        File vp = new File(vpPath);

        if (forcePath || !vp.exists() || !vpPath.endsWith("/")) {
          vp = new File(nvRamFolder.getAbsolutePath());
          iniConfiguration.getSection("paths").setProperty("VP", vp.getAbsolutePath().replaceAll("\\\\", "/") + "/");

          saveIni(ini, iniConfiguration);
          LOG.info("Changed VP path to " + vp.getAbsolutePath());
        }
      }
      catch (Exception e) {
        LOG.error("Failed to update VP path in pinemhi.ini: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    new Thread(() -> {
      Thread.currentThread().setName("PinemHi Updater");
      checkForUpdates();
    }).start();

    this.enabled = getAutoStart();
    if (enabled) {
      startMonitor();
      LOG.info("Auto-started Pinemhi " + PROCESS_NAME);
    }

    File nvramFolder = mameService.getNvRamFolder();
    if (nvramFolder != null && nvramFolder.exists()) {
      adjustVPPathForEmulator(nvramFolder, getPinemhiIni(), true);
    }

    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
