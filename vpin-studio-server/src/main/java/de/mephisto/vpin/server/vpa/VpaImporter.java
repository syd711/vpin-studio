package de.mephisto.vpin.server.vpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.EmulatorTypes;
import de.mephisto.vpin.restclient.ImportDescriptor;
import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VpaImporter {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  private final ImportDescriptor descriptor;
  private final File vpaFile;
  private final PinUPConnector connector;
  private final SystemService systemService;
  private final HighscoreService highscoreService;
  private final ObjectMapper objectMapper;

  public VpaImporter(@NonNull ImportDescriptor descriptor, @NonNull File file, @NonNull PinUPConnector connector,
                     @NonNull SystemService systemService, @NonNull HighscoreService highscoreService) {
    this.descriptor = descriptor;
    this.vpaFile = file;
    this.connector = connector;
    this.systemService = systemService;
    this.highscoreService = highscoreService;

    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  public int startImport() {
    try {
      LOG.info("Starting import of " + descriptor.getVpaFileName());

      boolean importRom = descriptor.isImportRom();
      boolean importPopperMedia = descriptor.isImportPopperMedia();
      boolean importPupPack = descriptor.isImportPupPack();

      unzipVpa(importRom, importPopperMedia, importPupPack);
      LOG.info("Finished unzipping of " + descriptor.getVpaFileName() + ", starting Popper import.");

      VpaManifest manifest = VpaUtil.readManifest(vpaFile);
      if(StringUtils.isEmpty(manifest.getGameFileName())) {
        LOG.error("The VPA manifest of " + vpaFile.getAbsolutePath() + " does not contain a game filename.");
        return -1;
      }

      File gameFile = getGameFile(manifest);
      Game gameByFilename = connector.getGameByFilename(manifest.getGameFileName());
      if (gameByFilename == null) {
        LOG.info("No existing game found for " + manifest.getGameDisplayName() + ", executing popper game import for " + manifest.getGameFileName());
        int newGameId = connector.importGame(manifest.getEmulatorType(), manifest.getGameName(), gameFile.getName(), manifest.getGameDisplayName());
        gameByFilename = connector.getGame(newGameId);
      }

      connector.importManifest(gameByFilename, manifest);

      if (descriptor.getPlaylistId() != -1) {
        connector.addToPlaylist(gameByFilename.getId(), descriptor.getPlaylistId());
      }

      boolean importHighscores = descriptor.isImportHighscores();
      if (importHighscores) {

        if(manifest.getAdditionalData().containsKey(VpaService.DATA_HIGHSCORE_HISTORY)) {
          String json = (String) manifest.getAdditionalData().get(VpaService.DATA_HIGHSCORE_HISTORY);
          VpaExporterJob.ScoreVersionEntry[] scores = objectMapper.readValue(json, VpaExporterJob.ScoreVersionEntry[].class);
          LOG.info("Importing " + scores.length + " scores.");
          for (VpaExporterJob.ScoreVersionEntry score : scores) {
            highscoreService.importScoreEntry(gameByFilename, score);
          }
        }

        if(manifest.getAdditionalData().containsKey(VpaService.DATA_HIGHSCORE)) {
          String raw = (String) manifest.getAdditionalData().get(VpaService.DATA_HIGHSCORE);
          List<Score> scores = highscoreService.parseScores(new Date(gameFile.lastModified()), raw, -1, -1);
//          parse all!
        }
      }

      return gameByFilename.getId();
    } catch (Exception e) {
      LOG.error("Import failed: " + e.getMessage(), e);
    }
    return -1;
  }

  private void unzipVpa(boolean importRom, boolean importPopperMedia, boolean importPupPack) {
    try {
      byte[] buffer = new byte[1024];
      ZipInputStream zis = new ZipInputStream(new FileInputStream(vpaFile));
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        File newFile = newFile(getDestDirForEntry(zipEntry), zipEntry);
        if (newFile.getParentFile().getName().equals("roms") && !importRom) {
          zipEntry = zis.getNextEntry();
          continue;
        }

        if (newFile.getAbsolutePath().contains("POPMedia") && !importPopperMedia) {
          zipEntry = zis.getNextEntry();
          continue;
        }

        if (newFile.getAbsolutePath().contains("PUPVideos") && !importPupPack) {
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
          FileOutputStream fos = new FileOutputStream(newFile);
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
          fos.close();
        }
        zipEntry = zis.getNextEntry();
      }

      zis.closeEntry();
      zis.close();
    } catch (Exception e) {
      LOG.error("VPA import of " + vpaFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
  }

  private File getDestDirForEntry(ZipEntry entry) {
    String name = entry.getName();
    if (name.startsWith("VisualPinball")) {
      return systemService.getVisualPinballInstallationFolder().getParentFile();
    }
    else if (name.startsWith("PinUPSystem")) {
      return systemService.getPinUPSystemFolder().getParentFile();
    }

    return systemService.getPinUPSystemFolder().getParentFile();
  }

  private File getGameFile(VpaManifest manifest) {
    String emulator = manifest.getEmulatorType();
    if (EmulatorTypes.VISUAL_PINBALL_X.equals(emulator)) {
      return new File(systemService.getVPXTablesFolder(), manifest.getGameFileName());
    }

    if (EmulatorTypes.FUTURE_PINBALL.equals(emulator)) {
      return new File(systemService.getFuturePinballTablesFolder(), manifest.getGameFileName());
    }

    return new File(systemService.getVPXTablesFolder(), manifest.getGameFileName());
  }

  public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }
    return destFile;
  }
}
