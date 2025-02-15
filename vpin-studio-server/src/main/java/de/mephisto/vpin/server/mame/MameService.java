package de.mephisto.vpin.server.mame;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.commons.utils.WinRegistry;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
  private FrontendService frontendService;

  @Autowired
  private EmulatorService emulatorService;

  private GameService gameService;

  public boolean clearCache() {
    long l = System.currentTimeMillis();
    mameCache.clear();
    List<String> romFolders = WinRegistry.getCurrentUserKeys(MAME_REG_FOLDER_KEY);
    LOG.info("Reading of " + romFolders.size() + " total mame options (" + (System.currentTimeMillis() - l) + "ms)");

    List<Game> knownGames = gameService.getKnownGames(-1);
    for (String romFolder : romFolders) {
      Optional<Game> first = knownGames.stream().filter(g -> (g.getRom() != null && g.getRom().equalsIgnoreCase(romFolder)) || (g.getRomAlias() != null && g.getRomAlias().equalsIgnoreCase(romFolder))).findFirst();
      if (first.isPresent()) {
        mameCache.put(romFolder.toLowerCase(), getOptions(romFolder));
      }
    }
    LOG.info("Read " + this.mameCache.size() + " mame options (" + (System.currentTimeMillis() - l) + "ms)");

    l = System.currentTimeMillis();
    romValidationCache.clear();
    List<GameEmulator> gameEmulators = emulatorService.getValidGameEmulators();
    for (GameEmulator gameEmulator : gameEmulators) {
      validateRoms(gameEmulator);
    }
    LOG.info("ROM validation took " + (System.currentTimeMillis() - l) + "ms.");

    return true;
  }

  public boolean clearCacheFor(@Nullable String rom) {
    if (!StringUtils.isEmpty(rom)) {
      mameCache.remove(rom);
      getOptions(rom);
      return true;
    }
    return false;
  }

  @NonNull
  public MameOptions getOptions(@NonNull String rom) {
    if (mameCache.containsKey(rom.toLowerCase())) {
      return mameCache.get(rom.toLowerCase());
    }

    List<String> romFolders = WinRegistry.getCurrentUserKeys(MAME_REG_FOLDER_KEY);
    MameOptions options = new MameOptions();
    options.setRom(rom);
    options.setExistInRegistry(romFolders.contains(rom.toLowerCase()));

    Map<String, Object> values = WinRegistry.getCurrentUserValues(MAME_REG_FOLDER_KEY +
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

  public MameOptions saveOptions(@NonNull MameOptions options) {
    String rom = options.getRom();

    if (!options.isExistInRegistry()) {
      WinRegistry.createKey(MAME_REG_FOLDER_KEY + rom);
    }
    options.setExistInRegistry(true);

    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_SKIP_STARTUP_TEST, options.isSkipPinballStartupTest() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_USE_SOUND, options.isUseSound() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_USE_SAMPLES, options.isUseSamples() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_DMD_COMPACT, options.isCompactDisplay() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_DMD_DOUBLE_SIZE, options.isDoubleDisplaySize() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_IGNORE_ROM_ERRORS, options.isIgnoreRomCrcError() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_CABINET_MODE, options.isCabinetMode() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_SHOW_DMD, options.isShowDmd() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_USER_EXTERNAL_DMD, options.isUseExternalDmd() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_COLORIZE_DMD, options.isColorizeDmd() ? 1 : 0);
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_SOUND_MODE, options.getSoundMode());
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_FORCE_STEREO, options.isForceStereo() ? 1 : 0);

    mameCache.put(options.getRom().toLowerCase(), options);
    return getOptions(rom);
  }

  public boolean deleteOptions(String rom) {
    WinRegistry.deleteKey(MAME_REG_FOLDER_KEY + rom);
    mameCache.remove(rom.toLowerCase());
    return true;
  }

  public boolean fillDmdPosition(DMDInfo dmdinfo) {
    List<String> romFolders = WinRegistry.getCurrentUserKeys(MAME_REG_FOLDER_KEY);
    String rom = dmdinfo.getGameRom();
    boolean existInRegistry = romFolders.contains(rom.toLowerCase());

    Map<String, Object> values = WinRegistry.getCurrentUserValues(MAME_REG_FOLDER_KEY +
        (existInRegistry ? rom : MameOptions.DEFAULT_KEY));
    dmdinfo.setLocallySaved(existInRegistry);
    dmdinfo.setX(getInteger(values, "dmd_pos_x"));
    dmdinfo.setY(getInteger(values, "dmd_pos_y"));
    dmdinfo.setWidth(getInteger(values, "dmd_width"));
    dmdinfo.setHeight(getInteger(values, "dmd_height"));
    return true;
  }

  public boolean saveDmdPosition(DMDInfo dmdinfo) {
    List<String> romFolders = WinRegistry.getCurrentUserKeys(MAME_REG_FOLDER_KEY);
    String rom = dmdinfo.getGameRom();
    if (!romFolders.contains(rom.toLowerCase())) {
      WinRegistry.createKey(MAME_REG_FOLDER_KEY + rom);
    }
    String regkey = MAME_REG_FOLDER_KEY + rom;
    WinRegistry.setIntValue(regkey, "dmd_pos_x", (int) dmdinfo.getX());
    WinRegistry.setIntValue(regkey, "dmd_pos_y", (int) dmdinfo.getY());
    WinRegistry.setIntValue(regkey, "dmd_width", (int) dmdinfo.getWidth());
    WinRegistry.setIntValue(regkey, "dmd_height", (int) dmdinfo.getHeight());
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

  public void installRom(UploadDescriptor uploadDescriptor, File tempFile, UploaderAnalysis analysis) throws IOException {
    GameEmulator gameEmulator = emulatorService.getGameEmulator(uploadDescriptor.getEmulatorId());
    installMameFile(uploadDescriptor, tempFile, analysis, AssetType.ZIP, gameEmulator.getRomFolder());
  }

  public void installNvRam(UploadDescriptor uploadDescriptor, File tempFile, UploaderAnalysis analysis) throws IOException {
    GameEmulator gameEmulator = emulatorService.getGameEmulator(uploadDescriptor.getEmulatorId());
    installMameFile(uploadDescriptor, tempFile, analysis, AssetType.NV, gameEmulator.getNvramFolder());
  }

  public boolean isValidRom(String name) {
    return !romValidationCache.containsKey(name);
  }

  public boolean validateRom(GameEmulator gameEmulator, String name) {
    try {
      File romFolder = gameEmulator.getRomFolder();
      File romFile = new File(romFolder, name + ".zip");
      if (romFile.exists()) {
        File mameExe = getMameExe(gameEmulator);
        if (mameExe != null) {
          List<String> cmds = Arrays.asList(mameExe.getName(), "-verifyroms", name);
          LOG.info("Executing ROM validation: " + String.join(" ", cmds));
          SystemCommandExecutor executor = new SystemCommandExecutor(cmds);
          executor.setDir(mameExe.getParentFile());
          executor.executeCommand();
          StringBuilder out = executor.getStandardOutputFromCommand();
          if (out != null) {
            String result = out.toString();
            return result.contains("1 were OK");
          }

          LOG.error("MAME command failed: " + executor.getStandardErrorFromCommand());
        }
        else {
          LOG.error("MAME exe not found.");
          return false;
        }
      }
      else {
        LOG.error("ROM file \"" + romFile.getAbsolutePath() + "\" not found.");
        return false;
      }
    }
    catch (Exception e) {
      LOG.error("ROM validation failed: " + e.getMessage(), e);
      return false;
    }
    return false;
  }

  public void validateRoms(GameEmulator gameEmulator) {
    try {
      File mameExe = getMameExe(gameEmulator);
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

  public void installCfg(UploadDescriptor uploadDescriptor, File tempFile, UploaderAnalysis analysis) throws IOException {
    GameEmulator gameEmulator = emulatorService.getGameEmulator(uploadDescriptor.getEmulatorId());
    installMameFile(uploadDescriptor, tempFile, analysis, AssetType.CFG, gameEmulator.getCfgFolder());
  }

  public void installMameFile(UploadDescriptor uploadDescriptor, File tempFile, UploaderAnalysis analysis, AssetType assetType, File folder) throws IOException {
    if (analysis == null) {
      analysis = new UploaderAnalysis(frontendService.getFrontend(), tempFile);
      analysis.analyze();
    }

    File out = new File(folder, uploadDescriptor.getOriginalUploadFileName());
    String nvFileName = analysis.getFileNameForAssetType(assetType);
    if (nvFileName != null) {
      out = new File(folder, nvFileName);
      if (out.exists() && !out.delete()) {
        throw new IOException("Failed to delete existing " + assetType.name() + " file " + out.getAbsolutePath());
      }
      ZipUtil.unzipTargetFile(tempFile, out, nvFileName);
      LOG.info("Installed " + assetType.name() + ": " + out.getAbsolutePath());
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
    SystemInfo si = new SystemInfo();
    File vpxFolder = si.resolveVpx64InstallFolder();
    File mameFolder = new File(vpxFolder, "VPinMAME");
    return mameFolder.exists() ? mameFolder : null;
  }

  @Nullable
  private File getMameExe(GameEmulator emulator) {
    File exe = new File(emulator.getMameFolder(), "PinMAME64.exe");
    if (!exe.exists()) {
      exe = new File(emulator.getMameFolder(), "PinMAME32.exe");
    }
    return exe.exists() ? exe : null;
  }

  @Override
  public void afterPropertiesSet() {
  }

  public void setGameService(GameService gameService) {
    this.gameService = gameService;
  }
}
