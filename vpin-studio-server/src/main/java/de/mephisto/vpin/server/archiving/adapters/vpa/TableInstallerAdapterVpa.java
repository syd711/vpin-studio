package de.mephisto.vpin.server.archiving.adapters.vpa;

import de.mephisto.vpin.restclient.archiving.ArchivePackageInfo;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.server.archiving.ArchiveDescriptor;
import de.mephisto.vpin.server.archiving.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPReg;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;

public class TableInstallerAdapterVpa implements TableInstallerAdapter, Job {
  private final static Logger LOG = LoggerFactory.getLogger(TableInstallerAdapterVpa.class);

  private final GameService gameService;
  private final FrontendService frontendService;
  private final ArchiveDescriptor archiveDescriptor;
  private final GameEmulator emulator;

  private File archiveFile;

  public TableInstallerAdapterVpa(@NonNull GameService gameService,
                                  @NonNull FrontendService frontendService,
                                  @NonNull ArchiveDescriptor archiveDescriptor,
                                  @NonNull GameEmulator emulator) {
    this.gameService = gameService;
    this.frontendService = frontendService;
    this.archiveDescriptor = archiveDescriptor;
    this.emulator = emulator;
  }

  public ArchiveDescriptor getArchiveDescriptor() {
    return archiveDescriptor;
  }

  @Override
  public void execute(JobDescriptor result) {
    installTable(result);
  }

  @Nullable
  @Override
  public void installTable(JobDescriptor result) {
    try {
      archiveFile = new File(archiveDescriptor.getSource().getLocation(), archiveDescriptor.getFilename());
      if (!archiveFile.exists()) {
        LOG.error("Failed to import " + archiveFile.getAbsolutePath() + ", file does not exist.");
        result.setError("Failed to import " + archiveFile.getAbsolutePath() + ", file does not exist.");
        return;
      }

      LOG.info("Starting import of " + archiveDescriptor.getFilename());
      result.setStatus("Extracting " + archiveFile.getAbsolutePath());
      unzipArchive();
      LOG.info("Finished unzipping of " + archiveDescriptor.getFilename() + ", starting game import.");

      TableDetails manifest = VpaArchiveUtil.readTableDetails(archiveFile);
      if (StringUtils.isEmpty(manifest.getGameFileName())) {
        LOG.error("The archive manifest of " + archiveFile.getAbsolutePath() + " does not contain a game filename.");
        result.setError("The archive manifest of " + archiveFile.getAbsolutePath() + " does not contain a game filename.");
        return;
      }

      File gameFile = getGameFile(emulator, manifest);
      Game game = gameService.getGameByFilename(emulator.getId(), manifest.getGameFileName());
      if (game == null) {
        LOG.info("No existing game found for " + manifest.getGameDisplayName() + ", executing game import for " + manifest.getGameFileName());
        int newGameId = frontendService.importGame(emulator.getId(), manifest.getGameName(), gameFile.getName(), manifest.getGameDisplayName(), null, new Date(gameFile.lastModified()));
        game = gameService.getGame(newGameId);
      }

      Frontend frontend = frontendService.getFrontend();
      result.setStatus("Importing Game to " + frontend.getName());
      frontendService.saveTableDetails(game.getId(), manifest);

      result.setStatus("Importing Highscores");
      importHighscore(game, archiveFile);

      LOG.info("Executing final table scan for " + game.getGameDisplayName());
      gameService.scanGame(game.getId());

      result.setGameId(game.getId());
    } catch (Exception e) {
      LOG.error("Import of \"" + archiveFile.getName() + "\" failed: " + e.getMessage(), e);
      result.setError("Import of \"" + archiveFile.getName() + "\" failed: " + e.getMessage());
    }
  }

  private void importHighscore(Game game, File zipFile) {
    String jsonData = VpaArchiveUtil.readStringFromZip(zipFile, VPReg.ARCHIVE_FILENAME);
    if (jsonData != null) {
      VPReg vpReg = new VPReg(emulator.getVPRegFile(), game.getRom(), game.getTableName());
      vpReg.restore(jsonData);
      LOG.info("Imported VPReg.stg data.");
    }
  }

  private void unzipArchive() {
//    try {
//      ZipFile zf = new ZipFile(archiveFile);
//      int totalCount = zf.size();
//      zf.close();
//
//      byte[] buffer = new byte[1024];
//      FileInputStream fileInputStream = new FileInputStream(archiveFile);
//      ZipInputStream zis = new ZipInputStream(fileInputStream);
//      ZipEntry zipEntry = zis.getNextEntry();
//      int currentCount = 0;
//      while (zipEntry != null) {
//        currentCount++;
//
//        File newFile = newFile(getDestDirForEntry(zipEntry), zipEntry);
//        if (isExcluded(newFile)) {
//          zis.closeEntry();
//          zipEntry = zis.getNextEntry();
//          continue;
//        }
//
//        LOG.info("Writing " + newFile.getAbsolutePath());
//        if (zipEntry.isDirectory()) {
//          if (!newFile.isDirectory() && !newFile.mkdirs()) {
//            throw new IOException("Failed to create directory " + newFile);
//          }
//        }
//        else {
//          // fix for Windows-created archives
//          File parent = newFile.getParentFile();
//          if (!parent.isDirectory() && !parent.mkdirs()) {
//            throw new IOException("Failed to create directory " + parent);
//          }
//
//          // write file content
//          status = "Extracting " + newFile;
//          FileOutputStream fos = new FileOutputStream(newFile);
//          int len;
//          while ((len = zis.read(buffer)) > 0) {
//            fos.write(buffer, 0, len);
//          }
//          fos.close();
//        }
//
//        progress = currentCount * 100 / totalCount;
//
//        zis.closeEntry();
//        zipEntry = zis.getNextEntry();
//      }
//      fileInputStream.close();
//      zis.closeEntry();
//      zis.close();
//    } catch (Exception e) {
//      LOG.error("Table installation of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
//    }
  }

  private boolean isExcluded(File newFile) {
    String name = newFile.getName();
    return VPReg.ARCHIVE_FILENAME.equals(name) || TableDetails.ARCHIVE_FILENAME.equals(name) || ArchivePackageInfo.ARCHIVE_FILENAME.equals(name);
  }

  private File getDestDirForEntry(ZipEntry entry) {
    EmulatorType type = EmulatorType.fromName(entry.getName());
    if (type != null && type.isVpxEmulator()) {
      return emulator.getInstallationFolder().getParentFile();
    }
    else {
      return frontendService.getFrontendInstallationFolder().getParentFile();
    }
  }

  private File getGameFile(GameEmulator emulator, TableDetails manifest) {
    File tablesFolder = emulator.getGamesFolder();
    return new File(tablesFolder, manifest.getGameFileName());
  }

  public File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    return new File(destinationDir, zipEntry.getName());
  }
}
