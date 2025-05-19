package de.mephisto.vpin.server.pinvol;

import de.mephisto.vpin.commons.utils.FileChangeListener;
import de.mephisto.vpin.commons.utils.FileMonitoringThread;
import de.mephisto.vpin.commons.utils.NirCmd;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.pinvol.PinVolPreferences;
import de.mephisto.vpin.restclient.pinvol.PinVolTableEntry;
import de.mephisto.vpin.restclient.pinvol.PinVolUpdate;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PinVolService implements InitializingBean, FileChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(PinVolService.class);

  private static final String PIN_VOL_TABLES_INI = "PinVolTables.ini";
  private static final String PIN_VOL_SETTINGS_INI = "PinVolSettings.ini";
  private static final String PIN_VOL_VOL_INI = "PinVolVol.ini";

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  private boolean enabled = false;
  private PinVolPreferences preferences = null;

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
      FileUtils.writeBatch("./resources/PinVol.bat", "cd /d %~dp0\ncd resources\nstart /min PinVol.exe\nexit\n");
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


  private void setInitialMute() {
    ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    if (serverSettings.isInitialMute()) {
      NirCmd.muteSystem(true);
      LOG.info("Applied initial system volume mute.");
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

  public PinVolPreferences getPinVolTablePreferences() {
    return preferences;
  }

  private void loadIni() {
    preferences = new PinVolPreferences();
    try {
      INIConfiguration pinVolSettingsConfig = getPinVolSettingsConfig();
      if (pinVolSettingsConfig != null) {
        preferences.setSsfDbLimit(pinVolSettingsConfig.getInt("SSFdBLimit", 10));
      }

      File tablesIni = getPinVolTablesIniFile();
      if (!tablesIni.exists()) {
        LOG.info("PinVol service table settings have not been loaded, because {} was not found.", tablesIni.getAbsolutePath());
        return;
      }
      try (FileInputStream fileInputStream = new FileInputStream(tablesIni)) {
        List<String> entries = IOUtils.readLines(fileInputStream, StandardCharsets.UTF_8);

        //we may have messed up older configs.
        List<String> filtered = new ArrayList<>();
        for (String entry : entries) {
          if (!filtered.contains(entry)) {
            filtered.add(entry);
          }
          else {
            LOG.warn("Filtered duplicate PinVolTables.ini entry: {}", entry);
          }
        }

        for (String entry : filtered) {
          PinVolTableEntry e = loadEntry(entry, preferences.getSsfDbLimit());
          if (e != null) {
            preferences.getTableEntries().add(e);
          }
        }
        LOG.info("Loaded " + preferences.getTableEntries().size() + " PinVOL table entries.");
      }
      File volIni = getPinVolVolIniFile();
      if (volIni.exists()) {
        INIConfiguration iniConfiguration = new INIConfiguration();
        iniConfiguration.setCommentLeadingCharsUsedInInput(";");
        iniConfiguration.setSeparatorUsedInOutput("=");
        iniConfiguration.setSeparatorUsedInInput("=");

        try (FileReader fileReader = new FileReader(volIni)) {
          iniConfiguration.read(fileReader);
        }

        preferences.setDefaultVol(iniConfiguration.getInt("Default", 0));
        preferences.setNight(iniConfiguration.getInt("Night", 0));
        preferences.setGlobal(iniConfiguration.getInt("Global", 0));
        LOG.info("Loaded {}", volIni.getAbsolutePath());
      }
      else {
        LOG.info("Skipped loading of {}, file not found.", volIni.getAbsolutePath());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to load PinVol settings: {}", e.getMessage(), e);
    }
  }

  private PinVolTableEntry loadEntry(String line, int ssfDbLimit) {
    String[] split = line.split("\\t");
    if (split.length == 6) {
      PinVolTableEntry entry = new PinVolTableEntry();
      entry.setName(split[0]);
      entry.setPrimaryVolume(Integer.parseInt(split[1]));
      entry.setSecondaryVolume(Integer.parseInt(split[2]));

      entry.setSsfBassVolume(parseGainValue(split[3], ssfDbLimit));
      entry.setSsfRearVolume(parseGainValue(split[4], ssfDbLimit));
      entry.setSsfFrontVolume(parseGainValue(split[5], ssfDbLimit));
      return entry;
    }
    return null;
  }

  private int parseGainValue(String value, int ssfDbLimit) {
    try {
      if (StringUtils.isEmpty(value)) {
        return 0;
      }
      int i = Integer.parseInt(value);
      return PinVolTableEntry.formatGainValue(i, ssfDbLimit);
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }

  private static File getPinVolTablesIniFile() {
    return new File(SystemService.RESOURCES, PIN_VOL_TABLES_INI);
  }

  private static File getPinVolSettingsIniFile() {
    return new File(SystemService.RESOURCES, PIN_VOL_SETTINGS_INI);
  }

  private static File getPinVolVolIniFile() {
    return new File(SystemService.RESOURCES, PIN_VOL_VOL_INI);
  }

  private void initListener() {
    File pinvolTablesFile = getPinVolTablesIniFile();
    if (pinvolTablesFile.exists()) {
      FileMonitoringThread monitoringThread = new FileMonitoringThread(this, pinvolTablesFile, true);
      monitoringThread.startMonitoring();
    }

    File pincolSettingsFile = getPinVolSettingsIniFile();
    if (pincolSettingsFile.exists()) {
      FileMonitoringThread settingsThread = new FileMonitoringThread(this, pincolSettingsFile, true);
      settingsThread.startMonitoring();
    }
  }

  public PinVolPreferences update(@NonNull PinVolUpdate update) {
    PinVolPreferences preferences = getPinVolTablePreferences();
    LOG.info("Loaded PinVolTables.ini with {} entries.", preferences.getTableEntries().size());
    PinVolTableEntry systemVolume = preferences.getSystemVolume();
    preferences.applyValues(systemVolume.getName(), update.getSystemVolume());

    List<Integer> gameIds = update.getGameIds();
    for (Integer gameId : gameIds) {
      Game game = gameService.getGame(gameId);
      if (game != null) {
        String key = PinVolPreferences.getKey(game.getGameFileName(), game.isVpxGame(), game.isFpGame());
        if (preferences.contains(key)) {
          preferences.applyValues(key, update.getTableVolume());
        }
        else {
          PinVolTableEntry entry = new PinVolTableEntry();
          entry.setName(key);
          entry.applyValues(update.getTableVolume());
          preferences.getTableEntries().add(entry);
        }
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.PINVOL, null);
      }
    }

    return saveIniFile(preferences);
  }

  private PinVolPreferences saveIniFile(PinVolPreferences preferences) {
    StringBuilder builder = new StringBuilder();
    builder.append("# PinVol volume levels list\n");
    builder.append("# Saved ");
    builder.append(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));
    builder.append("\n");
    builder.append("\n");

    List<PinVolTableEntry> tableEntries = preferences.getTableEntries();
    for (PinVolTableEntry tableEntry : tableEntries) {
      String value = tableEntry.toSettingsString(preferences.getSsfDbLimit());
      builder.append(value);
    }

    File iniFile = getPinVolTablesIniFile();
    try {
      if (iniFile.exists() && !iniFile.delete()) {
        LOG.error("Saving failed, can not delete {}", iniFile.getAbsolutePath());
      }
      else {
        FileOutputStream out = new FileOutputStream(iniFile);
        IOUtils.write(builder.toString(), out, StandardCharsets.UTF_8);
        out.close();
      }
    }
    catch (Exception e) {
      LOG.error("Failed to write {}: {}", iniFile.getAbsolutePath(), e.getMessage(), e);
    }

    loadIni();
    return getPinVolTablePreferences();
  }

  public void delete(@NonNull Game game) {
    PinVolPreferences pinVolTablePreferences = getPinVolTablePreferences();
    PinVolTableEntry entry = pinVolTablePreferences.getTableEntry(game.getGameFileName(), game.isVpxGame(), game.isFpGame());
    if (entry != null) {
      pinVolTablePreferences.getTableEntries().remove(entry);
      saveIniFile(pinVolTablePreferences);
      LOG.info("Deleted {}", entry);
      gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.PINVOL, null);
    }
  }

  @Nullable
  private INIConfiguration getPinVolSettingsConfig() {
    try {
      File volIni = getPinVolSettingsIniFile();
      if (volIni.exists()) {
        INIConfiguration iniConfiguration = new INIConfiguration();
        iniConfiguration.setCommentLeadingCharsUsedInInput(";");
        iniConfiguration.setSeparatorUsedInOutput("=");
        iniConfiguration.setSeparatorUsedInInput("=");

        try (FileReader fileReader = new FileReader(volIni)) {
          iniConfiguration.read(fileReader);
        }

        return iniConfiguration;
      }
      else {
        LOG.info("Skipped loading of {}, file not found.", volIni.getAbsolutePath());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to load {}", "PinVolSettings.ini", e);
    }
    return null;
  }

  @Override
  public void notifyFileChange(@Nullable File file) {
    LOG.info("PinVolSettings changed");
    loadIni();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    setSystemVolume();
    this.enabled = getPinVolAutoStart();

    new Thread(() -> {
      if (enabled) {
        startPinVol();
        LOG.info("Auto-started PinVol");
        boolean pinVolFound = systemService.waitForProcess("PinVol", 3);
        LOG.info("Found PinVol.exe process: {}", pinVolFound);
        try {
          Thread.sleep(3000);
        }
        catch (InterruptedException e) {
          //ignore
        }
      }
      setInitialMute();
    }).start();

    loadIni();
    initListener();
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
