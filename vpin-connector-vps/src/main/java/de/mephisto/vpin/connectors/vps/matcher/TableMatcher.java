package de.mephisto.vpin.connectors.vps.matcher;

import de.mephisto.vpin.connectors.vps.model.VpsAuthoredUrls;
import de.mephisto.vpin.connectors.vps.model.VpsTable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.EditDistance;
import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.util.List;

public class TableMatcher {

  private EditDistance<Double> cd = new CosineDistance();
  private EditDistance<Double> jwd = new JaroWinklerDistance();

  private double THRESHOLD_NOTFOUND = 4.5;
  private double THRESHOLD_CONFIRM = 0.3;

  //--------------------------------------

  public VpsTable findClosest(String fileName, String rom, String tableName, String manuf, int year, List<VpsTable> tables) {

    final double[] minDistance = new double[] {10000};
    final VpsTable[] found = new VpsTable[1];

    tables.stream()
        .forEach(table -> {

          // for debugging purpose and check the matching of a particular table, change id here and add breakpoint
          if ("0shM2rcl".equals(table.getId()) || "GNXfb2HW".equals(table.getId())) {
            boolean stop = true;
          }

          double dist = getTableDistance(table, fileName, tableName, manuf, year, rom);
          if (dist < minDistance[0]) {
            found[0] = table;
            minDistance[0] = dist;
          }
        });

    // return the closest table if the distance is not too high
    return minDistance[0] < THRESHOLD_NOTFOUND ? found[0] : null;
  }

  /**
   * Take a table display name and a rom and compare it to the given VpsTable
   */
  public boolean isClose(String fileName, String rom, String tableName, String manuf, int year, VpsTable table) {
    double dist = getTableDistance(table, fileName, tableName, manuf, year, rom);
    return dist <= THRESHOLD_CONFIRM;
  }

  private double getTableDistance(VpsTable table, String _fileName, String _tableName, String _manuf, int _year, String rom) {
    String tableName = VpsAutomatcher.cleanTable(table.getName().toLowerCase());
    String manuf = table.getManufacturer();
    String[] altmanufs = getAlternateManuf(manuf);
    int year = table.getYear();

    double minDistance = 10000;
    if (checkRom(rom, table)) {
      // return the minimal distance considering alternative manufacturer
      for (String altmanuf : altmanufs) {
        double dist = _tableName != null ? tableDistance(_tableName, _manuf, _year, tableName, altmanuf.toLowerCase(), year)
            : tableDistance(_fileName, tableName, altmanuf.toLowerCase(), year);
        if (dist < minDistance) {
          minDistance = dist;
        }
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
    double dTable = distance(tableName, _tableName) * 1;
    double dManuf = _manuf != null ? distance(_manuf, manuf) : 5;
    double dYear = _year > 1900 ? Math.min(Math.abs(_year - year), 5): 5;

    double dist = (1 + dTable)
        * (1 + dManuf / 35.0)
        * (1 + dYear / 20.0) - 1;
    return dist;
  }

  public double distance(String str1, String str2) {
    if (StringUtils.isEmpty(str1) && StringUtils.isEmpty(str2)) {
      return 0;
    }
    else if (StringUtils.equals(str1, str2)) {
      return 0;
    }
    else if (StringUtils.isEmpty(str1) || StringUtils.isEmpty(str2)) {
      return 5;
    }
    //else if (StringUtils.containsIgnoreCase(str1, str2)) {
    //  return Math.min((0.0 + str1.length() - str2.length()) / (0.0 + str2.length()), 1.0);
    //}
    //else if (StringUtils.containsIgnoreCase(str2, str1)) {
    //  return Math.min((str2.length()-str1.length())/(0.0 + str1.length()), 1.0);
    //}
    // else

    double ratio = cd.apply(str1, str2);

    double jw = jwd.apply(str1, str2);

    if (true) return ratio * jw; 

    // worst ratio, try without spaces
    if (ratio == 1.0) {
      ratio = cd.apply(
        str1.replace(" ", "").toLowerCase(), 
        str2.toLowerCase());
    }

    return ratio;
  }

  // ------------------------------------------------------

  /**
   * Take a rom and compare it with the one of the table
   * If rom is empty, verify the table does not use a rom
   * @return true if this is same rom
   */
  public boolean checkRom(String rom, VpsTable table) {

    //OLE 2025-05-05:
    // Disconnected the rom filtering as it is too error prone
    // VPS in many cases does not contain the roms or is missing a variant

    /*List<VpsAuthoredUrls> romFiles = table.getRomFiles();
    if (StringUtils.isNotEmpty(rom) && (romFiles != null && romFiles.size() > 0)) {
      // rom should be within the romFiles of the table if present
      boolean found = false;
      for (VpsAuthoredUrls romFile : romFiles) {
        found |= romFile.getVersion() != null
            // do not check equals, cf afm_113 && afm_113b
            && (StringUtils.startsWithIgnoreCase(romFile.getVersion(), rom)
            || StringUtils.startsWithIgnoreCase(rom, romFile.getVersion()));
      }
      return found;
    }*/
    //else
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
}
