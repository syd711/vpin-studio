package de.mephisto.vpin.server.vps;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.games.GameVpsMatch;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.Nullable;

@Service
public class VpsAutomatcher {
  private final static Logger LOG = LoggerFactory.getLogger(VpsAutomatcher.class);

  final static boolean LOG_DEBUG = false;

  private static VpsAutomatcher instance = new VpsAutomatcher();

  public static VpsAutomatcher getInstance() {
    return instance;
  }

  private VpsAutomatcher() {
  }

  //-------------------------------

  /**
   * pattern : Table (ManufacturerYear) info
   */
  static Pattern filePattern1 = Pattern.compile("([^(]+)\\((.*)(\\d\\d\\d\\d)\\)(.*)");
  /**
   * pattern : Table (Year Manufacturer) info
   */
  static Pattern filePattern2 = Pattern.compile("([^(]+)\\((\\d\\d\\d\\d)(.*)\\)(.*)");
  /**
   * pattern : Table Year info
   */
  static Pattern filePattern3 = Pattern.compile("(.+)(\\d\\d\\d\\d)(.+)");
  /**
   * Pattern for version number
   */
  static Pattern versionPattern = Pattern.compile("([rv]?[_.]?\\d+([_.]\\d+)+([r-]\\d+([_.]\\d+)*)?[abcde]?)(.*)");

  private static DecimalFormat decimalFormat = new DecimalFormat("0.00");

  private TableMatcher tableMatcher = new TableMatcher();

  private TableVersionMatcher tableVersionMatcher = new TableVersionMatcher();


  /**
   * Match filename against VPS Database mapping and return the VpsTable
   */
  public VpsTable autoMatchTable(VPS vpsDatabase, String filename) {
    TableNameParts parts = parseFilename(filename);
    String _cleanTableName = cleanTable(parts.tableName);
    return tableMatcher.findClosest(parts.displayName, null, true, _cleanTableName, parts.manufacturer, parts.year, vpsDatabase.getTables());
  }


  public GameVpsMatch autoMatch(VPS vpsDatabase, String gameBaseName) {
    return autoMatch(vpsDatabase, gameBaseName, null, null, null);
  }

  public GameVpsMatch autoMatch(VPS vpsDatabase, String gameBaseName, @Nullable String rom, @Nullable String author, @Nullable String version) {
    // first run a match with findClosest
    GameVpsMatch vpsMatch = new GameVpsMatch();
    // overwrite is forcibly true as GameVpsMatch is empty
    autoMatch(vpsMatch, vpsDatabase, gameBaseName, rom, StringUtils.isEmpty(rom), null, author, version, true);
    return vpsMatch;
  }

  /**
   * Match game and return a GameVpsMatch with the VPS Database mapping
   *
   * @return GameVpsMatch of ids
   */
  public GameVpsMatch autoMatch(VPS vpsDatabase, Game game, TableInfo tableInfo, boolean checkall, boolean overwrite, boolean useDisplayName) {
    GameVpsMatch vpsMatch = new GameVpsMatch();
    vpsMatch.setGameId(game.getId());
    vpsMatch.setExtTableId(game.getExtTableId());
    vpsMatch.setExtTableVersionId(game.getExtTableVersionId());
    vpsMatch.setVersion(game.getVersion());

    String gameFileName = game.getGameFileName();
    if (useDisplayName) {
      gameFileName = game.getGameDisplayName();
    }
    gameFileName = cleanFilename(FilenameUtils.getBaseName(gameFileName));

    autoMatch(vpsMatch, vpsDatabase, gameFileName, game.getRom(), checkall, 
      tableInfo!=null? tableInfo.getTableName(): null, 
      tableInfo!=null? tableInfo.getAuthorName(): null,
      tableInfo!=null? tableInfo.getTableVersion(): null,
      overwrite);
    return vpsMatch;
  }

  /**
   * Remove obvious file hints not related to the actual name.
   * @param baseName the file base name
   * @return
   */
  private String cleanFilename(String baseName) {
    String name = baseName;
    name = name.replace("VR Room", "");
    name = name.replace("VRRoom", "");
    return name;
  }

