package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.scripteval.EvaluationContext;
import de.mephisto.vpin.server.vpx.VPXUtil;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks the different lines that are in the vpx file.
 * Usually the variable not does differ that much.
 * We read the file from the end to save time.
 */
public class VPXFileScanner {
  private final static Logger LOG = LoggerFactory.getLogger(VPXFileScanner.class);

  //private final static int MAX_ROM_FILENAME_LENGTH = 32;
  private final static int MAX_FILENAME_LENGTH = 128;

  private final static List<String> PATTERN_TABLENAME = Arrays.asList("TableName");
  private final static List<String> PATTERN_ROM = Arrays.asList("cGameName", "cgamename", "RomSet1", "GameName");
  //private final static List<String> PATTERN_PUP_PACK = Arrays.asList("pGameName", "pgamename");

  private final static List<Pattern> romNamePatternList = new ArrayList<>();
  private final static List<Pattern> tableNamePatternList = new ArrayList<>();
  //private final static List<Pattern> pupPackPatternList = new ArrayList<>();

  static {
    PATTERN_ROM.forEach(p -> romNamePatternList.add(Pattern.compile(".*" + p + ".*=.*\".*\".*")));
    PATTERN_TABLENAME.forEach(p -> tableNamePatternList.add(Pattern.compile(".*" + p + ".*=.*\".*\".*")));
    //PATTERN_PUP_PACK.forEach(p -> pupPackPatternList.add(Pattern.compile(".*" + p + ".*=.*\".*\".*")));
  }

  private static final Pattern HS_FILENAME_PATTERN = Pattern.compile(".*HSFileName.*=.*\".*\".*");
  private static final Pattern TXT_FILENAME_PATTERN = Pattern.compile(".*\".*\\.txt\".*");

  private static final Pattern VAR_PATTERN = Pattern.compile("(?:Set *)?(\\w*)\\s*=\\s*(.*)");


  private static final ExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  VPXFileScanner() {
    //force scan method
  }

