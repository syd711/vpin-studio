package de.mephisto.vpin.connectors.vps.matcher;

import de.mephisto.vpin.connectors.vps.model.VpsTable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.EditDistance;
import org.apache.commons.text.similarity.JaccardDistance;

import java.util.ArrayList;
import java.util.List;

public class TableMatcher {
  /** for debugging */
  private VpsDebug debug;

  private EditDistance<Double> cd = new CosineDistance();
  //private EditDistance<Double> jwd = new JaroWinklerDistance();
  private EditDistance<Double> jd = new JaccardDistance();

  private double THRESHOLD_NOTFOUND = 2;
  private double THRESHOLD_CONFIRM = 0.3;

  //--------------------------------------

  public TableMatcher(VpsDebug debug) {
    this.debug = debug;
  }

  public VpsTable findClosest(String fileName, String rom, String tableName, String manuf, int year, List<VpsTable> tables) {
    List<VpsTable> matches = findAllClosest(fileName, rom, tableName, manuf, year, tables);
    return matches.size() > 0 ? matches.get(0) : null;
  }

  public List<VpsTable> findAllClosest(String fileName, String rom, String tableName, String manuf, int year, List<VpsTable> tables) {
    // clean table name
    String cleanTableName = StringUtils.isEmpty(tableName) ? null : cleanTable(tableName);

    final List<Double> distances = new ArrayList<>();
    final List<VpsTable> found = new ArrayList<>();
    final double[] minDist = new double[] { 10000 };

    tables.stream()
        .forEach(table -> { 

          // for debugging purpose and check the matching of a particular table, change id here and add breakpoint
          //if ("uP4_P2lE".equals(table.getId())) {
          //  boolean stop = true;
          //}

          double dist = getTableDistance(table, fileName, cleanTableName, manuf, year, rom, minDist[0]);
          // record new min
          if (dist < minDist[0]) {
            // The table(s) in the found list must be replaced 
            if (minDist[0] > THRESHOLD_NOTFOUND) {
              distances.clear();
              found.clear();
            }
            // now keep the new min
            minDist[0] = dist;
          }

          // NO table yet, keep it even if above threshold
          if (found.isEmpty()) {
            distances.add(dist);
            found.add(table);
          }
          else if (dist < THRESHOLD_NOTFOUND) {
            int idx = 0;
            while (idx < distances.size() && distances.get(idx) < dist) {
              idx++;
            }
            distances.add(idx, dist);
            found.add(idx, table);
          }
        });

    // return the matching tables, sorted by distance if the distance is not too high
    return found;
  }


  /**
   * Take a table display name and a rom and compare it to the given VpsTable
   */
  public boolean isClose(String fileName, String rom, String tableName, String manuf, int year, VpsTable table) {
    // clean table name
    String cleanTableName = StringUtils.isEmpty(tableName) ? null : cleanTable(tableName);

    double dist = getTableDistance(table, fileName, cleanTableName, manuf, year, rom, 10000);
    return dist <= THRESHOLD_CONFIRM;
  }

  private double getTableDistance(VpsTable table, String _fileName, String _tableName, String _manuf, int _year, String rom, double minDist) {
    String tableName = cleanTable(table.getName().toLowerCase());
    String manuf = table.getManufacturer();
    int year = table.getYear();

    if (checkRom(rom, table)) {
      // return the minimal distance considering alternative manufacturer
      return _tableName != null ? tableDistance(_tableName, _manuf, _year, tableName, manuf, year, debug, table.getId(), minDist)
          : tableDistance(_fileName, tableName, manuf, year, debug, table.getId(), minDist);
    }
    return 10000;
  }

  private double tableDistance(String _displayName, 
                              String tableName, String manuf, int year,
                              VpsDebug debug, String tableId, double minDist) {
    String clean = _displayName;

    double dManuf = 0.2;
    String[] altmanufs = getAlternateManuf(manuf);
    for (String altmanuf : altmanufs) {
      if (StringUtils.containsIgnoreCase(clean, altmanuf)) {
        clean = StringUtils.removeIgnoreCase(clean, altmanuf);
        dManuf = 0;
        if (!StringUtils.equalsIgnoreCase(manuf, altmanuf)) {
          manuf += " (" + altmanuf + ")";
        }
      }
    }

    double dYear = 0.2;
    if (clean.contains(Integer.toString(year))) {
      clean = StringUtils.removeIgnoreCase(clean, Integer.toString(year));
      dYear = 0;
    }

    clean = cleanTable(clean);
    double dTable = distance(clean, tableName);

    double dist = calculateDistance(dTable, dManuf, dYear);
   
    if ((dist < minDist || dist < THRESHOLD_NOTFOUND) && debug != null) {
      appendDebugScores(tableId, dist, tableName, dTable, manuf, dManuf, year, dYear);
    }
    
    return dist;
  }

