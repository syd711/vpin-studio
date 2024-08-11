package de.mephisto.vpin.server.score;

import org.apache.commons.lang3.StringUtils;

public class GameToProcessorFactory {

  private static final String[] LCDTYPE_W5_ROMS_PREFIX = {
    "ind250cc"
  };
  private static final String[] LCDTYPE_W7_ROMS_PREFIX = {
    "mousn_"
  };

  public DMDScoreProcessor getScanner(String gameName) {
    if (matchRom(LCDTYPE_W5_ROMS_PREFIX, gameName)) {
      return new DMDScoreScannerLCD(DMDScoreScannerLCD.Type.WIDTH_5);
    }
    else if (matchRom(LCDTYPE_W7_ROMS_PREFIX, gameName)) {
      return new DMDScoreScannerLCD(DMDScoreScannerLCD.Type.WIDTH_7);
    }
    // else
    return new DMDScoreSplitAndScan();
  }

  public DMDScoreProcessor getAnalyser(String gameName) {
    if (matchRom(LCDTYPE_W5_ROMS_PREFIX, gameName)) {
      return new DMDScoreAnalyserLCD5();
    }
    // else
    return new DMDScoreAnalyserDump();
  }

  //-----------------------------------------

  private boolean matchRom(String[] roms, String gameName) {
    for (String rom: roms) {
      if (StringUtils.startsWithIgnoreCase(gameName, rom)) {
        return true;
      }
    }
    return false;
  }
}
