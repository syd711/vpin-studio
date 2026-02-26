package de.mephisto.vpin.server.nvrams.parser;

import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object representing a single entry from a nvram mapping file.
 */
public class NVRamMapping extends NVRamObject {

  // A label describing this descriptor. 
  @JsonProperty("label")
  private String label;

  // An optional, abbreviated label for use when space is limited (like in a game launcher on a DMD). 
  @JsonProperty("short_label")
  private String shortLabel;

  // Address of the first byte/nibble to interpret
  @JsonProperty("start")
  private void unpackStart(Object start) {
    this.start = start != null ? BcdUtils.toInt(start) : 0;
  }
  private Integer start = 0;

  // Address of the last byte/nibble to interpret
  @JsonProperty("end")
  private void unpackEnd(Object end) {
    this.end = end != null ? BcdUtils.toInt(end) : null;
  }
  private Integer end;

  //  Number of bytes/nibbles to interpret, must be at least 1 (default)
  @JsonProperty("length")
  private void unpackLength(Object length) {
    this.length = length != null ? BcdUtils.toInt(length) : null;
  }
  private Integer length = 1;

  // Alternative to using start/end or start/length when addresses aren't contiguous.  List of addresses to use.  
  // Either `start` or `offsets` are required
  @JsonProperty("offsets")
  private List<Object> offsets;

  // A mask to apply to each byte before processing.  
  // For example, a mask of "0x5F" converts lowercase initials to uppercase and a mask of "0x0F" clears the upper four bits.
  @JsonProperty("mask")
  private void unpackMask(Object mask) {
    this.mask = mask != null ? BcdUtils.toInt(mask) : null;
  }
  private Integer mask;

  @JsonProperty("packed")
  private Boolean packed;

  @JsonProperty("nibble")
  private String nibble;

  @JsonProperty("endian")
  private String endian;

  // Only used for `bool` encoding.  Defaults to `false`.  If set to `true`, treat a value of zero as `true` and non-zero as `false`.
  @JsonProperty("invert")
  private Boolean invert;

  // Used to indicate that a field contains a time value as either a number of "seconds" or "minutes"
  private String units;
  // A string to append to the value (e.g., "M" if the value represents millions).
  private String suffix;

  private String encoding;
  // A numeric multiplier for a decoded `int`, `bcd`, or `bits`.
  private Object scale;
  // A numeric value to add to a decoded `int`, `bcd`, or `bits` value before displaying it.  Applied after `scale`.
  @JsonProperty("offset")
  private void unpackOffset(Object offset) {
    this.mask = offset != null ? BcdUtils.toInt(offset) : null;
  }
  private Integer offset = 0;

  // The valid range of values.
  @JsonProperty("min")
  private Integer min;
  @JsonProperty("max")
  private Integer max;
  // Enforce a subset of values between `min` and `max`
  @JsonProperty("multiple_of")
  private Integer multipleOf;

  // key used to treat a single descriptor as a list of groupings-sized ranges.
  @JsonProperty("groupings")
  private void unpackGroupings(Object groupings) {
    this.groupings = groupings != null ? BcdUtils.toInt(groupings) : null;
  }
  private Integer groupings;

  // represent the checksum's address
  @JsonProperty("checksum")
  private void unpackChecksum(Object checksum) {
    this.checksum = checksum != null ? BcdUtils.toInt(checksum) : null;
  }
  private Integer checksum;

  @JsonProperty("default")
  private String defaultVal;

  // The `null` property only applies to `ch` encoding.
  @JsonProperty("null")
  private String nullVal;

  //  list of strings or integers, used for the `enum` encoding (starting at index 0) and the `bits` encoding, as values for bit 0, 1, 2,..
  @JsonProperty("values")
  private Object values;

  //  A set of key/value pairs for a numeric field where some values have special meaning
  @JsonProperty("special_values")
  private Map<String, String> specialValues;

  //---------------------------------------- Getters only for JSONProperties

  public String getLabel() {
    return label;
  }

  public String getShortLabel() {
    return shortLabel;
  }

  public Integer getStart() {
    return start;
  }

  public Integer getEnd() {
    return end;
  }

  public Integer getLength() {
    return length;
  }

  public Integer getMask() {
    return mask;
  }

  public Integer getOffset() {
    return offset;
  }

  public Boolean getPacked() {
    return packed;
  }

  public String getEncoding() {
    return encoding;
  }

  public String getNibble() {
    return nibble;
  }

  public String getEndian() {
    return endian;
  }

  public Boolean getInvert() {
    return invert;
  }

  public String getUnits() {
    return units;
  }

