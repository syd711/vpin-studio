package de.mephisto.vpin.server.backup.adapters.vpbm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.commons.utils.SystemCommandOutput;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.VpbmHosts;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.backup.adapters.vpbm.config.VPinBackupManagerConfig;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.popper.PinUPConnector;
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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class VpbmService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VpbmService.class);
  public static String VPBM_FOLDER = SystemService.RESOURCES + "vpbm";

  @Autowired
  private SystemService systemService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private PinUPConnector pinUPConnector;

  public File getArchiveFolder() {
    return new File(systemService.getArchivesFolder(), "backups/Visual Pinball X/");
  }

  public File getExportFolder() {
    return new File(systemService.getArchivesFolder(), "exports/Visual Pinball X/");
  }

  public void backup(int tableId) {
    executeVPBM(Arrays.asList("-b", String.valueOf(tableId)));
  }

  public File export(String tablename) {
    String vpxName = FilenameUtils.getBaseName(tablename) + ".vpx";
    Game game = pinUPConnector.getGameByFilename(vpxName);
    if (game != null) {
      File backupFile = new File(getArchiveFolder(), tablename);
      File exportFile = new File(getExportFolder(), tablename);

      if (!backupFile.exists()) {
        backup(game.getId());
      }

      String exportHostId = (String) preferencesService.getPreferenceValue(PreferenceNames.VPBM_EXTERNAL_HOST_IDENTIFIER);
      executeVPBM(Arrays.asList("-e", String.valueOf(game.getId()), "-x", exportHostId));

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
    return executeVPBM(Arrays.asList("-u")) != null;
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

  public VpbmHosts getHostIds() {
    VpbmHosts ids = new VpbmHosts();
    ids.setInternalHostId((String) preferencesService.getPreferenceValue(PreferenceNames.VPBM_EXTERNAL_HOST_IDENTIFIER, ""));
    ids.setInternalHostId((String) preferencesService.getPreferenceValue(PreferenceNames.VPBM_INTERNAL_HOST_IDENTIFIER));
    return ids;
  }

  private SystemCommandOutput executeVPBM(List<String> options) {
    SystemCommandOutput out = new SystemCommandOutput();
    try {
      File dir = new File(SystemService.RESOURCES, VpbmArchiveSource.FOLDER_NAME);
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
    } catch (Exception e) {
      out.setErrOut("Failed to execute VPBM: " + e.getMessage());
      LOG.error("Failed to execute VPBM: " + e.getMessage(), e);
    }
    return out;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      File configFileFolder = new File(VPBM_FOLDER);
      File configJson = new File(configFileFolder, "vPinBackupManager.json");
      File archives = new File(SystemService.ARCHIVES_FOLDER);
      archives.mkdirs();

      if (!configJson.exists()) {
        throw new FileNotFoundException(VPBM_FOLDER + " file (" + configJson.getAbsolutePath() + ") not found.");
      }

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      VPinBackupManagerConfig config = objectMapper.readValue(configJson, VPinBackupManagerConfig.class);


      boolean dirty = false;
      File exportPath = new File(config.getExportPath());
      if (!exportPath.getAbsolutePath().equals(getExportFolder().getParentFile().getAbsolutePath())) {
        File exportFolder = new File(SystemService.ARCHIVES_FOLDER, "exports");
        exportFolder.mkdirs();
        config.setExportPath(exportFolder.getAbsolutePath());
        LOG.info("Updated VPBM export path to " + exportFolder.getAbsolutePath());
        dirty = true;
      }

      File backUpPath = new File(config.getBackupPath());
      if (!backUpPath.getAbsolutePath().equals(getArchiveFolder().getParentFile().getAbsolutePath())) {
        File backupsFolder = new File(SystemService.ARCHIVES_FOLDER, "backups");
        backupsFolder.mkdirs();
        config.setBackupPath(backupsFolder.getAbsolutePath());
        LOG.info("Updated VPBM backup path to " + backupsFolder.getAbsolutePath());
        dirty = true;
      }

      File vPinballPath = new File(config.getVpinballBasePath());
      if (!vPinballPath.exists()) {
        File vpBase = systemService.getVisualPinballInstallationFolder().getParentFile();
        config.setVpinballBasePath(vpBase.getAbsolutePath());
        LOG.info("Updated VPBM VP path to " + vpBase.getAbsolutePath());
        dirty = true;
      }

      File pinupSystemPath = new File(config.getPinup().getPinupDir());
      if (!pinupSystemPath.exists()) {
        pinupSystemPath = new File(config.getVpinballBasePath(), config.getPinup().getPinupDir());
      }

      if (!pinupSystemPath.exists()) {
        config.getPinup().setPinupDir(systemService.getPinUPSystemFolder().getAbsolutePath());
        LOG.info("Updated PinUPSystem path to " + systemService.getPinUPSystemFolder().getAbsolutePath());
        dirty = true;
      }

      if (dirty) {
        objectMapper.writeValue(configJson, config);
        LOG.info("Written updated VPBM config " + configJson.getAbsolutePath());
      }

      String internalHostId = preferencesService.getPreferences().getVpbmInternalHostId();
      if (StringUtils.isEmpty(internalHostId) || internalHostId.contains("ERROR")) {
        String hostId = executeVPBM(Arrays.asList("-h")).getStdOut();
        if (hostId != null) {
          preferencesService.savePreference(PreferenceNames.VPBM_INTERNAL_HOST_IDENTIFIER, hostId.trim());
          LOG.info("Updated internal host id to '" + hostId.trim() + "'");
        }
      }
      LOG.info("Finished vpbm installation check.");
    } catch (Exception e) {
      String msg = "Failed to run installation for vpbm: " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    }
  }
}
