package de.mephisto.vpin.server.mame;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.backups.BackupMameData;
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
import de.mephisto.vpin.server.util.FileUpdateWriter;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * See https://www.vpforums.org/index.php?showtopic=37182
 * for a description about all mame options.
 */
@Service
public class MameService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

  private final Map<String, MameOptions> mameOptionsCache = new ConcurrentHashMap<>();
  private final Map<String, Boolean> romValidationCache = new ConcurrentHashMap<>();
  private final List<String> mameRegistryEntriesCache = new ArrayList<>();

  @Autowired
  protected SystemService systemService;

  @Autowired
  private FolderLookupService folderLookupService;

  private File mameFolder;


  public boolean clearGamesCache(List<Game> knownGames) {
    this.mameFolder = null;
    getMameFolder();

    long l = System.currentTimeMillis();
    mameOptionsCache.clear();

    for (String romFolder : getMameEntries(false)) {
      List<Game> matches = knownGames.stream().filter(g -> (g.getRom() != null && g.getRom().equalsIgnoreCase(romFolder)) || (g.getRomAlias() != null && g.getRomAlias().equalsIgnoreCase(romFolder))).collect(Collectors.toList());
      if (!matches.isEmpty()) {
        mameOptionsCache.put(romFolder.toLowerCase(), getOptions(romFolder));
      }
    }
    LOG.info("Read {} mame options ({}ms)", this.mameOptionsCache.size(), System.currentTimeMillis() - l);
    return true;
  }

  public void clearValidationsCache(List<GameEmulator> gameEmulators) {
    long l = System.currentTimeMillis();
    romValidationCache.clear();
    List<File> folders = new ArrayList<>();
    for (GameEmulator gameEmulator : gameEmulators) {
      if (gameEmulator.isVpxEmulator() && !folders.contains(gameEmulator.getMameFolder())) {
        folders.add(gameEmulator.getMameFolder());
      }
    }

    for (File folder : folders) {
      validateRoms(folder);
    }
    LOG.info("ROM validation took " + (System.currentTimeMillis() - l) + "ms.");
  }

  public boolean clearCacheFor(@Nullable String rom) {
    if (!StringUtils.isEmpty(rom)) {
      mameOptionsCache.remove(rom.toLowerCase());
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

    List<String> romFolders = getMameEntries(false);
    if (romFolders.contains(rom.toLowerCase()) || romFolders.contains(rom)) {
      return systemService.getCurrentUserValues(MAME_REG_FOLDER_KEY + rom);
    }
    return null;
  }


  @NonNull
  public MameOptions getOptions(@NonNull String rom) {
    if (mameOptionsCache.containsKey(rom.toLowerCase())) {
      return mameOptionsCache.get(rom.toLowerCase());
    }

    MameOptions options = new MameOptions();
    options.setRom(rom);

    String key = options.isExistInRegistry() ? rom : MameOptions.DEFAULT_KEY;
    Map<String, Object> values = systemService.getCurrentUserValues(MAME_REG_FOLDER_KEY + key);
    options.setExistInRegistry(!values.isEmpty());

    if (values.isEmpty()) {
      values = systemService.getCurrentUserValues(MAME_REG_FOLDER_KEY + MameOptions.DEFAULT_KEY);
    }

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

    mameOptionsCache.put(options.getRom().toLowerCase(), options);
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
          systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, key, (Integer) value);
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

    systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, KEY_SKIP_STARTUP_TEST, options.isSkipPinballStartupTest() ? 1 : 0);
    systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, KEY_USE_SOUND, options.isUseSound() ? 1 : 0);
    systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, KEY_USE_SAMPLES, options.isUseSamples() ? 1 : 0);
    systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, KEY_DMD_COMPACT, options.isCompactDisplay() ? 1 : 0);
    systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, KEY_DMD_DOUBLE_SIZE, options.isDoubleDisplaySize() ? 1 : 0);
    systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, KEY_IGNORE_ROM_ERRORS, options.isIgnoreRomCrcError() ? 1 : 0);
    systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, KEY_CABINET_MODE, options.isCabinetMode() ? 1 : 0);
    systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, KEY_SHOW_DMD, options.isShowDmd() ? 1 : 0);
    systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, KEY_USER_EXTERNAL_DMD, options.isUseExternalDmd() ? 1 : 0);
    systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, KEY_COLORIZE_DMD, options.isColorizeDmd() ? 1 : 0);
    systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, KEY_SOUND_MODE, options.getSoundMode());
    systemService.setUserIntValue(MAME_REG_FOLDER_KEY + rom, KEY_FORCE_STEREO, options.isForceStereo() ? 1 : 0);

    mameOptionsCache.put(options.getRom().toLowerCase(), options);
    getMameEntries(true);
    return getOptions(rom);
  }

  public boolean deleteOptions(String rom) {
    systemService.deleteUserKey(MAME_REG_FOLDER_KEY + rom);
    mameOptionsCache.remove(rom.toLowerCase());
    return true;
  }

  /**
   * Fill the zone with infor form registry
   *
   * @return true if the DmdPosition is rom specific or false if this is default
   */
  public boolean fillDmdPosition(String rom, DMDInfoZone dmdinfo) {
    List<String> romFolders = getMameEntries(false);
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
    List<String> romFolders = getMameEntries(true);
    if (!romFolders.contains(rom.toLowerCase())) {
      systemService.createUserKey(MAME_REG_FOLDER_KEY + rom);
      getMameEntries(true);
    }
    String regkey = MAME_REG_FOLDER_KEY + rom;
    systemService.setUserIntValue(regkey, "dmd_pos_x", dmdinfo.getX());
    systemService.setUserIntValue(regkey, "dmd_pos_y", dmdinfo.getY());
    systemService.setUserIntValue(regkey, "dmd_width", dmdinfo.getWidth());
    systemService.setUserIntValue(regkey, "dmd_height", dmdinfo.getHeight());
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
    File cfgFile = folderLookupService.getCfgFile(game);
    return cfgFile != null && cfgFile.exists() && FileUtils.delete(cfgFile);
  }

  public boolean deleteRom(@NonNull Game game) {
    File romFile = folderLookupService.getRomFile(game);
    return romFile != null && romFile.exists() && FileUtils.delete(romFile);
  }

  //--------------------------------

  public void installRom(UploadDescriptor uploadDescriptor, Game game, GameEmulator emulator, File tempFile, UploaderAnalysis analysis) throws IOException {
    File romFolder = game != null ? folderLookupService.getRomFolder(game) : getRomsFolder();
    installMameFile(uploadDescriptor, tempFile, analysis, AssetType.ZIP, romFolder);
  }

  public void installNvRam(UploadDescriptor uploadDescriptor, Game game, GameEmulator emulator, File tempFile, UploaderAnalysis analysis) throws IOException {
    File nvramFolder = game != null ? folderLookupService.getNvRamFolder(game) : getNvRamFolder();
    installMameFile(uploadDescriptor, tempFile, analysis, AssetType.NV, nvramFolder);
  }

  public boolean isRomExists(@NonNull Game game) {
    File romFile = folderLookupService.getRomFile(game);
    return romFile != null && romFile.exists();
  }

  //TODO
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
          LOG.info("MAME rom validation finished: {} invalid ROMs found: {}", romValidationCache.size(), String.join(",", sorted));
        }
      }
    }
    catch (Exception e) {
      LOG.error("ROM validation failed: {}", e.getMessage(), e);
    }
  }

  public void installCfg(UploadDescriptor uploadDescriptor, Game game, GameEmulator emulator, File tempFile, UploaderAnalysis analysis) throws IOException {
    File cfgFolder = game != null ? folderLookupService.getCfgFolder(game) : getCfgFolder();
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
        LOG.info("Installed {}: {}", assetType.name(), out.getAbsolutePath());
      }
      else {
        LOG.warn("Installing mame asset {} failed: {}", assetType.name(), out.getAbsolutePath());
      }
    }
    else {
      if (out.exists() && !out.delete()) {
        throw new IOException("Failed to delete existing " + assetType.name() + " file " + out.getAbsolutePath());
      }
      org.apache.commons.io.FileUtils.copyFile(tempFile, out);
      LOG.info("Installed MAME asset {}: {}", assetType.name(), out.getAbsolutePath());
    }
  }

  public File getMameFolder() {
    if (mameFolder == null) {
      File vpxFolder = systemService.resolveVpx64InstallFolder();
      if (vpxFolder != null && vpxFolder.exists()) {
        mameFolder = new File(vpxFolder, "VPinMAME");
      }
    }
    return mameFolder;
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

  public File getAltColorFolder() {
    return new File(getMameFolder(), "altcolor");
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

  public boolean deleteDMDDeviceIniEntry(@NonNull Game game) {
    FileUpdateWriter iniConfiguration = new FileUpdateWriter();
    try {
      iniConfiguration.read(getDmdDeviceIni().toPath());
      iniConfiguration.removeSection(game.getRom());
      iniConfiguration.removeSection(game.getTableName());
      iniConfiguration.write(getDmdDeviceIni().toPath());
    }
    catch (IOException e) {
      LOG.error("Failed to write DMDDevice.ini: {}", e.getMessage(), e);
      return false;
    }
    return true;
  }

  private List<String> getMameEntries(boolean forceReload) {
    if (forceReload || this.mameRegistryEntriesCache.isEmpty()) {
      long l = System.currentTimeMillis();
      this.mameRegistryEntriesCache.addAll(systemService.getCurrentUserKeys(MAME_REG_FOLDER_KEY));
      LOG.info("Reading of {} total mame options ({}ms)", mameRegistryEntriesCache.size(), System.currentTimeMillis() - l);
    }
    return mameRegistryEntriesCache;
  }

  @Override
  public void afterPropertiesSet() {
    LOG.info("Initialized {}", this.getClass().getSimpleName());
  }
}
