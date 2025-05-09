package de.mephisto.vpin.server.archiving.adapters.vpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.restclient.archiving.ArchivePackageInfo;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.server.archiving.ArchiveDescriptor;
import de.mephisto.vpin.server.archiving.ArchiveSourceAdapter;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.WheelAugmenter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPReg;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

public class TableBackupAdapterVpa implements TableBackupAdapter, Job {
  private final static Logger LOG = LoggerFactory.getLogger(TableBackupAdapterVpa.class);
  private final Game game;
  private final ObjectMapper objectMapper;
  private final TableDetails tableDetails;

  private final FrontendService frontendService;

  private long totalSizeExpected;
  private File tempFile;
  private JobDescriptor result;


  public TableBackupAdapterVpa(@NonNull FrontendService frontendService,
                               @NonNull ArchiveSourceAdapter archiveSourceAdapter,
                               @NonNull Game game,
                               @NonNull TableDetails tableDetails) {
    this.game = game;
    this.tableDetails = tableDetails;

    this.frontendService = frontendService;

    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
  }

  public void execute(JobDescriptor jobDescriptor) {
    createBackup(jobDescriptor);
  }

  @Override
  public void createBackup(JobDescriptor result) {
    this.result = result;
    ArchiveDescriptor archiveDescriptor = new ArchiveDescriptor();
    ArchivePackageInfo packageInfo = new ArchivePackageInfo();

    archiveDescriptor.setCreatedAt(new Date());
    archiveDescriptor.setTableDetails(tableDetails);
    archiveDescriptor.setPackageInfo(packageInfo);

    result.setStatus("Calculating export size of " + game.getGameDisplayName());
    this.calculateTotalSize();
    LOG.info("Calculated total approx. size of " + FileUtils.readableFileSize(totalSizeExpected) + " for the archive of " + game.getGameDisplayName());

    String baseName = FilenameUtils.getBaseName(game.getGameFileName());
    File target = new File(VpaArchiveSource.FOLDER, baseName + ".vpa");
    target = FileUtils.uniqueFile(target);
    archiveDescriptor.setFilename(target.getName());

    tempFile = new File(target.getParentFile(), target.getName() + ".bak");

    if (target.exists() && !target.delete()) {
      throw new UnsupportedOperationException("Couldn't delete existing archive file " + target.getAbsolutePath());
    }
    if (tempFile.exists() && !tempFile.delete()) {
      throw new UnsupportedOperationException("Couldn't delete existing temporary archive file " + target.getAbsolutePath());
    }


    LOG.info("Packaging " + game.getGameDisplayName());
    long start = System.currentTimeMillis();
    FileOutputStream fos = null;
    ZipOutputStream zipOut = null;
    try {
      LOG.info("Creating temporary archive file " + tempFile.getAbsolutePath());
      fos = new FileOutputStream(tempFile);
      zipOut = new ZipOutputStream(fos);

      String gameFolderName = game.getEmulator().getInstallationFolder().getName();

      //store highscore
      //zip EM file
      if (game.getHighscoreTextFile() != null && game.getHighscoreTextFile().exists()) {
        packageInfo.setHighscore(true);
        zipFile(game.getHighscoreTextFile(), gameFolderName + "/User/" + game.getHighscoreTextFile().getName(), zipOut);
      }

      //zip nvram file
      if (game.getNvRamFile().exists()) {
        packageInfo.setHighscore(true);
        zipFile(game.getNvRamFile(), gameFolderName + "/VPinMAME/nvram/" + game.getNvRamFile().getName(), zipOut);
      }

      //write VPReg.stg data
      if (HighscoreType.VPReg.equals(game.getHighscoreType())) {
        packageInfo.setHighscore(true);
        File vprRegFile = game.getEmulator().getVPRegFile();
        VPReg reg = new VPReg(vprRegFile, game.getRom(), game.getTableName());
        String gameData = reg.toJson();
        if (gameData != null) {
          File regBackupTemp = File.createTempFile("vpreg-stg", "json");
          regBackupTemp.deleteOnExit();
          Files.write(regBackupTemp.toPath(), gameData.getBytes());
          zipFile(regBackupTemp, VPReg.ARCHIVE_FILENAME, zipOut);
          regBackupTemp.delete();
        }
      }

      if (game.getRomFile() != null && game.getRomFile().exists()) {
        packageInfo.setRom(true);
        zipFile(game.getRomFile(), gameFolderName + "/VPinMAME/roms/" + game.getRomFile().getName(), zipOut);
      }

      if (game.getPOVFile().exists()) {
        packageInfo.setPov(true);
        zipFile(game.getPOVFile(), gameFolderName + "/Tables/" + game.getPOVFile().getName(), zipOut);
      }

      if (game.getResFile().exists()) {
        packageInfo.setRes(true);
        zipFile(game.getResFile(), gameFolderName + "/Tables/" + game.getResFile().getName(), zipOut);
      }

      if (game.getGameFile().exists()) {
        packageInfo.setVpx(true);
        zipFile(game.getGameFile(), gameFolderName + "/Tables/" + game.getGameFile().getName(), zipOut);
      }

      if (game.getDirectB2SFile().exists()) {
        packageInfo.setDirectb2s(true);
        zipFile(game.getDirectB2SFile(), gameFolderName + "/Tables/" + game.getDirectB2SFile().getName(), zipOut);
      }

      // DMDs
//      if (game.getUltraDMDFolder().exists()) {
//        packageInfo.setUltraDMD(true);
//        zipFile(game.getUltraDMDFolder(), gameFolderName + "/Tables/" + game.getUltraDMDFolder().getName(), zipOut);
//      }
//
//      if (game.getFlexDMDFolder().exists()) {
//        packageInfo.setFlexDMD(true);
//        zipFile(game.getFlexDMDFolder(), gameFolderName + "/Tables/" + game.getFlexDMDFolder().getName(), zipOut);
//      }

      // Music and sounds
      if (game.isAltSoundAvailable()) {
        packageInfo.setAltSound(true);
        zipFile(game.getAltSoundFolder(), gameFolderName + "/VPinMAME/altsound/" + game.getAltSoundFolder().getName(), zipOut);
      }

      // Cfg
      if (game.getCfgFile() != null && game.getCfgFile().exists()) {
        packageInfo.setCfg(true);
        zipFile(game.getCfgFile(), gameFolderName + "/VPinMAME/cfg/" + game.getCfgFile().getName(), zipOut);
      }

      //colored DMD
      if (game.getAltColorFolder() != null && game.getAltColorFolder().exists()) {
        packageInfo.setAltColor(true);
        zipFile(game.getAltColorFolder(), gameFolderName + "/VPinMAME/altcolor/" + game.getAltColorFolder().getName(), zipOut);
      }

      //always zip music files if they are in a ROM named folder
      if (game.getMusicFolder() != null && game.getMusicFolder().exists()) {
        packageInfo.setMusic(true);
        zipFile(game.getMusicFolder(), gameFolderName + "/Music/" + game.getMusicFolder().getName(), zipOut);
      }

      zipPupPack(packageInfo, zipOut);
      zipPopperMedia(packageInfo, zipOut);
      zipTableDetails(zipOut);
      zipPackageInfo(zipOut, packageInfo);
    }
    catch (Exception e) {
      LOG.error("Create VPA for " + game.getGameDisplayName() + " failed: " + e.getMessage(), e);
      result.setError("Create VPA for " + game.getGameDisplayName() + " failed: " + e.getMessage());
      return;
    }
    finally {
      try {
        if (zipOut != null) {
          zipOut.close();
        }
      }
      catch (IOException e) {
        //ignore
      }

      try {
        if (fos != null) {
          fos.close();
        }
      }
      catch (IOException e) {
        //ignore
      }

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

  private boolean findAudioMatch(File[] allMusicFiles, String[] audioAssets) {
    if (allMusicFiles != null) {
      for (File file : allMusicFiles) {
        for (String tableMusicFile : audioAssets) {
          if (file.getName().equalsIgnoreCase(tableMusicFile)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private void calculateTotalSize() {
    if (game.getMusicFolder() != null && game.getMusicFolder().exists()) {
      totalSizeExpected += org.apache.commons.io.FileUtils.sizeOfDirectory(game.getMusicFolder());
    }

    VPinScreen[] values = VPinScreen.values();
    for (VPinScreen value : values) {
      List<FrontendMediaItem> items = frontendService.getMediaItems(game, value);
      for (FrontendMediaItem mediaItem : items) {
        totalSizeExpected += mediaItem.getFile().length();
      }
    }
    if (game.getPupPack() != null && game.getPupPack().getPupPackFolder() != null && game.getPupPack().getPupPackFolder().exists()) {
      totalSizeExpected += org.apache.commons.io.FileUtils.sizeOfDirectory(game.getPupPack().getPupPackFolder());
    }
    if (game.getGameFile().exists()) {
      totalSizeExpected += game.getGameFile().length();
    }

    if (game.getDirectB2SFile().exists()) {
      totalSizeExpected += game.getDirectB2SFile().length();
    }
  }

  private void zipPopperMedia(ArchivePackageInfo packageInfo, ZipOutputStream zipOut) throws IOException {
    //export popper menu data
//    packageInfo.setPopperMedia(true);
//    VPinScreen[] values = VPinScreen.values();
//    for (VPinScreen value : values) {
//      List<GameMediaItem> items = game.getGameMedia().getMediaItems(value);
//      for (GameMediaItem item : items) {
//        if (item.getFile().exists()) {
//          LOG.info("Packing " + item.getFile().getAbsolutePath());
//          File mediaFile = item.getFile();
//
//          //do not archive augmented icons
//          if (value.equals(VPinScreen.Wheel)) {
//            WheelAugmenter augmenter = new WheelAugmenter(item.getFile());
//            if (augmenter.getBackupWheelIcon().exists()) {
//              mediaFile = augmenter.getBackupWheelIcon();
//            }
//          }
//          zipFile(mediaFile, "PinUPSystem/POPMedia/" + systemService.getPupUpMediaFolderName(game) + "/" + value.name() + "/" + mediaFile.getName(), zipOut);
//        }
//      }
//    }
  }

  /**
   * Archives the PUP pack
   */
  private void zipPupPack(ArchivePackageInfo packageInfo, ZipOutputStream zipOut) throws IOException {
    if (game.getPupPack() != null && game.getPupPack().getPupPackFolder() != null && game.getPupPack().getPupPackFolder().exists()) {
      packageInfo.setPupPack(true);
      LOG.info("Packing " + game.getPupPack().getPupPackFolder().getAbsolutePath());
      zipFile(game.getPupPack().getPupPackFolder(), "PinUPSystem/PUPVideos/" + game.getPupPack().getPupPackFolder().getName(), zipOut);
    }
  }

  private void zipPackageInfo(ZipOutputStream zipOut, ArchivePackageInfo packageInfo) throws IOException {
    //store wheel icon as archive preview
    File mediaFile = game.getWheelImage();
    if (mediaFile != null && mediaFile.exists()) {
      //do not archive augmented icons
      WheelAugmenter augmenter = new WheelAugmenter(mediaFile);
      if (augmenter.getBackupWheelIcon().exists()) {
        mediaFile = augmenter.getBackupWheelIcon();
      }

      BufferedImage image = ImageUtil.loadImage(mediaFile);
      BufferedImage resizedImage = ImageUtil.resizeImage(image, ArchivePackageInfo.TARGET_WHEEL_SIZE_WIDTH);

      byte[] bytes = ImageUtil.toBytes(resizedImage);
      packageInfo.setThumbnail(Base64.getEncoder().encodeToString(bytes));

      byte[] original = Files.readAllBytes(game.getWheelImage().toPath());
      packageInfo.setIcon(Base64.getEncoder().encodeToString(original));
    }

    String packageInfoJson = objectMapper.writeValueAsString(packageInfo);
    File manifestFile = File.createTempFile("package-info", "json");
    manifestFile.deleteOnExit();
    Files.write(manifestFile.toPath(), packageInfoJson.getBytes());
    zipFile(manifestFile, ArchivePackageInfo.ARCHIVE_FILENAME, zipOut);
    manifestFile.delete();
  }

  private void zipTableDetails(ZipOutputStream zipOut) throws IOException {
    if (StringUtils.isEmpty(tableDetails.getGameFileName())) {
      tableDetails.setGameFileName(game.getGameFileName());
    }

    if (StringUtils.isEmpty(tableDetails.getGameName())) {
      tableDetails.setGameName(game.getGameName());
      tableDetails.setGameDisplayName(game.getGameDisplayName());
    }

    String tableDetailsJson = objectMapper.writeValueAsString(tableDetails);
    File tableDetailsTmpFile = File.createTempFile("table-details", "json");
    tableDetailsTmpFile.deleteOnExit();
    Files.write(tableDetailsTmpFile.toPath(), tableDetailsJson.getBytes());
    zipFile(tableDetailsTmpFile, TableDetails.ARCHIVE_FILENAME, zipOut);
    tableDetailsTmpFile.delete();
  }

  private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
    result.setStatus("Packing " + fileToZip.getAbsolutePath());
    if (result.getProgress() < 1 && tempFile.exists()) {
      if (totalSizeExpected > 0) {
        long l = tempFile.length() * 100 / totalSizeExpected / 100;
        result.setProgress(l);
      }
    }

    ZipUtil.zipFile(fileToZip, fileName, zipOut);
  }
}
