package de.mephisto.vpin.server.backups.adapters.vpa;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.backups.*;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.dmd.DMDBackupData;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.preferences.BackupSettings;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.dmd.DMDDeviceIniService;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.WheelAugmenter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.util.PngFrameCapture;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;

/**
 *
 */
@Service
public class VpaService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  private final static ObjectMapper objectMapper;

  private final static String MAME_FOLDER = "VPinMAME";

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

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
  private HighscoreBackupService highscoreBackupService;

  @Autowired
  private DMDService dmdService;

  @Autowired
  private DMDDeviceIniService dmdDeviceIniService;

  @Autowired
  private MameService mameService;

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private PreferencesService preferencesService;

  public ZipFile createProtectedArchive(@NonNull File target) {
    return VpaArchiveUtil.createZipFile(target);
  }

  //-------------------------------

  public void createBackup(BackupPackageInfo packageInfo,
                           JobDescriptor jobDescriptor,
                           BiConsumer<File, String> zipOut,
                           Game game, TableDetails tableDetails) throws IOException {
    File gameFolder = game.getGameFile().getParentFile();

    BackupSettings backupSettings = preferencesService.getJsonPreference(PreferenceNames.BACKUP_SETTINGS, BackupSettings.class);

    File romFile = game.getRomFile();
    if (backupSettings.isRom() && romFile != null && romFile.exists()) {
      packageInfo.setRom(BackupFileInfoFactory.create(romFile));
      if (!zipFile(jobDescriptor, romFile, MAME_FOLDER + "/roms/" + romFile.getName(), zipOut)) {
        return;
      }
    }

    File povFile = game.getPOVFile();
    if (backupSettings.isPov() && povFile.exists()) {
      packageInfo.setPov(BackupFileInfoFactory.create(povFile));
      if (!zipFile(jobDescriptor, povFile, povFile.getName(), zipOut)) {
        return;
      }
    }


    File resFile = game.getResFile();
    if (backupSettings.isRes() && resFile.exists()) {
      packageInfo.setRes(BackupFileInfoFactory.create(resFile));
      if (!zipFile(jobDescriptor, resFile, resFile.getName(), zipOut)) {
        return;
      }
    }

    File vbsFile = game.getVBSFile();
    if (backupSettings.isVbs() && vbsFile.exists()) {
      packageInfo.setVbs(BackupFileInfoFactory.create(vbsFile));
      if (!zipFile(jobDescriptor, vbsFile, vbsFile.getName(), zipOut)) {
        return;
      }
    }

    File iniFile = game.getIniFile();
    if (backupSettings.isIni() && iniFile.exists()) {
      packageInfo.setIni(BackupFileInfoFactory.create(iniFile));
      if (!zipFile(jobDescriptor, iniFile, iniFile.getName(), zipOut)) {
        return;
      }
    }

    File gameFile = game.getGameFile();
    if (gameFile.exists()) {
      packageInfo.setVpx(BackupFileInfoFactory.create(gameFile));
      if (!zipFile(jobDescriptor, gameFile, gameFile.getName(), zipOut)) {
        return;
      }
    }

    if (backupSettings.isDirectb2s()) {
      DirectB2S directB2SAndVersions = backglassService.getDirectB2SAndVersions(game);
      if (directB2SAndVersions != null) {
        List<String> versions = directB2SAndVersions.getVersions();
        List<File> files = new ArrayList<>();
        for (String version : versions) {
          String versionFileName = version;
          if (versionFileName.contains(File.separator)) {
            versionFileName = versionFileName.substring(versionFileName.lastIndexOf(File.separator) + 1);
          }
          File directB2SFile = new File(gameFolder, versionFileName);
          if (directB2SFile.exists()) {
            files.add(directB2SFile);
            if (!zipFile(jobDescriptor, directB2SFile, directB2SFile.getName(), zipOut)) {
              return;
            }
          }
        }

        if (backupSettings.isB2sSettings()) {
          DirectB2STableSettings tableSettings = backglassService.getTableSettings(game);
          if (tableSettings != null) {
            zipB2STableSettings(jobDescriptor, tableSettings, zipOut);
          }

          File b2STableSettingsFile = game.getB2STableSettingsFile();
          if (b2STableSettingsFile != null && b2STableSettingsFile.exists()) {
            zipFile(jobDescriptor, b2STableSettingsFile, DirectB2STableSettings.FILENAME, zipOut);
          }
        }

        if (!files.isEmpty()) {
          packageInfo.setDirectb2s(BackupFileInfoFactory.create(files.get(0), files));
        }
      }
    }


    //store highscore
    if (backupSettings.isHighscore() || backupSettings.isNvRam()) {
      File highscoreBackupFile = highscoreBackupService.backup(game);
      if (highscoreBackupFile != null && highscoreBackupFile.exists()) {
        packageInfo.setHighscore(BackupFileInfoFactory.create(highscoreBackupFile));
        if (!zipFile(jobDescriptor, highscoreBackupFile, "Highscore/" + highscoreBackupFile.getName(), zipOut)) {
          return;
        }
      }
    }

    // DMDs
    if (backupSettings.isDmd()) {
      DMDPackage dmdPackage = dmdService.getDMDPackage(game);
      if (dmdPackage != null && dmdPackage.isValid()) {
        File dmdFolder = dmdService.getDmdFolder(game);
        if (dmdFolder.exists()) {
          List<File> archiveFiles = new ArrayList<>();
          dmdPackage.setModificationDate(new Date(dmdFolder.lastModified()));
          if (!zipFile(jobDescriptor, dmdFolder, "DMD/" + dmdPackage.getName() + "/", zipOut)) {
            return;
          }
          packageInfo.setDmd(BackupFileInfoFactory.create(dmdFolder, archiveFiles));
        }
      }
    }

    //always zip music files if they are in a ROM named folder
    if (backupSettings.isMusic()) {
      File musicFolder = musicService.getMusicFolder(game);
      if (musicFolder != null && musicFolder.exists()) {
        packageInfo.setMusic(BackupFileInfoFactory.create(musicFolder));
        if (!zipFile(jobDescriptor, musicFolder, "Music/" + musicFolder.getName(), zipOut)) {
          return;
        }
      }
    }

    // sounds
    if (backupSettings.isAltSound() && game.isAltSoundAvailable()) {
      File altSoundFolder = altSoundService.getAltSoundFolder(game);
      if (altSoundFolder != null) {
        packageInfo.setAltSound(BackupFileInfoFactory.create(altSoundFolder));
        if (!zipFile(jobDescriptor, altSoundFolder, MAME_FOLDER + "/altsound/" + altSoundFolder.getName(), zipOut)) {
          return;
        }
      }
      else {
        LOG.warn("ALT sound was detected but no folder was found.");
      }
    }

    // Cfg
    File cfgFile = game.getCfgFile();
    if (cfgFile != null && cfgFile.exists()) {
      packageInfo.setCfg(BackupFileInfoFactory.create(cfgFile));
      if (!zipFile(jobDescriptor, cfgFile, MAME_FOLDER + "/cfg/" + cfgFile.getName(), zipOut)) {
        return;
      }
    }

    //colored DMD
    File altColorFolder = altColorService.getAltColorFolder(game);
    if (backupSettings.isAltColor() && altColorFolder != null && altColorFolder.exists()) {
      zipFile(jobDescriptor, altColorFolder, MAME_FOLDER + "/altcolor/" + altColorFolder.getName(), zipOut);
      File backupsFolder = new File(altColorFolder, "backups");
      if (backupsFolder.exists()) {
        if (!zipFile(jobDescriptor, backupsFolder, MAME_FOLDER + "/altcolor/" + altColorFolder.getName() + "/backups", zipOut)) {
          return;
        }
      }
      packageInfo.setAltColor(BackupFileInfoFactory.create(altColorFolder));
    }

    if (backupSettings.isPupPack()) {
      if (!zipPupPack(jobDescriptor, packageInfo, game, zipOut)) {
        return;
      }
    }

    if (backupSettings.isFrontendMedia()) {
      if (!zipFrontendMedia(jobDescriptor, packageInfo, game, zipOut)) {
        return;
      }
    }

    zipTableDetails(jobDescriptor, game, tableDetails, zipOut);

    if (backupSettings.isRegistryData()) {
      Map<String, Object> options = mameService.getOptionsRaw(game.getRom());
      if (options == null) {
        options = new HashMap<>();
      }
      packageInfo.setMameData(BackupFileInfoFactory.create(game.getRom(), null, null));

      BackupMameData mameData = new BackupMameData();
      mameData.setRegistryData(options);
      mameData.setRom(game.getRom());
      mameData.setAlias(game.getRomAlias());
      if (!zipMameRegistryData(jobDescriptor, mameData, zipOut)) {
        return;
      }
    }

    if (backupSettings.isStudioData()) {
      BackupDataStudio studioData = new BackupDataStudio();
      studioData.setComment(game.getComment());
      studioData.setCardsDisabled(game.isCardDisabled());
      studioData.setIgnoredValidations(ValidationState.toIdString(game.getIgnoredValidations()));
      if (!zipStudioDetails(jobDescriptor, studioData, zipOut)) {
        return;
      }
    }

    if (backupSettings.isDmdDeviceData()) {
      DMDBackupData backupData = dmdDeviceIniService.getBackupData(game);
      if (!zipDmdDeviceIni(jobDescriptor, backupData, zipOut)) {
        return;
      }
    }

    if (!jobDescriptor.isCancelled()) {
      writeWheelToPackageInfo(packageInfo, game);
    }
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

    DMDPackage dmdPackage = dmdService.getDMDPackage(game);
    if (dmdPackage != null && dmdPackage.isValid()) {
      totalSizeExpected += dmdPackage.getSize();
    }

    return totalSizeExpected;
  }

  private boolean zipFrontendMedia(JobDescriptor jobDescriptor, BackupPackageInfo packageInfo, Game game, BiConsumer<File, String> zipOut) {
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
          if (!zipFile(jobDescriptor, mediaFile, "Screens/" + value.name() + "/" + mediaFile.getName(), zipOut)) {
            return false;
          }
        }
      }
    }

    if (!screenFiles.isEmpty()) {
      packageInfo.setPopperMedia(BackupFileInfoFactory.create(game.getGameName(), null, screenFiles));
    }
    return true;
  }

  /**
   * Archives the PUP pack
   */
  private boolean zipPupPack(@NonNull JobDescriptor jobDescriptor, BackupPackageInfo packageInfo, Game game, BiConsumer<File, String> zipOut) {
    PupPack pupPack = pupPacksService.getPupPack(game);
    if (pupPack != null) {
      File pupackFolder = pupPack.getPupPackFolder();
      LOG.info("Packing {}", pupackFolder.getAbsolutePath());
      if (!zipFile(jobDescriptor, pupackFolder, "PUPPack/" + pupackFolder.getName(), zipOut)) {
        return false;
      }
      packageInfo.setPupPack(BackupFileInfoFactory.create(pupackFolder));
    }
    return true;
  }

  private boolean zipMameRegistryData(@NonNull JobDescriptor jobDescriptor, @NonNull BackupMameData registryData, BiConsumer<File, String> zipOut) throws IOException {
    String tableDetailsJson = objectMapper.writeValueAsString(registryData);

    File tableDetailsTmpFile = File.createTempFile("registry", ".json");
    tableDetailsTmpFile.deleteOnExit();
    Files.write(tableDetailsTmpFile.toPath(), tableDetailsJson.getBytes());
    if (!zipFile(jobDescriptor, tableDetailsTmpFile, BackupPackageInfo.REGISTRY_FILENAME, zipOut)) {
      return false;
    }
    if (!tableDetailsTmpFile.delete()) {
      LOG.warn("Failed to delete temporary registry.json file {}", tableDetailsTmpFile.getName());
    }
    return true;
  }

  private boolean zipStudioDetails(@NonNull JobDescriptor jobDescriptor, @NonNull BackupDataStudio backupDataStudio, BiConsumer<File, String> zipOut) throws IOException {
    String studioDataJson = objectMapper.writeValueAsString(backupDataStudio);

    File tmpStudioDataJson = File.createTempFile("vpin-studio", ".json");
    tmpStudioDataJson.deleteOnExit();
    Files.write(tmpStudioDataJson.toPath(), studioDataJson.getBytes());
    if (!zipFile(jobDescriptor, tmpStudioDataJson, BackupDataStudio.BACKUP_FILENAME, zipOut)) {
      return false;
    }
    if (!tmpStudioDataJson.delete()) {
      LOG.warn("Failed to delete temporary file {}", tmpStudioDataJson.getName());
    }
    return true;
  }

  private boolean zipDmdDeviceIni(@NonNull JobDescriptor jobDescriptor, @NonNull DMDBackupData data, BiConsumer<File, String> zipOut) throws IOException {
    String studioDataJson = objectMapper.writeValueAsString(data);

    File tmpDmdDeviceData = File.createTempFile("dmddevice", ".json");
    tmpDmdDeviceData.deleteOnExit();
    Files.write(tmpDmdDeviceData.toPath(), studioDataJson.getBytes());
    if (!zipFile(jobDescriptor, tmpDmdDeviceData, DMDBackupData.BACKUP_FILENAME, zipOut)) {
      return false;
    }
    if (!tmpDmdDeviceData.delete()) {
      LOG.warn("Failed to delete temporary file {}", tmpDmdDeviceData.getName());
    }
    return true;
  }

  private void writeWheelToPackageInfo(BackupPackageInfo packageInfo, Game game) throws IOException {
    try {
      //store wheel icon as archive preview
      File originalFile = frontendService.getWheelImage(game);
      File mediaFile = originalFile;
      if (mediaFile != null && mediaFile.exists()) {
        //do not archive augmented icons
        WheelAugmenter augmenter = new WheelAugmenter(mediaFile);
        if (augmenter.getBackupWheelIcon().exists()) {
          mediaFile = augmenter.getBackupWheelIcon();
        }

        String wheelFileName = FilenameUtils.getExtension(mediaFile.getName());
        if (wheelFileName.equalsIgnoreCase("apng")) {
          byte[] bytes = PngFrameCapture.captureFirstFrame(mediaFile);
          BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
          BufferedImage resizedImage = ImageUtil.resizeImage(image, BackupPackageInfo.TARGET_WHEEL_SIZE_WIDTH);
          byte[] resizedBytes = ImageUtil.toBytes(resizedImage);
          packageInfo.setThumbnail(Base64.getEncoder().encodeToString(resizedBytes));
        }
        else {
          BufferedImage image = ImageUtil.loadImage(mediaFile);
          BufferedImage resizedImage = ImageUtil.resizeImage(image, BackupPackageInfo.TARGET_WHEEL_SIZE_WIDTH);
          byte[] bytes = ImageUtil.toBytes(resizedImage);
          packageInfo.setThumbnail(Base64.getEncoder().encodeToString(bytes));
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to write original wheel png as thumbnail, using empty wheel instead: {}", e.getMessage());
      BufferedImage wheelImage = ResourceLoader.getResource("avatar-default.png");
      byte[] bytes = ImageUtil.toBytes(wheelImage);
      packageInfo.setThumbnail(Base64.getEncoder().encodeToString(bytes));
    }
  }

  private void zipTableDetails(@NonNull JobDescriptor jobDescriptor, Game game, TableDetails tableDetails, BiConsumer<File, String> zipOut) throws IOException {
    if (StringUtils.isEmpty(tableDetails.getGameFileName())) {
      tableDetails.setGameFileName(game.getGameFileName());
    }

    if (StringUtils.isEmpty(tableDetails.getGameName())) {
      tableDetails.setGameName(game.getGameName());
      tableDetails.setGameDisplayName(game.getGameDisplayName());
    }

    String tableDetailsJson = objectMapper.writeValueAsString(tableDetails);

    File tableDetailsTmpFile = File.createTempFile("table-details", ".json");
    tableDetailsTmpFile.deleteOnExit();
    Files.write(tableDetailsTmpFile.toPath(), tableDetailsJson.getBytes());
    zipFile(jobDescriptor, tableDetailsTmpFile, TableDetails.ARCHIVE_FILENAME, zipOut);
    if (!tableDetailsTmpFile.delete()) {
      LOG.warn("Failed to delete temporary table-details file {}", tableDetailsTmpFile.getName());
    }
  }

  private void zipB2STableSettings(@NonNull JobDescriptor jobDescriptor, DirectB2STableSettings directB2STableSettings, BiConsumer<File, String> zipOut) throws IOException {
    String tableDetailsJson = objectMapper.writeValueAsString(directB2STableSettings);
    File tableDetailsTmpFile = File.createTempFile("B2STableSettings", ".json");
    tableDetailsTmpFile.deleteOnExit();
    Files.write(tableDetailsTmpFile.toPath(), tableDetailsJson.getBytes());
    zipFile(jobDescriptor, tableDetailsTmpFile, DirectB2STableSettings.ARCHIVE_FILENAME, zipOut);
    if (!tableDetailsTmpFile.delete()) {
      LOG.warn("Failed to delete temporary B2STableSettings file {}", tableDetailsTmpFile.getName());
    }
  }

  private boolean zipFile(@NonNull JobDescriptor jobDescriptor, File fileToZip, String fileName, BiConsumer<File, String> zipOut) {
    if (jobDescriptor.isCancelled()) {
      LOG.info("Cancelled backup, skipped writing {}", fileToZip.getAbsolutePath());
      return false;
    }
    zipOut.accept(fileToZip, fileName);
    LOG.info("Zipped archive file: {}", fileToZip);
    return true;
  }

  @Override
  public void afterPropertiesSet() {
  }
}
