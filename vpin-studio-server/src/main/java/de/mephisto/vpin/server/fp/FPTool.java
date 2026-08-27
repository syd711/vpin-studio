package de.mephisto.vpin.server.fp;

import de.mephisto.vpin.server.roms.ScanResult;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Command line front end for {@link FPFileScanner}, handy for trying the scanner against a
 * table collection. The library itself does not print anything.
 *
 * <pre>
 *   java -jar fp-puppack-scanner-cli.jar "C:\vPinball\FuturePinball\Tables"
 *   java -jar fp-puppack-scanner-cli.jar "Table.fpt" --script table.vbs
 *   java -jar fp-puppack-scanner-cli.jar "Table.fpt" --list
 *   java -jar fp-puppack-scanner-cli.jar "Table.fpt" --records "Table Data"
 * </pre>
 */
public class FPTool {

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.out.println("Usage: FPTool <file.fpt | folder> [--script <out.vbs>] [--list] "
          + "[--records <stream>] [--csv]");
      return;
    }

    File input = null;
    Path scriptOut = null;
    String recordsOf = null;
    boolean list = false;
    boolean csv = false;

    for (int i = 0; i < args.length; i++) {
      String arg = args[i].toLowerCase(Locale.ROOT);
      if ("--script".equals(arg)) {
        scriptOut = Paths.get(args[++i]);
      }
      else if ("--records".equals(arg)) {
        recordsOf = args[++i];
      }
      else if ("--list".equals(arg)) {
        list = true;
      }
      else if ("--csv".equals(arg)) {
        csv = true;
      }
      else {
        input = new File(args[i]);
      }
    }

    if (input == null || !input.exists()) {
      System.err.println("Not found: " + input);
      System.exit(2);
      return;
    }

    if (input.isDirectory()) {
      scanFolder(input, csv);
    }
    else {
      scanSingle(input, scriptOut, list, recordsOf);
    }
  }

  private static void scanSingle(File table, Path scriptOut, boolean list, String recordsOf) throws IOException {
    long started = System.currentTimeMillis();
    System.out.println("File        : " + table.getName());
    System.out.println("Size        : " + table.length() + " bytes");

    if (!FPTFile.isCompoundFile(table)) {
      System.out.println("Format      : not an OLE2 compound file, this is not a Future Pinball table");
      return;
    }

    try (FPTFile file = new FPTFile(table)) {
      System.out.println("Format      : OLE2 compound file, " + file.getEntries().size() + " directory entries");

      if (list) {
        printStreams(file);
      }
      if (recordsOf != null) {
        printRecords(file, recordsOf);
      }

      String script = file.getTableScript();
      System.out.println("Script      : " + (script.isEmpty() ? "not found" : script.length() + " characters"));
      if (scriptOut != null && !script.isEmpty()) {
        Files.write(scriptOut, script.getBytes(StandardCharsets.ISO_8859_1));
        System.out.println("Script saved: " + scriptOut.toAbsolutePath());
      }
    }

    ScanResult result = FPFileScanner.scan(table);
    System.out.println("PuP-Pack    : " + (result.getPupPackName() == null ? "(none found)" : result.getPupPackName()));
    System.out.println("Took        : " + (System.currentTimeMillis() - started) + " ms");
  }

  private static void scanFolder(File folder, boolean csv) {
    File[] files = folder.listFiles(f -> f.isFile() && f.getName().toLowerCase(Locale.ROOT).endsWith(".fpt"));
    List<File> tables = files == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(files));
    tables.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));

    if (csv) {
      System.out.println("table;puppack;source;usePup");
    }
    int hits = 0;
    for (File table : tables) {
      ScanResult result = FPFileScanner.scan(table);
      if (result.getPupPackName() != null) {
        hits++;
      }
      String name = result.getPupPackName() == null ? "-" : result.getPupPackName();
      if (csv) {
//        System.out.println(table.getName() + ";" + name + ";" + result.getPupPackSource() + ";" + result.isUsePup());
      }
      else {
        System.out.printf("%-70s %s%n", trim(table.getName(), 68), name);
      }
    }
    if (!csv) {
      System.out.println();
      System.out.println(hits + " of " + tables.size() + " tables have a PuP-Pack name in their script.");
    }
  }

  private static void printStreams(FPTFile file) {
    System.out.println();
    System.out.printf("%-40s %10s%n", "stream", "size");
    for (FPTFile.Entry entry : file.getEntries()) {
      if (entry.isStream()) {
        System.out.printf("%-40s %10d%n", trim(entry.getName(), 38), entry.getSize());
      }
    }
    System.out.println();
  }

  private static void printRecords(FPTFile file, String streamName) throws IOException {
    FPTFile.Entry entry = file.getEntry(streamName);
    if (entry == null) {
      System.out.println("Stream not found: " + streamName);
      return;
    }
    byte[] data = file.readStream(entry);
    System.out.println();
    System.out.println("Records in \"" + entry.getName() + "\" (" + data.length + " bytes):");
    System.out.printf("%-8s %10s %10s%n", "tag", "offset", "size");

    int offset = 0;
    int shown = 0;
    while (offset + 8 <= data.length && shown < 200) {
      int length = readInt(data, offset);
      String tag = FPTFile.decodeTag(data, offset + 4);
      if (tag == null || length < 4 || length > data.length - offset - 4) {
        offset += 4;
        continue;
      }
      System.out.printf("%-8s %10d %10d%n", tag, offset + 8, length - 4);
      shown++;
      offset += 4 + length;
    }
    System.out.println();
  }

  private static int readInt(byte[] data, int offset) {
    return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8)
        | ((data[offset + 2] & 0xFF) << 16) | ((data[offset + 3] & 0xFF) << 24);
  }

  private static String trim(String value, int max) {
    return value.length() <= max ? value : value.substring(0, max - 3) + "...";
  }
}