  public String getSuffix() {
    return suffix;
  }

  public Object getScale() {
    return scale;
  }

  public List<Object> getOffsets() {
    return offsets;
  }

  public String getDefaultVal() {
    return defaultVal;
  }

  public String getNullVal() {
    return nullVal;
  }

  public Object getValues() {
    return values;
  }

  public Map<String, String> getSpecialValues() {
    return specialValues;
  }

  public Integer getGroupings() {
    return groupings;
  }

  public Integer getChecksum() {
    return checksum;
  }

  //------------------------------------------------

  public List<Integer> offsets() {

    if (offsets != null) {
      List<Integer> result = new ArrayList<>();
      for (Object offset : offsets) {
        result.add(BcdUtils.toInt(offset));
      }
      return result;
    }

    int calculatedEnd = start;
    if (length != null) {
      calculatedEnd = start + length - 1;
    } else if (end != null) {
      calculatedEnd = end;
      if (calculatedEnd < start) throw new AssertionError("end is less than start");
    }

    List<Integer> result = new ArrayList<>();
    for (int i = start; i <= calculatedEnd; i++) result.add(i);
    return result;
  }

  public byte[] getBytesUnmasked(SparseMemory memory) {
    List<Integer> offs = offsets();
    byte[] result = new byte[offs.size()];
    for (int i = 0; i < offs.size(); i++) {
      Integer b = memory.getByte(offs.get(i));
      if (b == null) return null;
      result[i] = b.byteValue();
    }
    return result;
  }

  public byte[] getBytes(NVRamMap mapJson, SparseMemory memory) {
    // special case for dip switches
    if ("dipsw".equals(encoding)) {
      int value = 0;
      byte[] dipswData = memory.getDipswData();
      if (dipswData == null) return null;
      for (int bit : offsets()) {
        value = (value << 1) + (DipSwitchUtils.dipswGet(dipswData, bit) ? 1 : 0);
      }
      return new byte[]{(byte) (value & 0xFF)};
    }

    byte[] ba = getBytesUnmasked(memory);
    if (ba == null) return null;

    // reverse little-endian sequences for integer encodings
    if (isLittleEndian(mapJson) && encoding != null &&
        (encoding.equals("bcd") || encoding.equals("int") ||
         encoding.equals("bits") || encoding.equals("bool"))) {
      reverse(ba);
    }

    Nibble nibble = nibble(mapJson);
    if (nibble != Nibble.BOTH) {
      // combine nibbles
      byte[] newBa = combineNibbles(ba, nibble);
      ba = newBa;
    }

    if (mask != null) {
      for (int i = 0; i < ba.length; i++) {
        ba[i] = (byte) ((ba[i] & 0xFF) & mask);
      }
    }

    return ba;
  }

  private byte[] combineNibbles(byte[] ba, Nibble nibble) {
    List<Byte> newBa = new ArrayList<>();
    int value = 0;
    List<Byte> baList = new ArrayList<>();
    for (byte b : ba) baList.add(b);

    int idx = 0;
    while (idx < baList.size()) {
      int b = baList.get(idx) & 0xFF;
      if (nibble == Nibble.LOW) {
        b = b & 0x0F;
      } else {
        b = b >> 4;
      }
      value = (value << 4) + b;
      int remaining = baList.size() - idx - 1;
      if (remaining % 2 == 0) {
        newBa.add((byte) value);
        value = 0;
      }
      idx++;
    }

    byte[] result = new byte[newBa.size()];
    for (int i = 0; i < newBa.size(); i++) result[i] = newBa.get(i);
    return result;
  }

  public Nibble nibble(NVRamMap mapJson) {
    if (packed != null && !packed) {
      return Nibble.LOW;
    }

    if (nibble != null) {
      return Nibble.fromString(nibble);
    }

    // use memory region's nibble setting
    int address = offsets().get(0);
    NVRamPlatform platform = mapJson.getRamPlatform();
    List<NVRamRegion> layout = platform.getMemoryLayout();
    for (NVRamRegion region : layout) {
      int regionStart = BcdUtils.toInt(region.getAddress());
      int regionEnd = regionStart + BcdUtils.toInt(region.getSize()) - 1;
      if (regionStart <= address && address <= regionEnd) {
        return region.getNibble();
      }
    }
    return Nibble.BOTH;
  }

  public boolean isLittleEndian(NVRamMap mapJson) {
    Boolean bigEndian = mapJson.isBigEndian();
    String defaultEndian = (bigEndian != null && bigEndian) ? "big" : "little";
    return "little".equals(StringUtils.defaultString(endian, defaultEndian));
  }

