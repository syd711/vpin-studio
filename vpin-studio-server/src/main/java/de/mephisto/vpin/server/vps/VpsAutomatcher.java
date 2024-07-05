package de.mephisto.vpin.server.vps;

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

@Service
public class VpsAutomatcher  {
  private final static Logger LOG = LoggerFactory.getLogger(VpsAutomatcher.class);

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
  static Pattern filePattern1 = Pattern.compile("([^(]+)\\((.*)(\\d\\d\\d\\d)\\)(.*)" );
  /**
   * pattern : Table (Year Manufacturer) info
   */
  static Pattern filePattern2 = Pattern.compile("([^(]+)\\((\\d\\d\\d\\d)(.*)\\)(.*)");
    /**
   * pattern : Table Year info
   */
  static Pattern filePattern3 = Pattern.compile("(.*)(\\d\\d\\d\\d)(.*)");
  /**
   * Pattern for version number
   */
  static Pattern versionPattern = Pattern.compile("([rv]?[_.]?\\d+([_.]\\d+)+([r-]\\d+([_.]\\d+)*)?[abcde]?)(.*)");

  private TableMatcher tableMatcher = new TableMatcher();

  private TableVersionMatcher tableVersionMatcher = new TableVersionMatcher();

  /**
   * Match game and return a GameVpsMatch with the VPS Database mapping
   * @return GameVpsMatch of ids
   */
  public GameVpsMatch autoMatch(VPS vpsDatabase, Game game, TableInfo tableInfo, boolean checkall, boolean overwrite) {
      GameVpsMatch vpsMatch = new GameVpsMatch();
      vpsMatch.setGameId(game.getId());
      vpsMatch.setExtTableId(game.getExtTableId());
      vpsMatch.setExtTableVersionId(game.getExtTableVersionId());
      vpsMatch.setVersion(game.getVersion());

      String gameFileName =  game.getGameFileName();
      gameFileName = FilenameUtils.getBaseName(gameFileName);

      autoMatch(vpsMatch, vpsDatabase, gameFileName, game.getRom(), checkall, tableInfo, overwrite);
      return vpsMatch;
  }

