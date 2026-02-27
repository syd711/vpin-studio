package de.mephisto.vpin.server.nvrams.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class NVRamToolHexDump {

  private static final int HEX_DUMP_BYTES_PER_LINE = 16;

  public void hexDump(NVRamMap mapJson, SparseMemory memory, Locale locale) throws IOException {
    Appendable bld = new StringBuilder(3000);
    hexDumpMemory(mapJson, memory, bld, locale, mapJson.getMemoryArea(null, "nvram"));
    hexDumpPinmameData(memory, bld, locale);
  }

  private void hexDumpMemory(NVRamMap mapJson, SparseMemory memory, Appendable bld, Locale locale, NVRamRegion memoryArea) throws IOException {
    int startAddr = BcdUtils.toInt(memoryArea.getAddress());
    byte[] data = memory.findRegion(startAddr).data;
    int size = BcdUtils.toInt(memoryArea.getSize());
    Nibble nibble = memoryArea.getNibble();

    // Build offset -> mapping dictionary
    Map<Integer, Object> entryMap = new LinkedHashMap<>();
    /*TBD 
    for (NVRamMapping m : mapJson.getMappings()) {
      if ("dip_switches".equals(m.getSection()) || "dipsw".equals(m.getEncoding())) continue;
      List<Integer> offs = m.offsets();
      entryMap.put(offs.get(0), m);
    }
    */
    for (ChecksumMapping checksum : mapJson.getChecksumEntries()) {
      entryMap.put(checksum.offsets().get(0), checksum);
    }

    int offset = 0;
    while (offset < size) {
      Object mappingObj = entryMap.get(startAddr + offset);
      String text = null;
      int count;
      if (mappingObj instanceof NVRamMapping) {
        NVRamMapping rm = (NVRamMapping) mappingObj;
        count = rm.offsets().size();

        String key = null; //TBD
        String lbl = rm.formatLabel(key, false);
        String value = StringUtils.defaultString(rm.formatEntry(mapJson, memory, locale), rm.getDefaultVal());
        text = (lbl != null ? lbl + ": " : "") + value;
      } 
      else if (mappingObj instanceof ChecksumMapping) {
        ChecksumMapping cm = (ChecksumMapping) mappingObj;
        count = cm.offsets().size();
        String[] lv = cm.formatMapping(memory, locale);
        text = lv[0] + ": " + lv[1];
      } 
      else {
        count = 1;
        while (count < HEX_DUMP_BYTES_PER_LINE && !entryMap.containsKey(startAddr + offset + count)) {
          count++;
        }
      }
      if (offset + count > size) count = size - offset;

      byte[] lineData = Arrays.copyOfRange(data, offset, offset + count);
      printLine(bld, "%04X: %s%n", locale, startAddr + offset, hexLine(lineData, nibble, text, locale));
      offset += count;
    }
  }

  private void hexDumpPinmameData(SparseMemory memory, Appendable bld, Locale locale) throws IOException {
    byte[] pinmameData = memory.getPinmameData();
    if (pinmameData == null) return;
    printLine(bld, "\nPinMAME data in .nv file:");
    int offset = 0;
    int length = pinmameData.length - 6;
    while (offset < length) {
      int count = 1;
      while (count < HEX_DUMP_BYTES_PER_LINE && offset + count < length) count++;
      byte[] line = Arrays.copyOfRange(pinmameData, offset, offset + count);
      
      System.out.printf("%04X: %s%n", offset, hexLine(line, Nibble.BOTH, null, locale));
      offset += count;
    }
    byte[] dipLine = Arrays.copyOfRange(pinmameData, offset, offset + 6);
    printLine(bld, "%04X: %s%n", locale, offset, hexLine(dipLine, Nibble.BOTH, "DIP Switches", locale));
  }

  public String hexLine(byte[] data, Nibble nibble, String text, Locale locale) {
    List<String> b = new ArrayList<>();
    List<Character> ch = new ArrayList<>();

    for (byte value : data) {
      int v = value & 0xFF;
      if (nibble == Nibble.LOW) {
        b.add(String.format(locale, " %1X", v & 0x0F));
      } else if (nibble == Nibble.HIGH) {
        b.add(String.format(locale, "%1X ", v >> 4));
      } else {
        b.add(String.format(locale, "%02X", v));
      }

      if (nibble == Nibble.BOTH) {
        ch.add((v >= 32 && v < 127) ? (char) v : '.');
      } else {
        ch.add(' ');
      }

      if (b.size() == HEX_DUMP_BYTES_PER_LINE) break;
    }

    while (b.size() < HEX_DUMP_BYTES_PER_LINE) {
      b.add("  ");
      ch.add(' ');
    }

    StringBuilder sb = new StringBuilder();
    for (Character c : ch) sb.append(c);
    String textStr = (text != null) ? text : sb.toString();
    return String.join(" ", b) + " | " + textStr;
  }

  private void printLine(Appendable bld, String line) throws IOException {
    bld.append(line).append("\n");
  }

  private void printLine(Appendable bld, String format, Locale locale, Object... args) throws IOException {
    try (Formatter formatter = new Formatter(bld)) {
      formatter.format(locale, format, args);
    }
    bld.append("\n");
  }
}
