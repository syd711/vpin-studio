package de.mephisto.vpin.server.backups.adapters.vpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.backups.BackupPackageInfo;
import de.mephisto.vpin.restclient.backups.BackupType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.preferences.BackupSettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.server.backups.BackupDescriptor;
import de.mephisto.vpin.server.backups.BackupSource;
import de.mephisto.vpin.server.backups.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

public class TableBackupAdapterVpa implements TableBackupAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(TableBackupAdapterVpa.class);

  @NonNull
  private final BackupSource backupSource;
  private final Game game;
  private final TableDetails tableDetails;
  private final BackupSettings backupSettings;
  private final VpaService vpaService;
  private boolean cancelled = false;

  public TableBackupAdapterVpa(@NonNull VpaService vpaService,
                               @NonNull BackupSource backupSource,
                               @NonNull Game game,
                               @NonNull TableDetails tableDetails,
                               @NonNull BackupSettings backupSettings) {
    this.vpaService = vpaService;
    this.backupSource = backupSource;
    this.game = game;
    this.tableDetails = tableDetails;
    this.backupSettings = backupSettings;
  }

  public void createBackup(JobDescriptor jobDescriptor) {
    LOG.info("********************* Table Backup: {} ***********************************", game.getGameDisplayName());
    BackupDescriptor backupDescriptor = new BackupDescriptor();
    BackupPackageInfo packageInfo = new BackupPackageInfo();

    backupDescriptor.setCreatedAt(new Date());
    backupDescriptor.setTableDetails(tableDetails);
    backupDescriptor.setPackageInfo(packageInfo);

    jobDescriptor.setStatus("Calculating export size of " + game.getGameDisplayName());
    long totalSizeExpected = vpaService.calculateTotalSize(game);
    LOG.info("Calculated total approx. size of " + FileUtils.readableFileSize(totalSizeExpected) + " for the archive of " + game.getGameDisplayName());

    String baseName = FilenameUtils.getBaseName(game.getGameFileName());
    File targetFolder = new File(backupSource.getLocation());
    if (!targetFolder.exists()) {
      targetFolder = VpaBackupSource.FOLDER;
    }

    File target = new File(targetFolder, baseName + "." + BackupType.VPA.name().toLowerCase());
    if (target.exists() && !backupSettings.isOverwriteBackup()) {
      target = FileUtils.uniqueFile(target);
    }
    backupDescriptor.setFilename(target.getName());

    try {
      File tempFile = File.createTempFile(target.getName(), ".bak");
      //---------
      LOG.info("Packaging " + game.getGameDisplayName());
      long start = System.currentTimeMillis();

      LOG.info("Creating temporary archive file " + tempFile.getAbsolutePath());

      ZipFile zipOut = vpaService.createProtectedArchive(tempFile);
      vpaService.createBackup(packageInfo, jobDescriptor, (fileToZip, fileName) -> {
        if (cancelled) {
          return;
        }

        jobDescriptor.setStatus("Packing " + fileToZip.getAbsolutePath());
        if (jobDescriptor.getProgress() < 1 && tempFile.exists()) {
          if (totalSizeExpected > 0) {
            long l = tempFile.length() * 100 / totalSizeExpected / 100;
            jobDescriptor.setProgress(l);
          }
        }
        try {
          ZipUtil.zipFileEncrypted(fileToZip, fileName, zipOut);
        }
        catch (IOException ioe) {
          LOG.error("Cannot add in zip " + fileName, ioe);
        }
      }, game, tableDetails);

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
      String packageInfoJson = objectMapper.writeValueAsString(packageInfo);

      if (!cancelled) {
        File manifestFile = File.createTempFile("package-info", "json");
        manifestFile.deleteOnExit();
        Files.write(manifestFile.toPath(), packageInfoJson.getBytes());
        jobDescriptor.setStatus("Packing " + manifestFile.getAbsolutePath());
        ZipUtil.zipFileEncrypted(manifestFile, BackupPackageInfo.PACKAGE_INFO_JSON_FILENAME, zipOut);
        manifestFile.delete();
      }

      jobDescriptor.setProgress(1);

      backupDescriptor.setSize(target.length());

      File temporaryTarget = new File(target.getParentFile(), target.getName() + ".bak");
      try {
        LOG.info("Copying backup file {} to {}", tempFile.getAbsolutePath(), temporaryTarget.getAbsolutePath());
        jobDescriptor.setStatus("Copying backup file to " + temporaryTarget.getParentFile().getAbsolutePath());
        org.apache.commons.io.FileUtils.copyFile(tempFile, temporaryTarget);
      }
      catch (IOException e) {
        LOG.error("Failed to copy temporary file to target: {}", e.getMessage(), e);
      }
      finally {
        if (!tempFile.delete()) {
          LOG.error("Failed to delete temporary target file {}", tempFile.getAbsolutePath());
        }
      }

      if (target.exists() && backupSettings.isOverwriteBackup()) {
        target.delete();
      }

      if (target.exists() && !target.delete()) {
        target = FileUtils.uniqueFile(target);
        LOG.error("Failed to delete existing backup file, create new one with unique name instead: {}", target.getAbsolutePath());
      }

      boolean renamed = temporaryTarget.renameTo(target);
      if (renamed) {
        LOG.info("Finished packing of " + target.getAbsolutePath() + ", took " + ((System.currentTimeMillis() - start) / 1000) + " seconds, " + FileUtils.readableFileSize(target.length()));
      }
      else {
        LOG.error("Final renaming export file to " + target.getAbsolutePath() + " failed.");
        jobDescriptor.setError("Final renaming export file to " + target.getAbsolutePath() + " failed.");
      }

      if (target.exists() && this.cancelled) {
        target.delete();
      }
    }
    catch (Exception e) {
      LOG.error("Create VPA for " + game.getGameDisplayName() + " failed: " + e.getMessage(), e);
      jobDescriptor.setError("Create VPA for " + game.getGameDisplayName() + " failed: " + e.getMessage());
      return;
    }
    finally {
      LOG.info("********************* /Table Backup: {} ***********************************", game.getGameDisplayName());
    }
  }

  @NonNull
  private static File createBackupTempFile(File target) {
    File tempFile = new File(target.getParentFile(), target.getName() + ".bak");
    try {
      tempFile = File.createTempFile(target.getName(), ".vpa");
    }
    catch (IOException e) {
      LOG.error("Failed to create temp file: {}", e.getMessage(), e);
    }
    tempFile.deleteOnExit();
    return tempFile;
  }

  public void simulateBackup() throws IOException {
    BackupPackageInfo packageInfo = new BackupPackageInfo();
    vpaService.createBackup(packageInfo, new JobDescriptor(), (fileToZip, fileName) -> {
      LOG.info("Added to backup: \"{}\" [Source {}]", fileName, fileToZip.getAbsolutePath());
    }, game, tableDetails);
  }

  @Override
  public void cancel(JobDescriptor jobDescriptor) {
    this.cancelled = true;
  }

  @Override
  public boolean isCancelable() {
    return true;
  }
}
