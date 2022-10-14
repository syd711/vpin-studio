package de.mephisto.vpin.server.roms;

import de.mephisto.vpin.server.util.ReverseLineInputStream;
import edu.umd.cs.findbugs.annotations.NonNull;
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

/**
 * Checks the different lines that are in the vpx file.
 * Usually the variable not does differ that much.
 * We read the file from the end to save time.
 */
public class VPXFileScanner {
  private final static Logger LOG = LoggerFactory.getLogger(VPXFileScanner.class);

  private final static int MAX_ROM_FILENAME_LENGTH = 16;

  private final static List<String> PATTERNS = Arrays.asList("cGameName", "cgamename", "RomSet1", "GameName");

  private final static List<Pattern> patternList = new ArrayList<>();

  static {
    PATTERNS.forEach(p -> patternList.add(Pattern.compile(".*" + p + ".*=.*\".*\".*")));
  }

  @NonNull
  public static ScanResult scan(@NonNull File gameFile) {
    ScanResult result = new ScanResult();

    BufferedReader bufferedReader = null;
    ReverseLineInputStream reverseLineInputStream = null;
    String line = null;
    try {
      reverseLineInputStream = new ReverseLineInputStream(gameFile);
      bufferedReader = new BufferedReader(new InputStreamReader(reverseLineInputStream));

      bufferedReader.readLine();//skip last line if empty
      int count = 0;
      while ((line = bufferedReader.readLine()) != null || count < 1000) {
        count++;

        if (result.isScanComplete()) {
          break;
        }

        if (line != null) {
          lineSearchRom(result, line);
          lineSearchNvOffset(result, line);
        }
      }

      if (!StringUtils.isEmpty(result.getRom())) {
        LOG.info("Finished scan of table " + gameFile.getAbsolutePath() + ", found ROM '" + result.getRom() + "'.");
      }
      else {
        LOG.info("Finished scan of table " + gameFile.getAbsolutePath() + ", no ROM found.");
      }
    } catch (Exception e) {
      LOG.error("Failed to read rom line '" + line + "' for  " + gameFile.getAbsolutePath() + ": " + e.getMessage(), e);
    } finally {
      try {
        if (reverseLineInputStream != null) {
          reverseLineInputStream.close();
        }

        if (bufferedReader != null) {
          bufferedReader.close();
        }
      } catch (Exception e) {
        LOG.error("Failed to close vpx file stream: " + e.getMessage(), e);
      }
    }

    return result;
  }

  private static void lineSearchNvOffset(@NonNull ScanResult result, @NonNull String line) {
    if (result.getNvOffset() > 0) {
      return;
    }

    if (line.trim().startsWith("NVOffset")) {
      String nvOffsetString = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));
      result.setNvOffset(Integer.parseInt(nvOffsetString));
    }
  }

  /**
   * Single line eval for rom name
   */
  private static void lineSearchRom(@NonNull ScanResult result, @NonNull String line) {
    if (result.getRom() != null) {
      return;
    }

    int patternMatch = matchesPatterns(line);
    if (patternMatch != -1) {
      String pattern = PATTERNS.get(patternMatch);
      if (line.contains("'") && line.trim().indexOf("'") < line.indexOf(pattern)) {
        return;
      }

      line = line.substring(line.indexOf(pattern) + pattern.length() + 1);
      int start = line.indexOf("\"") + 1;
      String rom = line.substring(start);
      int end = rom.indexOf("\"");

      if (end - start < MAX_ROM_FILENAME_LENGTH) {
        rom = rom.substring(0, end).trim();
      }

      result.setRom(rom);
    }
  }

  private static int matchesPatterns(String line) {
    for (int i = 0; i < patternList.size(); i++) {
      Pattern pattern = patternList.get(i);
      if (pattern.matcher(line).matches()) {
        return i;
      }
    }
    return -1;
  }
}
