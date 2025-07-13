package de.mephisto.vpin.server.archiving.adapters.vpa;

import com.fasterxml.jackson.databind.DeserializationFeature;
import de.mephisto.vpin.restclient.archiving.ArchiveFileInfoFactory;
import de.mephisto.vpin.restclient.archiving.ArchivePackageInfo;
import de.mephisto.vpin.restclient.archiving.RegistryData;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.highscores.HighscoreResolver;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPReg;
import de.mephisto.vpin.server.mame.MameService;
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
import java.util.*;
import java.util.function.BiConsumer;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *
 */
@Service
public class VpaService {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  private final static ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

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
  private DMDService dmdService;

  @Autowired
  private MameService mameService;

  @Autowired
  private BackglassService backglassService;

  //-------------------------------

  public void createBackup(ArchivePackageInfo packageInfo, BiConsumer<File, String> zipOut,
                           Game game, TableDetails tableDetails) throws IOException {

    GameEmulator emulator = emulatorService.getGameEmulator(game.getEmulatorId());
    File gameFolder = game.getGameFile().getParentFile();

    File romFile = game.getRomFile();
    if (romFile != null && romFile.exists()) {
      packageInfo.setRom(ArchiveFileInfoFactory.create(romFile));
      zipFile(romFile, "VPinMAME/roms/" + romFile.getName(), zipOut);
    }

    File povFile = game.getPOVFile();
    if (povFile.exists()) {
      packageInfo.setPov(ArchiveFileInfoFactory.create(povFile));
      zipFile(povFile, povFile.getName(), zipOut);
    }

    File resFile = game.getResFile();
    if (resFile.exists()) {
      packageInfo.setRes(ArchiveFileInfoFactory.create(resFile));
      zipFile(resFile, resFile.getName(), zipOut);
    }

    File vbsFile = game.getVBSFile();
    if (vbsFile.exists()) {
      packageInfo.setVbs(ArchiveFileInfoFactory.create(vbsFile));
      zipFile(vbsFile, vbsFile.getName(), zipOut);
    }

    File iniFile = game.getIniFile();
    if (iniFile.exists()) {
      packageInfo.setRes(ArchiveFileInfoFactory.create(iniFile));
      zipFile(iniFile, iniFile.getName(), zipOut);
    }

    File gameFile = game.getGameFile();
    if (gameFile.exists()) {
      packageInfo.setVpx(ArchiveFileInfoFactory.create(gameFile));
      zipFile(gameFile, gameFile.getName(), zipOut);
    }

    DirectB2S directB2SAndVersions = backglassService.getDirectB2SAndVersions(game);
    List<String> versions = directB2SAndVersions.getVersions();
    List<File> files = new ArrayList<>();
    for (String version : versions) {
      File directB2SFile = new File(gameFolder, version);
      if (directB2SFile.exists()) {
        files.add(directB2SFile);
        zipFile(directB2SFile, directB2SFile.getName(), zipOut);
      }
    }

    if (!files.isEmpty()) {
      packageInfo.setDirectb2s(ArchiveFileInfoFactory.create(files.get(0), files));
    }


    //store highscore
    //zip EM file
    File highscoreFile = highscoreResolver.getHighscoreTextFile(game);
    if (highscoreFile != null && highscoreFile.exists()) {
      packageInfo.setHighscore(ArchiveFileInfoFactory.create(highscoreFile));
      zipFile(highscoreFile, "Highscore/" + highscoreFile.getName(), zipOut);
    }

    //zip nvram file
    File nvRamFile = highscoreResolver.getNvRamFile(game);
    if (nvRamFile.exists()) {
      packageInfo.setHighscore(ArchiveFileInfoFactory.create(nvRamFile));
      packageInfo.setNvRam(ArchiveFileInfoFactory.create(nvRamFile));
      zipFile(nvRamFile, "VPinMAME/nvram/" + nvRamFile.getName(), zipOut);
    }

    //write VPReg.stg data
    if (HighscoreType.VPReg.equals(game.getHighscoreType())) {
      packageInfo.setHighscore(ArchiveFileInfoFactory.create(emulator.getVPRegFile()));
      File vprRegFile = emulator.getVPRegFile();
      VPReg reg = new VPReg(vprRegFile, game.getRom(), game.getTableName());
      String gameData = reg.toJson();
      if (gameData != null) {
        File regBackupTemp = File.createTempFile("vpreg-stg", "json");
        regBackupTemp.deleteOnExit();
        Files.write(regBackupTemp.toPath(), gameData.getBytes());
        zipFile(regBackupTemp, "Highscore/" + VPReg.ARCHIVE_FILENAME, zipOut);
        if (!regBackupTemp.delete()) {
          LOG.warn("Failed to delete temporary vpreg file {}", regBackupTemp.getName());
        }
      }
    }

    // DMDs
    DMDPackage dmdPackage = dmdService.getDMDPackage(game);
    if (dmdPackage != null) {
      File dmdFolder = dmdService.getDmdFolder(game);
      if (dmdFolder.exists()) {
        List<File> archiveFiles = new ArrayList<>();
        dmdPackage.setModificationDate(new Date(dmdFolder.lastModified()));
        File[] dmdFiles = dmdFolder.listFiles((dir, name) -> new File(dir, name).isFile());
        if (dmdFiles != null) {
          for (File dmdFile : dmdFiles) {
            archiveFiles.add(dmdFile);
            zipFile(dmdFile, "DMD/" + dmdPackage.getName() + "/", zipOut);
          }
        }
        packageInfo.setDmd(ArchiveFileInfoFactory.create(dmdFolder, archiveFiles));
      }
    }


    //always zip music files if they are in a ROM named folder
    File musicFolder = musicService.getMusicFolder(game);
    if (musicFolder != null && musicFolder.exists()) {
      packageInfo.setMusic(ArchiveFileInfoFactory.create(musicFolder));
      zipFile(musicFolder, "Music/" + musicFolder.getName(), zipOut);
    }

    // sounds
    if (game.isAltSoundAvailable()) {
      File altSoundFolder = altSoundService.getAltSoundFolder(game);
      if (altSoundFolder != null) {
        packageInfo.setAltSound(ArchiveFileInfoFactory.create(altSoundFolder));
        zipFile(altSoundFolder, "VPinMAME/altsound/" + altSoundFolder.getName(), zipOut);
      }
      else {
        LOG.warn("ALT sound was detected but no folder was found.");
      }
    }

    // Cfg
//    File cfgFile = game.getCfgFile();
//    if (cfgFile != null && cfgFile.exists()) {
//      packageInfo.setIni(true);
//      zipFile(cfgFile, baseFolder + "/VPinMAME/cfg/" + cfgFile.getName(), zipOut);
//    }

    //colored DMD
    File altColorFolder = altColorService.getAltColorFolder(game);
    if (altColorFolder != null && altColorFolder.exists()) {
      packageInfo.setAltColor(ArchiveFileInfoFactory.create(altColorFolder));
      zipFile(altColorFolder, "VPinMAME/altcolor/" + altColorFolder.getName(), zipOut);
    }

    zipPupPack(packageInfo, game, zipOut);
    zipFrontendMedia(packageInfo, game, zipOut);
    zipTableDetails(game, tableDetails, zipOut);

    Map<String, Object> options = mameService.getOptionsRaw(game.getRom());
    if (options != null) {
      packageInfo.setRegistryData(ArchiveFileInfoFactory.create(game.getRom(), null, null));

      RegistryData registryData = new RegistryData();
      registryData.setData(options);
      registryData.setRom(game.getRom());
      zipRegistryDetails(registryData, zipOut);
    }

    writeWheelToPackageInfo(packageInfo, game);
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

  private void zipFrontendMedia(ArchivePackageInfo packageInfo, Game game, BiConsumer<File, String> zipOut) {
    VPinScreen[] values = VPinScreen.values();
    List<File> screenFiles = new ArrayList<>();
    for (VPinScreen value : values) {
      List<FrontendMediaItem> items = frontendService.getMediaItems(game, value);
      for (FrontendMediaItem item : items) {
        if (item.getFile().exists()) {
          LOG.info("Packing {}", item.getFile().getAbsolutePath());
          File mediaFile = item.getFile();

          //do not archive augmented icons
          if (value.equals(VPinScreen.Wheel)) {
            WheelAugmenter augmenter = new WheelAugmenter(item.getFile());
            if (augmenter.getBackupWheelIcon().exists()) {
              mediaFile = augmenter.getBackupWheelIcon();
            }
          }
          screenFiles.add(mediaFile);
          zipFile(mediaFile, "Screens/" + value.name() + "/" + mediaFile.getName(), zipOut);
        }
      }
    }

    if (!screenFiles.isEmpty()) {
      packageInfo.setPopperMedia(ArchiveFileInfoFactory.create(game.getGameName(), null, screenFiles));
    }
  }

  /**
   * Archives the PUP pack
   */
  private void zipPupPack(ArchivePackageInfo packageInfo, Game game, BiConsumer<File, String> zipOut) {
    PupPack pupPack = pupPacksService.getPupPack(game);
    if (pupPack != null) {
      File pupackFolder = pupPack.getPupPackFolder();
      LOG.info("Packing {}", pupackFolder.getAbsolutePath());
      zipFile(pupackFolder, "PUPPack/" + pupackFolder.getName(), zipOut);
      packageInfo.setPupPack(ArchiveFileInfoFactory.create(pupackFolder));
    }
  }

  private void zipRegistryDetails(@NonNull RegistryData registryData, BiConsumer<File, String> zipOut) throws IOException {
    String tableDetailsJson = objectMapper.writeValueAsString(registryData);

    File tableDetailsTmpFile = File.createTempFile("registry", "json");
    tableDetailsTmpFile.deleteOnExit();
    Files.write(tableDetailsTmpFile.toPath(), tableDetailsJson.getBytes());
    zipFile(tableDetailsTmpFile, ArchivePackageInfo.REGISTRY_FILENAME, zipOut);
    if (!tableDetailsTmpFile.delete()) {
      LOG.warn("Failed to delete temporary registry.json file {}", tableDetailsTmpFile.getName());
    }
  }

  private void writeWheelToPackageInfo(ArchivePackageInfo packageInfo, Game game) throws IOException {
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

    String tableDetailsJson = objectMapper.writeValueAsString(tableDetails);

    File tableDetailsTmpFile = File.createTempFile("table-details", "json");
    tableDetailsTmpFile.deleteOnExit();
    Files.write(tableDetailsTmpFile.toPath(), tableDetailsJson.getBytes());
    zipFile(tableDetailsTmpFile, TableDetails.ARCHIVE_FILENAME, zipOut);
    if (!tableDetailsTmpFile.delete()) {
      LOG.warn("Failed to delete temporary table-details file {}", tableDetailsTmpFile.getName());
    }
  }

  private void zipFile(File fileToZip, String fileName, BiConsumer<File, String> zipOut) {
    zipOut.accept(fileToZip, fileName);
  }
}
