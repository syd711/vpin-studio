package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.vpx.VPXUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
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

  private final static List<String> PATTERN_TABLENAME = Arrays.asList("TableName");
  private final static List<String> PATTERN_ROM = Arrays.asList("cGameName", "cgamename", "RomSet1", "GameName");

  private final static List<Pattern> romNamePatternList = new ArrayList<>();
  private final static List<Pattern> tableNamePatternList = new ArrayList<>();

  static {
    PATTERN_ROM.forEach(p -> romNamePatternList.add(Pattern.compile(".*" + p + ".*=.*\".*\".*")));
    PATTERN_TABLENAME.forEach(p -> tableNamePatternList.add(Pattern.compile(".*" + p + ".*=.*\".*\".*")));
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

    List<String> allLines= new ArrayList<>();
    script = script.replaceAll("\r", "\n");

    allLines.addAll(Arrays.asList(script.split("\n")));
    Collections.reverse(allLines);
    scanLines(result, allLines);

    //apply table name as ROM name, e.g. for EM tables
    if (StringUtils.isEmpty(result.getRom()) && !StringUtils.isEmpty(result.getTableName())) {
      result.setRom(result.getTableName());
    }

    if (StringUtils.isEmpty(result.getRom())) {
      LOG.info("Regular scan failed, running deep scan for " + gameFile.getAbsolutePath());
      runDeepScan(gameFile, result);
    }

    if (!StringUtils.isEmpty(result.getRom())) {
      LOG.info("Finished scan of table " + gameFile.getAbsolutePath() + ", found ROM '" + result.getRom() + "', took " + (System.currentTimeMillis() - start) + " ms for " + allLines.size() + " lines.");
    }
    else if (StringUtils.isEmpty(result.getRom()) && StringUtils.isEmpty(result.getTableName()) && !StringUtils.isEmpty(result.getHsFileName())) {
      result.setTableName(FilenameUtils.getBaseName(result.getHsFileName()));
      LOG.info("Finished scan of table " + gameFile.getAbsolutePath() + ", found EM highscore filename '" + result.getHsFileName() + "', took " + (System.currentTimeMillis() - start) + " ms for " + allLines.size() + " lines.");
    }
    else {
      LOG.info("Finished scan of table " + gameFile.getAbsolutePath() + ", no ROM found" + "', took " + (System.currentTimeMillis() - start) + " ms for " + allLines.size() + " lines.");
    }

    if(!StringUtils.isEmpty(result.getSomeTextFile()) && StringUtils.isEmpty(result.getHsFileName())) {
      result.setHsFileName(result.getSomeTextFile());
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
        lineSearchTableName(result, line);
        lineSearchNvOffset(result, line);
        lineSearchHsFileName(result, line);
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

  private static void lineSearchHsFileName(ScanResult result, String line) {
    if (HS_FILENAME_PATTERN.matcher(line).matches()) {
      String pattern = "HSFileName";
      if (line.contains("'") && line.trim().indexOf("'") < line.indexOf(pattern)) {
        return;
      }

      String hsFileName = extractLineValue(line, pattern);
      result.setHsFileName(hsFileName);
    }
  }

  private static void lineSearchTextFileName(ScanResult result, String line) {
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

        result.setSomeTextFile(hsFileName);
      }
    }
  }

  private static void scanLines(ScanResult result, List<String> split) {
    String l;
    for (String line : split) {
      l = line;

      lineSearchRom(result, line);
      lineSearchTableName(result, line);
      lineSearchNvOffset(result, line);
      lineSearchHsFileName(result, line);
      lineSearchTextFileName(result, line);
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

  private static void lineSearchTableName(@NonNull ScanResult result, @NonNull String line) {
    if (!StringUtils.isEmpty(result.getTableName())) {
      return;
    }

    int patternMatch = matchesPatterns(tableNamePatternList, line);
    if (patternMatch != -1) {
      String pattern = PATTERN_TABLENAME.get(patternMatch);
      result.setTableName(extractLineValue(line, pattern));
    }
  }

  /**
   * Single line eval for rom name
   */
  private static void lineSearchRom(@NonNull ScanResult result, @NonNull String line) {
    if (!StringUtils.isEmpty(result.getRom())) {
      return;
    }

    int patternMatch = matchesPatterns(romNamePatternList, line);
    if (patternMatch != -1) {
      String pattern = PATTERN_ROM.get(patternMatch);
      result.setRom(extractLineValue(line, pattern));
    }
  }

  private static String extractLineValue(String line, String pattern) {
    //check if pattern match is behind a comment, then we ignore the line
    if (line.contains("'") && line.trim().indexOf("'") < line.indexOf(pattern)) {
      return null;
    }

    //remove leading quote
    String extract = line.substring(line.indexOf(pattern) + pattern.length() + 1);
    int start = extract.indexOf("\"") + 1;
    String value = extract.substring(start);
    int end = value.indexOf("\"");

    //check if the name matches with the allowed length of the filename
    //this may differ: EM highscore filenames are usually longer that nvram names, this is not differed here!
    if (end - start < MAX_FILENAME_LENGTH) {
      value = value.substring(0, end).trim();
    }
    return value;
  }

  private static int matchesPatterns(List<Pattern> patternList, String line) {
    for (int i = 0; i < patternList.size(); i++) {
      Pattern pattern = patternList.get(i);
      if (pattern.matcher(line).matches()) {
        return i;
      }
    }
    return -1;
  }
}
