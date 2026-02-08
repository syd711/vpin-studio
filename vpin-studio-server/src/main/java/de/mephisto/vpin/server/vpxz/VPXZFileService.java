package de.mephisto.vpin.server.vpxz;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.dmd.DMDBackupData;
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
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

  public ZipFile createVpxzZip(@NonNull File target) {
    return VpxzArchiveUtil.createZipFile(target);
  }

  //-------------------------------

  public void createVpxz(VPXZPackageInfo packageInfo,
                         JobDescriptor jobDescriptor,
                         BiConsumer<File, String> zipOut,
                         Game game, TableDetails tableDetails) throws IOException {
    File gameFolder = game.getGameFile().getParentFile();

    VPXZSettings vpxzSettings = preferencesService.getJsonPreference(PreferenceNames.VPXZ_SETTINGS, VPXZSettings.class);

    File romFile = game.getRomFile();
    if (vpxzSettings.isRom() && romFile != null && romFile.exists()) {
      packageInfo.setRom(VPXZFileInfoFactory.create(romFile));
      if (!zipFile(jobDescriptor, romFile, MAME_FOLDER + "/roms/" + romFile.getName(), zipOut)) {
        return;
      }
    }

    File nvramFile = game.getNvramFile();
    if (vpxzSettings.isNvRam() && nvramFile != null && nvramFile.exists()) {
      packageInfo.setNvRam(VPXZFileInfoFactory.create(nvramFile));
      if (!zipFile(jobDescriptor, romFile, MAME_FOLDER + "/nvram/" + nvramFile.getName(), zipOut)) {
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
    File cfgFile = game.getCfgFile();
    if (cfgFile != null && cfgFile.exists()) {
      packageInfo.setCfg(VPXZFileInfoFactory.create(cfgFile));
      if (!zipFile(jobDescriptor, cfgFile, MAME_FOLDER + "/cfg/" + cfgFile.getName(), zipOut)) {
        return;
      }
    }

    zipTableDetails(jobDescriptor, game, tableDetails, zipOut);
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
