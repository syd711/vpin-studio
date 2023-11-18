package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.vpx.VPXUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Checks the different lines that are in the vpx file.
 * Usually the variable not does differ that much.
 * We read the file from the end to save time.
 */
public class VPXFileScanner {
  private final static Logger LOG = LoggerFactory.getLogger(VPXFileScanner.class);

  private final static int MAX_ROM_FILENAME_LENGTH = 32;
  private final static int MAX_FILENAME_LENGTH = 128;

  private final static String PATTERN_TABLENAME = "TableName";
  private final static List<String> PATTERNS = Arrays.asList("cGameName", "cgamename", "RomSet1", "GameName", PATTERN_TABLENAME);

  private final static List<Pattern> patternList = new ArrayList<>();

  static {
    PATTERNS.forEach(p -> patternList.add(Pattern.compile(".*" + p + ".*=.*\".*\".*")));
  }

  private static final Pattern HS_FILENAME_PATTERN = Pattern.compile(".*HSFileName.*=.*\".*\".*");
  private static final Pattern TXT_FILENAME_PATTERN = Pattern.compile(".*\".*\\.txt\".*");

  private VPXFileScanner() {
    //force scan method
  }

  public static ScanResult scan(@NonNull File gameFile) {
    long start = System.currentTimeMillis();
    ScanResult result = new ScanResult();

    String l = null;
    String script = VPXUtil.readScript(gameFile);
    List<String> split = Arrays.asList(script.split(System.getProperty("line.separator")));
    Collections.reverse(split);
    scanLines(gameFile, start, result, split);

    if (StringUtils.isEmpty(result.getRom())) {
      LOG.info("Regular scan failed, running deep scan for " + gameFile.getAbsolutePath());
      runDeepScan(gameFile, result);
    }

    if (!StringUtils.isEmpty(result.getRom())) {
      LOG.info("Finished scan of table " + gameFile.getAbsolutePath() + ", found ROM '" + result.getRom() + "', took " + (System.currentTimeMillis() - start) + " ms.");
    }
    else if (StringUtils.isEmpty(result.getRom()) && StringUtils.isEmpty(result.getTableName()) && !StringUtils.isEmpty(result.getHsFileName())) {
      result.setTableName(FilenameUtils.getBaseName(result.getHsFileName()));
      LOG.info("Finished scan of table " + gameFile.getAbsolutePath() + ", found EM highscore filename '" + result.getHsFileName() + "', took " + (System.currentTimeMillis() - start) + " ms.");
    }
    else {
      LOG.info("Finished scan of table " + gameFile.getAbsolutePath() + ", no ROM found" + "', took " + (System.currentTimeMillis() - start) + " ms.");
    }

    if (StringUtils.isEmpty(result.getRom()) && !StringUtils.isEmpty(result.getTableName())) {
      result.setRom(result.getTableName());
    }

    return result;
  }