  public Object getValue(NVRamMap mapJson, SparseMemory memory) {
    if (encoding == null) return null;
    byte[] ba = getBytes(mapJson, memory);
    if (ba == null) return null;

    Long value = null;

    if ("bcd".equals(encoding)) {
      value = 0L;
      for (byte b : ba) {
        value = value * 100 + BcdUtils.bcdByteToInt(b & 0xFF);
      }
    } else if ("int".equals(encoding) || "bits".equals(encoding) ||
           "bool".equals(encoding) || "dipsw".equals(encoding) || "enum".equals(encoding)) {
      value = 0L;
      for (byte b : ba) {
        value = value * 256 + (b & 0xFF);
      }
    }

    if (value != null) {
      Object scaleObj = scale != null ? scale : 1;
      if (scaleObj instanceof Double) {
        double result = value * (Double) scaleObj;
        value = (long) result;
      } else {
        value *= BcdUtils.toInt(scaleObj);
      }
      value += offset;

      if ("bool".equals(encoding)) {
        boolean boolVal = value != 0;
        if (invert != null && invert) boolVal = !boolVal;
        return boolVal ? 1L : 0L;
      }
    }

    return value;
  }

  public void setValue(NVRamMap mapJson, SparseMemory memory, Object value) {
    if ("dipsw".equals(encoding)) {
      int intVal = ((Number) value).intValue();
      byte[] dipswData = memory.getDipswData();
      if (dipswData != null) {
        List<Integer> offs = offsets();
        for (int i = offs.size() - 1; i >= 0; i--) {
          DipSwitchUtils.dipswSet(dipswData, offs.get(i), (intVal & 1) != 0);
          intVal >>= 1;
        }
      }
      return;
    }

    byte[] oldBytes = getBytes(mapJson, memory);
    if (oldBytes == null) return;

    List<Byte> newBytes = new ArrayList<>();

    if ("ch".equals(encoding)) {
      String strVal = (String) value;
      for (char c : strVal.toCharArray()) newBytes.add((byte) c);
    } else if ("wpc_rtc".equals(encoding)) {
      LocalDateTime dt = (LocalDateTime) value;
      int year = dt.getYear();
      int dow = dt.getDayOfWeek().getValue() % 7 + 1; // 1=Sunday, 7=Saturday
      newBytes.add((byte) (year / 256));
      newBytes.add((byte) (year % 256));
      newBytes.add((byte) dt.getMonthValue());
      newBytes.add((byte) dt.getDayOfMonth());
      newBytes.add((byte) dow);
      newBytes.add((byte) dt.getHour());
      newBytes.add((byte) dt.getMinute());
    } else if ("bcd".equals(encoding) || "int".equals(encoding) ||
           "bool".equals(encoding) || "enum".equals(encoding)) {
      long intVal = ((Number) value).longValue();
      for (int i = 0; i < oldBytes.length; i++) {
        if ("bcd".equals(encoding)) {
          newBytes.add((byte) BcdUtils.intByteToBcd((int)(intVal % 100)));
          intVal /= 100;
        } else {
          newBytes.add((byte) (intVal % 256));
          intVal /= 256;
        }
      }

      Nibble nibble = nibble(mapJson);
      if (nibble != Nibble.BOTH) {
        List<Byte> nibbles = new ArrayList<>();
        for (byte b : newBytes) {
          if (nibble == Nibble.LOW) {
            nibbles.add((byte) (b & 0x0F));
            nibbles.add((byte) (b >> 4));
          } else {
            nibbles.add((byte) ((b << 4) & 0xF0));
            nibbles.add((byte) (b & 0xF0));
          }
        }
        if (nibbles.size() > length) {
          newBytes = nibbles.subList(0, nibbles.size() - 1);
        } else {
          newBytes = nibbles;
        }
      }

      if (!isLittleEndian(mapJson)) {
        Collections.reverse(newBytes);
      }
    } else {
      throw new IllegalArgumentException("Unsupported encoding: " + encoding);
    }

    byte[] resultBytes = new byte[newBytes.size()];
    for (int i = 0; i < newBytes.size(); i++) resultBytes[i] = newBytes.get(i);
    memory.updateMemory(start, resultBytes);

    //if (checksumEntry != null) {
    //  checksumEntry.update(memory);
    //}
  }

