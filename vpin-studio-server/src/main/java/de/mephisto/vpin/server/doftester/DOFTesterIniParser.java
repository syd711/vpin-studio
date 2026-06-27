package de.mephisto.vpin.server.doftester;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class DOFTesterIniParser {
  private static final Logger LOG = LoggerFactory.getLogger(DOFTesterIniParser.class);

  private final List<String> toyColumns = new ArrayList<>();
  private final Map<String, Map<String, List<DOFEventCode>>> romToyMap = new LinkedHashMap<>();

  public DOFTesterIniParser(File iniFile) throws IOException {
    parse(iniFile);
    LOG.info("Parsed DOF ini {}: {} ROMs, {} toy columns", iniFile.getName(), romToyMap.size(), toyColumns.size());
  }

  private void parse(File iniFile) throws IOException {
    boolean inConfigSection = false;
    try (BufferedReader reader = new BufferedReader(new FileReader(iniFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.equalsIgnoreCase("[Config DOF]")) {
          inConfigSection = true;
          continue;
        }
        if (inConfigSection && line.startsWith("[")) {
          break;
        }
        if (!inConfigSection) {
          continue;
        }
        if (line.startsWith("#") && toyColumns.isEmpty()) {
          parseToyColumns(line.substring(1).trim());
        }
        else if (!line.isEmpty() && !line.startsWith("#")) {
          parseRomRow(line);
        }
      }
    }
  }

  private void parseToyColumns(String headerLine) {
    for (String part : headerLine.split(",")) {
      toyColumns.add(part.trim().replaceAll("^\"|\"$", ""));
    }
  }

  private void parseRomRow(String line) {
    String[] parts = line.split(",", -1);
    if (parts.length == 0) {
      return;
    }
    String romName = parts[0].trim();
    if (StringUtils.isEmpty(romName)) {
      return;
    }

    Map<String, List<DOFEventCode>> toyEffects = new LinkedHashMap<>();
    for (int i = 1; i < parts.length && i < toyColumns.size(); i++) {
      String toyName = toyColumns.get(i);
      if (StringUtils.isEmpty(toyName)) {
        continue;
      }
      String cellValue = parts[i].trim();
      if (StringUtils.isEmpty(cellValue) || cellValue.equals("0")) {
        continue;
      }
      List<DOFEventCode> codes = parseEventCodes(cellValue);
      if (!codes.isEmpty()) {
        toyEffects.put(toyName, codes);
      }
    }
    romToyMap.put(romName.toLowerCase(), toyEffects);
  }

  // Parses "S24 I60/E102/L33 Blink fu500" → [S:24, E:102, L:33]
  private List<DOFEventCode> parseEventCodes(String cellValue) {
    List<DOFEventCode> result = new ArrayList<>();
    for (String segment : cellValue.split("/")) {
      String token = segment.trim().split("\\s+")[0];
      if (token.length() < 2) {
        continue;
      }
      char typeChar = token.charAt(0);
      if (!Character.isLetter(typeChar) || token.equals("ON") || token.equals("Blink")) {
        continue;
      }
      String numStr = token.substring(1);
      try {
        int number = Integer.parseInt(numStr);
        result.add(new DOFEventCode(String.valueOf(typeChar), number));
      }
      catch (NumberFormatException ignored) {
      }
    }
    return result;
  }

  public List<String> getToys(String romName) {
    Map<String, List<DOFEventCode>> effects = resolve(romName);
    if (effects == null) {
      return Collections.emptyList();
    }
    return new ArrayList<>(effects.keySet());
  }

  public List<DOFEventCode> getEventCodes(String romName, String toyName) {
    Map<String, List<DOFEventCode>> effects = resolve(romName);
    if (effects == null) {
      return Collections.emptyList();
    }
    return effects.getOrDefault(toyName, Collections.emptyList());
  }

  /**
   * Reverse lookup: given a ROM name and an event code, returns the toy column name
   * that triggers it, or null if not found.
   */
  public String getToyNameForCode(String romName, DOFEventCode code) {
    Map<String, List<DOFEventCode>> effects = resolve(romName);
    if (effects == null) {
      return null;
    }
    for (Map.Entry<String, List<DOFEventCode>> entry : effects.entrySet()) {
      for (DOFEventCode c : entry.getValue()) {
        if (c.getType().equalsIgnoreCase(code.getType()) && c.getNumber() == code.getNumber()) {
          return entry.getKey();
        }
      }
    }
    return null;
  }

  /**
   * Mirrors DOF's three-stage ROM lookup (LedControlConfigList::GetTableConfigDictionary):
   *   1. Exact match (case-insensitive)
   *   2. Version-suffix match: romName starts with "<entry>_"  (e.g. afm_113b matches afm)
   *   3. Bare-prefix match:    romName starts with "<entry>"   (e.g. afmtest matches afm)
   * Stages 2 and 3 iterate all config entries; stage 2 returns immediately on first hit,
   * stage 3 returns the first hit after the full scan.
   */
  private Map<String, List<DOFEventCode>> resolve(String romName) {
    String lower = romName.toLowerCase();

    // Stage 1: exact match
    Map<String, List<DOFEventCode>> exact = romToyMap.get(lower);
    if (exact != null) {
      return exact;
    }

    exact = romToyMap.get(lower.replaceAll("_", ""));
    if (exact != null) {
      return exact;
    }

    Map<String, List<DOFEventCode>> prefixMatch = null;
    for (Map.Entry<String, Map<String, List<DOFEventCode>>> entry : romToyMap.entrySet()) {
      String entryKey = entry.getKey(); // already stored lowercase

      // Stage 2: version-suffix match — e.g. "afm_113b" starts with "afm_"
      if (lower.startsWith(entryKey + "_")) {
        return entry.getValue();
      }

      // Stage 3: bare-prefix match — e.g. "afmtest" starts with "afm"
      if (prefixMatch == null && lower.startsWith(entryKey)) {
        prefixMatch = entry.getValue();
      }
    }

    return prefixMatch;
  }
}
