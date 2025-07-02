package de.mephisto.vpin.server.archiving.adapters.vpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.archiving.ArchiveType;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.restclient.archiving.ArchivePackageInfo;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.server.archiving.ArchiveDescriptor;
import de.mephisto.vpin.server.archiving.ArchiveSourceAdapter;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.zip.ZipOutputStream;

public class TableBackupAdapterVpa implements TableBackupAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(TableBackupAdapterVpa.class);
  private final Game game;
  private final TableDetails tableDetails;

  private final VpaService vpaService;
 

  public TableBackupAdapterVpa(@NonNull VpaService vpaService,
                               @NonNull ArchiveSourceAdapter archiveSourceAdapter,
                               @NonNull Game game,
                               @NonNull TableDetails tableDetails) {
    this.vpaService = vpaService;
    this.game = game;
    this.tableDetails = tableDetails;
  }

  public void createBackup(JobDescriptor result) {
    ArchiveDescriptor archiveDescriptor = new ArchiveDescriptor();
    ArchivePackageInfo packageInfo = new ArchivePackageInfo();

    archiveDescriptor.setCreatedAt(new Date());
    archiveDescriptor.setTableDetails(tableDetails);
    archiveDescriptor.setPackageInfo(packageInfo);

    result.setStatus("Calculating export size of " + game.getGameDisplayName());
    long totalSizeExpected = vpaService.calculateTotalSize(game);
    LOG.info("Calculated total approx. size of " + FileUtils.readableFileSize(totalSizeExpected) + " for the archive of " + game.getGameDisplayName());

    String baseName = FilenameUtils.getBaseName(game.getGameFileName());
    File target = new File(VpaArchiveSource.FOLDER, baseName + "." + ArchiveType.VPA.name().toLowerCase());
    target = FileUtils.uniqueFile(target);
    archiveDescriptor.setFilename(target.getName());

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

    try (FileOutputStream fos = new FileOutputStream(tempFile);
        ZipOutputStream zipOut = new ZipOutputStream(fos)) {

      vpaService.createBackup(packageInfo, (fileToZip, fileName) -> {
        result.setStatus("Packing " + fileToZip.getAbsolutePath());
        if (result.getProgress() < 1 && tempFile.exists()) {
          if (totalSizeExpected > 0) {
            long l = tempFile.length() * 100 / totalSizeExpected / 100;
            result.setProgress(l);
          }
        }
        try {
          ZipUtil.zipFile(fileToZip, fileName, zipOut);
        }
        catch (IOException ioe) {
          LOG.error("Cannot add in zip " + fileName, ioe);
        }
      }, game, tableDetails);

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
      String packageInfoJson = objectMapper.writeValueAsString(packageInfo);

      File manifestFile = File.createTempFile("package-info", "json");
      manifestFile.deleteOnExit();
      Files.write(manifestFile.toPath(), packageInfoJson.getBytes());

      result.setStatus("Packing " + manifestFile.getAbsolutePath());
      result.setProgress(1);
      ZipUtil.zipFile(manifestFile, ArchivePackageInfo.ARCHIVE_FILENAME, zipOut);
      manifestFile.delete();
    }
    catch (Exception e) {
      LOG.error("Create VPA for " + game.getGameDisplayName() + " failed: " + e.getMessage(), e);
      result.setError("Create VPA for " + game.getGameDisplayName() + " failed: " + e.getMessage());
      return;
    }
    finally {
      boolean renamed = tempFile.renameTo(target);
      if (renamed) {
        LOG.info("Finished packing of " + target.getAbsolutePath() + ", took " + ((System.currentTimeMillis() - start) / 1000) + " seconds, " + FileUtils.readableFileSize(target.length()));
      }
      else {
        LOG.error("Final renaming export file to " + target.getAbsolutePath() + " failed.");
        result.setError("Final renaming export file to " + target.getAbsolutePath() + " failed.");
        return;
      }
    }

    archiveDescriptor.setSize(target.length());
  }

  public void simulateBackup() throws IOException {
    ArchivePackageInfo packageInfo = new ArchivePackageInfo();
    vpaService.createBackup(packageInfo, (fileToZip, fileName) -> {
      LOG.info("Backup file " + fileName + ", " + fileToZip.getAbsolutePath());
    }, game, tableDetails);
  }
}
