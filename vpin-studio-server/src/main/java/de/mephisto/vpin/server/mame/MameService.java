package de.mephisto.vpin.server.mame;

import de.mephisto.vpin.restclient.backups.BackupMameData;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.dmd.DMDInfoZone;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * See https://www.vpforums.org/index.php?showtopic=37182
 * for a description about all mame options.
 */
@Service
public class MameService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(MameService.class);

  private final static String KEY_SKIP_STARTUP_TEST = "cheat";
  private final static String KEY_USE_SOUND = "sound";
  private final static String KEY_USE_SAMPLES = "samples";
  private final static String KEY_DMD_COMPACT = "dmd_compact";
  private final static String KEY_DMD_DOUBLE_SIZE = "dmd_doublesize";
  private final static String KEY_IGNORE_ROM_ERRORS = "ignore_rom_crc";
  private final static String KEY_CABINET_MODE = "cabinet_mode";
  private final static String KEY_SHOW_DMD = "showwindmd";
  private final static String KEY_USER_EXTERNAL_DMD = "showpindmd";
  private final static String KEY_COLORIZE_DMD = "dmd_colorize";
  private final static String KEY_SOUND_MODE = "sound_mode";
  private final static String KEY_FORCE_STEREO = "force_stereo";

  public final static String MAME_REG_FOLDER_KEY = "SOFTWARE\\Freeware\\Visual PinMame\\";

  private final Map<String, MameOptions> mameCache = new ConcurrentHashMap<>();
  private final Map<String, Boolean> romValidationCache = new ConcurrentHashMap<>();

  @Autowired
  protected SystemService systemService;

  public boolean clearGamesCache(List<Game> knownGames) {
    long l = System.currentTimeMillis();
    mameCache.clear();
    List<String> romFolders = systemService.getCurrentUserKeys(MAME_REG_FOLDER_KEY);
    LOG.info("Reading of " + romFolders.size() + " total mame options (" + (System.currentTimeMillis() - l) + "ms)");

    for (String romFolder : romFolders) {
      List<Game> matches = knownGames.stream().filter(g -> (g.getRom() != null && g.getRom().equalsIgnoreCase(romFolder)) || (g.getRomAlias() != null && g.getRomAlias().equalsIgnoreCase(romFolder))).collect(Collectors.toList());
      if (!matches.isEmpty()) {
        mameCache.put(romFolder.toLowerCase(), getOptions(romFolder));
      }
    }
    LOG.info("Read " + this.mameCache.size() + " mame options (" + (System.currentTimeMillis() - l) + "ms)");
    return true;
  }

  public boolean clearValidationsCache(List<GameEmulator> gameEmulators) {
    long l = System.currentTimeMillis();
    romValidationCache.clear();
    List<File> folders = new ArrayList<>();
    List<GameEmulator> filteredEmulators = gameEmulators.stream().filter(g -> g.isVpxEmulator()).collect(Collectors.toList());
    for (GameEmulator filteredEmulator : filteredEmulators) {
      if (!folders.contains(filteredEmulator.getMameFolder())) {
        folders.add(filteredEmulator.getMameFolder());
      }
    }

    for (File folder : folders) {
      validateRoms(folder);
    }
    LOG.info("ROM validation took " + (System.currentTimeMillis() - l) + "ms.");

    return true;
  }

  public boolean clearCacheFor(@Nullable String rom) {
    if (!StringUtils.isEmpty(rom)) {
      mameCache.remove(rom.toLowerCase());
      getOptions(rom);
      return true;
    }
    return false;
  }

  @Nullable
  public Map<String, Object> getOptionsRaw(@Nullable String rom) {
    if (rom == null) {
      return null;
    }

    List<String> romFolders = systemService.getCurrentUserKeys(MAME_REG_FOLDER_KEY);
    if (romFolders.contains(rom.toLowerCase()) || romFolders.contains(rom)) {
      return systemService.getCurrentUserValues(MAME_REG_FOLDER_KEY + rom);
    }
    return null;
  }


  @NonNull
  public MameOptions getOptions(@NonNull String rom) {
    if (mameCache.containsKey(rom.toLowerCase())) {
      return mameCache.get(rom.toLowerCase());
    }

    List<String> romFolders = systemService.getCurrentUserKeys(MAME_REG_FOLDER_KEY);
    MameOptions options = new MameOptions();
    options.setRom(rom);
    options.setExistInRegistry(romFolders.contains(rom.toLowerCase()) || romFolders.contains(rom));

    Map<String, Object> values = systemService.getCurrentUserValues(MAME_REG_FOLDER_KEY +
        (options.isExistInRegistry() ? rom : MameOptions.DEFAULT_KEY));

    options.setSkipPinballStartupTest(getBoolean(values, KEY_SKIP_STARTUP_TEST));
    options.setUseSound(getBoolean(values, KEY_USE_SOUND));
    options.setUseSamples(getBoolean(values, KEY_USE_SAMPLES));
    options.setCompactDisplay(getBoolean(values, KEY_DMD_COMPACT));
    options.setDoubleDisplaySize(getBoolean(values, KEY_DMD_DOUBLE_SIZE));
    options.setUseSamples(getBoolean(values, KEY_USE_SAMPLES));
    options.setIgnoreRomCrcError(getBoolean(values, KEY_IGNORE_ROM_ERRORS));
    options.setCabinetMode(getBoolean(values, KEY_CABINET_MODE));
    options.setShowDmd(getBoolean(values, KEY_SHOW_DMD));
    options.setUseExternalDmd(getBoolean(values, KEY_USER_EXTERNAL_DMD));
    options.setColorizeDmd(getBoolean(values, KEY_COLORIZE_DMD));
    options.setSoundMode(getInteger(values, KEY_SOUND_MODE));
    options.setForceStereo(getBoolean(values, KEY_FORCE_STEREO));

    mameCache.put(options.getRom().toLowerCase(), options);
    return options;
  }


  public void saveRegistryData(@NonNull BackupMameData mameData) {
    String rom = mameData.getRom();
    Set<Map.Entry<String, Object>> entries = mameData.getRegistryData().entrySet();
    if (!entries.isEmpty()) {
      systemService.createUserKey(MAME_REG_FOLDER_KEY + rom);

      for (Map.Entry<String, Object> entry : entries) {
        String key = entry.getKey();
        Object value = entry.getValue();

        if (value instanceof Integer) {
          systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, key, (Integer) value);
        }
      }
    }
  }

  public MameOptions saveOptions(@NonNull MameOptions options) {
    String rom = options.getRom();

    if (!options.isExistInRegistry()) {
      systemService.createUserKey(MAME_REG_FOLDER_KEY + rom);
    }
    options.setExistInRegistry(true);

    systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, KEY_SKIP_STARTUP_TEST, options.isSkipPinballStartupTest() ? 1 : 0);
    systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, KEY_USE_SOUND, options.isUseSound() ? 1 : 0);
    systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, KEY_USE_SAMPLES, options.isUseSamples() ? 1 : 0);
    systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, KEY_DMD_COMPACT, options.isCompactDisplay() ? 1 : 0);
    systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, KEY_DMD_DOUBLE_SIZE, options.isDoubleDisplaySize() ? 1 : 0);
    systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, KEY_IGNORE_ROM_ERRORS, options.isIgnoreRomCrcError() ? 1 : 0);
    systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, KEY_CABINET_MODE, options.isCabinetMode() ? 1 : 0);
    systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, KEY_SHOW_DMD, options.isShowDmd() ? 1 : 0);
    systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, KEY_USER_EXTERNAL_DMD, options.isUseExternalDmd() ? 1 : 0);
    systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, KEY_COLORIZE_DMD, options.isColorizeDmd() ? 1 : 0);
    systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, KEY_SOUND_MODE, options.getSoundMode());
    systemService.setUserValue(MAME_REG_FOLDER_KEY + rom, KEY_FORCE_STEREO, options.isForceStereo() ? 1 : 0);

    mameCache.put(options.getRom().toLowerCase(), options);
    return getOptions(rom);
  }

  public boolean deleteOptions(String rom) {
    systemService.deleteUserKey(MAME_REG_FOLDER_KEY + rom);
    mameCache.remove(rom.toLowerCase());
    return true;
  }

  /**
   * Fill the zone with infor form registry
   *
   * @return true if the DmdPosition is rom specific or false if this is default
   */
  public boolean fillDmdPosition(String rom, DMDInfoZone dmdinfo) {
    List<String> romFolders = systemService.getCurrentUserKeys(MAME_REG_FOLDER_KEY);
    boolean existInRegistry = romFolders.contains(rom.toLowerCase());

    Map<String, Object> values = systemService.getCurrentUserValues(MAME_REG_FOLDER_KEY +
        (existInRegistry ? rom : MameOptions.DEFAULT_KEY));
    dmdinfo.setX(getInteger(values, "dmd_pos_x"));
    dmdinfo.setY(getInteger(values, "dmd_pos_y"));
    dmdinfo.setWidth(getInteger(values, "dmd_width"));
    dmdinfo.setHeight(getInteger(values, "dmd_height"));
    return existInRegistry;
  }

  public boolean saveDmdPosition(String rom, DMDInfoZone dmdinfo) {
    List<String> romFolders = systemService.getCurrentUserKeys(MAME_REG_FOLDER_KEY);
    if (!romFolders.contains(rom.toLowerCase())) {
      systemService.createUserKey(MAME_REG_FOLDER_KEY + rom);
    }
    String regkey = MAME_REG_FOLDER_KEY + rom;
    systemService.setUserValue(regkey, "dmd_pos_x", (int) dmdinfo.getX());
    systemService.setUserValue(regkey, "dmd_pos_y", (int) dmdinfo.getY());
    systemService.setUserValue(regkey, "dmd_width", (int) dmdinfo.getWidth());
    systemService.setUserValue(regkey, "dmd_height", (int) dmdinfo.getHeight());
    return true;
  }


  private boolean getBoolean(Map<String, Object> values, String key) {
    return values.containsKey(key) && values.get(key) instanceof Integer && (((Integer) values.get(key)) == 1);
  }

  private int getInteger(Map<String, Object> values, String key) {
    if (values.containsKey(key) && values.get(key) instanceof Integer) {
      return (int) values.get(key);
    }
    return 0;
  }

  //---------------------------------

  public boolean deleteCfg(@NonNull Game game) {
    File cfgFile = game.getCfgFile();
    return cfgFile != null && cfgFile.exists() && FileUtils.delete(cfgFile);
  }

  //--------------------------------

  public void installRom(UploadDescriptor uploadDescriptor, GameEmulator gameEmulator, File tempFile, UploaderAnalysis analysis) throws IOException {
    File romFolder = gameEmulator != null ? gameEmulator.getRomFolder() : getRomsFolder();
    installMameFile(uploadDescriptor, tempFile, analysis, AssetType.ZIP, romFolder);
  }

  public void installNvRam(UploadDescriptor uploadDescriptor, GameEmulator gameEmulator, File tempFile, UploaderAnalysis analysis) throws IOException {
    File nvramFolder = gameEmulator != null ? gameEmulator.getNvramFolder() : getNvRamFolder();
    installMameFile(uploadDescriptor, tempFile, analysis, AssetType.NV, nvramFolder);
  }

  public boolean isRomExists(String name) {
    if (StringUtils.isEmpty(name)) {
      return false;
    }
    File romFile = new File(getRomsFolder(), name + ".zip");
    return romFile.exists();
  }

  public boolean isValidRom(String name) {
    return !romValidationCache.containsKey(name);
  }

