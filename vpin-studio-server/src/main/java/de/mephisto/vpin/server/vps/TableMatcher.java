package de.mephisto.vpin.server.vps;

import de.mephisto.vpin.connectors.vps.model.VpsAuthoredUrls;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.games.Game;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableMatcher {

  private double THRESHOLD_NOTFOUND = 3;

  /**
   * displayName pattern : Table (Manufacturer Year)
   */
  static Pattern filePatter = Pattern.compile("(.*)\\((.*) (\\d\\d\\d\\d)\\).*");

  private static TableMatcher instance = new TableMatcher();

  public static TableMatcher getInstance() {
    return instance;
  }

  public VpsTable findClosest(String displayName, String rom, List<VpsTable> tables) {

    final String _displayName = cleanChars(cleanWords(displayName));
    final String _tableName;
    final String _manuf;
    int _year;

    Matcher match = filePatter.matcher(_displayName);
    if (match.find()) {
      _tableName = match.group(1).trim();
      _manuf = match.group(2).trim();
      _year = Integer.parseInt(match.group(3));
    }
    else {
      _tableName = null;
      _manuf = null;
      _year = -1;
    }

    final double[] minDistance = new double[]{10000};
    final VpsTable[] found = new VpsTable[1];
    tables.stream()
        .filter(table -> checkRom(rom, table))
        .forEach(table -> {
          double dist = getTableDistance(table, _displayName, _tableName, _manuf, _year);
          if (dist < minDistance[0]) {
            found[0] = table;
            minDistance[0] = dist;
          }
        });

    // return the closest table if the distance is not too high
    VpsTable vpsTable = minDistance[0] < THRESHOLD_NOTFOUND ? found[0] : null;
    if (vpsTable == null) {
      List<VpsTable> vpsTables = new LegacyTableMatcher(tables).find(displayName);
      if (!vpsTables.isEmpty()) {
        vpsTable = vpsTables.get(0);
      }
    }
    return vpsTable;
  }


  /**
   * Take a table display name and a rom and compare it to the given VpsTable
   */
  public boolean isClose(String displayName, String rom, VpsTable table) {

    // first compare rom
    if (!checkRom(rom, table)) {
      return false;
    }

    final String _displayName = cleanChars(cleanWords(displayName));
    final String _tableName;
    final String _manuf;
    int _year;

    Matcher match = filePatter.matcher(_displayName);
    if (match.find()) {
      _tableName = match.group(1).trim();
      _manuf = match.group(2).trim();
      _year = Integer.parseInt(match.group(3));
    }
    else {
      _tableName = null;
      _manuf = null;
      _year = -1;
    }

    double dist = getTableDistance(table, _displayName, _tableName, _manuf, _year);
    return dist < THRESHOLD_NOTFOUND;
  }

  private double getTableDistance(VpsTable table, String _displayName, String _tableName, String _manuf, int _year) {
    String tableName = cleanChars(table.getName());
    String manuf = table.getManufacturer();
    String[] altmanufs = getAlternateManuf(manuf);
    int year = table.getYear();

    double minDistance = 10000;
    // return the minimal distance considering alternative manufacturer
    for (String altmanuf : altmanufs) {
      double dist = _tableName != null ? tableDistance(_tableName, _manuf, _year, tableName, altmanuf, year)
          : tableDistance(_displayName, tableName, altmanuf, year);
      if (dist < minDistance) {
        minDistance = dist;
      }
    }
    return minDistance;
  }

  private double tableDistance(String _displayName, String tableName, String manuf, int year) {
    double dist = 10000;

    String clean = _displayName;

    boolean confirmManuf = false;
    if (clean.contains(manuf.toLowerCase())) {
      clean = StringUtils.removeIgnoreCase(clean, manuf);
      confirmManuf = true;
    }

    boolean confirmYear = false;
    if (clean.contains(Integer.toString(year))) {
      clean = StringUtils.removeIgnoreCase(clean, Integer.toString(year));
      confirmYear = true;
    }

    double dTable = distance(clean, tableName);

    dist = (1 + dTable) * (confirmManuf ? 1 : 1.2) * (confirmYear ? 1 : 1.2);
    return dist;
  }

  private double tableDistance(String _tableName, String _manuf, int _year,
                               String tableName, String manuf, int year) {
    double dist = 10000;

    double dTable = distance(tableName, _tableName) * 1;
    double dManuf = distance(_manuf, manuf);
    double dYear = _year == year ? 0 : Math.abs(_year - year) == 1 ? 1 : 5;

    dist = (1 + dTable)
        * (1 + dManuf / 35.0)
        * (1 + dYear / 5.0);
    return dist;
  }

  public double distance(String str1, String str2) {

    if (StringUtils.isEmpty(str1) && StringUtils.isEmpty(str2)) {
      return 0;
    }
    else if (StringUtils.isEmpty(str1) || StringUtils.isEmpty(str2)) {
      return 5;
    }

    int ratio = FuzzySearch.weightedRatio(str1, str2);
    return ratio > 0 ? 10 * (100.0 / ratio - 1) : 10000;

    // Score 100 => distance = 0
    // Score 75 => distance = 3
  }

  // ------------------------------------------------------

  VpsTableVersion findVersion(VpsTable table, Game game, TableInfo tableInfo) {

    if (tableInfo == null) {
      return null;
    }

    String tableInfoName = tableInfo.getTableName();
    String tableInfoVersion = tableInfo.getTableVersion();
    String tableInfoAuthor = tableInfo.getAuthorName();

    if (StringUtils.isEmpty(tableInfoVersion) || StringUtils.isEmpty(tableInfoAuthor)) {
      return null;
    }
    // clean tableInfo author field and parse it
    String[] tableInfoAuthors = StringUtils.split(tableInfoAuthor.toLowerCase(), ",/-&");
    for (int i = 0, m = tableInfoAuthors.length; i < m; i++) {
      tableInfoAuthors[i] = tableInfoAuthors[i].trim();
    }
    tableInfoAuthor = StringUtils.join(tableInfoAuthors, " ");

    double distance = 10000;
    VpsTableVersion foundVersion = null;

    List<VpsTableVersion> tableFiles = table.getTableFiles();
    for (VpsTableVersion tableVersion : tableFiles) {
      if (tableVersion.getTableFormat() != null && tableVersion.getTableFormat().equalsIgnoreCase("FP")) {
        continue;
      }

      String name = tableVersion.getComment();
      String v = tableVersion.getVersion();
      String authors = null;

      // if match via name, rare but happens.., disconnect version match
      double dName = (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(tableInfoName))
          ? distance(name, tableInfoName) : 5;
      double dVersion = dName < 0.5 ? 0 : TableVersionMatcher.versionDistance(v, tableInfoVersion);

      // The version cannot have a greater date than the last modification of the game file
      // controversial, could be used as a criteria to bifurcate between to possible solutions
      //long lastmodif = game.getGameFile().lastModified();
      //if (lastmodif < tableVersion.getUpdatedAt()) {
      //	continue;
      //}

			/* Not really useful as it compares releaseDate with last update date
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(tableVersion.getUpdatedAt());
			int year = c.get(Calendar.YEAR);
			double dYear = tableInfo != null && StringUtils.isNotEmpty(tableInfo.getReleaseDate())
					? (tableInfo.getReleaseDate().contains(Integer.toString(year)) ? 1 : 2)
					: 1.2;*/

      List<String> tableVersionAuthors = tableVersion.getAuthors();
      int nbFirstAuthorsFoundInFirst = 0;
      double dAuthor = 0.0d;
      if (StringUtils.isNotEmpty(tableInfoAuthor) && tableVersionAuthors != null && tableVersionAuthors.size() > 0) {

        authors = StringUtils.join(tableVersionAuthors, " ").toLowerCase();

        // check if first authors of the table are also the first authors of the version
        for (int i = 0, m = tableInfoAuthors.length; i < m; i++) {
          for (int j = 0, n = tableVersionAuthors.size(); j < n && j < m; j++) {
            int r = FuzzySearch.ratio(tableInfoAuthors[i], tableVersionAuthors.get(j).toLowerCase());
            if (r > 90) {
              nbFirstAuthorsFoundInFirst++;
              break;
            }
          }
        }

        if (nbFirstAuthorsFoundInFirst == tableInfoAuthors.length) {
          // reduce weight of version when all authors of the tables are also the first authors of the version
          dVersion /= 3;

        }
        else {

          int r = FuzzySearch.weightedRatio(authors, tableInfoAuthor);
          dAuthor = r > 0 ? 100.0 / r - 1 : 100;
        }
      }

      double d = (1 + dVersion / 2) * (1 + dAuthor);
      if (d < distance) {
        distance = d;
        foundVersion = tableVersion;
      }
    }

    return distance <= 1.5 ? foundVersion : null;
  }

  /**
   * Take a rom and compare it with the one of the table
   * If rom is empty, verify the table does not use a rom
   *
   * @return true if this is same rom
   */
  public boolean checkRom(String rom, VpsTable table) {
    List<VpsAuthoredUrls> romFiles = table.getRomFiles();
    // if rom is empty, check the table does not use a rom
    if (StringUtils.isEmpty(rom)) {
      return romFiles == null || romFiles.size() == 0;
    }
    // else rom should be within the romFiles of the table if present
    if (romFiles != null && romFiles.size() > 0) {
      boolean found = false;
      for (VpsAuthoredUrls romFile : romFiles) {
        found |= romFile.getVersion() != null
            // do not check equals, cf afm_113 && afm_113b
            && (StringUtils.startsWithIgnoreCase(romFile.getVersion(), rom)
            || StringUtils.startsWithIgnoreCase(rom, romFile.getVersion()));
      }
      return found;
    }
    // else, conservative approach, check on name
    return true;
  }

  // ------------------------------------------------------

  private String[] getAlternateManuf(String manuf) {
    if (manuf.equalsIgnoreCase("Bally") || manuf.equalsIgnoreCase("Midway")) {
      return new String[]{"Bally", "Midway"};
    }
    else if (manuf.equalsIgnoreCase("Original") || manuf.equalsIgnoreCase("TBA")) {
      return new String[]{"Original", "TBA"};
    }
    else if (manuf.equalsIgnoreCase("Mylstar") || manuf.equalsIgnoreCase("Gottlieb")) {
      return new String[]{"Gottlieb", "Mylstar"};
    }
    else if (manuf.toLowerCase().startsWith("Taito")) {
      return new String[]{"Taito", "Taito do Brasil"};
    }
    else if (manuf.toLowerCase().startsWith("Spooky")) {
      return new String[]{"Spooky", "Spooky Pinball"};
    }

    return new String[]{manuf};
  }

  // all lower case !
  private static String[] exludedWords = {
      "mod", "bigus", "salas", "tasty", "thalamus", "paulie", "starlion",
      "vpx", "vpw", "vpinworkshop", "4k", "alt", "alt2", "(1)", "(2)"
  };

  private String cleanWords(String filename) {

    filename = filename.trim().toLowerCase();

    filename = filename.replaceAll("the ", "");
    filename = filename.replaceAll(", the", "");

    // remove exluded words
    for (String w : exludedWords) {
      filename = filename.replace(w, "");
    }

    // check if it ends with version, if yes remove
    boolean hasVersion = false;
    char ch;
    while (Character.isDigit(ch = filename.charAt(filename.length() - 1)) || ch == '.' || ch == '_' || ch == '-'
        || ch == ' ') {
      hasVersion = true;
      filename = StringUtils.substring(filename, 0, -1);
    }
    if (hasVersion && filename.charAt(filename.length() - 1) == 'v') {
      filename = StringUtils.substring(filename, 0, -1);
    }

    return filename;
  }

  private String cleanChars(String filename) {

    // replace underscore, ., -, ....
    filename = StringUtils.replaceChars(filename, "_.,-'", " ");

    // remove double spaces
    while (filename.contains("  ")) {
      filename = filename.replace("  ", " ");
    }

    return filename.trim();
  }

}
