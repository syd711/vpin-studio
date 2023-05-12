package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.commons.EmulatorType;
import de.mephisto.vpin.restclient.ArchivePackageInfo;
import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.restclient.TableDetails;
import de.mephisto.vpin.restclient.descriptors.ArchiveInstallDescriptor;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.vpreg.VPReg;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ArchiveInstallerJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveService.class);

  protected final ArchiveInstallDescriptor descriptor;
  protected File archiveFile;
  protected final SystemService systemService;

  private final PinUPConnector connector;
  private final HighscoreService highscoreService;
  private final GameService gameService;
  private final CardService cardService;

  private double progress;
  private String status;

  public ArchiveInstallerJob(@NonNull ArchiveInstallDescriptor descriptor,
                             @NonNull File archiveFile,
                             @NonNull PinUPConnector connector,
                             @NonNull SystemService systemService,
                             @NonNull HighscoreService highscoreService,
                             @NonNull GameService gameService,
                             @NonNull CardService cardService) {
    this.descriptor = descriptor;
    this.archiveFile = archiveFile;
    this.connector = connector;
    this.systemService = systemService;
    this.highscoreService = highscoreService;
    this.gameService = gameService;
    this.cardService = cardService;
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
  public boolean execute() {
    try {
      LOG.info("Starting import of " + descriptor.getFilename());

      status = "Extracting " + archiveFile.getAbsolutePath();
      unzipArchive();
      LOG.info("Finished unzipping of " + descriptor.getFilename() + ", starting Popper import.");

      TableDetails manifest = VpaArchiveUtil.readTableDetails(archiveFile);
      if (StringUtils.isEmpty(manifest.getGameFileName())) {
        LOG.error("The archive manifest of " + archiveFile.getAbsolutePath() + " does not contain a game filename.");
        return false;
      }

      File gameFile = getGameFile(manifest);
      Game game = gameService.getGameByFilename(manifest.getGameFileName());
      if (game == null) {
        LOG.info("No existing game found for " + manifest.getGameDisplayName() + ", executing popper game import for " + manifest.getGameFileName());
        int newGameId = connector.importGame(manifest.getEmulatorType(), manifest.getGameName(), gameFile.getName(), manifest.getGameDisplayName(), null);
        game = gameService.getGame(newGameId);
      }

      status = "Importing Game to Popper";
      connector.importManifest(game, manifest);
      if (descriptor.getPlaylistId() != -1) {
        connector.addToPlaylist(game.getId(), descriptor.getPlaylistId());
      }

      status = "Importing Highscores";
      importHighscore(game, archiveFile);

      LOG.info("Executing final table scan for " + game.getGameDisplayName());
      gameService.scanGame(game.getId());

      cardService.generateCard(game, false);
    } catch (Exception e) {
      LOG.error("Import of \"" + archiveFile.getName() + "\" failed: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  private void importHighscore(Game game, File zipFile) {
    String jsonData = VpaArchiveUtil.readVPRegJson(zipFile);
    if (jsonData != null) {
      VPReg vpReg = new VPReg(systemService.getVPRegFile(), game);
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
    if (name.startsWith("VisualPinball") || name.startsWith("Visual Pinball")) {
      return systemService.getVisualPinballInstallationFolder().getParentFile();
    }
    else if (name.startsWith("PinUPSystem")) {
      return systemService.getPinUPSystemFolder().getParentFile();
    }

    return systemService.getPinUPSystemFolder().getParentFile();
  }

  private File getGameFile(TableDetails manifest) {
    String emulator = manifest.getEmulatorType();
    if (EmulatorType.VISUAL_PINBALL_X.equals(emulator)) {
      return new File(systemService.getVPXTablesFolder(), manifest.getGameFileName());
    }

    if (EmulatorType.FUTURE_PINBALL.equals(emulator)) {
      return new File(systemService.getFuturePinballTablesFolder(), manifest.getGameFileName());
    }

    return new File(systemService.getVPXTablesFolder(), manifest.getGameFileName());
  }

  public File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
//      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }
    return destFile;
  }
}
