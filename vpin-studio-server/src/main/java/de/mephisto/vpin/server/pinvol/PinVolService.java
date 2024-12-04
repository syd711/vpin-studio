package de.mephisto.vpin.server.pinvol;

import de.mephisto.vpin.commons.utils.FileChangeListener;
import de.mephisto.vpin.commons.utils.FileMonitoringThread;
import de.mephisto.vpin.commons.utils.NirCmd;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.pinvol.PinVolTableEntry;
import de.mephisto.vpin.restclient.pinvol.PinVolPreferences;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PinVolService implements InitializingBean, FileChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(PinVolService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  private boolean enabled = false;

  private List<PinVolTableEntry> pinVolTableEntries = new ArrayList<>();

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
    PinVolPreferences prefs = new PinVolPreferences();
    prefs.setTableEntries(pinVolTableEntries);
    return prefs;
  }

  private void loadIni() {
    pinVolTableEntries.clear();
    try {
      File tablesIni = getPinVolTablesIniFile();
      FileInputStream fileInputStream = new FileInputStream(tablesIni);
      List<String> entries = IOUtils.readLines(fileInputStream, StandardCharsets.UTF_8);
      for (String entry : entries) {
        PinVolTableEntry e = createEntry(entry);
        if (e != null) {
          pinVolTableEntries.add(e);
        }
      }
      fileInputStream.close();
      LOG.info("Loaded " + pinVolTableEntries.size() + " PinVOL table entries.");
    }
    catch (Exception e) {
      LOG.error("Failed to load pin");
    }
  }

  private PinVolTableEntry createEntry(String line) {
    String[] split = line.split("\\t");
    if (split.length == 6) {
      PinVolTableEntry entry = new PinVolTableEntry();
      entry.setName(split[0]);
      entry.setPrimaryVolume(Integer.parseInt(split[1]));
      entry.setSecondaryVolume(Integer.parseInt(split[2]));
      entry.setSsfBassVolume(Integer.parseInt(split[3]));
      entry.setSsfRearVolume(Integer.parseInt(split[4]));
      entry.setSsfFrontVolume(Integer.parseInt(split[5]));
      return entry;
    }
    return null;
  }

  private static File getPinVolTablesIniFile() {
    return new File(SystemService.RESOURCES, "PinVolTables.ini");
  }

  private void initListener() {
    FileMonitoringThread monitoringThread = new FileMonitoringThread(this, getPinVolTablesIniFile(), true);
    monitoringThread.startMonitoring();
  }

  @Override
  public void notifyFileChange(@Nullable File file) {
    LOG.info("PinVolTable.ini changed");
    loadIni();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    setSystemVolume();
    this.enabled = getPinVolAutoStart();
    if (enabled) {
      startPinVol();
      LOG.info("Auto-started PinVol");
    }

    loadIni();
    initListener();
  }
}
