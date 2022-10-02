package de.mephisto.vpin.server.roms;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.util.ReverseLineInputStream;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class RomService {
  private final static Logger LOG = LoggerFactory.getLogger(RomService.class);

  private final static int MAX_ROM_FILENAME_LENGTH = 16;

  private final static List<String> PATTERNS = Arrays.asList("cGameName", "cgamename", "RomSet1", "GameName");

  private final List<Pattern> patternList = new ArrayList<>();

  public RomService() {
    PATTERNS.forEach(p -> patternList.add(Pattern.compile(".*" + p + ".*=.*\".*\".*")));
  }

  @Nullable
  public String scanRom(Game game) {
    String romName = scanRomName(game.getGameFile());
    game.setRom(romName);
    if (!StringUtils.isEmpty(romName)) {
      LOG.info("Finished scan of table " + game + ", found ROM '" + romName + "'.");
      return romName;
    }
    LOG.info("Finished scan of table " + game + ", no ROM found.");
    return null;
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
