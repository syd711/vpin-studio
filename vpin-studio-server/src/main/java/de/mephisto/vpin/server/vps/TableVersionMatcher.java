package de.mephisto.vpin.server.vps;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.EditDistance;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;

public class TableVersionMatcher {

  private double THRESHOLD_NOTFOUND = 4.0;

  EditDistance<Double> ed = new CosineDistance();

  public VpsTableVersion findVersion(VpsTable table, String tableInfoName, String tableInfoAuthor, boolean authorFromTable, String tableInfoVersion, StringBuilder debug) {

    // one single file, returns it
    List<VpsTableVersion> tableFiles = table.getTableFiles();
    if (tableFiles.size()==1) {
        return tableFiles.get(0);
    }

    // Clean version
    tableInfoVersion = cleanVersion(tableInfoVersion);

    // if nothing is there to determine a version, returns null
    if (StringUtils.isEmpty(tableInfoName) 
            && StringUtils.isEmpty(tableInfoVersion) 
            && StringUtils.isEmpty(tableInfoAuthor)) {
      return null;
    }
      
    // clean tableInfo author field and parse it
    String[] tableInfoAuthors = StringUtils.split(tableInfoAuthor.toLowerCase(), ",/-&");
    for (int i = 0, m = tableInfoAuthors.length; i < m; i++) {
      tableInfoAuthors[i] = tableInfoAuthors[i].trim();
    }

    //------------------------------------

    double distance = 10000;
    VpsTableVersion foundVersion = null;

    for (VpsTableVersion tableVersion : tableFiles) {
      // skip non VPX tables
      if (tableVersion.getTableFormat() != null && tableVersion.getTableFormat().equalsIgnoreCase("FP")) {
        continue;
      }

      // distance on name
      String name = tableVersion.getComment();
      if (StringUtils.containsIgnoreCase(name, "Reupload")) {
        name = StringUtils.substringBefore(name, "Reupload");
      }
      double dName = 1;
      if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(tableInfoName)) {
        dName = distance(name, tableInfoName);
      }
      
      // distance on version
      double dVersion = 1;
      double dAuthor = 0.5d;

      List<String> tableVersionAuthors = tableVersion.getAuthors();
      // if strong match via name, disconnect other  match
      if (dName < 0.3) {
        dVersion = 0;
        dAuthor = 0;
      } 
      else {
        if (StringUtils.isNotEmpty(tableVersion.getVersion()) && StringUtils.isNotEmpty(tableInfoVersion)) {
          dVersion = versionDistance(tableVersion.getVersion(), tableInfoVersion);
        }

        // check match of authors 
        if (tableInfoAuthors.length > 0 && tableVersionAuthors != null && tableVersionAuthors.size() > 0) {
          double D = tableInfoAuthors.length * tableVersionAuthors.size();
          ArrayList<String> clone = new ArrayList<>(tableVersionAuthors);
          dAuthor = authorDistance(tableInfoAuthors, 0, clone, clone.size()) / D; 
          // reduce importance of dVersion if good rate of authors in exact same position as the VPS table 
          if (dAuthor == 0) {
            dVersion /= 3;
          }
          // reduce influence of the score as it is not real table author
          if (!authorFromTable) {
            dAuthor /= 2;
          }
        }
      }

      // now calculate the distance
      double d = (1 + dName) * (1 + dAuthor) * (1 + dVersion / 2)  - 1;

      if (VpsAutomatcher.LOG_DEBUG) {
        VpsAutomatcher.startDebug(debug);
        VpsAutomatcher.appendDebug(debug, d, 6);
        VpsAutomatcher.appendDebug(debug, tableVersion.getComment(), 30);
        VpsAutomatcher.appendDebug(debug, dName, 6);
        VpsAutomatcher.appendDebug(debug, StringUtils.join(tableVersionAuthors, ", "), 50);
        VpsAutomatcher.appendDebug(debug, dAuthor, 6);
        VpsAutomatcher.appendDebug(debug, tableVersion.getVersion(), 10);
        VpsAutomatcher.appendDebug(debug, dVersion, 6);
        VpsAutomatcher.endDebug(debug);
      }

      if (d < distance) {
        distance = d;
        foundVersion = tableVersion;
      }
    }

