package de.mephisto.vpin.server.achievements.scoreserver;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal text edits of the plugin sections in VPinballX.ini, the rest of the file (comments, order, line endings) is
 * left as it is.
 */
public class VpxIni {
  public static final String INI = "VPinballX.ini";
  private static final Pattern ENABLE = Pattern.compile("^\\s*enable\\s*=\\s*(.*)$", Pattern.CASE_INSENSITIVE);
  private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

  private VpxIni() {
  }

  /**
   * The VPinballX.ini VPX uses (VPX's own lookup): {@code %APPDATA%/VPinballX/<major>.<minor>/VPinballX.ini} (10.8.1+),
   * the ini next to the executable, {@code %APPDATA%/VPinballX/VPinballX.ini} (10.8.0).
   */
  @Nullable
  public static File find(@NonNull File appData, @Nullable File vpxFolder, @Nullable String vpxVersion) {
    List<File> candidates = new ArrayList<>();
    File base = new File(appData, "VPinballX");
    String[] v = vpxVersion != null ? vpxVersion.split("\\.") : new String[0];
    if (v.length >= 2) {
      candidates.add(new File(base, v[0] + "." + v[1] + "/" + INI));
    }
    else {
      File[] versions = base.listFiles(f -> f.isDirectory() && f.getName().matches("\\d+\\.\\d+"));
      if (versions != null) {
        Arrays.sort(versions, (a, b) -> compareVersion(b.getName(), a.getName()));
        for (File f : versions) {
          candidates.add(new File(f, INI));
        }
      }
    }
    if (vpxFolder != null) {
      candidates.add(new File(vpxFolder, INI));
    }
    candidates.add(new File(base, INI));
    for (File f : candidates) {
      if (f.isFile()) {
        return f;
      }
    }
    return null;
  }

  public static boolean isPluginEnabled(@NonNull File ini, @NonNull String pluginId) throws IOException {
    return isPluginEnabled(read(ini).text, pluginId);
  }

  static boolean isPluginEnabled(@NonNull String text, @NonNull String pluginId) {
    String header = ("[plugin." + pluginId + "]").toLowerCase();
    boolean inSection = false;
    for (String line : text.split("\\r?\\n", -1)) {
      String s = line.strip();
      if (s.startsWith("[")) {
        inSection = s.toLowerCase().equals(header);
        continue;
      }
      if (inSection) {
        Matcher m = ENABLE.matcher(s);
        if (m.matches()) {
          return m.group(1).strip().equals("1");
        }
      }
    }
    return false;
  }

  /**
   * Sets {@code Enable = 1|0} in {@code [Plugin.<id>]}. A backup {@code VPinballX.ini.studio.bak} is written before
   * the first change.
   *
   * @return true if the file was changed
   */
  public static boolean setPluginEnabled(@NonNull File ini, @NonNull String pluginId, boolean enabled) throws IOException {
    Content content = read(ini);
    String updated = setPluginEnabled(content.text, pluginId, enabled);
    if (updated.equals(content.text)) {
      return false;
    }
    File backup = new File(ini.getParentFile(), ini.getName() + ".studio.bak");
    if (!backup.exists()) {
      Files.copy(ini.toPath(), backup.toPath());
    }
    byte[] body = updated.getBytes(content.charset);
    byte[] data = body;
    if (content.bom) {
      data = new byte[BOM.length + body.length];
      System.arraycopy(BOM, 0, data, 0, BOM.length);
      System.arraycopy(body, 0, data, BOM.length, body.length);
    }
    File tmp = new File(ini.getParentFile(), ini.getName() + ".studio.tmp");
    Files.write(tmp.toPath(), data);
    Files.move(tmp.toPath(), ini.toPath(), StandardCopyOption.REPLACE_EXISTING);
    return true;
  }

  @NonNull
  static String setPluginEnabled(@NonNull String text, @NonNull String pluginId, boolean enabled) {
    String nl = text.contains("\r\n") ? "\r\n" : "\n";
    String value = enabled ? "1" : "0";
    List<String> lines = new ArrayList<>(Arrays.asList(text.split("\\r?\\n", -1)));
    boolean endsWithNl = !lines.isEmpty() && lines.get(lines.size() - 1).isEmpty();
    if (endsWithNl) {
      lines.remove(lines.size() - 1);
    }
    String header = ("[plugin." + pluginId + "]").toLowerCase();

    int start = -1;
    for (int i = 0; i < lines.size(); i++) {
      if (lines.get(i).strip().toLowerCase().equals(header)) {
        start = i;
        break;
      }
    }
    if (start < 0) {
      if (!enabled) {
        return text; // not there means disabled
      }
      if (!lines.isEmpty() && !lines.get(lines.size() - 1).isBlank()) {
        lines.add("");
      }
      lines.add("[Plugin." + pluginId + "]");
      lines.add("Enable = " + value);
    }
    else {
      int end = lines.size();
      for (int j = start + 1; j < lines.size(); j++) {
        if (lines.get(j).strip().startsWith("[")) {
          end = j;
          break;
        }
      }
      boolean found = false;
      for (int j = start + 1; j < end; j++) {
        Matcher m = ENABLE.matcher(lines.get(j));
        if (m.matches()) {
          found = true;
          if (m.group(1).strip().equals(value)) {
            return text;
          }
          lines.set(j, "Enable = " + value);
          break;
        }
      }
      if (!found) {
        if (!enabled) {
          return text;
        }
        lines.add(start + 1, "Enable = " + value);
      }
    }
    return String.join(nl, lines) + (endsWithNl || start < 0 ? nl : "");
  }

  private record Content(String text, Charset charset, boolean bom) {
  }

  private static Content read(File ini) throws IOException {
    byte[] raw = Files.readAllBytes(ini.toPath());
    boolean bom = raw.length >= 3 && raw[0] == BOM[0] && raw[1] == BOM[1] && raw[2] == BOM[2];
    byte[] body = bom ? Arrays.copyOfRange(raw, 3, raw.length) : raw;
    for (Charset cs : List.of(StandardCharsets.UTF_8, Charset.forName("windows-1252"))) {
      try {
        CharsetDecoder decoder = cs.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        return new Content(decoder.decode(java.nio.ByteBuffer.wrap(body)).toString(), cs, bom);
      }
      catch (Exception e) {
        // next charset
      }
    }
    return new Content(new String(body, StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1, bom);
  }

  /**
   * Compares dotted version strings numerically ("10.8.1.3788" vs "10.8.1"), missing segments are 0.
   */
  public static int compareVersion(@Nullable String a, @Nullable String b) {
    String[] x = a != null ? a.trim().split("\\.") : new String[0];
    String[] y = b != null ? b.trim().split("\\.") : new String[0];
    for (int i = 0; i < Math.max(x.length, y.length); i++) {
      int c = Integer.compare(segment(x, i), segment(y, i));
      if (c != 0) {
        return c;
      }
    }
    return 0;
  }

  /**
   * @return true if both versions have the same major.minor.revision
   */
  public static boolean sameRevision(@Nullable String a, @Nullable String b) {
    if (a == null || b == null || a.isBlank() || b.isBlank()) {
      return false;
    }
    String[] x = a.trim().split("\\.");
    String[] y = b.trim().split("\\.");
    for (int i = 0; i < 3; i++) {
      if (segment(x, i) != segment(y, i)) {
        return false;
      }
    }
    return true;
  }

  private static int segment(String[] parts, int i) {
    if (i >= parts.length) {
      return 0;
    }
    try {
      return Integer.parseInt(parts[i].replaceAll("\\D.*", ""));
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }
}
