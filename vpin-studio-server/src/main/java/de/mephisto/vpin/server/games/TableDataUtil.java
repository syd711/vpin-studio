package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.system.ScoringDB;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class TableDataUtil {

  public static boolean isSupported(ScoringDB scoringDB, HighscoreFiles highscoreFiles, String rom, String tableName, String effectiveHighscoreFilename, boolean played) {
    if (scoringDB.getNotSupported().contains(String.valueOf(rom).toLowerCase())) {
      return false;
    }

    if (scoringDB.getSupportedNvRams().contains(String.valueOf(rom).toLowerCase())) {
      return true;
    }

    if (highscoreFiles.getVpRegEntries().contains(String.valueOf(rom).toLowerCase())) {
      return true;
    }

    if (highscoreFiles.getVpRegEntries().contains(tableName)) {
      return true;
    }

    //maybe we have a highscore file instead
    if (!StringUtils.isEmpty(effectiveHighscoreFilename) && !highscoreFiles.getTextFiles().contains(effectiveHighscoreFilename)) {
      return true;
    }

    if(!played) {
      return true;
    }

    return false;
  }

  @Nullable
  public static String getEffectiveRom(@NonNull TableDetails tableDetails, @NonNull GameDetails gameDetails) {
    String rom = tableDetails.getRomName();
    if (StringUtils.isEmpty(rom)) {
      rom = gameDetails.getRomName();
    }
    return rom;
  }

  @Nullable
  public static String getEffectiveHighscoreFilename(@NonNull TableDetails tableDetails, @NonNull GameDetails gameDetails, @NonNull ServerSettings serverSettings) {
    String hs = tableDetails.getMappedValue(serverSettings.getMappingHsFileName());
    if (StringUtils.isEmpty(hs)) {
      hs = gameDetails.getHsFileName();
    }
    return hs;
  }

  @Nullable
  public static String getEffectiveTableName(@NonNull TableDetails tableDetails, @NonNull GameDetails gameDetails) {
    String rom = tableDetails.getRomAlt();
    if (StringUtils.isEmpty(rom)) {
      rom = gameDetails.getTableName();
    }
    return rom;
  }
}
