package de.mephisto.vpin.server.archiving.adapters.vpa;

import de.mephisto.vpin.restclient.archiving.ArchivePackageInfo;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameMediaService;
import de.mephisto.vpin.server.highscores.HighscoreResolver;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPReg;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.WheelAugmenter;
import de.mephisto.vpin.server.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * - Table folder
 * - <table>.vpx
 * - <table>.vbs (if necessary)
 * - <table>.ini (if desired)
 * - <table>.directb2s (if desired)
 * - music (if table has music)
 * - *
 * - pupvideos (if table has pup)
 * - *
 * - pinmame
 * - roms
 * - <rom>.zip
 * - altcolor
 * - <rom>
 * - <rom>.cRZ (if you want)
 * - altsound
 * - <rom>
 * - * (if you want)
 * - nvram
 * - <rom>.nv (if you want)
 * - ini
 * - <rom>.ini (if you want)
 */
@Service
public class VpaService {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private AltColorService altColorService;

  @Autowired
  private MusicService musicService;

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private HighscoreResolver highscoreResolver;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private DMDService dmdService;

  //-------------------------------

  public void createBackup(ArchivePackageInfo packageInfo, BiConsumer<File, String> zipOut,
                           Game game, TableDetails tableDetails) throws IOException {

    GameEmulator emulator = emulatorService.getGameEmulator(game.getEmulatorId());
    String gameFolderName = emulator.getInstallationFolder().getName();

    //store highscore
    //zip EM file
    File highscoreFile = highscoreResolver.getHighscoreTextFile(game);
    if (highscoreFile != null && highscoreFile.exists()) {
      packageInfo.setHighscore(highscoreFile.getName());
      zipFile(highscoreFile, gameFolderName + "/User/" + highscoreFile.getName(), zipOut);
    }

    //zip nvram file
    File nvRamFile = highscoreResolver.getNvRamFile(game);
    if (nvRamFile.exists()) {
      packageInfo.setHighscore(nvRamFile.getName());
      zipFile(nvRamFile, gameFolderName + "/VPinMAME/nvram/" + nvRamFile.getName(), zipOut);
    }

    //write VPReg.stg data
    if (HighscoreType.VPReg.equals(game.getHighscoreType())) {
      packageInfo.setHighscore("VPReg.stg");
      File vprRegFile = emulator.getVPRegFile();
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

    File romFile = game.getRomFile();
    if (romFile != null && romFile.exists()) {
      packageInfo.setRom(romFile.getName());
      zipFile(romFile, gameFolderName + "/VPinMAME/roms/" + romFile.getName(), zipOut);
    }

    File povFile = game.getPOVFile();
    if (povFile.exists()) {
      packageInfo.setPov(povFile.getName());
      zipFile(povFile, gameFolderName + "/Tables/" + povFile.getName(), zipOut);
    }

    File resFile = game.getResFile();
    if (resFile.exists()) {
      packageInfo.setRes(resFile.getName());
      zipFile(resFile, gameFolderName + "/Tables/" + resFile.getName(), zipOut);
    }

    File vbsFile = game.getVBSFile();
    if (vbsFile.exists()) {
      packageInfo.setVbs(vbsFile.getName());
      zipFile(vbsFile, gameFolderName + "/Tables/" + vbsFile.getName(), zipOut);
    }

    File iniFile = game.getIniFile();
    if (iniFile.exists()) {
      packageInfo.setRes(iniFile.getName());
      zipFile(iniFile, gameFolderName + "/Tables/" + iniFile.getName(), zipOut);
    }

    File gameFile = game.getGameFile();
    if (gameFile.exists()) {
      packageInfo.setVpx(gameFile.getName());
      zipFile(gameFile, gameFolderName + "/Tables/" + gameFile.getName(), zipOut);
    }

    File directB2SFile = game.getDirectB2SFile();
    if (directB2SFile.exists()) {
      packageInfo.setDirectb2s(directB2SFile.getName());
      zipFile(directB2SFile, gameFolderName + "/Tables/" + directB2SFile.getName(), zipOut);
    }

    // DMDs
    DMDPackage dmdPackage = dmdService.getDMDPackage(game);
    if (dmdPackage != null) {
      File dmdFolder = dmdService.getDmdFolder(game);
      if (dmdFolder.exists()) {
        dmdPackage.setModificationDate(new Date(dmdFolder.lastModified()));
        File[] dmdFiles = dmdFolder.listFiles((dir, name) -> new File(dir, name).isFile());
        if (dmdFiles != null) {
          for (File dmdFile : dmdFiles) {
            String relativeName = dmdFile.getAbsolutePath().substring(dmdFolder.getAbsolutePath().length());
            zipFile(dmdFile, gameFolderName + "/Tables/" + dmdPackage.getName() + "/" + relativeName, zipOut);
          }
        }
      }

    }

    // Music and sounds
    if (game.isAltSoundAvailable()) {
      File altSoundFolder = altSoundService.getAltSoundFolder(game);
      packageInfo.setAltSound(altSoundFolder.getName());
      zipFile(altSoundFolder, gameFolderName + "/VPinMAME/altsound/" + altSoundFolder.getName(), zipOut);
    }

    // Cfg
//    File cfgFile = game.getCfgFile();
//    if (cfgFile != null && cfgFile.exists()) {
//      packageInfo.setIni(true);
//      zipFile(cfgFile, gameFolderName + "/VPinMAME/cfg/" + cfgFile.getName(), zipOut);
//    }

    //colored DMD
    File altColorFolder = altColorService.getAltColorFolder(game);
    if (altColorFolder != null && altColorFolder.exists()) {
      packageInfo.setAltColor(altColorFolder.getName());
      zipFile(altColorFolder, gameFolderName + "/VPinMAME/altcolor/" + altColorFolder.getName(), zipOut);
    }

    //always zip music files if they are in a ROM named folder
    File musicFolder = musicService.getMusicFolder(game);
    if (musicFolder != null && musicFolder.exists()) {
      packageInfo.setMusic(musicFolder.getName());
      zipFile(musicFolder, gameFolderName + "/Music/" + musicFolder.getName(), zipOut);
    }

    zipPupPack(packageInfo, game, zipOut);
    zipPopperMedia(packageInfo, game, zipOut);
    zipTableDetails(game, tableDetails, zipOut);
    zipPackageInfo(packageInfo, game, zipOut);
  }

  public long calculateTotalSize(Game game) {
    long totalSizeExpected = 0;

    File musicFolder = musicService.getMusicFolder(game);
    if (musicFolder != null && musicFolder.exists()) {
      totalSizeExpected += org.apache.commons.io.FileUtils.sizeOfDirectory(musicFolder);
    }

    VPinScreen[] values = VPinScreen.values();
    for (VPinScreen value : values) {
      List<FrontendMediaItem> items = frontendService.getMediaItems(game, value);
      for (FrontendMediaItem mediaItem : items) {
        totalSizeExpected += mediaItem.getFile().length();
      }
    }

    PupPack pupPack = pupPacksService.getPupPack(game);
    if (pupPack != null) {
      totalSizeExpected += org.apache.commons.io.FileUtils.sizeOfDirectory(pupPack.getPupPackFolder());
    }

    if (game.getGameFile().exists()) {
      totalSizeExpected += game.getGameFile().length();
    }

    if (game.getDirectB2SFile().exists()) {
      totalSizeExpected += game.getDirectB2SFile().length();
    }

    return totalSizeExpected;
  }

  private void zipPopperMedia(ArchivePackageInfo packageInfo, Game game, BiConsumer<File, String> zipOut) throws IOException {
    // deactivate media exports
    if (true) return;

    //export popper menu data
    packageInfo.setPopperMedia(game.getGameName());
    VPinScreen[] values = VPinScreen.values();
    for (VPinScreen value : values) {
      List<FrontendMediaItem> items = frontendService.getMediaItems(game, value);
      for (FrontendMediaItem item : items) {
        if (item.getFile().exists()) {
          LOG.info("Packing " + item.getFile().getAbsolutePath());
          File mediaFile = item.getFile();

          //do not archive augmented icons
          if (value.equals(VPinScreen.Wheel)) {
            WheelAugmenter augmenter = new WheelAugmenter(item.getFile());
            if (augmenter.getBackupWheelIcon().exists()) {
              mediaFile = augmenter.getBackupWheelIcon();
            }
          }
          zipFile(mediaFile, "PinUPSystem/POPMedia/Visual Pinball X/" + value.name() + "/" + mediaFile.getName(), zipOut);
        }
      }
    }
  }

  /**
   * Archives the PUP pack
   */
  private void zipPupPack(ArchivePackageInfo packageInfo, Game game, BiConsumer<File, String> zipOut) throws IOException {
    PupPack pupPack = pupPacksService.getPupPack(game);
    if (pupPack != null) {
      File pupackFolder = pupPack.getPupPackFolder();
      packageInfo.setPupPack(pupPack.getName());
      LOG.info("Packing " + pupackFolder.getAbsolutePath());
      zipFile(pupackFolder, "PinUPSystem/PUPVideos/" + pupackFolder.getName(), zipOut);
    }
  }

  private void zipPackageInfo(ArchivePackageInfo packageInfo, Game game, BiConsumer<File, String> zipOut) throws IOException {
    //store wheel icon as archive preview
    File originalFile = frontendService.getWheelImage(game);
    File mediaFile = originalFile;
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

      byte[] original = Files.readAllBytes(originalFile.toPath());
      packageInfo.setIcon(Base64.getEncoder().encodeToString(original));
    }
  }

  private void zipTableDetails(Game game, TableDetails tableDetails, BiConsumer<File, String> zipOut) throws IOException {
    if (StringUtils.isEmpty(tableDetails.getGameFileName())) {
      tableDetails.setGameFileName(game.getGameFileName());
    }

    if (StringUtils.isEmpty(tableDetails.getGameName())) {
      tableDetails.setGameName(game.getGameName());
      tableDetails.setGameDisplayName(game.getGameDisplayName());
    }

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    String tableDetailsJson = objectMapper.writeValueAsString(tableDetails);

    File tableDetailsTmpFile = File.createTempFile("table-details", "json");
    tableDetailsTmpFile.deleteOnExit();
    Files.write(tableDetailsTmpFile.toPath(), tableDetailsJson.getBytes());
    zipFile(tableDetailsTmpFile, TableDetails.ARCHIVE_FILENAME, zipOut);
    tableDetailsTmpFile.delete();
  }

  private void zipFile(File fileToZip, String fileName, BiConsumer<File, String> zipOut) throws IOException {
    zipOut.accept(fileToZip, fileName);
  }

}
