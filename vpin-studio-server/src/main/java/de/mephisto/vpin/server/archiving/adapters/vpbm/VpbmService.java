package de.mephisto.vpin.server.archiving.adapters.vpbm;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.BackupSettings;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.restclient.util.SystemCommandOutput;
import de.mephisto.vpin.server.emulators.EmulatorService;
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

import static de.mephisto.vpin.server.system.SystemService.RESOURCES;

@Service
public class VpbmService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VpbmService.class);
  public static String VPBM_FOLDER = RESOURCES + "vpbm";


  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private SystemService systemService;


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
    List<GameEmulator> vpxGameEmulators = emulatorService.getVpxGameEmulators();
    Game game = null;
    for (GameEmulator vpxGameEmulator : vpxGameEmulators) {
      game = frontendService.getGameByFilename(vpxGameEmulator.getId(), vpxName);
      if (game != null) {
        break;
      }
    }

    if (game != null) {
      File backupFile = new File(getArchiveFolder(), tablename);
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

      if (ids.isEmpty()) {
        LOG.warn("No export host set, skipping export");
        return null;
      }

      executeVPBM(Arrays.asList("--set-alt-hosts=" + String.join(";", ids)));

      return backupFile;
    }
    else {
      LOG.warn("Game not found for VPX filename " + vpxName);
    }
    return null;
  }

  public String restore(String tableId) {
    String tableFilename = "\"" + tableId + "\"";
    SystemCommandOutput systemCommandOutput = executeVPBM(Arrays.asList("-i", tableFilename, "--disable-delete-checks"));
    String stdOut = systemCommandOutput.getStdOut();
    String errorOut = systemCommandOutput.getErrOut();
    LOG.info("VPBM restore std out result:\n" + stdOut);
    LOG.info("VPBM restore err out result:\n" + errorOut);
    return stdOut + "\n" + errorOut;
  }

  public void refresh() {
    executeVPBM(Arrays.asList("-g"));
  }


  public Boolean update() {
    //long start = System.currentTimeMillis();
    LOG.info("Executing VPBM update");
    boolean result = executeVPBM(Arrays.asList("-u")) != null;
    LOG.info("Finished VPBM update, refreshing config.");
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
    long start = System.currentTimeMillis();
    try {
      if (!systemService.isDotNetInstalled()) {
        LOG.error("Can't execute VPBM command, no .net installation found!");
        return out;
      }

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
    finally {
      LOG.info("VPBM command \"vPinBackupManager.exe " + String.join(" ", options) + "\" took " + (System.currentTimeMillis() - start) + " ms.");
    }
    return out;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.BACKUP_VIEW_ENABLED) {
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

  public boolean clearCache() {
    return true;
  }
}
