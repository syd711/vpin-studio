package de.mephisto.vpin.server.backup.adapters.vpbm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.VpbmHosts;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.backup.adapters.vpbm.config.VPinBackupManagerConfig;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.GithubUtil;
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
  public static String VPINZIP_FOLDER = SystemService.RESOURCES + "vpbm";

  @Autowired
  private SystemService systemService;

  @Autowired
  private PreferencesService preferencesService;

  public File getArchiveFolder() {
    return new File(systemService.getArchivesFolder(), "backups/Visual Pinball X/");
  }

  public void backup(int tableId) {
    executeVPBM("-b", String.valueOf(tableId));
  }

  public void restore(String tableId) {
    String tableFilename = "\"" + tableId + "\"";
    executeVPBM("-i", tableFilename);
  }

  public void refresh() {
    executeVPBM("-g", null);
  }


  public Boolean update() {
    return executeVPBM("-u", null) != null;
  }

  public boolean isUpdateAvailable() {
    String version = getVersion();
    String versionNumber = version.substring(version.indexOf(" ") + 1).trim();
    return GithubUtil.checkForUpdate(versionNumber, "https://github.com/mmattner/vPinBackupManagerApp/releases/latest") != null;
  }

  public String getVersion() {
    String version = executeVPBM("-v", null);
    if (!StringUtils.isEmpty(version)) {
      return version.trim();
    }
    return "Unable to determine version, check log for details.";
  }

  public VpbmHosts saveHostIds(VpbmHosts vpbmHosts) {
    preferencesService.savePreference(PreferenceNames.VPBM_EXTERNAL_HOST_IDENTIFIER, vpbmHosts.getExternalHostId());
    preferencesService.savePreference(PreferenceNames.VPBM_INTERNAL_HOST_IDENTIFIER, vpbmHosts.getInternalHostId());
    return vpbmHosts;
  }

  public VpbmHosts getHostIds() {
    VpbmHosts ids = new VpbmHosts();
    ids.setInternalHostId((String) preferencesService.getPreferenceValue(PreferenceNames.VPBM_EXTERNAL_HOST_IDENTIFIER, ""));

    String internalHostId = (String) preferencesService.getPreferenceValue(PreferenceNames.VPBM_INTERNAL_HOST_IDENTIFIER);
    if (StringUtils.isEmpty(internalHostId)) {
      String hostId = executeVPBM("-h", null);
      if (hostId != null) {
        preferencesService.savePreference(PreferenceNames.VPBM_INTERNAL_HOST_IDENTIFIER, hostId.trim());
        ids.setInternalHostId(hostId);
      }
    }
    return ids;
  }

  private String executeVPBM(String option, String param) {
    try {
      File dir = new File(SystemService.RESOURCES, VpbmArchiveSource.FOLDER_NAME);
      File exe = new File(dir, "vPinBackupManager.exe");
      List<String> commands = new ArrayList<>(Arrays.asList(exe.getAbsolutePath(), option));
      if (param != null) {
        commands.add(param);
      }
      LOG.info("Executing VPBM command (" + dir.getAbsolutePath() + "): " + String.join(" ", commands));
      SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
      executor.setDir(dir);
      executor.executeCommand();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardOutputFromCommand.toString())) {
        LOG.info("Vpinzip Command StdOut:\n" + standardOutputFromCommand);
        return standardOutputFromCommand.toString();
      }
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("Vpinzip Command Error:\n" + standardErrorFromCommand);
        return standardOutputFromCommand.toString();
      }

    } catch (Exception e) {
      LOG.error("Failed to execute VPBM: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      File configFileFolder = new File(VPINZIP_FOLDER);
      File configJson = new File(configFileFolder, "vPinBackupManager.json");
      File archives = new File(SystemService.ARCHIVES_FOLDER);
      archives.mkdirs();

      if (!configJson.exists()) {
        throw new FileNotFoundException(VPINZIP_FOLDER + " file (" + configJson.getAbsolutePath() + ") not found.");
      }

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      VPinBackupManagerConfig config = objectMapper.readValue(configJson, VPinBackupManagerConfig.class);


      boolean dirty = false;
      File exportPath = new File(config.getExportPath());
      if (!exportPath.exists()) {
        File exportFolder = new File(SystemService.ARCHIVES_FOLDER, "exports");
        exportFolder.mkdirs();
        config.setExportPath(exportFolder.getAbsolutePath());
        LOG.info("Updated VPBM export path to " + exportFolder.getAbsolutePath());
        dirty = true;
      }

      File backUpPath = new File(config.getBackupPath());
      if (!backUpPath.exists()) {
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

      if (dirty) {
        objectMapper.writeValue(configJson, config);
        LOG.info("Written updated VPBM config " + configJson.getAbsolutePath());
      }

      LOG.info("Finished vpbm installation check.");
    } catch (Exception e) {
      String msg = "Failed to run installation for vpbm: " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    }
  }
}
