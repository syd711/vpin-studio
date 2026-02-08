package de.mephisto.vpin.server.vpxz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.preferences.VPXZSettings;
import de.mephisto.vpin.restclient.vpxz.VPXZPackageInfo;
import de.mephisto.vpin.restclient.vpxz.VPXZType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.ZipUtil;
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

public class VPXZCreationJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZCreationJob.class);

  @NonNull
  private final VPXZSource source;
  private final Game game;
  private final TableDetails tableDetails;
  private final VPXZSettings vpxzSettings;
  private final VPXZFileService vpxzFileService;
  private boolean cancelled = false;

  public VPXZCreationJob(@NonNull VPXZFileService vpxzFileService,
                         @NonNull VPXZSource source,
                         @NonNull Game game,
                         @NonNull TableDetails tableDetails,
                         @NonNull VPXZSettings vpxzSettings) {
    this.vpxzFileService = vpxzFileService;
    this.source = source;
    this.game = game;
    this.tableDetails = tableDetails;
    this.vpxzSettings = vpxzSettings;
  }

  public void execute(@NonNull JobDescriptor jobDescriptor) {
    create(jobDescriptor);
  }

  public void create(JobDescriptor jobDescriptor) {
    LOG.info("********************* VPXZ: {} ***********************************", game.getGameDisplayName());
    VPXZDescriptor descriptor = new VPXZDescriptor();
    VPXZPackageInfo packageInfo = new VPXZPackageInfo();

    descriptor.setCreatedAt(new Date());
    descriptor.setTableDetails(tableDetails);
    descriptor.setPackageInfo(packageInfo);

    jobDescriptor.setStatus("Calculating export size of " + game.getGameDisplayName());
    long totalSizeExpected = vpxzFileService.calculateTotalSize(game);
    LOG.info("Calculated total approx. size of " + FileUtils.readableFileSize(totalSizeExpected) + " for the .vpxz file of " + game.getGameDisplayName());

    String baseName = FilenameUtils.getBaseName(game.getGameFileName());
    File targetFolder = new File(source.getLocation());
    if (!targetFolder.exists()) {
      targetFolder = VPXZSourceImpl.FOLDER;
    }

    File target = new File(targetFolder, baseName + "." + VPXZType.VPXZ.name().toLowerCase());
    if (target.exists() && !vpxzSettings.isOverwriteFile()) {
      target = FileUtils.uniqueFile(target);
    }
    descriptor.setFilename(target.getName());

    try {
      File tempFile = File.createTempFile(target.getName(), ".bak");
      //---------
      LOG.info("Packaging " + game.getGameDisplayName());
      long start = System.currentTimeMillis();

      LOG.info("Creating temporary vpxz file " + tempFile.getAbsolutePath());

      ZipFile zipOut = vpxzFileService.createVpxzZip(tempFile);
      vpxzFileService.createVpxz(packageInfo, jobDescriptor, (fileToZip, fileName) -> {
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
          ZipUtil.zipFileUnencrypted(fileToZip, fileName, zipOut);
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
        ZipUtil.zipFileUnencrypted(manifestFile, VPXZPackageInfo.PACKAGE_INFO_JSON_FILENAME, zipOut);
        manifestFile.delete();
      }

      jobDescriptor.setProgress(1);

      descriptor.setSize(target.length());

      File temporaryTarget = new File(target.getParentFile(), target.getName() + ".bak");
      try {
        LOG.info("Copying vpxz file {} to {}", tempFile.getAbsolutePath(), temporaryTarget.getAbsolutePath());
        jobDescriptor.setStatus("Copying vpxz file to " + temporaryTarget.getParentFile().getAbsolutePath());
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

      if (target.exists() && vpxzSettings.isOverwriteFile()) {
        target.delete();
      }

      if (target.exists() && !target.delete()) {
        target = FileUtils.uniqueFile(target);
        LOG.error("Failed to delete existing .vpxz file, create new one with unique name instead: {}", target.getAbsolutePath());
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
      LOG.error("Create .vpxz for " + game.getGameDisplayName() + " failed: " + e.getMessage(), e);
      jobDescriptor.setError("Create .vpxz for " + game.getGameDisplayName() + " failed: " + e.getMessage());
      return;
    }
    finally {
      LOG.info("********************* /VPXZ: {} ***********************************", game.getGameDisplayName());
    }
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
