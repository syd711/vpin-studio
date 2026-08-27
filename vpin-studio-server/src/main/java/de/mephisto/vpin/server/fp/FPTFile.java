package de.mephisto.vpin.server.fp;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Read only access to a Future Pinball table file (.fpt).
 * <p>
 * A .fpt is an OLE2 compound file (signature D0 CF 11 E0 A1 B1 1A E1), the same container
 * format that Word 97 and Visual Pinball use. Its streams contain a chain of records:
 *
 * <pre>
 *   [uint32 length][4 byte tag][payload of length - 4 bytes]
 * </pre>
 *
 * The tag is stored as the arithmetic negation of the FourCC read as a little endian
 * uint32, so the bytes B2 BE B2 BA are 0xBAB2BEB2 and 0x100000000 - 0xBAB2BEB2 is
 * 0x454D414E, which spells "NAME". Long payloads are compressed and start with the magic
 * "zLZO" followed by an LZO1X stream, see {@link Lzo1xDecompressor}.
 * <p>
 * The table script lives in the CODE record at the end of the "Table Data" stream.
 */
public class FPTFile implements Closeable {

  public static final String TABLE_DATA_STREAM = "Table Data";
  public static final String CODE_RECORD = "CODE";

  private static final int ENDOFCHAIN = 0xFFFFFFFE;
  private static final int FREESECT = 0xFFFFFFFF;
  private static final byte[] ZLZO_MAGIC = {'z', 'L', 'Z', 'O'};

  /** Streams above this size are never script carriers, skipping them saves a lot of io. */
  private static final long MAX_STREAM_SIZE = 64L * 1024 * 1024;
  private static final int MAX_SCRIPT_SIZE = 16 * 1024 * 1024;

  private final RandomAccessFile raf;
  private final int sectorSize;
  private final int miniSectorSize;
  private final int miniStreamCutoff;
  private final int[] fat;
  private final int[] miniFat;
  private final List<Entry> entries = new ArrayList<>();
  private byte[] miniStream = new byte[0];

  /** A directory entry: a storage (folder) or a stream (file) inside the container. */
  public static class Entry {
    private final String name;
    private final int type;
    private final int startSector;
    private final long size;

    Entry(String name, int type, int startSector, long size) {
      this.name = name;
      this.type = type;
      this.startSector = startSector;
      this.size = size;
    }

    public String getName() {
      return name;
    }

    public long getSize() {
      return size;
    }

    public boolean isStream() {
      return type == 2;
    }

    public boolean isRoot() {
      return type == 5;
    }

    @Override
    public String toString() {
      return name + " (" + size + " bytes)";
    }
  }

  /** @return true when the file starts with the OLE2 compound file signature. */
  public static boolean isCompoundFile(File file) {
    try (RandomAccessFile check = new RandomAccessFile(file, "r")) {
      byte[] signature = new byte[8];
      check.readFully(signature);
      return (signature[0] & 0xFF) == 0xD0 && (signature[1] & 0xFF) == 0xCF
          && (signature[2] & 0xFF) == 0x11 && (signature[3] & 0xFF) == 0xE0;
    }
    catch (IOException e) {
      return false;
    }
  }

  public FPTFile(File file) throws IOException {
    this.raf = new RandomAccessFile(file, "r");
    try {
      byte[] header = new byte[512];
      raf.readFully(header);
      ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
      if ((header[0] & 0xFF) != 0xD0 || (header[1] & 0xFF) != 0xCF) {
        throw new IOException("not an OLE2 compound file: " + file.getName());
      }
      this.sectorSize = 1 << buffer.getShort(30);
      this.miniSectorSize = 1 << buffer.getShort(32);
      this.miniStreamCutoff = buffer.getInt(56);

      int fatSectorCount = buffer.getInt(44);
      int firstDirectorySector = buffer.getInt(48);
      int firstMiniFatSector = buffer.getInt(60);
      int miniFatSectorCount = buffer.getInt(64);
      int firstDifatSector = buffer.getInt(68);
      int difatSectorCount = buffer.getInt(72);

      this.fat = readFat(buffer, fatSectorCount, firstDifatSector, difatSectorCount);
      this.miniFat = readMiniFat(firstMiniFatSector, miniFatSectorCount);
      readDirectory(firstDirectorySector);
    }
    catch (IOException | RuntimeException e) {
      raf.close();
      throw e;
    }
  }

  public List<Entry> getEntries() {
    return entries;
  }

  /** @return the stream with the given name, or null when it does not exist. */
  public Entry getEntry(String name) {
    for (Entry entry : entries) {
      if (entry.isStream() && entry.getName().equalsIgnoreCase(name)) {
        return entry;
      }
    }
    return null;
  }

  public byte[] readStream(Entry entry) throws IOException {
    long size = Math.min(entry.getSize(), MAX_STREAM_SIZE);
    if (entry.getSize() < miniStreamCutoff && !entry.isRoot()) {
      return readMiniChain(entry.startSector, size);
    }
    return readChain(entry.startSector, size);
  }

