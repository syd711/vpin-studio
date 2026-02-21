package de.mephisto.vpin.server.vpxz;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.backups.BackupPackageInfo;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.preferences.VPXZSettings;
import de.mephisto.vpin.restclient.vpxz.VPXZFileInfoFactory;
import de.mephisto.vpin.restclient.vpxz.VPXZPackageInfo;
import de.mephisto.vpin.restclient.vpxz.VpxzArchiveUtil;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.WheelAugmenter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.util.PngFrameCapture;
import de.mephisto.vpin.server.vpx.FolderLookupService;
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
import java.util.List;
import java.util.function.BiConsumer;

/**
 *
 */
@Service
public class VPXZFileService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZFileService.class);

  private final static ObjectMapper objectMapper;

  private final static String MAME_FOLDER = "pinmame";

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private MusicService musicService;

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private DMDService dmdService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private FolderLookupService folderLookupService;


  private static final int TARGET_WHEEL_SIZE_WIDTH = 150;


  public ZipFile createVpxzZip(@NonNull File target) {
    return VpxzArchiveUtil.createZipFile(target);
  }

  //-------------------------------

  public void createVpxz(VPXZPackageInfo packageInfo,
                         JobDescriptor jobDescriptor,
                         String vpxStandaloneFile,
                         BiConsumer<File, String> zipOut,
                         Game game, TableDetails tableDetails) throws IOException {
    VPXZSettings vpxzSettings = preferencesService.getJsonPreference(PreferenceNames.VPXZ_SETTINGS, VPXZSettings.class);

    File romFile = folderLookupService.getRomFile(game);
    if (vpxzSettings.isRom() && romFile != null && romFile.exists()) {
      packageInfo.setRom(VPXZFileInfoFactory.create(romFile));
      if (!zipFile(jobDescriptor, romFile, MAME_FOLDER + "/roms/" + romFile.getName(), zipOut)) {
        return;
      }
    }

    File nvRamFile = folderLookupService.getNvRamFile(game);
    if (vpxzSettings.isNvRam() && nvRamFile.exists()) {
      packageInfo.setNvRam(VPXZFileInfoFactory.create(nvRamFile));
      if (!zipFile(jobDescriptor, nvRamFile, MAME_FOLDER + "/nvram/" + nvRamFile.getName(), zipOut)) {
        return;
      }
    }

    File povFile = game.getPOVFile();
    if (vpxzSettings.isPov() && povFile.exists()) {
      packageInfo.setPov(VPXZFileInfoFactory.create(povFile));
      if (!zipFile(jobDescriptor, povFile, povFile.getName(), zipOut)) {
        return;
      }
    }


    File resFile = game.getResFile();
    if (vpxzSettings.isRes() && resFile.exists()) {
      packageInfo.setRes(VPXZFileInfoFactory.create(resFile));
      if (!zipFile(jobDescriptor, resFile, resFile.getName(), zipOut)) {
        return;
      }
    }

    File vbsFile = game.getVBSFile();
    if (vpxzSettings.isVbs() && vbsFile.exists()) {
      packageInfo.setVbs(VPXZFileInfoFactory.create(vbsFile));
      if (!zipFile(jobDescriptor, vbsFile, vbsFile.getName(), zipOut)) {
        return;
      }
    }

    File iniFile = game.getIniFile();
    if (vpxzSettings.isIni() && iniFile.exists()) {
      packageInfo.setIni(VPXZFileInfoFactory.create(iniFile));
      if (!zipFile(jobDescriptor, iniFile, iniFile.getName(), zipOut)) {
        return;
      }
    }

    File gameFile = game.getGameFile();
    if (gameFile.exists()) {
      packageInfo.setVpx(VPXZFileInfoFactory.create(gameFile));
      if (!zipFile(jobDescriptor, gameFile, gameFile.getName(), zipOut)) {
        return;
      }
    }

    // Cfg
    File cfgFile = folderLookupService.getCfgFile(game);
    if (cfgFile != null && cfgFile.exists()) {
      packageInfo.setCfg(VPXZFileInfoFactory.create(cfgFile));
      if (!zipFile(jobDescriptor, cfgFile, MAME_FOLDER + "/cfg/" + cfgFile.getName(), zipOut)) {
        return;
      }
    }

    // vpx standalone
    if(!StringUtils.isEmpty(vpxStandaloneFile)) {
      File vpxScriptsFolder = new File(VPXZService.VPX_SCRIPTS_FOLDER);
      File vpxStandalone = new File(vpxScriptsFolder, vpxStandaloneFile);
      if (vpxStandalone.exists()) {
        packageInfo.setVbs(VPXZFileInfoFactory.create(vpxStandalone));
        String fileName = FilenameUtils.getBaseName(game.getGameFileName()) + ".vbs";
        if (!zipFile(jobDescriptor, vpxStandalone, fileName, zipOut)) {
          return;
        }
      }
    }

    zipTableDetails(jobDescriptor, game, tableDetails, zipOut);
    zipWheel(jobDescriptor, game, zipOut);
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

  private void zipWheel(JobDescriptor jobDescriptor, Game game, BiConsumer<File, String> zipOut) throws IOException {
    String wheelImageName = FilenameUtils.getBaseName(game.getGameFileName()) + ".png";

    try {
      //store wheel icon as archive preview
      File mediaFile = frontendService.getWheelImage(game);
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
          BufferedImage resizedImage = ImageUtil.resizeImage(image, TARGET_WHEEL_SIZE_WIDTH);
          File tmp = File.createTempFile(wheelFileName, ".png");
          tmp.deleteOnExit();
          ImageUtil.write(resizedImage, tmp);
          zipFile(jobDescriptor, tmp, wheelImageName, zipOut);
          tmp.delete();
        }
        else {
          BufferedImage image = ImageUtil.loadImage(mediaFile);
          BufferedImage resizedImage = ImageUtil.resizeImage(image, TARGET_WHEEL_SIZE_WIDTH);
          File tmp = File.createTempFile(wheelFileName, ".png");
          tmp.deleteOnExit();
          ImageUtil.write(resizedImage, tmp);
          zipFile(jobDescriptor, tmp, wheelImageName, zipOut);
          tmp.delete();
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to write original wheel png as thumbnail, using empty wheel instead: {}", e.getMessage());
      BufferedImage wheelImage = ResourceLoader.getResource("avatar-default.png");
      File tmp = File.createTempFile("avatar-default", ".png");
      tmp.deleteOnExit();
      ImageUtil.write(wheelImage, tmp);
      zipFile(jobDescriptor, tmp, wheelImageName, zipOut);
      tmp.delete();
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