  public static ScanResult scan(@NonNull File gameFile, @NonNull File scriptFolder) {
    ScanResult result = new ScanResult();
    Future<ScanResult> future = scheduler.submit(new Callable<ScanResult>() {
      @Override
      public ScanResult call() {
        long start = System.currentTimeMillis();

        String script = VPXUtil.readScript(gameFile);
        List<String> allLines = scanLines(gameFile, scriptFolder, result, script);

        //---------------------
        // Post manupulations

        if (StringUtils.isNotEmpty(result.getGameName())) {
          result.setRom(result.getGameName());
        }

        //apply table name as ROM name, e.g. for EM tables
        if (StringUtils.isEmpty(result.getRom()) && !StringUtils.isEmpty(result.getTableName())) {
          result.setRom(result.getTableName());
        }

        if (StringUtils.isEmpty(result.getRom()) && allLines.size() > 1) {
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

        if (!result.isFoundControllerStop()) {
//      LOG.warn("No 'Controller.stop' call found for \"" + gameFile.getAbsolutePath() + "\"");
        }

        if (!StringUtils.isEmpty(result.getSomeTextFile()) && StringUtils.isEmpty(result.getHsFileName())) {
          result.setHsFileName(result.getSomeTextFile());
        }

        if (StringUtils.isEmpty(result.getRom()) && !StringUtils.isEmpty(result.getHsFileName())) {
          result.setRom(FilenameUtils.getBaseName(result.getHsFileName()));
        }

        return result;
      }
    });

    try {
      return future.get(130, TimeUnit.SECONDS);
    }
    catch (TimeoutException e) {
      LOG.error("Failed to read {}: {}", gameFile.getAbsolutePath(), "read timed out");
      return result;
    }
    catch (Exception e) {
      LOG.error("Failed to read {}: {}", gameFile.getAbsolutePath(), e.getMessage(), e);
      return result;
    }
  }

  private static void runDeepScan(File gameFile, ScanResult result) {
    BufferedReader bufferedReader = null;
    ReverseLineInputStream reverseLineInputStream = null;
    String line = null;
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

        if (result.isScanComplete() || line.trim().equals("Option Explicit")) {
          break;
        }

        lineSearchRom(result, line);
        lineSearchTableName(result, line);
        lineSearchNvOffset(result, line);
        lineSearchHsFileName(result, line);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read rom line '" + line + "' for  " + gameFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    finally {
      try {
        if (reverseLineInputStream != null) {
          reverseLineInputStream.close();
        }

        if (bufferedReader != null) {
          bufferedReader.close();
        }
      }
      catch (Exception e) {
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
    if (!line.contains("Array(") && TXT_FILENAME_PATTERN.matcher(line).matches()) {
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
        if (hsFileName.endsWith("LUT.txt") || hsFileName.toLowerCase().contains("debug") || hsFileName.contains("/")) {
          return;
        }

        result.setSomeTextFile(hsFileName);
      }
    }
  }

  public static List<String> scanLines(File gameFile, File scriptFolder, ScanResult result, String script) {
    EvaluationContext evalctxt = new EvaluationContext();

    // so that curDir can be resolved 
    evalctxt.setVarValue("curdir", ".");
    // simulate Table1 object
    Map<String, String> table1 = new HashMap<>();
    table1.put("Filename", FilenameUtils.getBaseName(gameFile.getName()));
    evalctxt.setVarValue("Table1", table1);

    return scanLines(evalctxt, gameFile, scriptFolder, result, script);
  }

  /**
   * Recursive, sharing EvaluationContext
   */
  private static List<String> scanLines(EvaluationContext evalctxt, File gameFile, File scriptFolder, ScanResult result, String script) {

    List<String> allLines = new ArrayList<>();
    script = script.replaceAll("\r\n", "\n");
    script = script.replaceAll("\r", "\n");
    allLines.addAll(Arrays.asList(script.split("\n")));
    LOG.info("Scanning {} lines for {}", allLines.size(), gameFile.getName());
    //Collections.reverse(allLines);

    List<String> includedScripts = new ArrayList<>();

    int nbline = 0;
    String prevLine = "";
    for (String fulline : allLines) {
      String[] statements = stripComments(fulline);
      for (String statement : statements) {
        if (statement.endsWith("_")) {
          prevLine += statement.substring(0, statement.length() - 1) + " ";
        }
        else {
          prevLine += statement;
          detectLine(result, includedScripts, evalctxt, nbline, prevLine);
          // new real line
          prevLine = "";
        }
      }
      nbline++;
    }

    for (String included : includedScripts) {
      File includedFile = new File(scriptFolder, included);
      if (includedFile.exists()) {
        try (InputStream in = new FileInputStream(includedFile)) {
          String includedScript = IOUtils.toString(in, StandardCharsets.UTF_8);
          result.getScripts().clear();
          scanLines(evalctxt, gameFile, scriptFolder, result, includedScript);
        }
        catch (IOException ioe) {
          LOG.error("Cannot open included script {}", included, ioe);
        }
      }
    }
    result.getScripts().addAll(includedScripts);

    //-------------------
    // Copy discovered variables into Scanresult

    String cGameName = StringUtils.defaultString(evalctxt.getVarValue("cGameName"), evalctxt.getVarValue("GameName"));
    if (StringUtils.isNotEmpty(cGameName)) {
      result.setRom(cGameName);
    }

    String tableName = StringUtils.defaultString(evalctxt.getVarValue("TableName"), evalctxt.getVarValue("B2STableName"));
    if (StringUtils.isNotEmpty(tableName)) {
      result.setTableName(tableName);
    }

    String pGameName = evalctxt.getVarValue("pGameName");
    if (StringUtils.isNotEmpty(pGameName)) {
      result.setPupPackName(pGameName);
    }

    Object vrroom = ObjectUtils.firstNonNull(evalctxt.getVarValue("vrroom"), evalctxt.getVarValue("vr_room"));
    if (vrroom != null) {
      result.setVrRoomSupport(vrroom != null);
      result.setVrRoomDisabled(vrroom == null || vrroom.toString().equals("0"));
    }

    return allLines;
  }

  private static void detectLine(ScanResult result, List<String> includedScripts, EvaluationContext evalctxt, int nbline, String line) {
    try {
      lineDetectWith(result, line, evalctxt);
      lineEvaluateVars(line, evalctxt);
      lineSearchGameName(result, line, evalctxt);
      //lineSearchRom(result, line);
      //lineSearchPupPack(result, line);
      //lineSearchTableName(result, line);
      lineSearchNvOffset(result, line);
      lineSearchHsFileName(result, line);
      lineSearchTextFileName(result, line);
      lineSearchControllerStop(result, line);
      //lineSearchVRRoom(result, line);
      lineSearchDMDType(result, line);
      lineSearchInitUltraDmd(result, line, evalctxt);
      lineSearchDMDProjectFolder(result, line, evalctxt);
      lineSearchScript(includedScripts, line, evalctxt);
    }
    catch (Exception e) {
      LOG.error("error on line " + nbline, e);
    }
  }

  /**
   * Complex case
   * Const tableName = "ex""'otic" & " 'and' " & "com'""plex" ' with a "comment" at the end
   * => ex"'otic 'and' com'"plex
   */
  public static String[] stripComments(String line) {
    ArrayList<String> statements = new ArrayList<>();
    int idx = 0;
    boolean inString = false;
    StringBuilder bld = new StringBuilder();
    while (idx < line.length()) {
      char c = line.charAt(idx);
      if (c == '\'' && !inString) {
        // start of comment then end of statements
        break;
      }
      // detection of a column outside a string => capture statement and re-init buffer
      if (c == ':' && !inString) {
        String statement = bld.toString().trim();
        if (statement.length() > 0) {
          statements.add(statement);
        }
        bld.setLength(0);
      }
      else {
        // this is a character to keep
        if (c == '\"') {
          // detection of String
          inString = !inString;
        }
        bld.append(c);
      }
      idx++;
    }
    // end of the line, capture latest statement
    String statement = bld.toString().trim();
    if (statement.length() > 0) {
      statements.add(statement);
    }
    return statements.toArray(new String[0]);
  }

  private static void lineEvaluateVars(String line, EvaluationContext evalctxt) {
    if (StringUtils.startsWithIgnoreCase(line, "const ")) {
      final String varargs = line.substring(6);
      evalctxt.onEvaluateList("TO_ARRAY(" + varargs + ")", list -> {
        //LOG.info("line parsed {}", varargs);
      });
    }
    else {
      Matcher matcher = VAR_PATTERN.matcher(line);
      if (matcher.matches()) {
        String var = matcher.group(1);
        String varExp = matcher.group(2).trim();
        evalctxt.setVarExpression(var, varExp);
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
      }
      catch (Exception e) {
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

  private static void lineSearchControllerStop(@NonNull ScanResult result, @NonNull String line) {
    //for some reason there are 2x variants with upper and lower case
    if (line.toLowerCase().contains("controller.stop")) {
      result.setFoundControllerStop(true);
    }
    //for some reason there are 2x variants with upper and lower case
    if (line.toLowerCase().contains("table1_exit")) {
      result.setFoundTableExit(true);
    }
  }

  /**
   * Single line eval for rom name
   */
  private static void lineSearchRom(@NonNull ScanResult result, @NonNull String line) {
    if (!StringUtils.isEmpty(result.getRom())) {
      if (!line.toLowerCase().startsWith("const cgamename")) {
        return;
      }
    }

    int patternMatch = matchesPatterns(romNamePatternList, line);
    if (patternMatch != -1) {
      String pattern = PATTERN_ROM.get(patternMatch);
      result.setRom(extractLineValue(line, pattern));
    }
  }

  private static void lineDetectWith(@NonNull ScanResult result, @NonNull String line, EvaluationContext evalctxt) {
    if (line.startsWith("With ")) {
      String object = null;
      if (StringUtils.startsWithIgnoreCase(line, "With Controller")) {
        object = "Controller";
      }
      else if (StringUtils.startsWithIgnoreCase(line, "With FlexDMD")) {
        object = "FlexDMD";
      }
      if (object != null) {
        evalctxt.setTempValue("withCurrentObject", object);
      }
    }
    else if (line.startsWith("End With")) {
      evalctxt.removeTempValue("withCurrentObject");
    }
  }

  /**
   * Single line eval for rom name
   */
  private static void lineSearchGameName(@NonNull ScanResult result, @NonNull String line, EvaluationContext evalctxt) {
    if (line.contains(".GameName")) {
      String gameName = extractAfterPatternsValue(line, ".GameName", "=");
      gameName = StringUtils.substringBefore(gameName, ":");
      // object if specified before
      String object = StringUtils.substringBefore(line, ".GameName").trim();
      if (StringUtils.isEmpty(object)) {
        // object is not specified, get the With Object statement
        object = evalctxt.getTempValue("withCurrentObject");
      }

      // handler when gameName is computed
      if (StringUtils.isNotEmpty(object)) {
        final String with = object;
        evalctxt.onEvaluateString(gameName, res -> {
          if ("Controller".equalsIgnoreCase(with)) {
            result.setGameName(res);
          }
          else if ("FlexDMD".equalsIgnoreCase(with)) {
            result.setDMDGameName(res);
          }
        });
      }
    }
  }

  /**
   * Single line eval for rom name
   */
  /*private static void lineSearchVRRoom(@NonNull ScanResult result, @NonNull String line) {
    if (result.isVrRoomSupport()) {
      return;
    }

    if (line.toLowerCase().contains("const vrroom ") || line.toLowerCase().contains("const vr_room ")) {
      result.setVrRoomSupport(true);

      if (line.toLowerCase().contains("const vrroom = 0")
          || line.toLowerCase().contains("const vrroom=0")
          || line.toLowerCase().contains("const vr_room=0")
          || line.toLowerCase().contains("const vr_room = 0")
      ) {
        result.setVrRoomDisabled(true);
      }
    }
  }*/

  /**
   * Single line eval for rom name
   */
  /*private static void lineSearchPupPack(@NonNull ScanResult result, @NonNull String line) {
    if (!StringUtils.isEmpty(result.getPupPackName()) || line.startsWith("'")) {
      return;
    }

    int patternMatch = matchesPatterns(pupPackPatternList, line);
    if (patternMatch != -1) {
      String pattern = PATTERN_PUP_PACK.get(patternMatch);
      result.setPupPackName(extractLineValue(line, pattern));
    }
  }*/
  private static void lineSearchDMDType(ScanResult result, String line) {
    if (StringUtils.containsIgnoreCase(line, "CreateObject(\"UltraDMD.DMDObject\")")) {
      result.setDMDType("UltraDMD");
    }
    else if (StringUtils.containsIgnoreCase(line, "CreateObject(\"FlexDMD.FlexDMD\")")) {
      result.setDMDType("FlexDMD");
    }
  }

  private static void lineSearchInitUltraDmd(ScanResult result, String line, EvaluationContext evalctxt) {
    if (StringUtils.startsWithIgnoreCase(line, "InitUltraDMD")) {
      int idx = line.indexOf(",");
      String folder = line.substring(12, idx);
      //String tableName = line.substring(idx + 1);

      // cf UltraDMD_Options.vbs
      result.setDMDType("UltraDMD");
      evalctxt.onEvaluateString(folder, res -> result.setDMDProjectFolder(res + ".UltraDMD"));
    }
  }

  private static void lineSearchDMDProjectFolder(ScanResult result, String line, EvaluationContext evalctxt) {
    String prjFolder = null;
    if (StringUtils.containsIgnoreCase(line, ".SetProjectFolder")) {
      prjFolder = extractAfterPatternsValue(line, ".SetProjectFolder");
    }
    else if (StringUtils.containsIgnoreCase(line, ".ProjectFolder")) {
      prjFolder = extractAfterPatternsValue(line, ".ProjectFolder", "=");
    }

    if (prjFolder != null) {
      evalctxt.onEvaluateString(prjFolder, res -> {
        if (res.startsWith(".\\")) {
          res = StringUtils.substring(res, 2);
          res = StringUtils.removeEnd(res, "\\");
        }
        else if (res.startsWith("./")) {
          res = StringUtils.substring(res, 2);
          res = StringUtils.removeEnd(res, "/");
        }
        result.setDMDProjectFolder(res);
      });
    }
  }

  private static void lineSearchScript(List<String> includedScripts, String line, EvaluationContext evalctxt) {
    if (StringUtils.containsIgnoreCase(line, "GetTextFile")) {
      String script = StringUtils.substringAfter(line, "GetTextFile");
      script = StringUtils.substringBetween(script, "(", ")").trim();
      evalctxt.onEvaluateString(script, res -> {
        includedScripts.add(res);
      });
    }
  }

  //------------------------------------------------------------

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

  private static String extractAfterPatternsValue(String line, String... patterns) {
    String extract = line;
    for (String pattern : patterns) {
      int idx = StringUtils.indexOfIgnoreCase(extract, pattern);
      // pattern not found
      if (idx < 0) {
        return extract;
      }
      extract = extract.substring(idx + pattern.length()).trim();
    }
    return extract;
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
