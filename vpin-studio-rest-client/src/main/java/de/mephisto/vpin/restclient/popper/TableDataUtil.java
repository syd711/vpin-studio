package de.mephisto.vpin.restclient.popper;

import de.mephisto.vpin.restclient.games.GameDetailsRepresentation;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public class TableDataUtil {

  public static String getEffectiveRom(@NonNull TableDetails tableDetails, @NonNull GameDetailsRepresentation gameDetails) {
    String rom = tableDetails.getRomName();
    if (StringUtils.isEmpty(rom)) {
      rom = gameDetails.getRomName();
    }
    return rom;
  }

  public static String getEffectiveHighscoreFilename(@NonNull TableDetails tableDetails, @NonNull GameDetailsRepresentation gameDetails, @NonNull ServerSettings serverSettings) {
    String hs = tableDetails.getMappedValue(serverSettings.getMappingHsFileName());
    if (StringUtils.isEmpty(hs)) {
      hs = gameDetails.getHsFileName();
    }
    return hs;
  }

  public static String getEffectiveTableName(@NonNull TableDetails tableDetails, @NonNull GameDetailsRepresentation gameDetails) {
    String rom = tableDetails.getRomAlt();
    if (StringUtils.isEmpty(rom)) {
      rom = gameDetails.getTableName();
    }
    return rom;
  }
}