  private static void runDeepScan(File gameFile, ScanResult result) {
    BufferedReader bufferedReader = null;
    ReverseLineInputStream reverseLineInputStream = null;
    String line = null;
    int count = 0;
    try {
      reverseLineInputStream = new ReverseLineInputStream(gameFile);
      bufferedReader = new BufferedReader(new InputStreamReader(reverseLineInputStream));

      bufferedReader.readLine();//skip last line if empty
      boolean continueRead = true;
      while (continueRead) {
        line = bufferedReader.readLine();
        if (line == null) {
          line = bufferedReader.readLine();
          line = bufferedReader.readLine();
          if (line == null) {
            break;
          }
        }
        count++;

        if (result.isScanComplete() || line.trim().equals("Option Explicit")) {
          break;
        }

        lineSearchRom(result, line);
        lineSearchNvOffset(result, line);
        lineSearchHsFileName(result, line);
        lineSearchEMHighscore(gameFile, result, line);
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
  }

  private static void lineSearchEMHighscore(File gameFile, ScanResult result, String line) {
    if (!StringUtils.isEmpty(line) && line.contains(".txt\"")) {
      String emFilename = line.substring(0, line.lastIndexOf(".txt\"") + 4);
      emFilename = emFilename.substring(emFilename.lastIndexOf("\"") + 1);
      if (result.getHsFileName() == null) {
        result.setHsFileName(emFilename);
      }
    }
  }

  private static void scanLines(@NonNull File gameFile, long start, ScanResult result, List<String> split) {
    String l;
    for (String line : split) {
      l = line;

      if (result.isScanComplete()) {
        break;
      }

      lineSearchRom(result, line);
      lineSearchNvOffset(result, line);
      lineSearchHsFileName(result, line);
    }
  }

  /**
   * Extracts an asset filename from the given line.
   * The asset may have a leading path info which is formatted too
   *
   * @param line      the line to check for assets
   * @param assetType the asset type to check
   * @return the asset filename or null if it could not be extracted
   */
  @Nullable
  private static String extractAsset(@NonNull String line, @NonNull String assetType) {
    String value = line.substring(0, line.indexOf("." + assetType) + (assetType.length() + 1));
    if (value.contains("\"")) {
      String asset = value.substring(value.lastIndexOf("\"") + 1);
      asset = asset.replaceAll("\\\\", "/");
      asset = asset.replaceAll("//", "/");
      if (!asset.startsWith(".")) {
        return asset;
      }
    }
    return null;
  }

  private static void lineSearchHsFileName(@NonNull ScanResult result, @NonNull String line) {
    if (result.getHsFileName() != null) {
      return;
    }

    if (HS_FILENAME_PATTERN.matcher(line).matches()) {
      String pattern = "HSFileName";
      if (line.contains("'") && line.trim().indexOf("'") < line.indexOf(pattern)) {
        return;
      }

      String hsFileName = extractLineValue(line, pattern);
      result.setHsFileName(hsFileName);
    }

    if (TXT_FILENAME_PATTERN.matcher(line).matches()) {
      String pattern = ".txt";
      if (line.contains("'") && line.trim().indexOf("'") < line.indexOf(pattern)) {
        return;
      }

      String hsFileName = line.substring(0, line.indexOf(".txt") + 4);
      if (hsFileName.contains("\"")) {
        hsFileName = hsFileName.substring(hsFileName.lastIndexOf("\"") + 1);
        if (hsFileName.equals(".txt")) {
          return;
        }
        if (hsFileName.endsWith("LUT.txt")) {
          return;
        }

        result.setHsFileName(hsFileName);
      }
    }
  }

  private static void lineSearchNvOffset(@NonNull ScanResult result, @NonNull String line) {
    if (result.getNvOffset() > 0) {
      return;
    }

    if (line.trim().startsWith("NVOffset") && line.contains("(") && line.contains(")")) {
      String nvOffsetString = null;
      try {
        nvOffsetString = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
        result.setNvOffset(Integer.parseInt(nvOffsetString));
      } catch (Exception e) {
        LOG.error("Failed to read NVOffset from line \"" + line + "\" and segment \"" + nvOffsetString + "\": " + e.getMessage());
      }
    }
  }

  /**
   * Single line eval for rom name
   */
  private static void lineSearchRom(@NonNull ScanResult result, @NonNull String line) {
    int patternMatch = matchesPatterns(line);
    if (patternMatch != -1) {
      String pattern = PATTERNS.get(patternMatch);

      //check if pattern match is behind a comment, then we ignore the line
      if (line.contains("'") && line.trim().indexOf("'") < line.indexOf(pattern)) {
        return;
      }

      //remove leading quote
      String extract = line.substring(line.indexOf(pattern) + pattern.length() + 1);
      int start = extract.indexOf("\"") + 1;
      String rom = extract.substring(start);
      int end = rom.indexOf("\"");

      //check if the name matches with the allowed length of the filename
      //this may differ: EM highscore filenames are usually longer that nvram names, this is not differed here!
      if (end - start < MAX_ROM_FILENAME_LENGTH) {
        rom = rom.substring(0, end).trim();
      }

      if (pattern.equals(PATTERN_TABLENAME)) {
        result.setTableName(rom);
      }
      result.setRom(rom);
    }
  }

  private static String extractLineValue(String line, String key) {
    line = line.substring(line.indexOf(key) + key.length() + 1);
    int start = line.indexOf("\"") + 1;
    String value = line.substring(start);
    int end = value.indexOf("\"");

    if (end - start < MAX_FILENAME_LENGTH) {
      value = value.substring(0, end).trim();
    }
    return value;
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
