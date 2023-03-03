package de.mephisto.vpin.server.vpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.HighscoreType;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.*;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreVersion;
import de.mephisto.vpin.server.popper.GameMediaItem;
import de.mephisto.vpin.server.popper.WheelAugmenter;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.util.vpreg.VPReg;
import de.mephisto.vpin.server.util.vpreg.VPRegScoreSummary;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class VpaExporterJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);
  public static final int TARGET_WHEEL_SIZE_WIDTH = 100;

  private final File vprRegFile;
  private final File musicFolder;
  private final Game game;
  private final ExportDescriptor exportDescriptor;
  private final VpaManifest manifest;
  private final Highscore highscore;
  private final List<HighscoreVersion> scoreHistory;
  private final ObjectMapper objectMapper;
  private final VpaSourceAdapter vpaSourceAdapter;

  private final File targetFolder;
  private final String vpaVersion;

  public VpaExporterJob(@NonNull File vprRegFile,
                        @NonNull File musicFolder,
                        @NonNull Game game,
                        @NonNull ExportDescriptor exportDescriptor,
                        @NonNull VpaManifest manifest,
                        @Nullable Highscore highscore,
                        @NonNull List<HighscoreVersion> scoreHistory,
                        @NonNull VpaSourceAdapter vpaSource,
                        @NonNull File targetFolder,
                        @NonNull String vpaVersion) {
    this.vprRegFile = vprRegFile;
    this.musicFolder = musicFolder;
    this.game = game;
    this.exportDescriptor = exportDescriptor;
    this.manifest = manifest;
    this.highscore = highscore;
    this.scoreHistory = scoreHistory;
    this.vpaSourceAdapter = vpaSource;
    this.targetFolder = targetFolder;
    this.vpaVersion = vpaVersion;
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
  }

  public boolean execute() {
    String baseName = FilenameUtils.getBaseName(manifest.getGameFileName());
    File target = new File(targetFolder, baseName + ".vpa");
    target = FileUtils.uniqueFile(target);
    File tempFile = new File(target.getParentFile(), target.getName() + ".bak");

    if (target.exists() && !target.delete()) {
      throw new UnsupportedOperationException("Couldn't delete existing VPA file " + target.getAbsolutePath());
    }
    if (tempFile.exists() && !tempFile.delete()) {
      throw new UnsupportedOperationException("Couldn't delete existing temporary VPA file " + target.getAbsolutePath());
    }

    VpaPackageInfo packageInfo = new VpaPackageInfo();
    manifest.setPackageInfo(packageInfo);

    LOG.info("Packaging " + game.getGameDisplayName());
    long start = System.currentTimeMillis();
    FileOutputStream fos = null;
    ZipOutputStream zipOut = null;
    try {
      LOG.info("Creating temporary archive file " + tempFile.getAbsolutePath());
      fos = new FileOutputStream(tempFile);
      zipOut = new ZipOutputStream(fos);

      //store highscore history
      if (exportDescriptor.isExportHighscores()) {
        //zip EM file
        if (game.getEMHighscoreFile() != null && game.getEMHighscoreFile().exists()) {
          packageInfo.setHighscore(true);
          zipFile(game.getEMHighscoreFile(), getGameFolderName() + "/User/" + game.getEMHighscoreFile().getName(), zipOut);
        }

        //zip nvram file
        if (game.getNvRamFile().exists()) {
          packageInfo.setHighscore(true);
          zipFile(game.getNvRamFile(), getGameFolderName() + "/VPinMAME/nvram/" + game.getNvRamFile().getName(), zipOut);
        }

        //write VPReg.stg data
        if (HighscoreType.VPReg.equals(game.getHighscoreType())) {
          packageInfo.setHighscore(true);
          VPReg reg = new VPReg(vprRegFile, game);
          VPRegScoreSummary summary = reg.readHighscores();
          String vpRegJson = objectMapper.writeValueAsString(summary);
          manifest.getAdditionalData().put(VpaService.DATA_VPREG_HIGHSCORE, vpRegJson);
        }

        //write highscore history
        List<ScoreVersionEntry> scores = scoreHistory.stream().map(ScoreVersionEntry::new).collect(Collectors.toList());
        packageInfo.setHighscoreHistoryRecords(scores.size());
        String scoresJson = objectMapper.writeValueAsString(scores);
        manifest.getAdditionalData().put(VpaService.DATA_HIGHSCORE_HISTORY, scoresJson);

        //write raw highscore
        if (highscore != null && highscore.getRaw() != null) {
          manifest.getAdditionalData().put(VpaService.DATA_HIGHSCORE, highscore.getRaw());
        }
      }

      if (exportDescriptor.isExportRom() && game.getRomFile() != null && game.getRomFile().exists()) {
        packageInfo.setRom(true);
        zipFile(game.getRomFile(), getGameFolderName() + "/VPinMAME/roms/" + game.getRomFile().getName(), zipOut);
      }

      if (game.getPOVFile().exists()) {
        packageInfo.setPov(true);
        zipFile(game.getPOVFile(), getGameFolderName() + "/Tables/" + game.getPOVFile().getName(), zipOut);
      }

      if (game.getResFile().exists()) {
        packageInfo.setRes(true);
        zipFile(game.getResFile(), getGameFolderName() + "/Tables/" + game.getResFile().getName(), zipOut);
      }

      if (game.getGameFile().exists()) {
        packageInfo.setVpx(true);
        zipFile(game.getGameFile(), getGameFolderName() + "/Tables/" + game.getGameFile().getName(), zipOut);
      }

      if (game.getDirectB2SFile().exists()) {
        packageInfo.setDirectb2s(true);
        zipFile(game.getDirectB2SFile(), getGameFolderName() + "/Tables/" + game.getDirectB2SFile().getName(), zipOut);
      }

      if (game.getDirectB2SMediaFile().exists()) {
        zipFile(game.getDirectB2SMediaFile(), "AdditionalFiles/" + game.getDirectB2SMediaFile().getName(), zipOut);
      }

      // DMDs
      if (game.getUltraDMDFolder().exists()) {
        packageInfo.setUltraDMD(true);
        zipFile(game.getUltraDMDFolder(), getGameFolderName() + "/Tables/" + game.getUltraDMDFolder().getName(), zipOut);
      }

      if (game.getFlexDMDFolder().exists()) {
        packageInfo.setFlexDMD(true);
        zipFile(game.getFlexDMDFolder(), getGameFolderName() + "/Tables/" + game.getFlexDMDFolder().getName(), zipOut);
      }

      // Music and sounds
      if (game.getAltSoundFolder() != null && game.getAltSoundFolder().exists()) {
        packageInfo.setAltSound(true);
        zipFile(game.getAltSoundFolder(), getGameFolderName() + "/VPinMAME/altsound/" + game.getAltSoundFolder().getName(), zipOut);
      }

      // Cfg
      if (game.getCfgFile() != null && game.getCfgFile().exists()) {
        packageInfo.setCfg(true);
        zipFile(game.getCfgFile(), getGameFolderName() + "/VPinMAME/cfg/" + game.getCfgFile().getName(), zipOut);
      }

      //colored DMD
      if (game.getAltColorFolder() != null && game.getAltColorFolder().exists()) {
        packageInfo.setAltColor(true);
        zipFile(game.getAltColorFolder(), getGameFolderName() + "/VPinMAME/altcolor/" + game.getAltColorFolder().getName(), zipOut);
      }

      if (exportDescriptor.isExportMusic()) {
        //aways pack whole music folder, it may contain gamefiles
        packageInfo.setMusic(true);
        File[] files = musicFolder.listFiles((dir, name) -> name.endsWith(".mp3"));
        if (files != null) {
          for (File file : files) {
            zipFile(file, getGameFolderName() + "/Music/" + file.getName(), zipOut);
          }
        }

        if (game.getMusicFolder() != null && game.getMusicFolder().exists()) {
          zipFile(game.getMusicFolder(), getGameFolderName() + "/Music/" + game.getMusicFolder().getName(), zipOut);
        }
      }

      zipPupPack(packageInfo, zipOut);
      zipPopperMedia(packageInfo, zipOut);
      zipManifest(zipOut, target);
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

      boolean renamed = tempFile.renameTo(target);
      if (renamed) {
        LOG.info("Finished packing of " + target.getAbsolutePath() + ", took " + ((System.currentTimeMillis() - start) / 1000) + " seconds, " + FileUtils.readableFileSize(target.length()));
      }
      else {
        LOG.error("Final renaming export file to " + target.getAbsolutePath() + " failed.");
      }

      //reset vpa cache
      vpaSourceAdapter.invalidate();
    }
    return true;
  }

  private void zipPopperMedia(VpaPackageInfo packageInfo, ZipOutputStream zipOut) throws IOException {
    //export popper menu data
    if (exportDescriptor.isExportPopperMedia()) {
      packageInfo.setPopperMedia(true);
      PopperScreen[] values = PopperScreen.values();
      for (PopperScreen value : values) {
        GameMediaItem gameMediaItem = game.getGameMedia().get(value);
        if (gameMediaItem != null && gameMediaItem.getFile().exists()) {
          LOG.info("Packing " + gameMediaItem.getFile().getAbsolutePath());
          File mediaFile = gameMediaItem.getFile();

          //do not archive augmented icons
          if (value.equals(PopperScreen.Wheel)) {
            WheelAugmenter augmenter = new WheelAugmenter(gameMediaItem.getFile());
            if (augmenter.getBackupWheelIcon().exists()) {
              mediaFile = augmenter.getBackupWheelIcon();
            }
          }
          zipFile(mediaFile, "PinUPSystem/POPMedia/" + getPupUpMediaFolderName() + "/" + value.name() + "/" + mediaFile.getName(), zipOut);
        }
      }
    }
  }

  /**
   * Archives the PUP pack
   */
  private void zipPupPack(VpaPackageInfo packageInfo, ZipOutputStream zipOut) throws IOException {
    if (exportDescriptor.isExportPupPack()) {
      if (game.getPupPackFolder() != null && game.getPupPackFolder().exists()) {
        packageInfo.setPupPack(true);
        LOG.info("Packing " + game.getPupPackFolder().getAbsolutePath());
        zipFile(game.getPupPackFolder(), "PinUPSystem/PUPVideos/" + game.getPupPackFolder().getName(), zipOut);
      }
    }
  }

  private void zipManifest(ZipOutputStream zipOut, File target) throws IOException {
    //store wheel icon as archive preview
    GameMediaItem mediaItem = game.getGameMedia().get(PopperScreen.Wheel);
    if (mediaItem != null) {
      File mediaFile = mediaItem.getFile();
      //do not archive augmented icons
      WheelAugmenter augmenter = new WheelAugmenter(mediaItem.getFile());
      if (augmenter.getBackupWheelIcon().exists()) {
        mediaFile = augmenter.getBackupWheelIcon();
      }

      BufferedImage image = ImageUtil.loadImage(mediaFile);
      BufferedImage resizedImage = ImageUtil.resizeImage(image, TARGET_WHEEL_SIZE_WIDTH);

      byte[] bytes = ImageUtil.toBytes(resizedImage);
      String encode = Base64.getEncoder().encodeToString(bytes);
      manifest.setThumbnail(encode);

      manifest.setVpaFilename(target.getName());

      byte[] original = Files.readAllBytes(mediaItem.getFile().toPath());
      manifest.setIcon(Base64.getEncoder().encodeToString(original));
    }

    manifest.setEmulatorType(VpaUtil.getEmulatorType(game.getGameFile()));
    manifest.setVpaVersion(vpaVersion);

    if (StringUtils.isEmpty(manifest.getGameFileName())) {
      manifest.setGameFileName(game.getGameFileName());
    }

    if (StringUtils.isEmpty(manifest.getGameName())) {
      manifest.setGameName(game.getGameDisplayName());
      manifest.setGameDisplayName(game.getGameDisplayName());
    }

    manifest.setTableName(game.getTableName());
    if (!StringUtils.isEmpty(game.getRom())) {
      manifest.setRomName(game.getRom());
    }

    String manifestString = objectMapper.writeValueAsString(manifest);
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
      LOG.info("Zipping " + fileToZip.getCanonicalPath());
      if (fileName.endsWith("/")) {
        zipOut.putNextEntry(new ZipEntry(fileName));
        zipOut.closeEntry();
      }
      else {
        zipOut.putNextEntry(new ZipEntry(fileName + "/"));
        zipOut.closeEntry();
      }

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
    zipOut.closeEntry();
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

  public static class ScoreVersionEntry {
    private String oldRaw;
    private String newRaw;
    private int changedPosition;
    private Date createdAt;

    public ScoreVersionEntry() {

    }

    private ScoreVersionEntry(HighscoreVersion version) {
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
