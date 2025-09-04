package de.mephisto.vpin.server.backups.adapters.vpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.backups.BackupPackageInfo;
import de.mephisto.vpin.restclient.backups.BackupType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
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
  private final VpaService vpaService;
  private boolean cancelled = false;

  public TableBackupAdapterVpa(@NonNull VpaService vpaService,
                               @NonNull BackupSource backupSource,
                               @NonNull Game game,
                               @NonNull TableDetails tableDetails) {
    this.vpaService = vpaService;
    this.backupSource = backupSource;
    this.game = game;
    this.tableDetails = tableDetails;
  }

  public void createBackup(JobDescriptor jobDescriptor) {
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
    target = FileUtils.uniqueFile(target);
    backupDescriptor.setFilename(target.getName());

    File tempFile = new File(target.getParentFile(), target.getName() + ".bak");

    if (target.exists() && !target.delete()) {
      throw new UnsupportedOperationException("Couldn't delete existing archive file " + target.getAbsolutePath());
    }
    if (tempFile.exists() && !tempFile.delete()) {
      throw new UnsupportedOperationException("Couldn't delete existing temporary archive file " + target.getAbsolutePath());
    }

    //---------
    LOG.info("Packaging " + game.getGameDisplayName());
    long start = System.currentTimeMillis();

    LOG.info("Creating temporary archive file " + tempFile.getAbsolutePath());

    try {
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
    }
    catch (Exception e) {
      LOG.error("Create VPA for " + game.getGameDisplayName() + " failed: " + e.getMessage(), e);
      jobDescriptor.setError("Create VPA for " + game.getGameDisplayName() + " failed: " + e.getMessage());
      return;
    }
    finally {
      boolean renamed = tempFile.renameTo(target);
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
