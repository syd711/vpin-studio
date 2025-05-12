package de.mephisto.vpin.connectors.vps.matcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class TableNameSplitter {

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



  public TableNameParts parseFilename(String gameFileName) {

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
      parts.manufacturer = VpsAutomatcher.cleanChars(manuf);
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
      parts.version = extractVersion(parts.extra);
      if (StringUtils.isNotEmpty(parts.version)) {
        parts.extra = StringUtils.remove(parts.extra, parts.version).trim();
      }
    }
    // when not found in extra, check in table name
    if (parts.version == null) {
      parts.version = extractVersion(parts.tableName);
      if (StringUtils.isNotEmpty(parts.version)) {
        parts.tableName = StringUtils.remove(parts.tableName, parts.version);
      }
    }

    return parts;
  }

  public String extractVersion(String tableName) {
    Matcher match = versionPattern.matcher(tableName);
    if (match.find()) {
      return match.group(1);
    }
    return null;
  }

  /**
   * displayName = tableName (manufacturer year) extra
   */
  public static class TableNameParts {
    String displayName = null;
    String tableName = null;
    String manufacturer = null;
    int year = -1;
    String extra = null;
    String version = null;
  }

}
