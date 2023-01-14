package de.mephisto.vpin.server.vpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.popper.GameMediaItem;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class VpaExporter {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  private final Game game;
  private VpaManifest manifest;
  private final File target;
  private final VpaExportListener listener;
  private ObjectMapper objectMapper;

  public VpaExporter(@NonNull Game game, @NonNull VpaManifest manifest, @NonNull File target, @NonNull VpaExportListener listener) {
    this.game = game;
    this.manifest = manifest;
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

      String manifestString = objectMapper.writeValueAsString(manifest);
      File manifestFile = File.createTempFile("vpa-manifest", "json");
      manifestFile.deleteOnExit();
      Files.write(manifestFile.toPath(), manifestString.getBytes());
      zipFile(manifestFile, "manifest.json", zipOut);

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
      if (game.getAltSoundFolder().exists()) {
        zipFile(game.getAltSoundFolder(), getGameFolderName() + "/VPinMAME/altsound/" + game.getAltSoundFolder().getName(), zipOut);
      }

      if (game.getMusicFolder().exists()) {
        zipFile(game.getMusicFolder(), getGameFolderName() + "/Music/" + game.getMusicFolder().getName(), zipOut);
      }

      // PupPack and Media
      if (game.getPupPackFolder().exists()) {
        LOG.info("Packing " + game.getPupPackFolder().getAbsolutePath());
        zipFile(game.getPupPackFolder(), "PinUPSystem/PUPVideos/" + game.getPupPackFolder().getName(), zipOut);
      }

      PopperScreen[] values = PopperScreen.values();
      for (PopperScreen value : values) {
        GameMediaItem gameMediaItem = game.getGameMedia().get(value);
        if (gameMediaItem != null && gameMediaItem.getFile().exists()) {
          LOG.info("Packing " + gameMediaItem.getFile().getAbsolutePath());
          zipFile(gameMediaItem.getFile(), "PinUPSystem/POPMedia/" + getPupUpMediaFolderName() + "/" + value.name() + "/" + gameMediaItem.getFile().getName(), zipOut);
        }
      }
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
      for (File childFile : children) {
        zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
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
}
