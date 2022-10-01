package de.mephisto.vpin.server.roms;

import de.mephisto.vpin.server.GameInfo;
import de.mephisto.vpin.server.util.PropertiesStore;
import de.mephisto.vpin.server.util.ReverseLineInputStream;
import de.mephisto.vpin.server.util.SystemInfo;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class RomManager {
  private final static Logger LOG = LoggerFactory.getLogger(RomManager.class);

  private final static int MAX_ROM_FILENAME_LENGTH = 16;

  private final static List<String> PATTERNS = Arrays.asList("cGameName", "cgamename", "RomSet1", "GameName");

  private final PropertiesStore store;

  private final List<Pattern> patternList = new ArrayList<>();

  public RomManager() {
    this.store = PropertiesStore.create("repository.properties");
    PATTERNS.forEach(p -> patternList.add(Pattern.compile(".*" + p + ".*=.*\".*\".*")));
  }

  @Nullable
  public String scanRom(GameInfo gameInfo) {
    String romName = scanRomName(gameInfo.getGameFile());
    gameInfo.setRom(romName);
    writeGameInfo(gameInfo);
    if (!StringUtils.isEmpty(romName)) {
      LOG.info("Finished scan of table " + gameInfo + ", found ROM '" + romName + "'.");
      return romName;
    }
    LOG.info("Finished scan of table " + gameInfo + ", no ROM found.");
    return null;
  }

  private void writeGameInfo(GameInfo game) {
    String romName = game.getRom();
    if (romName != null && romName.length() > 0) {
      game.setRom(romName);
      LOG.info("Update of " + game.getGameFile().getName() + " successful, written ROM name '" + romName + "'");

      File romFile = new File(SystemInfo.getInstance().getMameRomFolder(), romName + ".zip");
      if (romFile.exists()) {
        game.setRomFile(romFile);
      }
    }
    else {
      LOG.info("Skipped Update of " + game.getGameFile().getName() + ", no rom name found.");
    }
    this.store.set(formatGameKey(game.getId()) + ".rom", romName != null ? romName : "");
    this.store.set(formatGameKey(game.getId()) + ".displayName", game.getGameDisplayName());
  }

  public String getRomName(int id) {
    return this.store.getString(formatGameKey(id) + ".rom");
  }


  private String formatGameKey(int id) {
    return "gameId." + id;
  }

  public boolean wasScanned(int id) {
    return store.containsKey(formatGameKey(id) + ".rom");
  }

  /**
   * Checks the different lines that are in the vpx file.
   * Usually the variable not does differ that much.
   * We read the file from the end to save time.
   *
   * @param gameFile the table file which contains the rom that is searched.
   * @return the ROM name or null
   */
  private String scanRomName(File gameFile) {
    String romName = null;
    BufferedReader bufferedReader = null;
    ReverseLineInputStream reverseLineInputStream = null;
    try {
      reverseLineInputStream = new ReverseLineInputStream(gameFile);
      bufferedReader = new BufferedReader(new InputStreamReader(reverseLineInputStream));

      String line;
      bufferedReader.readLine();//skip last line if empty
      int count = 0;
      while ((line = bufferedReader.readLine()) != null || count < 1000) {
        count++;
        if (line != null) {
          if (matchesPatterns(line)) {
            if (line.indexOf("'") != 0) {
              int start = line.indexOf("\"") + 1;
              romName = line.substring(start);
              int end = romName.indexOf("\"");

              if (end - start < MAX_ROM_FILENAME_LENGTH) {
                romName = romName.substring(0, end).trim();
                break;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to read rom line '" + romName + "' for  " + gameFile.getAbsolutePath() + ": " + e.getMessage(), e);
    } finally {
      try {
        if(reverseLineInputStream != null) {
          reverseLineInputStream.close();
        }

        if(bufferedReader != null) {
          bufferedReader.close();
        }
      } catch (Exception e) {
        LOG.error("Failed to close vpx file stream: " + e.getMessage(), e);
      }
    }
    return romName;
  }

  private boolean matchesPatterns(String line) {
    for (Pattern pattern : patternList) {
      if (pattern.matcher(line).matches()) {
        return true;
      }
    }
    return false;
  }
}