  /**
   * Reads the table script: the CODE record of the "Table Data" stream, LZO decompressed.
   *
   * @return the script, or an empty string when this table does not expose one
   */
  public String getTableScript() throws IOException {
    Entry tableData = getEntry(TABLE_DATA_STREAM);
    if (tableData == null || tableData.getSize() == 0 || tableData.getSize() > MAX_STREAM_SIZE) {
      return "";
    }
    return extractScript(readStream(tableData));
  }

  static String extractScript(byte[] tableData) {
    int codeOffset = findRecord(tableData, CODE_RECORD);
    if (codeOffset >= 0) {
      int magic = indexOf(tableData, ZLZO_MAGIC, Math.max(0, codeOffset - 8));
      if (magic >= 0 && magic - codeOffset < 64) {
        byte[] script = unpack(tableData, magic);
        if (script != null) {
          return new String(script, StandardCharsets.ISO_8859_1);
        }
      }
    }

    //fallback for tables that lay out their records differently: take the largest
    //compressed block that decodes to text
    byte[] best = null;
    int inspected = 0;
    for (int i = indexOf(tableData, ZLZO_MAGIC, 0); i >= 0 && inspected < 12;
         i = indexOf(tableData, ZLZO_MAGIC, i + 4), inspected++) {
      byte[] candidate = unpack(tableData, i);
      if (candidate != null && (best == null || candidate.length > best.length)) {
        best = candidate;
      }
    }
    return best == null ? "" : new String(best, StandardCharsets.ISO_8859_1);
  }

  /**
   * The number of header bytes between the zLZO magic and the compressed stream differs
   * per record type, so a few offsets are tried and the first readable result wins.
   */
  private static byte[] unpack(byte[] data, int magicOffset) {
    byte[] best = null;
    for (int skip : new int[]{8, 4, 12, 16}) {
      int start = magicOffset + skip;
      if (start >= data.length) {
        break;
      }
      byte[] candidate = Lzo1xDecompressor.decompress(data, start, MAX_SCRIPT_SIZE);
      if (candidate.length < 64 || !isMostlyText(candidate)) {
        continue;
      }
      if (best == null || candidate.length > best.length) {
        best = candidate;
      }
      if (best.length > 4096) {
        break;
      }
    }
    return best;
  }

  /** @return the payload offset of the first record with the given tag, or -1. */
  static int findRecord(byte[] data, String tag) {
    int offset = 0;
    int guard = 0;
    while (offset + 8 <= data.length && guard++ < 1_000_000) {
      int length = readInt(data, offset);
      String recordTag = decodeTag(data, offset + 4);
      if (recordTag == null || length < 4 || length > data.length - offset - 4) {
        offset += 4; //resynchronise, not every stream starts on a record boundary
        continue;
      }
      if (recordTag.equals(tag)) {
        return offset + 8;
      }
      offset += 4 + length;
    }
    return -1;
  }

  /**
   * Decodes a record tag. It is stored as the negation of the little endian uint32, so
   * 0x100000000 minus the stored value spells the original FourCC.
   *
   * @return the tag, or null when the result is not a readable FourCC
   */
  static String decodeTag(byte[] data, int offset) {
    if (offset + 4 > data.length) {
      return null;
    }
    long stored = (data[offset] & 0xFFL)
        | ((data[offset + 1] & 0xFFL) << 8)
        | ((data[offset + 2] & 0xFFL) << 16)
        | ((data[offset + 3] & 0xFFL) << 24);
    long tag = (0x100000000L - stored) & 0xFFFFFFFFL;
    char[] chars = new char[4];
    for (int i = 0; i < 4; i++) {
      int b = (int) ((tag >> (8 * i)) & 0xFF);
      if (b > 126 || !(Character.isLetterOrDigit(b) || b == '_')) {
        return null;
      }
      chars[i] = (char) b;
    }
    return new String(chars);
  }

  //-------------------------------------------------------------- compound file plumbing

  private int[] readFat(ByteBuffer header, int fatSectorCount, int firstDifatSector, int difatSectorCount)
      throws IOException {
    List<Integer> fatSectors = new ArrayList<>();
    for (int i = 0; i < 109 && fatSectors.size() < fatSectorCount; i++) {
      int sector = header.getInt(76 + 4 * i);
      if (sector >= 0 && sector != FREESECT) {
        fatSectors.add(sector);
      }
    }
    int difat = firstDifatSector;
    for (int n = 0; n < difatSectorCount && difat != ENDOFCHAIN && difat != FREESECT; n++) {
      ByteBuffer sector = ByteBuffer.wrap(readSector(difat)).order(ByteOrder.LITTLE_ENDIAN);
      int entriesPerSector = sectorSize / 4 - 1;
      for (int i = 0; i < entriesPerSector && fatSectors.size() < fatSectorCount; i++) {
        int value = sector.getInt(i * 4);
        if (value >= 0 && value != FREESECT) {
          fatSectors.add(value);
        }
      }
      difat = sector.getInt(sectorSize - 4);
    }

    int[] table = new int[fatSectors.size() * (sectorSize / 4)];
    int index = 0;
    for (int sectorNumber : fatSectors) {
      ByteBuffer sector = ByteBuffer.wrap(readSector(sectorNumber)).order(ByteOrder.LITTLE_ENDIAN);
      for (int i = 0; i < sectorSize / 4; i++) {
        table[index++] = sector.getInt(i * 4);
      }
    }
    return table;
  }

