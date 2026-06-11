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
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

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
  private File cachedPinVolExe = null;
  private final List<FileMonitoringThread> monitoringThreads = new ArrayList<>();

  public boolean isRunning() {
    return systemService.isProcessRunning("PinVol");
  }

  public boolean isValid() {
    return getPinVolExe().exists();
  }

  public boolean killPinVol() {
    return systemService.killProcesses("PinVol");
  }

  private void startPinVol() {
    try {
      File pinVolExe = getPinVolExe();
      if (pinVolExe.exists()) {
        String pinVolDir = pinVolExe.getParentFile().getAbsolutePath();
        File file = FileUtils.writeBatch(SystemService.RESOURCES + "PinVol.bat", "start \"\" /min /d \"" + pinVolDir + "\" \"" + pinVolExe.getAbsolutePath() + "\"\nexit\n");
        if (file.exists()) {
          List<String> commands = Arrays.asList("cmd", "/c", "start", "PinVol.bat");
          SystemCommandExecutor executor = new SystemCommandExecutor(commands);
          executor.setDir(new File(SystemService.RESOURCES));
          executor.executeCommand();
          LOG.info("Executed PinVol command: {}", String.join(" ", commands));
        }
        else {
          LOG.error("Failed to start PinVol.bat, file does not exist: {}", file.getAbsolutePath());
        }
      }
      else {
        LOG.warn("{} does not exist; couldn't start it", pinVolExe.getAbsolutePath());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to launch PinVol.exe: {}", e.getMessage(), e);
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
    if (killPinVol()) {
      long deadline = System.currentTimeMillis() + 5000;
      while (isRunning() && System.currentTimeMillis() < deadline) {
        try {
          Thread.sleep(200);
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
      startPinVol();
    }
    return isRunning();
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

        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (String entry : entries) {
          if (!seen.add(entry)) {
            LOG.warn("Filtered duplicate PinVolTables.ini entry: {}", entry);
          }
        }

        for (String entry : seen) {
          PinVolTableEntry e = loadEntry(entry, preferences.getSsfDbLimit());
          if (e != null) {
            preferences.getTableEntries().add(e);
          }
        }
        LOG.info("Loaded {} PinVOL table entries.", preferences.getTableEntries().size());
      }
      File volIni = getPinVolVolIniFile();
      if (volIni.exists()) {
        INIConfiguration iniConfiguration = new INIConfiguration();
        iniConfiguration.setCommentLeadingCharsUsedInInput("#;");
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
      try {
        PinVolTableEntry entry = new PinVolTableEntry();
        entry.setName(split[0]);
        entry.setPrimaryVolume(Integer.parseInt(split[1]));
        entry.setSecondaryVolume(Integer.parseInt(split[2]));
        entry.setSsfBassVolume(parseGainValue(split[3], ssfDbLimit));
        entry.setSsfRearVolume(parseGainValue(split[4], ssfDbLimit));
        entry.setSsfFrontVolume(parseGainValue(split[5], ssfDbLimit));
        return entry;
      }
      catch (NumberFormatException e) {
        LOG.warn("Skipped malformed PinVolTables.ini entry: {}", line);
      }
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

  public File getPinVolTablesIniFile() {
    return new File(getPinVolExe().getParentFile(), PIN_VOL_TABLES_INI);
  }

  public File getPinVolSettingsIniFile() {
    return new File(getPinVolExe().getParentFile(), PIN_VOL_SETTINGS_INI);
  }

  public File getPinVolVolIniFile() {
    return new File(getPinVolExe().getParentFile(), PIN_VOL_VOL_INI);
  }

  private File getPinVolExe() {
    if (cachedPinVolExe == null) {
      cachedPinVolExe = resolvePinVolExe();
    }
    return cachedPinVolExe;
  }

  private File resolvePinVolExe() {
    String installFolderPreferences = (String) preferencesService.getPreferenceValue(PreferenceNames.PINVOL_FOLDER);
    return installFolderPreferences == null ?
        new File(SystemService.RESOURCES, "PinVol.exe") :
        new File(installFolderPreferences, "PinVol.exe");
  }

  private void initListener() {
    for (FileMonitoringThread thread : monitoringThreads) {
      thread.stopMonitoring();
    }
    monitoringThreads.clear();

    File pinvolTablesFile = getPinVolTablesIniFile();
    if (pinvolTablesFile.exists()) {
      FileMonitoringThread monitoringThread = new FileMonitoringThread(this, pinvolTablesFile, true);
      monitoringThread.startMonitoring();
      monitoringThreads.add(monitoringThread);
    }

    File pinvolSettingsFile = getPinVolSettingsIniFile();
    if (pinvolSettingsFile.exists()) {
      FileMonitoringThread settingsThread = new FileMonitoringThread(this, pinvolSettingsFile, true);
      settingsThread.startMonitoring();
      monitoringThreads.add(settingsThread);
    }
  }

  public PinVolPreferences save(@NonNull PinVolUpdate update) {
    PinVolPreferences updated = update(update);
    if (isRunning() && isValid()) {
      restart();
    }
    return updated;
  }

  public PinVolPreferences update(@NonNull PinVolUpdate update) {
    loadIni();
    PinVolTableEntry systemVolume = this.preferences.getSystemVolume();
    if (systemVolume != null) {
      this.preferences.applyValues(systemVolume.getName(), update.getSystemVolume());
    }

    List<Integer> gameIds = update.getGameIds();
    for (Integer gameId : gameIds) {
      Game game = gameService.getGame(gameId);
      if (game != null) {
        String key = PinVolPreferences.getKey(game.getGameFileName(), game.isVpxGame(), game.isFpGame());
        if (this.preferences.contains(key)) {
          this.preferences.applyValues(key, update.getTableVolume());
        }
        else {
          PinVolTableEntry entry = new PinVolTableEntry();
          entry.setName(key);
          entry.applyValues(update.getTableVolume());
          this.preferences.getTableEntries().add(entry);
        }
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.PINVOL, null);
      }
    }

    return saveIniFile(this.preferences);
  }

  private PinVolPreferences saveIniFile(PinVolPreferences preferences) {
    StringBuilder builder = new StringBuilder();
    builder.append("# PinVol volume levels list\n");
    builder.append("# Saved ");
    DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
    builder.append(df.format(Instant.now()));
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
        try (FileOutputStream out = new FileOutputStream(iniFile)) {
          IOUtils.write(builder.toString(), out, StandardCharsets.UTF_8);
        }
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
        iniConfiguration.setCommentLeadingCharsUsedInInput("#;");
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
    this.enabled = preferencesService.getPreferences().getPinVolAutoStartEnabled();

    new Thread(() -> {
      if (enabled) {
        startPinVol();
        LOG.info("Auto-started PinVol");
        boolean pinVolFound = systemService.isProcessRunning("PinVol");
        LOG.info("Found PinVol.exe process: {}", pinVolFound);
      }
      setInitialMute();
    }, "pinvol-autostart").start();

    loadIni();
    initListener();

    preferencesService.addChangeListener((propertyName, oldValue, newValue) -> {
      if (PreferenceNames.PINVOL_FOLDER.equals(propertyName)) {
        cachedPinVolExe = null;
        loadIni();
        initListener();
      }
    });

    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