//  public boolean validateRom(GameEmulator gameEmulator, String name) {
//    try {
//      File romFolder = gameEmulator.getRomFolder();
//      File romFile = new File(romFolder, name + ".zip");
//      if (romFile.exists()) {
//        File mameExe = getMameExe(gameEmulator.getMameFolder());
//        if (mameExe != null) {
//          List<String> cmds = Arrays.asList(mameExe.getName(), "-verifyroms", name);
//          LOG.info("Executing ROM validation: " + String.join(" ", cmds));
//          SystemCommandExecutor executor = new SystemCommandExecutor(cmds);
//          executor.setDir(mameExe.getParentFile());
//          executor.executeCommand();
//          StringBuilder out = executor.getStandardOutputFromCommand();
//          if (out != null) {
//            String result = out.toString();
//            return result.contains("1 were OK");
//          }
//
//          LOG.error("MAME command failed: " + executor.getStandardErrorFromCommand());
//        }
//        else {
//          LOG.error("MAME exe not found.");
//          return false;
//        }
//      }
//      else {
//        LOG.error("ROM file \"" + romFile.getAbsolutePath() + "\" not found.");
//        return false;
//      }
//    }
//    catch (Exception e) {
//      LOG.error("ROM validation failed: " + e.getMessage(), e);
//      return false;
//    }
//    return false;
//  }

  public void validateRoms(@NonNull File mameFolder) {
    try {
      File mameExe = getMameExe(mameFolder);
      if (mameExe != null) {
        List<String> cmds = Arrays.asList(mameExe.getName(), "-verifyroms");
        LOG.info("Executing ROM validation: " + String.join(" ", cmds));
        SystemCommandExecutor executor = new SystemCommandExecutor(cmds);
        executor.setDir(mameExe.getParentFile());
        executor.executeCommand();
        StringBuilder out = executor.getStandardOutputFromCommand();
        if (out != null) {
          String result = out.toString();
          String[] split = result.split("\n");
          for (String s : split) {
            if (s.contains("romset") && s.contains("is bad") && s.contains("[")) {
              String rom = s.substring("romset".length() + 1, s.lastIndexOf("[") - 2);
              romValidationCache.put(rom, false);
            }
          }
          List<String> sorted = new ArrayList<>(romValidationCache.keySet());
          sorted.sort(String::compareTo);
          LOG.info("MAME rom validation finished: " + romValidationCache.size() + " invalid ROMs found: " + String.join(",", sorted));
        }

//        StringBuilder err = executor.getStandardErrorFromCommand();
//        if (err != null && !StringUtils.isEmpty(err.toString())) {
//          LOG.error("MAME command failed: " + err);
//        }
      }
    }
    catch (Exception e) {
      LOG.error("ROM validation failed: " + e.getMessage(), e);
    }
  }

  public void installCfg(UploadDescriptor uploadDescriptor, GameEmulator gameEmulator, File tempFile, UploaderAnalysis analysis) throws IOException {
    File cfgFolder = gameEmulator != null ? gameEmulator.getCfgFolder() : getCfgFolder();
    installMameFile(uploadDescriptor, tempFile, analysis, AssetType.CFG, cfgFolder);
  }

  private void installMameFile(UploadDescriptor uploadDescriptor, File tempFile, UploaderAnalysis analysis, AssetType assetType, File folder) throws IOException {
    File out = new File(folder, uploadDescriptor.getOriginalUploadFileName());
    String nvFileName = analysis.getFileNameForAssetType(assetType);
    if (nvFileName != null) {
      out = new File(folder, nvFileName);
      if (out.exists() && !out.delete()) {
        throw new IOException("Failed to delete existing " + assetType.name() + " file " + out.getAbsolutePath());
      }
      if (PackageUtil.unpackTargetFile(tempFile, out, nvFileName)) {
        LOG.info("Installed " + assetType.name() + ": " + out.getAbsolutePath());
      }
      else {
        LOG.warn("Installing mame asset " + assetType.name() + " failed: " + out.getAbsolutePath());
      }
    }
    else {
      if (out.exists() && !out.delete()) {
        throw new IOException("Failed to delete existing " + assetType.name() + " file " + out.getAbsolutePath());
      }
      org.apache.commons.io.FileUtils.copyFile(tempFile, out);
      LOG.info("Installed " + assetType.name() + ": " + out.getAbsolutePath());
    }
  }

  public File getMameFolder() {
    File vpxFolder = systemService.resolveVpx64InstallFolder();
    if (vpxFolder != null && vpxFolder.exists()) {
      return new File(vpxFolder, "VPinMAME");
    }
    return null;
  }

  public static final String NVRAM_DIRECTORY = "nvram_directory";
  public static final String ROMS_DIRECTORY = "rompath";
  public static final String CFG_DIRECTORY = "cfg_directory";

  private File nvRamFolder = null;
  private File romsFolder = null;
  private File cfgFolder = null;

  private File getVpinMameSetupFolder(String directoryType) {
    Map<String, Object> values = systemService.getCurrentUserValues(MameService.MAME_REG_FOLDER_KEY + MameOptions.GLOBALS_KEY);
    return values.containsKey(directoryType) ? new File((String) values.get(directoryType)) : null;
  }

  public File getNvRamFolder() {
    if (nvRamFolder == null) {
      nvRamFolder = getVpinMameSetupFolder(NVRAM_DIRECTORY);
      LOG.info("Resolved registry PinMAME nvram folder: {}", nvRamFolder);
    }
    return nvRamFolder;
  }

  public File getCfgFolder() {
    if (cfgFolder == null) {
      cfgFolder = getVpinMameSetupFolder(CFG_DIRECTORY);
      LOG.info("Resolved registry PinMAME cfg folder: {}", cfgFolder);
    }
    return cfgFolder;
  }

  public File getRomsFolder() {
    if (romsFolder == null) {
      romsFolder = getVpinMameSetupFolder(ROMS_DIRECTORY);
      LOG.info("Resolved registry PinMAME roms folder: {}", romsFolder);
    }
    return romsFolder;
  }

  @Nullable
  private File getMameExe(@NonNull File mameFolder) {
    File exe = new File(mameFolder, "PinMAME64.exe");
    if (!exe.exists()) {
      exe = new File(mameFolder, "PinMAME32.exe");
    }
    return exe.exists() ? exe : null;
  }

  @Nullable
  public Boolean runSetupExe() {
    File mameFolder = getMameFolder();
    File exe = new File(mameFolder, "Setup64.exe");
    if (!exe.exists()) {
      exe = new File(mameFolder, "Setup.exe");
    }
    // not run it
    return runExe(exe);
  }

  @Nullable
  public Boolean runFlexSetupExe() {
    File mameFolder = getMameFolder();
    File exe = new File(mameFolder, "FlexDMDUI.exe");
    // not run it
    return runExe(exe);
  }

  private Boolean runExe(File exe) {
    if (!exe.exists()) {
      return false;
    }
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        desktop.open(exe);
        return true;
      }
      catch (IOException ioe) {
        LOG.error("Error while executing {}", exe.getAbsolutePath(), ioe);
      }
    }
    return true;
  }

  public File getDmdDeviceIni() {
    File mameFolder = getMameFolder();
    return new File(mameFolder, "DMDDevice.ini");
  }


  @Override
  public void afterPropertiesSet() {
  }
}