  /**
   * Match filename and fill the GameVpsMatch with VPS Database mapping
   */
  public void autoMatch(GameVpsMatch vpsMatch, VPS vpsDatabase, String gameFileName, String rom, boolean checkall, TableInfo tableInfo, boolean overwrite) {
    try {

      LOG.info("Find closest table for " + gameFileName);

      //------------------------------------------------------
      // Step 1, decompose the filename in elements:
      // tablename (manuf year) author version extra

      final String _displayName = gameFileName
            // remove reference VP10, VP9.2, VPX08....
            .replaceAll("VPX?[\\d\\.]+", "")
            // remove all features [FSS] [DT] [B&W] [CCX+PGI] (2 or 3 uppercase letters between [])
            .replaceAll("\\[(\\+?[A-Z][A-Z&][A-Z]?){1,2}\\]", "")
            .toLowerCase();
      String _tableName = null;
      String _manuf = null;
      String _extra = null;
      String _version = null;
      int _year = -1;

      Matcher match = filePattern1.matcher(_displayName);
      if (match.find()) {
        _tableName = match.group(1).trim();
        _manuf = cleanChars(match.group(2)).trim();
        _year = Integer.parseInt(match.group(3));
        _extra = match.group(4).trim();
      }
      else {
        match = filePattern2.matcher(_displayName);
        if (match.find()) {
          _tableName = match.group(1).trim();
          _manuf = cleanChars(match.group(3)).trim();
          _year = Integer.parseInt(match.group(2));
          _extra = match.group(4).trim();
        }
        else {
          match = filePattern3.matcher(_displayName);
          if (match.find()) {
            _tableName = match.group(1).trim();
            _year = Integer.parseInt(match.group(2));
            _extra = match.group(3).trim();
          }
        }
      }
      // if _tableName and _extra are null at that stage, cancel the match and put _displayName in _tableName 
      if (_tableName==null && _extra==null) {
        _tableName = _displayName;
      }

      // check version either in table name or in extra
      if (StringUtils.isNotEmpty(_extra)) {
        match = versionPattern.matcher(_extra);
        if (match.find()) {
          _version = match.group(1);
          if (StringUtils.isNotEmpty(_version)) {
            _extra = StringUtils.remove(_extra, _version);
          }
        }
      } 
      if (_version == null) {
        match = versionPattern.matcher(_tableName);
        if (match.find()) {
          _version = match.group(1);
          if (StringUtils.isNotEmpty(_version)) {
            _tableName = StringUtils.remove(_tableName, _version);
          }
        }
      }
      // still null, try from tableInfo
      if (_version == null && StringUtils.isNotEmpty(tableInfo.getTableVersion())) {
        if (!StringUtils.containsIgnoreCase(tableInfo.getTableVersion(), "VP")) {
          match = versionPattern.matcher(tableInfo.getTableVersion());
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

      String _cleanTableName = cleanTable(_tableName);
 
      LOG.info("parsed : Table="+_cleanTableName + " | Manuf=" + _manuf + " | year=" + _year 
        + " | version=" + _version + " | extra=" + _extra );

      //------------------------------------------------------
      // Step 2, match the table elements with VPS database

      VpsTable vpsTable = null;
      if (StringUtils.isEmpty(vpsMatch.getExtTableId()) || overwrite) {

        // first check already mapped table and confirm mapping
        if (StringUtils.isNotEmpty(vpsMatch.getExtTableId())) {
          VpsTable vpsTableById = vpsDatabase.getTableById(vpsMatch.getExtTableId());
          if (tableMatcher.isClose(_displayName, rom, checkall, _cleanTableName, _manuf, _year, vpsTableById)) {
            vpsTable = vpsTableById;
          }
        }
        // if not found, find closest
        if (vpsTable == null) {
          vpsTable = tableMatcher.findClosest(_displayName, rom, checkall, _cleanTableName, _manuf, _year, vpsDatabase.getTables());
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
        } else {
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

   
      if (vpsTable!=null && (StringUtils.isEmpty(vpsMatch.getExtTableVersionId()) || overwrite)) {  

        if (_extra == null) {
          // extra being null, take table name but remove the vps table name
          _extra = cleanTable(_tableName).replace(cleanTable(vpsTable.getName().toLowerCase()), "").trim();
        } else {
          _extra = cleanChars(_extra);
        }      

        VpsTableVersion version = tableVersionMatcher.findVersion(vpsTable, _extra, _version, tableInfo);
        if (version != null) {
          LOG.info(gameFileName + ": matched to VPS table version \"" + version + "\"");
          vpsMatch.setExtTableVersionId(version.getId());
        }
        else {
          LOG.info(gameFileName + ": Emptied table version");
          vpsMatch.setExtTableVersionId(null);
        }
      }

      //------------------------------------------------------
      // Step 4, add the parsed version
      if (StringUtils.isNotEmpty(_version)) {
        vpsMatch.setVersion(_version);
      }

      LOG.info("Finished auto-match for \"" + gameFileName + "\"");
    } catch (Exception e) {
      LOG.error("Error auto-matching table data: " + e.getMessage(), e);
    }
  }

  //-------------------------------------------------

  private String cleanTable(String tableName) {
    tableName = cleanChars(tableName);
    tableName = tableName.replaceAll("the ", "");
    tableName = tableName.replaceAll(", the", "");
    return tableName;
  }

  private String cleanChars(String filename) {
    // replace underscore, ., -, ....
    filename = StringUtils.replaceChars(filename, "_.,+-'[]()", " ");
    // remove double spaces
    while (filename.contains("  ")) {
      filename = filename.replace("  ", " ");
    }
    return filename.trim();
  }
}
