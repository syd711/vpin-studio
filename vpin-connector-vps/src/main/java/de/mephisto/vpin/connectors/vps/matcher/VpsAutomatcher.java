package de.mephisto.vpin.connectors.vps.matcher;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import edu.umd.cs.findbugs.annotations.Nullable;

public class VpsAutomatcher {
  private final static Logger LOG = LoggerFactory.getLogger(VpsAutomatcher.class);

  private VpsDebug debug;

  public VpsAutomatcher(VpsDebug debug) {
    this.debug = debug;
    this.tableMatcher = new TableMatcher(debug);
    this.tableVersionMatcher = new TableVersionMatcher(debug);
  }

  //-------------------------------
  // run tests in https://regex101.com/

  /**
   * pattern : Table (Manufacturer Year) extra | Table (Year Manufacturer) extra
   */
  static Pattern filePattern1 = Pattern.compile("\\(((([\\w ]+)[() ]*(\\d\\d\\d\\d))|((\\d\\d\\d\\d)[() ]*)([\\w ]+))\\)(.*)");
  /**
   * pattern : Table Year extra
   */
  static Pattern filePattern3 = Pattern.compile("(.+)(\\d\\d\\d\\d)(.+)");
  /**
   * Pattern for version number
   */
  static Pattern versionPattern = Pattern.compile("([rv]?[_.]?\\d+([_.]\\d+)+([r-]\\d+([_.]\\d+)*)?[abcde]?)(.*)", Pattern.CASE_INSENSITIVE);

  private TableMatcher tableMatcher;

  private TableVersionMatcher tableVersionMatcher;

  //------------------------------------
  /**
   * Match filename against VPS Database mapping and return the VpsTable
   */
  public VpsTable autoMatchTable(VPS vpsDatabase, String filename) {
    return autoMatchTable(vpsDatabase, filename, null);
  }

  public VpsTable autoMatchTable(VPS vpsDatabase, String filename, String rom) {
    TableNameParts parts = parseFilename(filename);
    return tableMatcher.findClosest(parts.displayName, rom, parts.tableName, parts.manufacturer, parts.year, vpsDatabase.getTables());
  }

  /**
   * Match filename against VPS Database mapping and return the VpsTable
   */
  public List<VpsTable> autoMatchTables(VPS vpsDatabase, String filename) {
    TableNameParts parts = parseFilename(filename);
    return tableMatcher.findAllClosest(parts.displayName, null, parts.tableName, parts.manufacturer, parts.year, vpsDatabase.getTables());
  }

  //------------------------------------
  public VpsMatch autoMatch(VPS vpsDatabase, String[] tableFormats, String gameFileName) {
    return autoMatch(vpsDatabase, tableFormats, gameFileName, null, null, null);
  }

  public VpsMatch autoMatch(VPS vpsDatabase, String[] tableFormats, String gameFileName, @Nullable String rom, @Nullable String author, @Nullable String version) {
    // first run a match with findClosest
    VpsMatch vpsMatch = new VpsMatch();
    // overwrite is forcibly true as GameVpsMatch is empty
    autoMatch(vpsMatch, vpsDatabase, tableFormats, gameFileName, rom, null, author, version, null, true);
    return vpsMatch;
  }

