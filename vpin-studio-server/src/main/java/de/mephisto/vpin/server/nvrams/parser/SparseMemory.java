package de.mephisto.vpin.server.nvrams.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representing memory contents for a portion of the full address space.
 */
public class SparseMemory {

  /** Portion of PinMAME .nv file beyond in-game memory area, ends with 6 bytes of DIP switch data. */
  private byte[] pinmameData = null;

  /** 6-byte, little-endian byte array with DIP switch settings */
  private byte[] dipswData = null;

  /** Memory areas loaded from PinMAME .nv file or otherwise. */
  private final List<MemoryRegion> memory = new ArrayList<>();

  public static class MemoryRegion {
    public int baseAddress;
    public byte[] data;

    public MemoryRegion(int baseAddress, byte[] data) {
      this.baseAddress = baseAddress;
      this.data = data;
    }
  }

  public MemoryRegion findRegion(int address) {
    for (MemoryRegion region : memory) {
      int regionBase = region.baseAddress;
      int regionSize = region.data.length;
      if (regionBase <= address && address < regionBase + regionSize) {
        return region;
      }
    }
    return null;
  }

  public void updateMemory(int address, byte[] data) {
    MemoryRegion region = findRegion(address);
    if (region != null) {
      int regionBase = region.baseAddress;
      int regionSize = region.data.length;
      int offset = address - regionBase;
      int dataSize = data.length;
      if (address + dataSize > regionBase + regionSize) {
        throw new IllegalArgumentException(
          String.format("Update of %d bytes to 0x%X overflows region", dataSize, address));
      }
      System.arraycopy(data, 0, region.data, offset, dataSize);
    } else {
      byte[] copy = new byte[data.length];
      System.arraycopy(data, 0, copy, 0, data.length);
      memory.add(new MemoryRegion(address, copy));
    }
  }

  /**
   * Return the byte at a given memory location, or null if it isn't represented.
   */
  public Integer getByte(int address) {
    for (MemoryRegion region : memory) {
      int regionBase = region.baseAddress;
      int regionSize = region.data.length;
      if (regionBase <= address && address < regionBase + regionSize) {
        return region.data[address - regionBase] & 0xFF;
      }
    }
    return null;
  }

  public List<Integer> getRange(int start, int length) {
    List<Integer> data = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      data.add(getByte(start + i));
    }
    return data;
  }

  public void setPinmameData(byte[] data) {
    this.pinmameData = data;
  }

  public byte[] getPinmameData() {
    return pinmameData;
  }

  /**
   * Save DIP switch values. Automatically extends to 6 bytes if shorter.
   */
  public void setDipswData(byte[] data) {
    int padding = 6 - data.length;
    if (padding > 0) {
      byte[] extended = new byte[6];
      System.arraycopy(data, 0, extended, 0, data.length);
      this.dipswData = extended;
    } else {
      this.dipswData = data;
    }
  }

  /**
   * Returns DIP switch values (6 bytes) or null if not available.
   */
  public byte[] getDipswData() {
    if (dipswData != null) {
      return dipswData;
    }
    if (pinmameData != null) {
      byte[] result = new byte[6];
      System.arraycopy(pinmameData, pinmameData.length - 6, result, 0, 6);
      return result;
    }
    return null;
  }
}