  private int[] readMiniFat(int firstMiniFatSector, int miniFatSectorCount) throws IOException {
    if (firstMiniFatSector == ENDOFCHAIN || firstMiniFatSector == FREESECT || miniFatSectorCount <= 0) {
      return new int[0];
    }
    byte[] data = readChain(firstMiniFatSector, (long) miniFatSectorCount * sectorSize);
    int[] table = new int[data.length / 4];
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < table.length; i++) {
      table[i] = buffer.getInt(i * 4);
    }
    return table;
  }

  private void readDirectory(int firstDirectorySector) throws IOException {
    byte[] directory = readChain(firstDirectorySector, Long.MAX_VALUE);
    for (int offset = 0; offset + 128 <= directory.length; offset += 128) {
      ByteBuffer buffer = ByteBuffer.wrap(directory, offset, 128).order(ByteOrder.LITTLE_ENDIAN);
      int type = directory[offset + 66] & 0xFF;
      if (type == 0) {
        continue;
      }
      int nameLength = Math.max(0, Math.min(64, buffer.getShort(offset + 64)));
      String name = new String(directory, offset, Math.max(0, nameLength - 2), StandardCharsets.UTF_16LE);
      Entry entry = new Entry(name, type, buffer.getInt(offset + 116), buffer.getLong(offset + 120));
      entries.add(entry);
      if (entry.isRoot()) {
        //the root entry points at the mini stream that holds all streams below the cutoff
        miniStream = readChain(entry.startSector, Math.min(entry.getSize(), MAX_STREAM_SIZE));
      }
    }
  }

  private byte[] readChain(int startSector, long size) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int sector = startSector;
    long remaining = size;
    int guard = 0;
    while (sector != ENDOFCHAIN && sector != FREESECT && sector >= 0 && remaining > 0) {
      if (guard++ > 4_000_000 || sector >= fat.length) {
        break;
      }
      byte[] data = readSector(sector);
      int take = (int) Math.min(sectorSize, remaining);
      out.write(data, 0, take);
      if (remaining != Long.MAX_VALUE) {
        remaining -= take;
      }
      sector = fat[sector];
    }
    return out.toByteArray();
  }

  private byte[] readMiniChain(int startSector, long size) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int sector = startSector;
    long remaining = size;
    int guard = 0;
    while (sector != ENDOFCHAIN && sector != FREESECT && sector >= 0 && remaining > 0) {
      if (guard++ > 4_000_000 || sector >= miniFat.length) {
        break;
      }
      int offset = sector * miniSectorSize;
      if (offset + miniSectorSize > miniStream.length) {
        break;
      }
      int take = (int) Math.min(miniSectorSize, remaining);
      out.write(miniStream, offset, take);
      remaining -= take;
      sector = miniFat[sector];
    }
    return out.toByteArray();
  }

  private byte[] readSector(int sector) throws IOException {
    byte[] data = new byte[sectorSize];
    raf.seek((long) (sector + 1) * sectorSize);
    int read = 0;
    while (read < sectorSize) {
      int n = raf.read(data, read, sectorSize - read);
      if (n < 0) {
        break;
      }
      read += n;
    }
    return data;
  }

  private static boolean isMostlyText(byte[] data) {
    int check = Math.min(data.length, 512);
    int text = 0;
    for (int i = 0; i < check; i++) {
      char c = (char) (data[i] & 0xFF);
      if ((c >= 32 && c <= 126) || c == '\r' || c == '\n' || c == '\t') {
        text++;
      }
    }
    return check == 0 || (double) text / check >= 0.85;
  }

  private static int readInt(byte[] data, int offset) {
    return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8)
        | ((data[offset + 2] & 0xFF) << 16) | ((data[offset + 3] & 0xFF) << 24);
  }

  private static int indexOf(byte[] haystack, byte[] needle, int from) {
    outer:
    for (int i = Math.max(0, from); i <= haystack.length - needle.length; i++) {
      for (int j = 0; j < needle.length; j++) {
        if (haystack[i + j] != needle[j]) {
          continue outer;
        }
      }
      return i;
    }
    return -1;
  }

  @Override
  public void close() throws IOException {
    raf.close();
  }
}
