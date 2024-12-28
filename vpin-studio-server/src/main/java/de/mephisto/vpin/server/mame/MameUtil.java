package de.mephisto.vpin.server.mame;

import de.mephisto.vpin.commons.utils.WinRegistry;
import de.mephisto.vpin.restclient.mame.MameOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MameUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MameUtil.class);

  public static final String NVRAM_DIRECTORY = "nvram_directory";
  public static final String ROMS_DIRECTORY = "rompath";

  private static String nvRamFolder = null;
  private static String romsFolder = null;

  public static String getNvRamFolder() {
    if (nvRamFolder == null) {
      Map<String, Object> values = WinRegistry.getCurrentUserValues(MameService.MAME_REG_FOLDER_KEY + MameOptions.GLOBALS_KEY);
      if (values.containsKey(NVRAM_DIRECTORY)) {
        nvRamFolder = (String) values.get(NVRAM_DIRECTORY);
        LOG.info("Resolved registry PinMAME nvram folder: {}", nvRamFolder);
      }
      else {
        nvRamFolder = "-invalid-";
      }
    }
    return nvRamFolder;
  }

  public static String getRomsFolder() {
    if (romsFolder == null) {
      Map<String, Object> values = WinRegistry.getCurrentUserValues(MameService.MAME_REG_FOLDER_KEY + MameOptions.GLOBALS_KEY);
      if (values.containsKey(ROMS_DIRECTORY)) {
        romsFolder = (String) values.get(ROMS_DIRECTORY);
        LOG.info("Resolved registry PinMAME roms folder: {}", romsFolder);
      }
      else {
        romsFolder = "-invalid-";
      }
    }
    return romsFolder;
  }
}
