package de.mephisto.vpin.server.fp;

import de.mephisto.vpin.server.roms.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scans a Future Pinball table (.fpt) for the name of the PuP-Pack it drives.
 * <p>
 * PinUP Player is started from the table script with
 * <code>PuPlayer.B2SInit "", &lt;name&gt;</code>, where the name is the folder below
 * PinUPSystem\PUPVideos. Most tables assign that name to a variable first, usually
 * cPuPPack, so the scanner resolves variables as well as literals.
 */
public class FPFileScanner {

  private static final Logger LOG = LoggerFactory.getLogger(FPFileScanner.class);

  private static final Pattern B2SINIT_LITERAL =
      Pattern.compile("B2SInit\\s*\"[^\"]*\"\\s*,\\s*\"([^\"]{1,120})\"", Pattern.CASE_INSENSITIVE);
  private static final Pattern B2SINIT_VARIABLE =
      Pattern.compile("B2SInit\\s*\"[^\"]*\"\\s*,\\s*([A-Za-z_][A-Za-z0-9_]*)", Pattern.CASE_INSENSITIVE);
  private static final Pattern PUPSTART_LITERAL =
      Pattern.compile("(?<![A-Za-z0-9_])PuPStart\\s*\\(?\\s*\"([^\"]{1,120})\"", Pattern.CASE_INSENSITIVE);
  private static final Pattern USE_PUP =
      Pattern.compile("(?<![A-Za-z0-9_])usePUP\\s*=\\s*(true|false|1|0)(?![A-Za-z0-9_])", Pattern.CASE_INSENSITIVE);

  /** Variable names table authors commonly use for the PuP-Pack folder. */
  private static final List<String> KNOWN_VARIABLES = Arrays.asList(
      "cPuPPack", "cPupPackName", "PuPPack", "PuPPackName", "cGameName", "GameName", "cPuPPackFolder");

  /** Values that are never a PuP-Pack folder name. */
  private static final Set<String> IGNORED = new HashSet<>(Arrays.asList(
      "", "b2s", "pinupplayer.pindisplay", "pup-pack_name", "puppackname", "none"));

  public static ScanResult scan(File fpt) {
    ScanResult result = new ScanResult();
    if (fpt == null || !fpt.exists() || !FPTFile.isCompoundFile(fpt)) {
      return result;
    }

    String script = "";
    try (FPTFile file = new FPTFile(fpt)) {
      script = file.getTableScript();
    }
    catch (IOException e) {
      LOG.warn("Failed to read Future Pinball table {}: {}", fpt.getName(), e.getMessage());
      return result;
    }

    if (script.isEmpty()) {
      LOG.info("No table script found in {}", fpt.getName());
      return result;
    }

    String code = stripComments(script);

    for (String value : findAll(code, B2SINIT_LITERAL)) {
      if (accept(result, value, "B2SInit literal")) {
        return result;
      }
    }
    for (String variable : findAll(code, B2SINIT_VARIABLE)) {
      for (String value : resolveVariable(code, variable)) {
        if (accept(result, value, "B2SInit via " + variable)) {
          return result;
        }
      }
    }
    for (String value : findAll(code, PUPSTART_LITERAL)) {
      if (accept(result, value, "PuPStart literal")) {
        return result;
      }
    }
    for (String variable : KNOWN_VARIABLES) {
      for (String value : resolveVariable(code, variable)) {
        if (accept(result, value, "assignment " + variable)) {
          return result;
        }
      }
    }
    return result;
  }

  private static boolean accept(ScanResult result, String value, String source) {
    String name = value.trim();
    if (name.isEmpty() || name.length() > 80 || IGNORED.contains(name.toLowerCase(Locale.ROOT))) {
      return false;
    }
    result.setPupPackName(name);
    return true;
  }

  private static List<String> findAll(String text, Pattern pattern) {
    List<String> values = new ArrayList<>();
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      String value = matcher.group(1);
      if (!values.contains(value)) {
        values.add(value);
      }
    }
    return values;
  }

  private static List<String> resolveVariable(String code, String variable) {
    Pattern pattern = Pattern.compile(
        "(?<![A-Za-z0-9_])" + Pattern.quote(variable) + "\\s*=\\s*\"([^\"]{0,120})\"", Pattern.CASE_INSENSITIVE);
    return findAll(code, pattern);
  }

  /**
   * Removes VBScript comments so a commented out PuP line is not mistaken for the real
   * one. Control characters count as line breaks because script text extracted from a
   * table can carry padding bytes.
   */
  static String stripComments(String script) {
    StringBuilder out = new StringBuilder(script.length());
    StringBuilder line = new StringBuilder(256);
    boolean inString = false;
    boolean inComment = false;

    for (int i = 0; i <= script.length(); i++) {
      char c = i < script.length() ? script.charAt(i) : '\n';
      if (c < 32 && c != '\t') {
        out.append(line).append('\n');
        line.setLength(0);
        inString = false;
        inComment = false;
        continue;
      }
      if (inComment) {
        continue;
      }
      if (c == '"') {
        inString = !inString;
      }
      else if (c == '\'' && !inString) {
        inComment = true;
        continue;
      }
      line.append(c);
    }
    return out.toString();
  }
}
