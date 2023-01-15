package de.mephisto.vpin.server.vpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import de.mephisto.vpin.commons.EmulatorTypes;
import de.mephisto.vpin.restclient.ExportDescriptor;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreVersion;
import de.mephisto.vpin.server.popper.GameMediaItem;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class VpaExporter {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  private final Game game;
  private final ExportDescriptor exportDescriptor;
  private final List<HighscoreVersion> scoreHistory;
  private final File target;
  private final VpaExportListener listener;
  private final ObjectMapper objectMapper;

  public VpaExporter(@NonNull Game game, @NonNull ExportDescriptor exportDescriptor, @NonNull List<HighscoreVersion> scoreHistory, @NonNull File target, @NonNull VpaExportListener listener) {
    this.game = game;
    this.exportDescriptor = exportDescriptor;
    this.scoreHistory = scoreHistory;
    this.target = target;
    this.listener = listener;
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  public void export() {
    if (target.exists() && !target.delete()) {
      throw new UnsupportedOperationException("Couldn't delete existing VPA file " + target.getAbsolutePath());
    }

    LOG.info("Packaging " + game.getGameDisplayName());
    long start = System.currentTimeMillis();
    FileOutputStream fos = null;
    ZipOutputStream zipOut = null;
    try {
      fos = new FileOutputStream(target);
      zipOut = new ZipOutputStream(fos);

      //store highscore history
      if (exportDescriptor.isExportHighscores()) {
        List<ScoreEntry> scores = scoreHistory.stream().map(ScoreEntry::new).collect(Collectors.toList());
        exportDescriptor.getManifest().getAdditionalData().put("highscores", scores);
      }

      zipManifest(zipOut);

      if (game.getRomFile() != null && game.getRomFile().exists()) {
        zipFile(game.getRomFile(), getGameFolderName() + "/VPinMAME/roms/" + game.getRomFile().getName(), zipOut);
      }

      if (game.getPOVFile() != null && game.getPOVFile().exists()) {
        zipFile(game.getPOVFile(), getGameFolderName() + "/Tables/" + game.getPOVFile().getName(), zipOut);
      }

      if (game.getGameFile().exists()) {
        zipFile(game.getGameFile(), getGameFolderName() + "/Tables/" + game.getGameFile().getName(), zipOut);
      }

      if (game.getDirectB2SFile().exists()) {
        zipFile(game.getDirectB2SFile(), getGameFolderName() + "/Tables/" + game.getDirectB2SFile().getName(), zipOut);
      }

      // DMDs
      if (game.getUltraDMDFolder().exists()) {
        zipFile(game.getUltraDMDFolder(), getGameFolderName() + "/Tables/" + game.getUltraDMDFolder().getName(), zipOut);
      }

      if (game.getFlexDMDFolder().exists()) {
        zipFile(game.getFlexDMDFolder(), getGameFolderName() + "/Tables/" + game.getFlexDMDFolder().getName(), zipOut);
      }

      // Highscores
      if (game.getEMHighscoreFile() != null && game.getEMHighscoreFile().exists()) {
        zipFile(game.getEMHighscoreFile(), getGameFolderName() + "/User/" + game.getEMHighscoreFile().getName(), zipOut);
      }

      if (game.getNvRamFile().exists()) {
        zipFile(game.getNvRamFile(), getGameFolderName() + "/VPinMAME/nvram/" + game.getNvRamFile().getName(), zipOut);
      }

      // Music and sounds
      if (game.getAltSoundFolder() != null && game.getAltSoundFolder().exists()) {
        zipFile(game.getAltSoundFolder(), getGameFolderName() + "/VPinMAME/altsound/" + game.getAltSoundFolder().getName(), zipOut);
      }

      if (game.getMusicFolder() != null && game.getMusicFolder().exists()) {
        zipFile(game.getMusicFolder(), getGameFolderName() + "/Music/" + game.getMusicFolder().getName(), zipOut);
      }

      zipPupPack(zipOut);
      zipPopperMedia(zipOut);

      LOG.info("Finished packing of " + target.getAbsolutePath() + ", took " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
    } catch (Exception e) {
      LOG.error("Create VPA for " + game.getGameDisplayName() + " failed: " + e.getMessage(), e);
    } finally {
      try {
        if (zipOut != null) {
          zipOut.close();
        }
      } catch (IOException e) {
        //ignore
      }

      try {
        if (fos != null) {
          fos.close();
        }
      } catch (IOException e) {
        //ignore
      }
    }
  }

  private void zipPopperMedia(ZipOutputStream zipOut) throws IOException {
    //export popper menu data
    if (exportDescriptor.isExportPopperMedia()) {
      PopperScreen[] values = PopperScreen.values();
      for (PopperScreen value : values) {
        GameMediaItem gameMediaItem = game.getGameMedia().get(value);
        if (gameMediaItem != null && gameMediaItem.getFile().exists()) {
          LOG.info("Packing " + gameMediaItem.getFile().getAbsolutePath());
          zipFile(gameMediaItem.getFile(), "PinUPSystem/POPMedia/" + getPupUpMediaFolderName() + "/" + value.name() + "/" + gameMediaItem.getFile().getName(), zipOut);
        }
      }
    }
  }

  /**
   * Archives the PUP pack
   *
   * @param zipOut
   * @throws IOException
   */
  private void zipPupPack(ZipOutputStream zipOut) throws IOException {
    if (exportDescriptor.isExportPupPack()) {
      if (game.getPupPackFolder() != null && game.getPupPackFolder().exists()) {
        LOG.info("Packing " + game.getPupPackFolder().getAbsolutePath());
        zipFile(game.getPupPackFolder(), "PinUPSystem/PUPVideos/" + game.getPupPackFolder().getName(), zipOut);
      }
    }
  }

  private void zipManifest(ZipOutputStream zipOut) throws IOException {
    //store wheel icon as archive preview
    GameMediaItem mediaItem = game.getGameMedia().get(PopperScreen.Wheel);
    if (mediaItem != null) {
      byte[] bytes = Files.readAllBytes(mediaItem.getFile().toPath());
      Base64Encoder encoder = new Base64Encoder();
      String encode = encoder.encode(bytes);
      exportDescriptor.getManifest().setIcon(encode);
    }


    String extension = FilenameUtils.getExtension(game.getGameFile().getName());
    if (extension.equals("vpx")) {
      exportDescriptor.getManifest().setEmulatorType(EmulatorTypes.VISUAL_PINBALL_X);
    }
    else if (extension.equals("fp")) {
      exportDescriptor.getManifest().setEmulatorType(EmulatorTypes.FUTURE_PINBALL);
    }

    String manifestString = objectMapper.writeValueAsString(exportDescriptor.getManifest());
    File manifestFile = File.createTempFile("vpa-manifest", "json");
    manifestFile.deleteOnExit();
    Files.write(manifestFile.toPath(), manifestString.getBytes());
    zipFile(manifestFile, "manifest.json", zipOut);
  }

  private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
    if (fileToZip.isHidden()) {
      return;
    }
    if (fileToZip.isDirectory()) {
      if (fileName.endsWith("/")) {
        zipOut.putNextEntry(new ZipEntry(fileName));
        zipOut.closeEntry();
      }
      else {
        zipOut.putNextEntry(new ZipEntry(fileName + "/"));
        zipOut.closeEntry();
      }
      this.listener.exported(fileToZip, fileName);

      File[] children = fileToZip.listFiles();
      if (children != null) {
        for (File childFile : children) {
          zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
        }
      }
      return;
    }
    FileInputStream fis = new FileInputStream(fileToZip);
    ZipEntry zipEntry = new ZipEntry(fileName);
    zipOut.putNextEntry(zipEntry);
    byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
      zipOut.write(bytes, 0, length);
    }
    fis.close();
  }

  private String getGameFolderName() {
    String filename = game.getGameFile().getName();
    if (filename.endsWith(".fp")) {
      return "FuturePinball";
    }

    if (filename.endsWith(".fx")) {
      return "Pinball FX3";
    }

    return "VisualPinball";
  }

  private String getPupUpMediaFolderName() {
    String filename = game.getGameFile().getName();
    if (filename.endsWith(".fp")) {
      return "Future Pinball";
    }

    if (filename.endsWith(".fx")) {
      return "Pinball FX3";
    }

    return "Visual Pinball X";
  }

  static class ScoreEntry {
    private final String oldRaw;
    private final String newRaw;
    private final int changedPosition;
    private final Date createdAt;

    private ScoreEntry(HighscoreVersion version) {
      this.oldRaw = version.getOldRaw();
      this.newRaw = version.getNewRaw();
      this.changedPosition = version.getChangedPosition();
      this.createdAt = version.getCreatedAt();
    }

    public String getOldRaw() {
      return oldRaw;
    }

    public String getNewRaw() {
      return newRaw;
    }

    public int getChangedPosition() {
      return changedPosition;
    }

    public Date getCreatedAt() {
      return createdAt;
    }
  }
}
