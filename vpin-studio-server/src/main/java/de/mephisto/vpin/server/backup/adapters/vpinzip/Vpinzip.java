package de.mephisto.vpin.server.backup.adapters.vpinzip;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Vpinzip {
  private final static Logger LOG = LoggerFactory.getLogger(Vpinzip.class);
  public static String VPINZIP_FOLDER = SystemService.RESOURCES + "vpinzip";
  private static File configJson;

  private String tableIdOrFilename;

  private String param;

  private Vpinzip() {
    //force build
  }

  public static void initVPBMFolders(SystemService systemService) throws Exception {
    try {
      File configFileFolder = new File(SystemService.RESOURCES);
      configJson = new File(configFileFolder, "vPinBackupManager.json");
      File archives = new File(SystemService.ARCHIVES_FOLDER);
      archives.mkdirs();

      File backupFolder = new File(SystemService.ARCHIVES_FOLDER, "backups");
      backupFolder.mkdirs();
      File exportFolder = new File(SystemService.ARCHIVES_FOLDER, "exports");
      exportFolder.mkdirs();

      if (!configJson.exists()) {
        throw new FileNotFoundException(VPINZIP_FOLDER + " file (" + configJson.getAbsolutePath() + ") not found.");
      }

      FileInputStream fileInputStream = new FileInputStream(configJson);
      java.util.List<String> lines = IOUtils.readLines(fileInputStream, StandardCharsets.UTF_8);
      fileInputStream.close();

      boolean writeUpdates = false;
      List<String> updatedLines = new ArrayList<>();
      for (String line : lines) {
        if (line.contains("VpinballBasePath") && line.contains("%s")) {
          line = String.format(line, systemService.getVisualPinballInstallationFolder().getParentFile().getAbsolutePath()).replace("\\", "\\\\");
          writeUpdates = true;
        }
        else if (line.contains("BackupPath") && line.contains("%s")) {
          line = String.format(line, backupFolder.getAbsolutePath()).replace("\\", "\\\\");
          writeUpdates = true;
        }
        else if (line.contains("ExportPath") && line.contains("%s")) {
          line = String.format(line, exportFolder.getAbsolutePath()).replace("\\", "\\\\");
          writeUpdates = true;
        }
        updatedLines.add(line);
      }

      if (writeUpdates) {
        FileOutputStream out = new FileOutputStream(configJson);
        IOUtils.writeLines(updatedLines, "\n", out, StandardCharsets.UTF_8);
        out.close();
        LOG.info("Written updates to " + configJson.getAbsolutePath());
      }

      LOG.info("Finished vpbm installation check.");
    } catch (Exception e) {
      String msg = "Failed to run installation for vpbm: " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    }
  }

  public static File getArchiveFolder(SystemService systemService) {
    return new File(systemService.getVpaArchiveFolder(), "backups/Visual Pinball X/");
  }

  public static Vpinzip backup(int tableId) {
    Vpinzip cmd = new Vpinzip();
    cmd.param = "-b";
    cmd.tableIdOrFilename = String.valueOf(tableId);
    return cmd;
  }

  public static Vpinzip restore(String tableId) {
    Vpinzip cmd = new Vpinzip();
    cmd.param = "-i";
    cmd.tableIdOrFilename = "\"" + tableId + "\"";
    return cmd;
  }

  public static Vpinzip refresh() {
    Vpinzip cmd = new Vpinzip();
    cmd.param = "-g";
    return cmd;
  }

  public void execute() {
    try {
      File dir = new File(SystemService.RESOURCES, VpinzipArchiveSource.FOLDER_NAME);
      List<String> commands = new ArrayList<>(Arrays.asList("vPinBackupManager.exe", "-c", "\"" + configJson.getAbsolutePath() + "\"", param));
      if (tableIdOrFilename != null) {
        commands.add(tableIdOrFilename);
      }
      LOG.info("Executing VPBM command: " + String.join(" ", commands));
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(dir);
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("Vpinzip Command Error:\n" + standardErrorFromCommand);
      }
      if (!StringUtils.isEmpty(standardOutputFromCommand.toString())) {
        LOG.info("Vpinzip Command StdOut:\n" + standardOutputFromCommand);
      }
      executor.executeCommand();
    } catch (Exception e) {
      LOG.error("Failed to execute VPBM: " + e.getMessage(), e);
    }
  }
}