  /**
   * Match filename and fill the GameVpsMatch with VPS Database mapping
   */
  protected void autoMatch(GameVpsMatch vpsMatch, VPS vpsDatabase, String gameFileName, String rom, boolean checkall, 
      String tableInfoName, String tableInfoAuthor, String tableInfoVersion, boolean overwrite) {
    try {
      LOG.info("Find closest table for " + gameFileName);

      //------------------------------------------------------
      // Step 1, decompose the filename in elements:
      // tablename (manuf year) author version extra
      TableNameParts parts = parseFilename(gameFileName);

      String _version = null;

      Matcher match = null;
      // check version in extra
      if (StringUtils.isNotEmpty(parts.extra)) {
        match = versionPattern.matcher(parts.extra);
        if (match.find()) {
          _version = match.group(1);
          if (StringUtils.isNotEmpty(_version)) {
            parts.extra = StringUtils.remove(parts.extra, _version).trim();
          }
        }
      }
      // when not found in extra, check in table name
      if (_version == null) {
        match = versionPattern.matcher(parts.tableName);
        if (match.find()) {
          _version = match.group(1);
          if (StringUtils.isNotEmpty(_version)) {
            parts.tableName = StringUtils.remove(parts.tableName, _version);
          }
        }
      }
      // still not found, try from tableInfo
      if (_version == null && StringUtils.isNotEmpty(tableInfoVersion)) {
        if (!StringUtils.containsIgnoreCase(tableInfoVersion, "VP")) {
          match = versionPattern.matcher(tableInfoVersion);
          if (match.find()) {
            _version = match.group(1);
          }
        }
      }

      // clean the version if found
      if (_version != null) {
        _version = _version.replace('_', '.').trim();
        _version = StringUtils.removeStart(_version, "v");
        _version = StringUtils.removeStart(_version, "r");
        _version = StringUtils.removeStart(_version, ".");
      }

      String _cleanTableName = cleanTable(parts.tableName);

      LOG.info("parsed : Table=" + _cleanTableName + " | Manuf=" + parts.manufacturer + " | year=" + parts.year
          + " | version=" + _version + " | extra=" + parts.extra);

      //------------------------------------------------------
      // Step 2, match the table elements with VPS database

      VpsTable vpsTable = null;
      if (StringUtils.isEmpty(vpsMatch.getExtTableId()) || overwrite) {

        // first check already mapped table and confirm mapping
        if (StringUtils.isNotEmpty(vpsMatch.getExtTableId())) {
          VpsTable vpsTableById = vpsDatabase.getTableById(vpsMatch.getExtTableId());
          if (tableMatcher.isClose(parts.displayName, rom, checkall, _cleanTableName, parts.manufacturer, parts.year, vpsTableById)) {
            vpsTable = vpsTableById;
          }
        }
        // if not found, find closest
        if (vpsTable == null) {
          vpsTable = tableMatcher.findClosest(parts.displayName, rom, checkall, _cleanTableName, parts.manufacturer, parts.year, vpsDatabase.getTables());
        }

        if (vpsTable == null) {
          LOG.info("No table found by TableMatcher, use legacy matcher");

          List<VpsTable> vpsTables = new LegacyTableMatcher(vpsDatabase.getTables()).find(gameFileName);
          if (!vpsTables.isEmpty()) {
            vpsTable = vpsTables.get(0);
            LOG.info("Table found by legacy matcher '" + vpsTable + "'");
          }
          else {
            LOG.info("No table found");
          }
        }
        else {
          LOG.info("Table found or confirmed by TableMatcher '" + vpsTable + "'");
        }

        if (vpsTable != null) {
          // table found => update the TableId
          LOG.info(gameFileName + ": matched to VPS table \"" + vpsTable.getId() + "\"");
          vpsMatch.setExtTableId(vpsTable.getId());
        }
        else {
          LOG.info(gameFileName + ": Emptied table version");
          vpsMatch.setExtTableVersionId(null);
        }
      }
      else {
        // keep same table
        vpsTable = vpsDatabase.getTableById(vpsMatch.getExtTableId());
      }

      //------------------------------------------------------
      // Step 3, match the elements with VPS database

      if (vpsTable != null && (StringUtils.isEmpty(vpsMatch.getExtTableVersionId()) || overwrite)) {

        if (parts.extra == null) {
          // extra being null, take table name but remove the vps table name
          parts.extra = cleanTable(parts.tableName).replace(cleanTable(vpsTable.getName().toLowerCase()), "").trim();
        }
        else {
          parts.extra = cleanChars(parts.extra);
        }

        // the name in VPX File takes precedence over the extra information form filename
        tableInfoName = StringUtils.defaultString(tableInfoName, parts.extra);
    
        // the version in filename takes precedence over the version in the VPX file
        tableInfoVersion = StringUtils.defaultIfEmpty(_version, tableInfoVersion);
    
        // Parse and clean the authors
        // flag to tell this is not forcibly author as it is taken from extra so if author is not matched, 
        // bad score should be lowered as this may be caused by the fqct it is not author
        boolean authorFromTable = true;
        if (tableInfoAuthor==null) {
          // make sure it is not null
          String authorFromFile = StringUtils.defaultString(cleanWords(parts.extra));
          tableInfoAuthor = authorFromFile;
          authorFromTable = false;
        }

        LOG.info("parsed : Name=" + tableInfoName + " | Authors=" + tableInfoAuthor + " | version=" + tableInfoVersion);

        // capture debug information
        StringBuilder debug = new StringBuilder();
        if (LOG_DEBUG) {
          VpsAutomatcher.startDebug(debug);
          VpsAutomatcher.appendDebug(debug, " SCORE", 6);
          VpsAutomatcher.appendDebug(debug, "NAME", 30);
          VpsAutomatcher.appendDebug(debug, " dNAME", 6);
          VpsAutomatcher.appendDebug(debug, "AUTHORS", 50);
          VpsAutomatcher.appendDebug(debug, " dAUTH", 6);
          VpsAutomatcher.appendDebug(debug, "VERSION", 10);
          VpsAutomatcher.appendDebug(debug, " dVERS", 6);
          VpsAutomatcher.endDebug(debug);
        }

        VpsTableVersion vpsVersion = tableVersionMatcher.findVersion(vpsTable, tableInfoName, tableInfoAuthor, authorFromTable, tableInfoVersion, debug);

        if (LOG_DEBUG) {
          LOG.info(debug.toString());
        }

        if (vpsVersion != null) {
          LOG.info(gameFileName + ": matched to VPS table version \"" + vpsVersion + "\"");
          vpsMatch.setExtTableVersionId(vpsVersion.getId());
        }
        else {
          LOG.info(gameFileName + ": No table version matched");
          vpsMatch.setExtTableVersionId(null);
        }
      }

      //------------------------------------------------------
      // Step 4, add the parsed version
      if (StringUtils.isNotEmpty(_version) && (StringUtils.isEmpty(vpsMatch.getVersion()) || overwrite)) {
        vpsMatch.setVersion(_version);
      }

      LOG.info("Finished auto-match for \"" + gameFileName + "\"");
    }
    catch (Exception e) {
      LOG.error("Error auto-matching table data: " + e.getMessage(), e);
    }
  }

