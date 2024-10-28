package de.mephisto.vpin.tools.wheels;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsAuthoredUrls;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

public class WheelIconConverter {


  private WheelIconConverter() throws IOException {
    VPS vps = new VPS();
    vps.reload();

    File folder = new File("C:\\workspace\\tarcisio-wheel-icons\\original\\");
    File[] files = folder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".png");
      }
    });

    for (File file : files) {
      String name = FilenameUtils.getBaseName(file.getName());
      if (name.contains("(")) {
        name = name.substring(0, name.lastIndexOf("(")).trim();
      }

      TableMatcher matcher = new TableMatcher();
      VpsTable closest = matcher.findClosest(file.getName(), null, false, null, null, 0, vps.getTables());
      if (closest == null) {
        String opName = name;
        closest = findByName(vps, opName);
        while (closest == null && opName.contains(" ")) {
          opName = opName.substring(0, opName.lastIndexOf(" "));
          closest = findByName(vps, opName);
        }
      }

      if (closest == null) {
        String opName = name;
        closest = findByName(vps, opName);
        while (closest == null && opName.contains(" ")) {
          opName = opName.substring(0, opName.lastIndexOf(" "));
          closest = findByName(vps, opName);
        }
      }

      if (closest != null) {
        File target = new File(file.getParentFile().getParentFile(), closest.getId() + ".png");
        if (target.exists()) {
          continue;
        }
//        FileUtils.copyFile(file, target);
      }
      else {
        System.out.println(file.getName());
      }
    }
  }

  private static VpsTable findByName(VPS vps, String name) {
    List<VpsTable> tables = vps.getTables();
    for (VpsTable table : tables) {
      if (table.getName().startsWith(name)) {
        return table;
      }
    }
    return null;
  }

  public static void main(String[] args) throws IOException {
    new WheelIconConverter();
  }

  public class TableMatcher {

    private JaroWinklerSimilarity jwd = new JaroWinklerSimilarity();

    private double THRESHOLD_NOTFOUND = 5.5;

    //--------------------------------------

    public VpsTable findClosest(String _fileName, String _rom, boolean checkall,
                                String _tableName, String _manuf, int _year, List<VpsTable> tables) {

      final double[] minDistance = new double[]{10000};
      final VpsTable[] found = new VpsTable[1];
      tables.stream()
          .filter(table -> checkRom(_rom, table, checkall))
          .forEach(table -> {
            double dist = getTableDistance(table, _fileName, _tableName, _manuf, _year);
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
    public boolean isClose(String _fileName, String _rom, boolean checkall,
                           String _tableName, String _manuf, int _year, VpsTable table) {

      // first compare rom
      if (!checkRom(_rom, table, checkall)) {
        return false;
      }

      double dist = getTableDistance(table, _fileName, _tableName, _manuf, _year);
      return dist < THRESHOLD_NOTFOUND;
    }

    private double getTableDistance(VpsTable table, String _fileName, String _tableName, String _manuf, int _year) {
      String tableName = cleanChars(table.getName());
      String manuf = table.getManufacturer();
      String[] altmanufs = getAlternateManuf(manuf);
      int year = table.getYear();

      double minDistance = 10000;
      // return the minimal distance considering alternative manufacturer
      for (String altmanuf : altmanufs) {
        double dist = _tableName != null ? tableDistance(_tableName, _manuf, _year, tableName, altmanuf, year)
            : tableDistance(_fileName, tableName, altmanuf, year);
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
          * (1 + dManuf / 35)
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
      else if (StringUtils.containsIgnoreCase(str1, str2)) {
        return Math.min((str1.length() - str2.length()) / (0.0 + str2.length()), 1.0);
      }
      else if (StringUtils.containsIgnoreCase(str2, str1)) {
        return Math.min((str2.length() - str1.length()) / (0.0 + str1.length()), 1.0);
      }
      // else

      //int ratio = FuzzySearch.weightedRatio(str1, str2);
      //double d = ratio > 0 ? 10 * (100.0 / ratio - 1) : 10000;

      double ratio = jwd.apply(str1.toLowerCase(), str2.toLowerCase());
      return ratio > 0 ? 10 * (1.0 / ratio - 1) : 10000;

      // Score 100 => distance = 0
      // Score 75 => distance = 3
    }

    // ------------------------------------------------------

    /**
     * Take a rom and compare it with the one of the table
     * If rom is empty, verify the table does not use a rom
     *
     * @return true if this is same rom
     */
    public boolean checkRom(String rom, VpsTable table, boolean checkall) {
      if (checkall) {
        return true;
      }
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
      // else when rom is expected but the table does not declare any rom
      else {
        return false;
      }
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
}
