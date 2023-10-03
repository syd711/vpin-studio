package de.mephisto.vpin.server.archiving.adapters.vpa;

import de.mephisto.vpin.restclient.archiving.ArchivePackageInfo;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.restclient.popper.EmulatorType;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.server.archiving.ArchiveDescriptor;
import de.mephisto.vpin.server.archiving.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.vpreg.VPReg;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class TableInstallerAdapterVpa implements TableInstallerAdapter, Job {
  private final static Logger LOG = LoggerFactory.getLogger(TableInstallerAdapterVpa.class);

  private final SystemService systemService;
  private final GameService gameService;
  private final PinUPConnector pinUPConnector;
  private final ArchiveDescriptor archiveDescriptor;

  private File archiveFile;
  private double progress;
  private String status;

  public TableInstallerAdapterVpa(@NonNull SystemService systemService,
                                  @NonNull GameService gameService,
                                  @NonNull PinUPConnector pinUPConnector,
                                  @NonNull ArchiveDescriptor archiveDescriptor) {
    this.systemService = systemService;
    this.gameService = gameService;
    this.pinUPConnector = pinUPConnector;
    this.archiveDescriptor = archiveDescriptor;
  }

  public ArchiveDescriptor getArchiveDescriptor() {
    return archiveDescriptor;
  }

  @Override
  public double getProgress() {
    return progress;
  }

  @Override
  public String getStatus() {
    return status;
  }

  @Override
  public JobExecutionResult execute() {
    return installTable();
  }

  @Nullable
  @Override
  public JobExecutionResult installTable() {
    JobExecutionResult result = new JobExecutionResult();
    try {
      archiveFile = new File(archiveDescriptor.getSource().getLocation(), archiveDescriptor.getFilename());
      if (!archiveFile.exists()) {
        LOG.error("Failed to import " + archiveFile.getAbsolutePath() + ", file does not exist.");
        return null;
      }

      LOG.info("Starting import of " + archiveDescriptor.getFilename());
      status = "Extracting " + archiveFile.getAbsolutePath();
      unzipArchive();
      LOG.info("Finished unzipping of " + archiveDescriptor.getFilename() + ", starting Popper import.");

      TableDetails manifest = VpaArchiveUtil.readTableDetails(archiveFile);
      if (StringUtils.isEmpty(manifest.getGameFileName())) {
        LOG.error("The archive manifest of " + archiveFile.getAbsolutePath() + " does not contain a game filename.");
        return null;
      }

      File gameFile = getGameFile(manifest);
      Game game = gameService.getGameByFilename(manifest.getGameFileName());
      if (game == null) {
        LOG.info("No existing game found for " + manifest.getGameDisplayName() + ", executing popper game import for " + manifest.getGameFileName());
        int newGameId = pinUPConnector.importGame(pinUPConnector.getDefaultGameEmulator().getId(), manifest.getGameName(), gameFile.getName(), manifest.getGameDisplayName(), null);
        game = gameService.getGame(newGameId);
      }

      status = "Importing Game to Popper";
      pinUPConnector.saveTableDetails(game.getId(), manifest);

      status = "Importing Highscores";
      importHighscore(game, archiveFile);

      LOG.info("Executing final table scan for " + game.getGameDisplayName());
      gameService.scanGame(game.getId());

      result.setGameId(game.getId());
    } catch (Exception e) {
      LOG.error("Import of \"" + archiveFile.getName() + "\" failed: " + e.getMessage(), e);
      result.setError("Import of \"" + archiveFile.getName() + "\" failed: " + e.getMessage());
    }
    return result;
  }

  private void importHighscore(Game game, File zipFile) {
    String jsonData = VpaArchiveUtil.readVPRegJson(zipFile);
    if (jsonData != null) {
      VPReg vpReg = new VPReg(game.getEmulator().getVPRegFile(), game.getRom(), game.getTableName());
      vpReg.restore(jsonData);
      LOG.info("Imported VPReg.stg data.");
    }
  }

  private void unzipArchive() {
    try {
      ZipFile zf = new ZipFile(archiveFile);
      int totalCount = zf.size();
      zf.close();

      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();
      int currentCount = 0;
      while (zipEntry != null) {
        currentCount++;

        File newFile = newFile(getDestDirForEntry(zipEntry), zipEntry);
        if (isExcluded(newFile)) {
          zis.closeEntry();
          zipEntry = zis.getNextEntry();
          continue;
        }

        LOG.info("Writing " + newFile.getAbsolutePath());
        if (zipEntry.isDirectory()) {
          if (!newFile.isDirectory() && !newFile.mkdirs()) {
            throw new IOException("Failed to create directory " + newFile);
          }
        }
        else {
          // fix for Windows-created archives
          File parent = newFile.getParentFile();
          if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory " + parent);
          }

          // write file content
          status = "Extracting " + newFile;
          FileOutputStream fos = new FileOutputStream(newFile);
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
          fos.close();
        }

        progress = currentCount * 100 / totalCount;

        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (Exception e) {
      LOG.error("Table installation of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
  }

  private boolean isExcluded(File newFile) {
    String name = newFile.getName();
    return VPReg.ARCHIVE_FILENAME.equals(name) || TableDetails.ARCHIVE_FILENAME.equals(name) || ArchivePackageInfo.ARCHIVE_FILENAME.equals(name);
  }

  private File getDestDirForEntry(ZipEntry entry) {
    String name = entry.getName();
    if (Emulator.isVisualPinball(name)) {
      return pinUPConnector.getDefaultGameEmulator().getInstallationFolder().getParentFile();
    }
    else if (name.startsWith("PinUPSystem")) {
      return systemService.getPinUPSystemFolder().getParentFile();
    }

    return systemService.getPinUPSystemFolder().getParentFile();
  }

  private File getGameFile(TableDetails manifest) {
    File tablesFolder = pinUPConnector.getDefaultGameEmulator().getTablesFolder();
    String emulator = manifest.getEmulatorType();
    if (EmulatorType.VISUAL_PINBALL_X.equals(emulator)) {
      return new File(tablesFolder, manifest.getGameFileName());
    }

    return new File(tablesFolder, manifest.getGameFileName());
  }

  public File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    return new File(destinationDir, zipEntry.getName());
  }
}