  //-------------------------------------------------

  private TableNameParts parseFilename(String gameFileName) {

    TableNameParts parts = new TableNameParts();

    parts.displayName = gameFileName
        // remove reference VP10, VP9.2, VPX08....
        .replaceAll("VPX?[\\d\\.]+", "")
        // remove all features [FSS] [DT] [B&W] [CCX+PGI] (2 or 3 uppercase letters between [])
        .replaceAll("\\[(\\+?[A-Z][A-Z&][A-Z]?){1,2}\\]", "")
        .toLowerCase();

    Matcher match = filePattern1.matcher(parts.displayName);
    if (match.find()) {
      parts.tableName = match.group(1).trim();
      parts.manufacturer = cleanChars(match.group(2)).trim();
      parts.year = Integer.parseInt(match.group(3));
      parts.extra = match.group(4).trim();
    }
    else {
      match = filePattern2.matcher(parts.displayName);
      if (match.find()) {
        parts.tableName = match.group(1).trim();
        parts.manufacturer = cleanChars(match.group(3)).trim();
        parts.year = Integer.parseInt(match.group(2));
        parts.extra = match.group(4).trim();
      }
      else {
        match = filePattern3.matcher(parts.displayName);
        if (match.find()) {
          parts.tableName = match.group(1).trim();
          parts.year = Integer.parseInt(match.group(2));
          parts.extra = match.group(3).trim();
        }
      }
    }
    // if parts._tableName and _extra are null at that stage, cancel the match and put _displayName in _tableName 
    if (parts.tableName == null && parts.extra == null) {
      parts.tableName = parts.displayName;
    }

    return parts;
  }

  private String cleanTable(String tableName) {
    tableName = cleanChars(tableName);
    tableName = tableName.replaceAll("the ", "");
    tableName = tableName.replaceAll(", the", "");
    return tableName;
  }

  // all lower case !
  private static String[] exludedWords = {
    "mod", "vpx", "vr", "dt", "fss", "fs", "4k", "alt", "alt2", "(1)", "(2)", "edition", "version"
  };

  private String cleanWords(String filename) {
    if (filename==null) {
        return null;
    }
    // remove exluded words
    for (String w : exludedWords) {
      filename = filename.replace(w, "");
    }
    return cleanChars(filename).trim();
  }

  static String cleanChars(String filename) {
    // replace underscore, ., -, ....
    filename = StringUtils.replaceChars(filename, "_.,+-'[]()", "          ");
    // remove double spaces
    while (filename.contains("  ")) {
      filename = filename.replace("  ", " ");
    }
    return filename.trim();
  }

  public static void startDebug(StringBuilder debug) {
    debug.append("\n | ");
  }
  public static void appendDebug(StringBuilder bld, double d, int size) {
    bld.append(StringUtils.leftPad(decimalFormat.format(d), size))
      .append(" | ");
  }
  
  public static void appendDebug(StringBuilder bld, String txt, int size) {
    bld.append(StringUtils.rightPad(txt != null ? StringUtils.abbreviate(txt, size): "", size))
      .append(" | ");
  }
  public static void endDebug(StringBuilder debug) {
  }

  /**
   * displayName = tableName (manufacturer year) extra
   */
  private class TableNameParts {
    String displayName = null;
    String tableName = null;
    String manufacturer = null;
    int year = -1;
    String extra = null;
  }
}