  private double tableDistance(String _tableName, String _manuf, int _year,
                               String tableName, String manuf, int year, 
                               VpsDebug debug, String tableId, double minDist) {
    double dTable = distance(tableName, _tableName) * 1;

    double dManuf = 0.2;
    if (_manuf != null) {
      String[] altmanufs = getAlternateManuf(manuf);
      String altmanuf = null;
      dManuf = 10000;
      for (String m : altmanufs) {
        double d = distance(_manuf, m);
        if (d < dManuf) {
          dManuf = d;
          altmanuf = m;
        }
      }
      if (!StringUtils.equalsIgnoreCase(manuf, altmanuf)) {
        manuf += " (" + altmanuf + ")";
      }
    }

    double dYear = _year > 1900 ? Math.min(Math.abs(_year - year), 5.0) / 5.0: 0.2;

    double dist = calculateDistance(dTable, dManuf, dYear);

    if ((dist < minDist || dist < THRESHOLD_NOTFOUND) && debug != null) {
      appendDebugScores(tableId, dist, tableName, dTable, manuf, dManuf, year, dYear);
    }
    return dist;
  }

  private double calculateDistance(double dTable, double dManuf, double dYear) {
    return (1 + dTable * 9) * (1 + dManuf * 3) * (1 + dYear * 1.5) - 1;
  }

  private void appendDebugScores(String tableId, double dist, String tableName, double dTable, String manuf, double dManuf, int year, double dYear) {
    debug.startDebug();
    debug.appendDebug(tableId, 12);
    debug.appendDebug(dist, 8);
    debug.appendDebug(tableName, 40);
    debug.appendDebug(dTable, 6);
    debug.appendDebug(manuf, 20);
    debug.appendDebug(dManuf, 6);
    debug.appendDebug(Integer.toString(year), 5);
    debug.appendDebug(dYear, 6);
    debug.endDebug();
  }

  public double distance(String str1, String str2) {
    if (StringUtils.isEmpty(str1) && StringUtils.isEmpty(str2)) {
      return 0;
    }
    else if (StringUtils.equals(str1, str2)) {
      return 0;
    }
    else if (StringUtils.isEmpty(str1) || StringUtils.isEmpty(str2)) {
      return 1;
    }

    double ratioCD = cd.apply(str1.toLowerCase(), str2.toLowerCase());
    //double ratioJWD = jwd.apply(str1.toLowerCase(), str2.toLowerCase());
    double ratioJD = jd.apply(str1.toLowerCase(), str2.toLowerCase());

    return ratioCD * ratioJD; 
  }

  // ------------------------------------------------------

  public String cleanTable(String tableName) {
    tableName = VpsAutomatcher.cleanChars(tableName);
    tableName = tableName.replaceAll("the ", "");
    tableName = tableName.replaceAll(", the", "");

    // Remove obvious file hints not related to the actual name.
    //tableName = tableName.replace("VR Room", "");
    //tableName = tableName.replace("VRRoom", "");

    return tableName;
  }

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
      return new String[] {"Bally", "Midway"};
    }
    else if (manuf.equalsIgnoreCase("Original") || manuf.equalsIgnoreCase("TBA")) {
      return new String[] {"Original", "TBA"};
    }
    else if (manuf.equalsIgnoreCase("Mylstar") || manuf.equalsIgnoreCase("Gottlieb")) {
      return new String[] {"Gottlieb", "Mylstar"};
    }
    else if (manuf.toLowerCase().startsWith("taito")) {
      return new String[] {"Taito do Brasil", "Taito"};
    }
    else if (manuf.toLowerCase().startsWith("spooky")) {
      return new String[] {"Spooky Pinball", "Spooky"};
    }

    return new String[]{manuf};
  }

}