  /**
   * Match filename and fill the GameVpsMatch with VPS Database mapping
   */
  public void autoMatch(VpsMatch vpsMatch, VPS vpsDatabase, String[] tableFormats, String gameFileName, String rom, 
      String tableInfoName, String tableInfoAuthor, String tableInfoVersion, Long lastUpdate, boolean overwrite) {
    try {
      LOG.info("Find closest table for " + gameFileName);

      //------------------------------------------------------
      // Step 1, decompose the filename in elements:
      // tablename (manuf year) author version extra
      TableNameParts parts = parseFilename(gameFileName);

      LOG.info("parsed : Table=" + parts.tableName + " | Manuf=" + parts.manufacturer + " | year=" + parts.year + " | extra=" + parts.extra);

      //------------------------------------------------------
      // Step 2, match the table elements with VPS database

      VpsTable vpsTable = null;
      if (StringUtils.isEmpty(vpsMatch.getExtTableId()) || overwrite) {

        // first check already mapped table and confirm mapping
        if (StringUtils.isNotEmpty(vpsMatch.getExtTableId())) {
          VpsTable vpsTableById = vpsDatabase.getTableById(vpsMatch.getExtTableId());
          if (tableMatcher.isClose(parts.displayName, rom, parts.tableName, parts.manufacturer, parts.year, vpsTableById)) {
            vpsTable = vpsTableById;
          }
        }
        // if not found, find closest
        if (vpsTable == null) {
          vpsTable = tableMatcher.findClosest(parts.displayName, rom, parts.tableName, parts.manufacturer, parts.year, vpsDatabase.getTables());
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
      // Step 3, match the table version with VPS database

      String version = parts.version;

      if (vpsTable != null && (StringUtils.isEmpty(vpsMatch.getExtTableVersionId()) || overwrite)) {

        if (parts.extra == null) {
          // extra being null, take table name but remove the vps table name
          parts.extra = StringUtils.removeIgnoreCase(parts.tableName, vpsTable.getName()).trim();
        }
        else {
          parts.extra = cleanChars(parts.extra);
        }
        // the name in VPX File takes precedence over the extra information form filename
        String name = StringUtils.defaultString(tableInfoName, parts.extra);

        // still no version found, try from tableInfo
        if (version == null && StringUtils.isNotEmpty(tableInfoVersion)) {
          if (!StringUtils.containsIgnoreCase(tableInfoVersion, "VP")) {
            Matcher match = versionPattern.matcher(tableInfoVersion);
            if (match.find()) {
              version = match.group(1);
            }
          }
        }
        // clean the version if found
        if (version != null) {
          version = version.replace('_', '.').toLowerCase().trim();
          version = StringUtils.removeStart(version, "v");
          version = StringUtils.removeStart(version, "r");
          version = StringUtils.removeStart(version, ".");
        }
      
        // Parse and clean the authors
        // flag to tell this is not forcibly author as it is taken from extra so if author is not matched, 
        // bad score should be lowered as this may be caused by the fact it is not author
        boolean authorsFromTable = true;
        String authors = tableInfoAuthor;
        if (StringUtils.isEmpty(authors)) {
          // make sure it is not null
          authorsFromTable = false;
          authors = StringUtils.defaultString(cleanWords(parts.extra));
        }

        LOG.info("parsed : Name=" + name + " | Authors=" + authors + " | version=" + version);

        // capture debug information
        if (debug != null) {
          debug.startDebug();
          debug.appendDebug(" SCORE", 6);
          debug.appendDebug("NAME", 30);
          debug.appendDebug(" dNAME", 6);
          debug.appendDebug("AUTHORS", 50);
          debug.appendDebug(" dAUTH", 6);
          debug.appendDebug("VERSION", 10);
          debug.appendDebug(" dVERS", 6);
          debug.endDebug();
        }

        // match version
        VpsTableVersion vpsVersion = tableVersionMatcher.findVersion(vpsTable, tableFormats, name, authors, authorsFromTable, version, lastUpdate);

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
      if (StringUtils.isNotEmpty(version) && (StringUtils.isEmpty(vpsMatch.getVersion()) || overwrite)) {
        vpsMatch.setVersion(version);
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
        .replaceAll("\\[(\\+?[A-Z][A-Z&][A-Z]?){1,2}\\]", "");
        //.toLowerCase();

    Matcher match = filePattern1.matcher(parts.displayName);
    if (match.find()) {
      parts.tableName = parts.displayName.substring(0, match.start());
      String manuf = ObjectUtils.firstNonNull(match.group(3), match.group(7));
      parts.manufacturer = cleanChars(manuf).trim();
      String year = ObjectUtils.firstNonNull(match.group(4), match.group(6));
      parts.year = Integer.parseInt(year);
      parts.extra = match.group(8).trim();
    }
    else {
      match = filePattern3.matcher(parts.displayName);
      if (match.find()) {
        parts.tableName = match.group(1).trim();
        parts.year = Integer.parseInt(match.group(2));
        parts.extra = match.group(3).trim();
      }
    }
    // if parts._tableName and _extra are null at that stage, cancel the match and put _displayName in _tableName 
    if (parts.tableName == null && parts.extra == null) {
      parts.tableName = parts.displayName;
    }

    // check version in extra
    if (StringUtils.isNotEmpty(parts.extra)) {
      match = versionPattern.matcher(parts.extra);
      if (match.find()) {
        parts.version = match.group(1);
        if (StringUtils.isNotEmpty(parts.version)) {
          parts.extra = StringUtils.remove(parts.extra, parts.version).trim();
        }
      }
    }
    // when not found in extra, check in table name
    if (parts.version == null) {
      match = versionPattern.matcher(parts.tableName);
      if (match.find()) {
        parts.version = match.group(1);
        if (StringUtils.isNotEmpty(parts.version)) {
          parts.tableName = StringUtils.remove(parts.tableName, parts.version);
        }
      }
    }
    return parts;
  }

  // all lower case !
  private static String[] exludedWords = {
    "mod", "vpx", "vr", "dt", "fss", "fs", "4k", "alt", "alt2", "(1)", "(2)", "edition", "version", "vrroom"
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

  /**
   * displayName = tableName (manufacturer year) extra
   */
  private class TableNameParts {
    String displayName = null;
    String tableName = null;
    String manufacturer = null;
    int year = -1;
    String extra = null;
    String version = null;
  }
}
