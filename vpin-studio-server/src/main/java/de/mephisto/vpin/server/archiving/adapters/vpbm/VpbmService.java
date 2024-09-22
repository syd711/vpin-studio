package de.mephisto.vpin.server.archiving.adapters.vpbm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.commons.utils.SystemCommandOutput;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.preferences.BackupSettings;
import de.mephisto.vpin.server.archiving.adapters.vpbm.config.VPinBackupManagerConfig;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.GithubUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;

@Service
public class VpbmService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VpbmService.class);
  public static String VPBM_FOLDER = RESOURCES + "vpbm";

  @Autowired
  private SystemService systemService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private FrontendService frontendService;

  public File getArchiveFolder() {
    return new File(getArchivesFolder(), "backups/Visual Pinball X/");
  }

  public File getExportFolder() {
    return new File(getArchivesFolder(), "exports/Visual Pinball X/");
  }

  public File getArchivesFolder() {
    File file = new File(RESOURCES, "archives/");
    if (!file.exists()) {
      file.mkdirs();
    }
    return file;
  }

  public File getBundlesFolder() {
    File file = new File(getArchivesFolder(), "bundles/");
    if (!file.exists()) {
      file.mkdirs();
    }
    return file;
  }

  public String backup(int tableId) {
    SystemCommandOutput systemCommandOutput = executeVPBM(Arrays.asList("-b", String.valueOf(tableId), "--disable-delete-checks"));
    if (!StringUtils.isEmpty(systemCommandOutput.getErrOut())) {
      return systemCommandOutput.getErrOut();
    }
    return null;
  }

  public File export(String tablename) {
    String vpxName = FilenameUtils.getBaseName(tablename) + ".vpx";
    // use the default emulator, VpbmService, does not support multiple emulator
    GameEmulator defaultEmu = frontendService.getDefaultGameEmulator();
    Game game = frontendService.getGameByFilename(defaultEmu.getId(), vpxName);
    if (game != null) {
      File backupFile = new File(getArchiveFolder(), tablename);
      File exportFile = new File(getExportFolder(), tablename);

      if (!backupFile.exists()) {
        backup(game.getId());
      }

      BackupSettings backupSettings = preferencesService.getJsonPreference(PreferenceNames.BACKUP_SETTINGS, BackupSettings.class);
      List<String> ids = new ArrayList<>();
      if (backupSettings.getVpbmExternalHostId1() != null) {
        ids.add(backupSettings.getVpbmExternalHostId1().trim());
      }
      if (backupSettings.getVpbmExternalHostId2() != null) {
        ids.add(backupSettings.getVpbmExternalHostId2().trim());
      }
      if (backupSettings.getVpbmExternalHostId3() != null) {
        ids.add(backupSettings.getVpbmExternalHostId3().trim());
      }

      if (ids.isEmpty()) {
        LOG.warn("No export host set, skipping export");
        return null;
      }

      executeVPBM(Arrays.asList("-e", String.valueOf(game.getId()), "--set-alt-hosts=" + String.join(";", ids)));

      return exportFile;
    }
    else {
      LOG.warn("Game not found for VPX filename " + vpxName);
    }
    return null;
  }

  public String restore(String tableId) {
    String tableFilename = "\"" + tableId + "\"";
    SystemCommandOutput systemCommandOutput = executeVPBM(Arrays.asList("-i", tableFilename));
    return systemCommandOutput.getStdOut();
  }

  public void refresh() {
    executeVPBM(Arrays.asList("-g"));
  }


  public Boolean update() {
    //long start = System.currentTimeMillis();
    LOG.info("Executing VPBM update");
    boolean result = executeVPBM(Arrays.asList("-u")) != null;
    LOG.info("Finished VPBM update, refreshing config.");
    refreshConfig();
    return result;
  }

  public boolean isUpdateAvailable() {
    String version = getVersion();
    String versionNumber = version.substring(version.indexOf(" ") + 1).trim();
    return GithubUtil.checkForUpdate(versionNumber, "https://github.com/mmattner/vPinBackupManagerApp/releases/latest") != null;
  }

  public String getVersion() {
    String version = executeVPBM(Arrays.asList("-v")).getStdOut();
    if (!StringUtils.isEmpty(version)) {
      return version.trim();
    }
    return "Unable to determine version, check log for details.";
  }

  private SystemCommandOutput executeVPBM(List<String> options) {
    SystemCommandOutput out = new SystemCommandOutput();

    try {
      File dir = new File(RESOURCES, VpbmArchiveSource.FOLDER_NAME);
      File exe = new File(dir, "vPinBackupManager.exe");
      List<String> commands = new ArrayList<>(Arrays.asList(exe.getAbsolutePath()));
      commands.addAll(options);
      LOG.info("Executing VPBM command (" + dir.getAbsolutePath() + "): " + String.join(" ", commands));
      SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
      executor.setDir(dir);
      executor.executeCommand();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardOutputFromCommand.toString())) {
        LOG.info("VPBM Command StdOut: " + standardOutputFromCommand.toString().trim());
        out.setStdOut(standardOutputFromCommand.toString());
      }
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("VPBM Command Error: " + standardErrorFromCommand);
        out.setErrOut(standardErrorFromCommand.toString());
      }
    }
    catch (Exception e) {
      out.setErrOut("Failed to execute VPBM: " + e.getMessage());
      LOG.error("Failed to execute VPBM: " + e.getMessage(), e);
    }
    return out;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    refreshConfig();
  }

  private void refreshConfig() {
    long start = System.currentTimeMillis();
    try {
      File configFileFolder = new File(VPBM_FOLDER);
      File configJsonFile = new File(configFileFolder, "vPinBackupManager.json");
      File archives = new File(SystemService.ARCHIVES_FOLDER);
      if (!archives.exists()) {
        archives.mkdirs();
      }


      File exportFolder = new File(SystemService.ARCHIVES_FOLDER, "exports");
      if (!exportFolder.exists()) {
        exportFolder.mkdirs();
      }


      File backupsFolder = new File(SystemService.ARCHIVES_FOLDER, "backups");
      if (!backupsFolder.exists()) {
        backupsFolder.mkdirs();
      }

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


      VPinBackupManagerConfig config = new VPinBackupManagerConfig();
      if (configJsonFile.exists()) {
        config = objectMapper.readValue(configJsonFile, VPinBackupManagerConfig.class);
      }

      boolean dirty = false;
      File exportPath = new File(config.getExportPath());
      if (!exportPath.getAbsolutePath().equals(getExportFolder().getParentFile().getAbsolutePath())) {
        config.setExportPath(exportFolder.getAbsolutePath());
        LOG.info("Updated VPBM export path to " + exportFolder.getAbsolutePath());
        dirty = true;
      }

      File backUpPath = new File(config.getBackupPath());
      if (!backUpPath.getAbsolutePath().equals(getArchiveFolder().getParentFile().getAbsolutePath())) {
        config.setBackupPath(backupsFolder.getAbsolutePath());
        LOG.info("Updated VPBM backup path to " + backupsFolder.getAbsolutePath());
        dirty = true;
      }

      File vPinballPath = new File(config.getVpinballBasePath());
      if (!vPinballPath.exists()) {
        GameEmulator defaultEmu = frontendService.getDefaultGameEmulator();
        if (defaultEmu != null) {
          File vpBase = defaultEmu.getInstallationFolder().getParentFile();
          config.setVpinballBasePath(vpBase.getAbsolutePath());
          LOG.info("Updated VPBM VP path to " + vpBase.getAbsolutePath());
          dirty = true;
        }
      }

      FrontendType frontendType = frontendService.getFrontendType();
      if (frontendType.supportPupPacks()) {
        File pinupSystemPath = new File(config.getPinup().getPinupDir());
        if (!pinupSystemPath.exists()) {
          pinupSystemPath = new File(config.getVpinballBasePath(), config.getPinup().getPinupDir());
        }

        if (!pinupSystemPath.exists()) {
          config.getPinup().setPinupDir(systemService.getPinupInstallationFolder().getAbsolutePath());
          LOG.info("Updated PinUPSystem path to " + systemService.getPinupInstallationFolder().getAbsolutePath());
          dirty = true;
        }
      }

      if (dirty || !configJsonFile.exists()) {
        objectMapper.writeValue(configJsonFile, config);
        LOG.info("Written updated VPBM config " + configJsonFile.getAbsolutePath());
      }

      BackupSettings backupSettings = preferencesService.getJsonPreference(PreferenceNames.BACKUP_SETTINGS, BackupSettings.class);
      if (StringUtils.isEmpty(backupSettings.getVpbmInternalHostId()) || backupSettings.getVpbmInternalHostId().contains("ERROR")) {
        String hostId = executeVPBM(Arrays.asList("-h")).getStdOut();
        if (hostId != null) {
          backupSettings.setVpbmInternalHostId(hostId.trim());
          preferencesService.savePreference(PreferenceNames.BACKUP_SETTINGS, backupSettings);
          LOG.info("Updated internal host id to '" + hostId.trim() + "'");
        }
      }
      LOG.info("Finished VPBM configuration check, took " + (System.currentTimeMillis() - start) + "ms.");
    }
    catch (Exception e) {
      String msg = "Failed to run configuration check for vpbm: " + e.getMessage();
      LOG.error(msg, e);
    }
  }
}