  public String formatValue(Object value, Locale locale) {
    if (value == null) return null;

    long lv = ((Number) value).longValue();

    if (specialValues != null && specialValues.containsKey(String.valueOf(lv))) {
      return specialValues.get(String.valueOf(lv));
    }

    if ("seconds".equals(units)) {
      long s = lv % 60; long m = (lv / 60) % 60; long h = lv / 3600;
      return String.format("%d:%02d:%02d", h, m, s);
    } else if ("minutes".equals(units)) {
      long m = lv % 60; long h = lv / 60;
      return String.format("%d:%02d:00", h, m);
    }

    return BcdUtils.formatNumber(lv, locale) + StringUtils.defaultString(suffix);
  }

  @SuppressWarnings("unchecked")
  public List<Object> entryValues(NVRamMap mapJson) {
    if (!"enum".equals(encoding) && !"dipsw".equals(encoding)) {
      throw new IllegalArgumentException("Entry doesn't use enum/dipsw encoding");
    }
    NVRamMetadata metadata = mapJson.getMetadata();
    if (values instanceof String) {
      Map<String, List<Object>> sharedValues = metadata.getValues();
      if (sharedValues != null) {
        return sharedValues.getOrDefault((String) values, new ArrayList<>());
      }
      return new ArrayList<>();
    }
    return (List<Object>) values;
  }


  //-------------------------------------------------

  @SuppressWarnings("unchecked")
  public String formatEntry(NVRamMap mapJson, SparseMemory memory, Locale locale) {
    if (encoding == null) return null;

    byte[] ba = getBytes(mapJson, memory);
    if (ba == null) return null;

    Object value = getValue(mapJson, memory);

    if ("bcd".equals(encoding) || "int".equals(encoding)) {
      return formatValue(value, locale);
    } 
    else if ("bool".equals(encoding)) {
      return ((Number) value).longValue() != 0 ? "true" : "false";
    } 
    else if ("bits".equals(encoding)) {
      List<Object> lvalues = (List<Object>) values;
      if (lvalues == null) return "0";
      long intValue = ((Number) value).longValue();
      if (lvalues.get(0) instanceof Number) {
        int mask = 1;
        long bitsValue = 0;
        for (Object b : lvalues) {
          if ((intValue & mask) != 0) bitsValue += ((Number) b).longValue();
          mask <<= 1;
        }
        return formatValue(bitsValue, locale);
      } 
      else if (lvalues.get(0) instanceof String) {
        int mask = 1;
        List<String> setValues = new ArrayList<>();
        for (Object b : lvalues) {
          if ((intValue & mask) != 0) setValues.add((String) b);
          mask <<= 1;
        }
        return String.join(", ", setValues);
      }
    } 
    else if ("enum".equals(encoding) || "dipsw".equals(encoding)) {
      List<Object> values = entryValues(mapJson);
      int idx = ((Number) value).intValue();
      if (idx >= values.size()) return "?" + idx;
      Object v = values.get(idx);
      return Objects.toString(v);
    } 
    else if ("ch".equals(encoding)) {
      NVRamMetadata metadata = mapJson.getMetadata();
      StringBuilder result = new StringBuilder();
      String charMap = metadata.getCharMap();
      for (byte b : ba) {
        int bInt = b & 0xFF;
        if (charMap != null) {
          result.append(bInt < charMap.length() ? charMap.charAt(bInt) : "?");
        } else if (bInt == 0 && !"ignore".equals(StringUtils.defaultString(nullVal, "ignore"))) {
          break;
        } else {
          result.append((char) bInt);
        }
      }
      if (result.toString().equals(defaultVal)) return null;
      return result.toString();
    } 
    else if ("raw".equals(encoding)) {
      StringBuilder sb = new StringBuilder();
      for (byte b : ba) {
        if (sb.length() > 0) sb.append(" ");
        sb.append(String.format("%02x", b & 0xFF));
      }
      return sb.toString();
    } 
    else if ("wpc_rtc".equals(encoding)) {
      return String.format(locale, "%04d-%02d-%02d %02d:%02d",
          (ba[0] & 0xFF) * 256 + (ba[1] & 0xFF),
          ba[2] & 0xFF, ba[3] & 0xFF, ba[5] & 0xFF, ba[6] & 0xFF);
    }
    return "[?" + encoding + "?]";
  }

  public String formatLabel(String key, boolean useShortLabel) {
    String lbl = StringUtils.defaultString(label, "?");
    if (lbl.startsWith("_")) lbl = null;
    if (useShortLabel) {
      if (shortLabel != null) lbl = shortLabel;
    }
    if (key != null && lbl != null) lbl = key + " " + lbl;
    return lbl;
  }

  private void reverse(byte[] arr) {
    for (int i = 0, j = arr.length - 1; i < j; i++, j--) {
      byte tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
    }
  }
}
