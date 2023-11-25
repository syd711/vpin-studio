package de.mephisto.vpin.server.mame;

import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.server.util.WinRegistry;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

  public final static String MAME_REG_FOLDER_KEY = "SOFTWARE\\Freeware\\Visual PinMame\\";

  private final Map<String, MameOptions> mameCache = new ConcurrentHashMap<>();

  public boolean clearCache() {
    long l = System.currentTimeMillis();
    mameCache.clear();
    List<String> romFolders = WinRegistry.getCurrentUserKeys(MAME_REG_FOLDER_KEY);
    for (String romFolder : romFolders) {
      mameCache.put(romFolder.toLowerCase(), getOptions(romFolder));
    }
    LOG.info("Read " + this.mameCache.size() + " mame options (" + (System.currentTimeMillis() - l) + "ms)");
    return true;
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

    if (options.isExistInRegistry()) {
      Map<String, Object> values = WinRegistry.getCurrentUserValues(MAME_REG_FOLDER_KEY + rom);

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
      options.setSoundMode(getBoolean(values, KEY_SOUND_MODE));
    }

    mameCache.put(options.getRom().toLowerCase(), options);
    return options;
  }

  public MameOptions saveOptions(@NonNull MameOptions options) {
    String rom = options.getRom();
    options.setExistInRegistry(true);

    if (!options.isExistInRegistry()) {
      WinRegistry.createKey(MAME_REG_FOLDER_KEY + rom);
    }
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
    WinRegistry.setIntValue(MAME_REG_FOLDER_KEY + rom, KEY_SOUND_MODE, options.isSoundMode() ? 1 : 0);

    mameCache.put(options.getRom().toLowerCase(), options);
    return getOptions(rom);
  }

  private boolean getBoolean(Map<String, Object> values, String key) {
    return values.containsKey(key) && values.get(key) instanceof Integer && (((Integer) values.get(key)) == 1);
  }

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      clearCache();
    }).start();
  }
}
