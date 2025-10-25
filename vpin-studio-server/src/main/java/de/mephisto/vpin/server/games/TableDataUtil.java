package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableDataUtil {
  private final static Logger LOG = LoggerFactory.getLogger(TableDataUtil.class);

  @Nullable
  public static String getEffectiveRom(TableDetails tableDetails, @NonNull GameDetails gameDetails) {
    String rom = tableDetails != null ? tableDetails.getRomName() : null;
    if (StringUtils.isEmpty(rom)) {
      rom = gameDetails.getRomName();
    }
    return rom;
  }

  @Nullable
  public static String getEffectiveHighscoreFilename(TableDetails tableDetails, @NonNull GameDetails gameDetails, @NonNull ServerSettings serverSettings, @NonNull FrontendType frontendType) {
    if (frontendType.equals(FrontendType.Popper)) {
      String hs = tableDetails != null ? getMappedValue(tableDetails, serverSettings.getMappingHsFileName()) : null;
      if (!StringUtils.isEmpty(hs)) {
        return hs;
      }
    }
    return gameDetails.getHsFileName();
  }

  @Nullable
  public static String getEffectiveTableName(TableDetails tableDetails, @NonNull GameDetails gameDetails, @NonNull FrontendType frontendType) {
    if (frontendType.equals(FrontendType.Popper)) {
      String rom = tableDetails != null ? tableDetails.getRomAlt() : null;
      if (!StringUtils.isEmpty(rom)) {
        return rom;
      }
    }
    return gameDetails.getTableName();
  }

  public static String getMappedValue(TableDetails tableDetails, String key) {
    switch (key) {
      case "WEBGameID": {
        return tableDetails.getWebGameId();
      }
      case "CUSTOM2": {
        return tableDetails.getCustom2();
      }
      case "CUSTOM3": {
        return tableDetails.getCustom3();
      }
      case "CUSTOM4": {
        return tableDetails.getCustom4();
      }
      case "CUSTOM5": {
        return tableDetails.getCustom5();
      }
      case "Special": {
        return tableDetails.getSpecial();
      }
      case "MediaSearch": {
        return tableDetails.getMediaSearch();
      }
    }
    throw new UnsupportedOperationException("Invalid popper mapping field " + key);
  }

  public static void setMappedFieldValue(TableDetails tableDetails, String field, String value) {
    try {
      if (field == null) {
        return;
      }

      switch (field) {
        case "WEBGameID": {
          tableDetails.setWebGameId(value);
          break;
        }
        case "CUSTOM2": {
          tableDetails.setCustom2(value);
          break;
        }
        case "CUSTOM3": {
          tableDetails.setCustom3(value);
          break;
        }
        case "CUSTOM4": {
          tableDetails.setCustom4(value);
          break;
        }
        case "CUSTOM5": {
          tableDetails.setCustom5(value);
          break;
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to set TableDetails value: {}", e.getMessage(), e);
    }
  }
}
