package de.mephisto.vpin.server.nvrams.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Simplified mapping object used for checksum values.
 */
public class ChecksumMapping {

  private String label;
  private boolean bigEndian;
  private boolean checksum16;
  private String formatting;
  private Integer start;
  private Integer end;
  private int checksum;

  public ChecksumMapping(int start, int end, Integer checksumAddr, String label,
               boolean checksum16, boolean bigEndian) {
    this.label = label;
    this.bigEndian = bigEndian;
    this.checksum16 = checksum16;
    this.formatting = checksum16 ? "0x%04X" : "0x%02X";
    this.start = start;
    this.end = end;

    if (checksumAddr != null) {
      this.checksum = checksumAddr;
    } else {
      // checksum included in start-end range
      this.end = end - length();
      this.checksum = this.end + 1;
    }
  }

  public int length() {
    return 1 + (checksum16 ? 1 : 0);
  }

  public List<Integer> offsets() {
    List<Integer> offsets = new ArrayList<>();
    offsets.add(checksum);
    if (checksum16) {
      offsets.add(checksum + 1);
    }
    return offsets;
  }

  public List<Integer> coverage() {
    List<Integer> result = new ArrayList<>();
    for (int i = start; i <= end; i++) {
      result.add(i);
    }
    return result;
  }

  public int calculate(SparseMemory memory) {
    int sum = -1;
    for (int address : coverage()) {
      Integer b = memory.getByte(address);
      if (b != null) sum -= b;
    }
    if (checksum16) {
      return sum & 0xFFFF;
    }
    return sum & 0xFF;
  }

  public void update(SparseMemory memory) {
    int sum = calculate(memory);
    if (checksum16) {
      byte[] data;
      if (bigEndian) {
        data = new byte[]{(byte) ((sum >> 8) & 0xFF), (byte) (sum & 0xFF)};
      } else {
        data = new byte[]{(byte) (sum & 0xFF), (byte) ((sum >> 8) & 0xFF)};
      }
      memory.updateMemory(checksum, data);
    } else {
      memory.updateMemory(checksum, new byte[]{(byte) (sum & 0xFF)});
    }
  }

  public int getValue(SparseMemory memory) {
    Integer val = memory.getByte(checksum);
    if (val == null) return 0;
    if (checksum16) {
      Integer next = memory.getByte(checksum + 1);
      if (next == null) return val;
      if (bigEndian) {
        return (val << 8) + next;
      } else {
        return val + (next << 8);
      }
    }
    return val;
  }

  public boolean isValid(SparseMemory memory) {
    return getValue(memory) == calculate(memory);
  }

  public String[] formatMapping(SparseMemory memory, Locale locale) {
    int stored = getValue(memory);
    int calculated = calculate(memory);
    String lbl;
    if (checksum16) {
      lbl = String.format("checksum16[%X:%X]", start, end - 2);
    } else {
      lbl = String.format("checksum8[%X:%X]", start, end - 1);
    }

    String value = String.format(formatting, stored);
    if (stored != calculated) {
      value += " != " + String.format(formatting, calculated);
    }
    if (label != null && !label.isEmpty()) {
      value += " (" + label + ")";
    }
    return new String[]{lbl, value};
  }

  public int getStart() { return start; }
  public int getEnd() { return end; }
  public int getChecksum() { return checksum; }
  public String getFormatting() { return formatting; }
  public String getLabel() { return label; }
}