    return distance <= THRESHOLD_NOTFOUND ? foundVersion : null;
  }

  private double authorDistance(String[] tableInfoAuthors, int i, List<String> tableVersionAuthors, int max) {
    if (i < tableInfoAuthors.length) {
      for (int j = 0, n = tableVersionAuthors.size(); j < n; j++) {
        String auth1 = tableInfoAuthors[i];
        String auth2 = tableVersionAuthors.get(j).toLowerCase();
        double r = ed.apply(auth1, auth2);
        if (r < 0.3) {
          // author found, remove it and continue
          tableVersionAuthors.remove(j);
          // distance is the delta in position between the two authors 
          // + distance of the arrays without this author 
          return j + authorDistance(tableInfoAuthors, i + 1, tableVersionAuthors, max);
        }
      }
      // author not found in the array
      return max + authorDistance(tableInfoAuthors, i + 1, tableVersionAuthors, max);
    }
    // end of iteration
    return 0;
  }

  /**
   * Version 1 is the version from the VPS table
   * the version 2 (matched table version) 
   * version2 cannot be anterior to the VPS table, so penalize distance
   */
  public double versionDistance(String version1, String version2) {

    if (StringUtils.isEmpty(version1) && StringUtils.isEmpty(version2)) {
      return 0;
    }

    VersionTokenizer tokenizer1 = new VersionTokenizer(cleanVersion(version1));
    VersionTokenizer tokenizer2 = new VersionTokenizer(cleanVersion(version2));

    int number1 = 0, number2 = 0;
    int depth = 0;

    while (tokenizer1.MoveNext()) {
      if (!tokenizer2.MoveNext()) {
        // Version1 is longer than version2
        return 0.3 / Math.pow(10.0, depth);
      }
      number1 = tokenizer1.getNumber();
      number2 = tokenizer2.getNumber();

      // release
      if (number1 < number2) {
        // the more deep we are and the lesser the difference has an impact
        return Math.max(3 - depth, 1);  
      } 
      else if (number1 > number2) {
        // calculate number of release difference
        return Math.min((number1 - number2) / Math.pow(10.0, depth), 0.3);
      }
      depth++;
    }
    if (tokenizer2.MoveNext()) {
      // Version2 is longer than version1
      return 0.3 / Math.pow(10.0, depth);
    }

    String suffix1 = tokenizer1.getSuffix();
    String suffix2 = tokenizer2.getSuffix();
    if (StringUtils.startsWithAny(suffix1, "-", "r", "/", ":") && StringUtils.startsWithAny(suffix2, "-", "r", "/", ":")) {
      return versionDistance(suffix1.substring(1), suffix2.substring(1)) / 4;
    } else {
      if (StringUtils.isNotEmpty(suffix1) && StringUtils.isEmpty(suffix2)) {
        return 0.2;
      }
      if (StringUtils.isEmpty(suffix1) && StringUtils.isNotEmpty(suffix2)) {
        return 0.2;
      }

      int diff = StringUtils.compare(suffix1, suffix2);
      if (diff<0) {
        return 1;
      } else if (diff==0) {
        return 0;
      } else {
        return 0.1;
      }
    }
  }

  public double distance(String str1, String str2) {

    if (StringUtils.isEmpty(str1) && StringUtils.isEmpty(str2)) {
      return 0;
    }
    else if (StringUtils.isEmpty(str1) || StringUtils.isEmpty(str2)) {
      return 5;
    }

    double ratioCosine = ed.apply(str1.toLowerCase(), str2.toLowerCase());
    double ratio = ratioCosine;
    return ratio;

    //int ratio = FuzzySearch.weightedRatio(str1, str2);
    //return ratio > 0 ? (100.0 / ratio - 1) : 100;

    // Score 100 => distance = 0
    // Score 75 => distance = 3
  }

  private String cleanVersion(String version) {
    if (version==null) {
      return "";
    }
    if (StringUtils.startsWithIgnoreCase(version, "VP")) {
      int p = 2, l = version.length();
      while (p<l && StringUtils.indexOf("XS0123456789.", version.charAt(p))>=0) {
        p++;
      }
      version = version.substring(p);
    }
    return version.trim();
  }

  public static class VersionTokenizer {
    private final String _versionString;
    private final int _length;

    private int _position;
    private int _number;

    public int getNumber() {
      return _number;
    }

    public String getSuffix() {
      return _versionString.substring(_position);
    }

    public VersionTokenizer(String versionString) {
      if (versionString == null) {
        throw new IllegalArgumentException("versionString is null");
      }
      _versionString = versionString;
      _length = versionString.length();

      // remove all non first non numeric characters
      while (_position < _length) {
        char c = _versionString.charAt(_position);
        if (c >= '0' && c <= '9') break;
        _position++;
      }
    }

    public boolean MoveNext() {
      _number = 0;
      boolean hasValue = false;

      // No more characters ?
      while (_position < _length) {
        char c = _versionString.charAt(_position);
        if (c == '.') {
            _position++;
            break;
        }
        if (c < '0' || c > '9') { 
            break;
        }
        _number = _number * 10 + (c - '0');
        _position++;
        hasValue = true;
      }
      return hasValue;
    }
  }
}