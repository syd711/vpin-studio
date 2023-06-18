package de.mephisto.vpin.server.mame;

import de.mephisto.vpin.restclient.client.AltSoundServiceClient;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MameService {
  private final static Logger LOG = LoggerFactory.getLogger(MameService.class);

  private final static String KEY_SKIP_STARTUP_TEST = "cheat";
  private final static String KEY_USE_SOUND = "sound";
  private final static String KEY_USE_SAMPLES = "samples";
  private final static String KEY_IGNORE_ROM_ERRORS = "ignore_rom_crc";
  private final static String KEY_CABINET_MODE = "cabinet_mode";
  private final static String KEY_SHOW_DMD = "showwindmd";
  private final static String KEY_USER_EXTERNAL_DMD = "showpindmd";
  private final static String KEY_COLORIZE_DMD = "dmd_colorize";

  @Autowired
  private SystemService systemService;

  public MameOptions getOptions(@NonNull String rom) {
    MameOptions options = new MameOptions();
    options.setRom(rom);

    boolean mameRegistryEntryPresent = systemService.isMameRegistryEntryPresent(rom);
    options.setExistInRegistry(mameRegistryEntryPresent);
    if(mameRegistryEntryPresent) {
      options.setSkipPinballStartupTest(systemService.getMameRegistryBooleanValue(rom, KEY_SKIP_STARTUP_TEST));
      options.setUseSound(systemService.getMameRegistryBooleanValue(rom, KEY_USE_SOUND));
      options.setUseSamples(systemService.getMameRegistryBooleanValue(rom, KEY_USE_SAMPLES));
      options.setIgnoreRomCrcError(systemService.getMameRegistryBooleanValue(rom, KEY_IGNORE_ROM_ERRORS));
      options.setCabinetMode(systemService.getMameRegistryBooleanValue(rom, KEY_CABINET_MODE));
      options.setShowDmd(systemService.getMameRegistryBooleanValue(rom, KEY_SHOW_DMD));
      options.setUseExternalDmd(systemService.getMameRegistryBooleanValue(rom, KEY_USER_EXTERNAL_DMD));
      options.setColorizeDmd(systemService.getMameRegistryBooleanValue(rom, KEY_COLORIZE_DMD));
    }

    return options;
  }

  public MameOptions saveOptions(@NonNull MameOptions options) {
    String rom = options.getRom();

    systemService.writeRegistry(SystemService.MAME_REG_KEY + rom, KEY_SKIP_STARTUP_TEST, options.isSkipPinballStartupTest() ? 1 : 0);
    systemService.writeRegistry(SystemService.MAME_REG_KEY + rom, KEY_USE_SOUND, options.isUseSound() ? 1 : 0);
    systemService.writeRegistry(SystemService.MAME_REG_KEY + rom, KEY_USE_SAMPLES, options.isUseSamples() ? 1 : 0);
    systemService.writeRegistry(SystemService.MAME_REG_KEY + rom, KEY_IGNORE_ROM_ERRORS, options.isIgnoreRomCrcError() ? 1 : 0);
    systemService.writeRegistry(SystemService.MAME_REG_KEY + rom, KEY_CABINET_MODE, options.isCabinetMode() ? 1 : 0);
    systemService.writeRegistry(SystemService.MAME_REG_KEY + rom, KEY_SHOW_DMD, options.isShowDmd() ? 1 : 0);
    systemService.writeRegistry(SystemService.MAME_REG_KEY + rom, KEY_USER_EXTERNAL_DMD, options.isUseExternalDmd() ? 1 : 0);
    systemService.writeRegistry(SystemService.MAME_REG_KEY + rom, KEY_COLORIZE_DMD, options.isColorizeDmd() ? 1 : 0);
    return getOptions(rom);
  }
}
