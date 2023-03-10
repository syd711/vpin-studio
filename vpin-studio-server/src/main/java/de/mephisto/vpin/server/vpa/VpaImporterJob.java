package de.mephisto.vpin.server.vpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.EmulatorType;
import de.mephisto.vpin.restclient.VpaImportDescriptor;
import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.vpreg.VPReg;
import de.mephisto.vpin.server.util.vpreg.VPRegScoreSummary;
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

public class VpaImporterJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  protected final VpaImportDescriptor descriptor;
  protected File vpaFile;
  protected final SystemService systemService;

  private final PinUPConnector connector;
  private final HighscoreService highscoreService;
  private final GameService gameService;
  private final CardService cardService;
  private final ObjectMapper objectMapper;

  private double progress;
  private String status;

  public VpaImporterJob(@NonNull VpaImportDescriptor descriptor,
                        @NonNull File vpaFile,
                        @NonNull PinUPConnector connector,
                        @NonNull SystemService systemService,
                        @NonNull HighscoreService highscoreService,
                        @NonNull GameService gameService,
                        @NonNull CardService cardService) {
    this.descriptor = descriptor;
    this.vpaFile = vpaFile;
    this.connector = connector;
    this.systemService = systemService;
    this.highscoreService = highscoreService;
    this.gameService = gameService;
    this.cardService = cardService;

    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
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
      LOG.info("Starting import of " + descriptor.getUuid());

      boolean importRom = descriptor.isImportRom();
      boolean importPopperMedia = descriptor.isImportPopperMedia();
      boolean importPupPack = descriptor.isImportPupPack();
      boolean importHighscores = descriptor.isImportHighscores();

      status = "Extracting " + vpaFile.getAbsolutePath();
      unzipVpa(importRom, importPopperMedia, importPupPack, importHighscores);
      LOG.info("Finished unzipping of " + descriptor.getUuid() + ", starting Popper import.");

      VpaManifest manifest = VpaUtil.readManifest(vpaFile);
      if (StringUtils.isEmpty(manifest.getGameFileName())) {
        LOG.error("The VPA manifest of " + vpaFile.getAbsolutePath() + " does not contain a game filename.");
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
      game.setRom(manifest.getRomName());
      game.setTableName(manifest.getTableName());

      if (descriptor.getPlaylistId() != -1) {
        connector.addToPlaylist(game.getId(), descriptor.getPlaylistId());
      }

      if (importHighscores) {
        status = "Importing Highscores";
        importHighscores(game, manifest);
      }

      highscoreService.scanScore(game);
      LOG.info("Final highscore scan");

      cardService.generateCard(game, false);
    } catch (Exception e) {
      LOG.error("Import of \"" + vpaFile.getName() + "\" failed: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  private void importHighscores(Game game, VpaManifest manifest) {
    try {
      if (manifest.getAdditionalData().containsKey(VpaService.DATA_HIGHSCORE_HISTORY)) {
        String json = (String) manifest.getAdditionalData().get(VpaService.DATA_HIGHSCORE_HISTORY);
        VpaExporterJob.ScoreVersionEntry[] scores = objectMapper.readValue(json, VpaExporterJob.ScoreVersionEntry[].class);
        for (VpaExporterJob.ScoreVersionEntry score : scores) {
          highscoreService.importScoreEntry(game, score);
        }
        LOG.info("Finished importing " + scores.length + " highscore version entries.");
      }
    } catch (Exception e) {
      LOG.error("Error importing highscore history of " + game.getGameDisplayName() + ": " + e.getMessage(), e);
    }

    try {
      if (manifest.getAdditionalData().containsKey(VpaService.DATA_VPREG_HIGHSCORE)) {
        String json = (String) manifest.getAdditionalData().get(VpaService.DATA_VPREG_HIGHSCORE);
        VPRegScoreSummary summary = objectMapper.readValue(json, VPRegScoreSummary.class);
        VPReg vpReg = new VPReg(systemService.getVPRegFile(), game);
        vpReg.restoreHighscore(summary);
      }
    } catch (Exception e) {
      LOG.error("Error importing VPReg scores of " + game.getGameDisplayName() + ": " + e.getMessage(), e);
    }
  }

  private void unzipVpa(boolean importRom, boolean importPopperMedia, boolean importPupPack, boolean importHighscores) {
    try {
      ZipFile zf = new ZipFile(vpaFile);
      int totalCount = zf.size();


      byte[] buffer = new byte[1024];
      ZipInputStream zis = new ZipInputStream(new FileInputStream(vpaFile));
      ZipEntry zipEntry = zis.getNextEntry();
      int currentCount = 0;
      while (zipEntry != null) {
        currentCount++;
        File newFile = newFile(getDestDirForEntry(zipEntry), zipEntry);
        String folderName = newFile.getParentFile().getName();

        if (folderName.equals("roms") && !importRom) {
          zipEntry = zis.getNextEntry();
          continue;
        }

        if ((folderName.equals("User") || folderName.equals("nvram")) && !importHighscores) {
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

        if (newFile.getAbsolutePath().replaceAll("\\\\", "/").contains("VisualPinball/Music") && !importPupPack) {
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

    //copy directb2s file to another folder
    if (zipEntry.getName().endsWith(".directb2s") && zipEntry.getName().contains("AdditionalFiles")) {
      String name = zipEntry.getName().substring(zipEntry.getName().lastIndexOf('/') + 1);
      destFile = new File(systemService.getDirectB2SMediaFolder(), name);
      LOG.info("Importing media directb2s file \"" + destFile.getCanonicalPath() + "\"");
    }

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
//      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }
    return destFile;
  }
}
