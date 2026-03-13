package de.mephisto.vpin.server.nvrams.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UnknownFormatConversionException;

import org.apache.commons.lang3.StringUtils;

public class NVRamToolDump {


  public String dump(NVRamMap mapJson, SparseMemory memory, Locale locale, boolean verifyChecksums) throws IOException {
    Appendable bld = new StringBuilder(3000);
    printLine(bld, "Using map ../maps/" + mapJson.getMapPath() + " for " + mapJson.getNvramName());
    printLine(bld, "Dumping known entries for " + mapJson.getNvramName() + " [" + mapJson.getRomName() + "]...");

    // audits and adjustments
    dumpMapOfMappings(bld, "audits", mapJson.getAudits(), mapJson, memory, locale);
    dumpMapOfMappings(bld, "adjustments", mapJson.getAdjustments(), mapJson, memory, locale);

    // game_state
    if (mapJson.getGameState() != null) {
      dumpMappings(bld, "Game State", mapJson.getGameState().getMappings(), mapJson, memory, locale);
    }
    // dip_switches
    if (memory.getDipswData() != null) {
      dumpMappings(bld, "DIP Switches", mapJson.getDipSwitches(), mapJson, memory, locale); 
    }
    dumpScores(bld, "high_scores", mapJson.getHighScores(), mapJson, memory, locale);
    dumpScores(bld, "mode_champions", mapJson.getModeChampions(), mapJson, memory, locale);

    NVRamMapping lp = mapJson.getLastPlayed();
    if (lp != null) {
      String played = lp.formatEntry(mapJson, memory, locale);
      printLine(bld, "Last Played: " + played);
    }

    if (verifyChecksums) {
      for (ChecksumMapping checksum : mapJson.getChecksumEntries()) {
        String calc = String.format(locale, checksum.getFormatting(), checksum.calculate(memory));
        String stored = String.format(locale, checksum.getFormatting(), checksum.getValue(memory));
        if (!calc.equals(stored)) {
          printLine(bld, "checksum at 0x%X: %s != %s %s", locale,
              checksum.getStart(), calc, stored, checksum.getLabel());
        }
      }
    }
    return bld.toString();
  }

  //============================================================

  private void dumpMapOfMappings(Appendable bld, String section, Map<String, NVRamMappings> sectionMap,
        NVRamMap mapJson, SparseMemory memory, Locale locale) throws IOException {
    if (sectionMap != null) {

      List<String> groups = new ArrayList<>(sectionMap.keySet());
      Collections.sort(groups);
      for (String group : groups) {
        if (group.startsWith("_")) continue;

        printGroupName(bld, group);

        NVRamMappings mapMap = sectionMap.get(group);
        List<String> keys = mapMap.keySet();
        Collections.sort(keys);
        for (String entryKey : keys) {
          if (entryKey.startsWith("_")) continue;
          NVRamMapping entry = mapMap.get(entryKey);
          dumpMapping(bld, mapJson, memory, locale, entry, entryKey);
        }
      }
    }
  }

  private void dumpMappings(Appendable bld, String group, Map<String,NVRamMapping> sectionMap, 
      NVRamMap mapJson, SparseMemory memory, Locale locale) throws IOException {
    if (sectionMap != null) {

      printGroupName(bld, group);

      for (String entryKey : sectionMap.keySet()) {
        if (entryKey.startsWith("_")) continue;
        NVRamMapping entry = sectionMap.get(entryKey);
        dumpMapping(bld, mapJson, memory, locale, entry, null);
      }
    }
  }

  private void dumpMappings(Appendable bld, String group, List<NVRamMapping> mappings, 
      NVRamMap mapJson, SparseMemory memory, Locale locale) throws IOException {
    if (mappings != null) {

      printGroupName(bld, group);

      for (NVRamMapping mapEntry : mappings) {
        dumpMapping(bld, mapJson, memory, locale, mapEntry, null);
      }
    }
  }

  private void dumpScores(Appendable bld, String group, List<NVRamScore> scores, 
      NVRamMap mapJson, SparseMemory memory, Locale locale) throws IOException {
    if (scores != null) {

      printGroupName(bld, group);

      for (NVRamScore score : scores) {
        String lbl = score.formatLabel(false);
        String value = score.formatHighScore(mapJson, memory, locale);
        printLine(bld, lbl + ": " + value);
      }
    }
  }


  private void dumpMapping(Appendable bld, NVRamMap mapJson, SparseMemory memory, Locale locale,
      NVRamMapping entry, String entryKey) throws IOException {
    String value = entry.formatEntry(mapJson, memory, locale);
    String lbl = "";
    if (entryKey != null) {
      if (value == null) value = StringUtils.defaultString(entry.getDefaultVal());
      lbl = entry.formatLabel(entryKey, false);
    } 
    else {
      lbl = entry.formatLabel(null, false);
    }
    if (value != null) {
      printLine(bld, lbl + ": " + value);
    }
  }

  private void printGroupName(Appendable bld, String group) throws IOException {
    printLine(bld, "");
    printLine(bld, group);
    printLine(bld, "-".repeat(group.length()));
  }

  private void printLine(Appendable bld, String line) throws IOException {
    bld.append(line).append("\n");
  }

  private void printLine(Appendable bld, String format, Locale locale, Object... args) throws IOException {
    try (Formatter formatter = new Formatter(bld)) {
      formatter.format(locale, format, args);
    }
    catch (UnknownFormatConversionException ufce) {
      System.out.println("error in format " + ufce.getMessage());
    }
    bld.append("\n");
  }
}
