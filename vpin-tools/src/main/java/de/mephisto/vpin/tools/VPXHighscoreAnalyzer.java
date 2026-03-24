package de.mephisto.vpin.tools;

import de.mephisto.vpin.server.vpx.VPXUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Reads a list of VPX filenames from a text file, locates each matching .vpx
 * file in a configurable search folder, extracts the embedded VBScript, and
 * determines how the table stores its high scores.
 *
 * Detection priority (mirrors HighscoreResolver):
 *   1. NVRam  – script references SaveToRam / .nv / nvram
 *   2. VPReg  – script uses SaveValue / LoadValue (VPReg.stg / dpregstg)
 *   3. Ini    – script references a _glf.ini file
 *   4. EM     – script writes to a plain .txt high-score file
 *   5. Unknown
 *
 * Usage: adjust the three constants below and run main().
 */
public class VPXHighscoreAnalyzer {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // ── Configuration ────────────────────────────────────────────────────────────

  /** Text file where every line is a VPX filename (with or without .vpx extension). */
  private static final String INPUT_LIST_FILE = "vpin-tools/table-list.txt";

  /** Folder tree that is searched recursively for the matching .vpx files. */
  private static final String VPX_SEARCH_FOLDER = "C:/vPinball/VisualPinball/Tables";

  /** Where to write the analysis report (CSV). Leave empty to print to stdout only. */
  private static final String OUTPUT_REPORT_FILE = "vpin-tools/highscore-analysis.csv";

  // ── Script patterns ──────────────────────────────────────────────────────────

  // NVRam: table saves state to a .nv file via PinMAME / direct nvram access
  private static final Pattern NVRAM_PATTERN = Pattern.compile(
      "(?i)(SaveToRam|LoadFromRam|\\.nv[\"'\\s]|nvram|NVOffset|NVRead|NVWrite|PinMAME)", Pattern.CASE_INSENSITIVE);

  // VPReg / dpregstg: table uses VPinball registry storage
  private static final Pattern VPREG_PATTERN = Pattern.compile(
      "(?i)(SaveValue|LoadValue|VPReg|dpregstg)", Pattern.CASE_INSENSITIVE);

  // Goldflinger ini file (e.g. tablename_glf.ini)
  private static final Pattern INI_PATTERN = Pattern.compile(
      "(?i)_glf\\.ini", Pattern.CASE_INSENSITIVE);

  // EM / plain text high-score file
  private static final Pattern TEXT_HS_PATTERN = Pattern.compile(
      "(?i)(HighScore.*\\.txt|\\bhs\\b.*\\.txt|\\.txt.*[Hh]igh[Ss]core|Open.*For.*Output|WriteHS|LoadHS|SaveHS)",
      Pattern.CASE_INSENSITIVE);

  // ── Entry point ──────────────────────────────────────────────────────────────

  public static void main(String[] args) throws IOException {
    File listFile = new File(INPUT_LIST_FILE);
    if (!listFile.exists()) {
      LOG.error("Input list file not found: {}", listFile.getAbsolutePath());
      System.exit(1);
    }

    File searchRoot = new File(VPX_SEARCH_FOLDER);
    if (!searchRoot.isDirectory()) {
      LOG.error("VPX search folder is not a directory: {}", searchRoot.getAbsolutePath());
      System.exit(1);
    }

    List<String> tableNames = Files.readAllLines(listFile.toPath()).stream()
        .map(String::trim)
        .filter(l -> !l.isEmpty() && !l.startsWith("#"))
        .collect(Collectors.toList());

    LOG.info("Loaded {} table names from {}", tableNames.size(), listFile.getAbsolutePath());

    List<File> allVpxFiles = collectVpxFiles(searchRoot);
    LOG.info("Indexed {} .vpx files under {}", allVpxFiles.size(), searchRoot.getAbsolutePath());

    List<AnalysisResult> results = new ArrayList<>();

    for (String searchName : tableNames) {
      List<File> matches = findMatches(searchName, allVpxFiles);

      if (matches.isEmpty()) {
        LOG.warn("No .vpx file found for: {}", searchName);
        results.add(new AnalysisResult(searchName, null, HighscoreType.NOT_FOUND, ""));
        continue;
      }

      if (matches.size() > 1) {
        LOG.info("Found {} matches for '{}', analysing all", matches.size(), searchName);
      }

      for (File vpxFile : matches) {
        LOG.info("Analysing: {}", vpxFile.getName());
        results.add(analyse(searchName, vpxFile));
      }
    }

    printReport(results);

    if (!OUTPUT_REPORT_FILE.isEmpty()) {
      writeCsvReport(results, new File(OUTPUT_REPORT_FILE));
    }
  }

  // ── Core analysis ────────────────────────────────────────────────────────────

  private static AnalysisResult analyse(String searchName, File vpxFile) {
    String script = VPXUtil.readScript(vpxFile);

    if (script == null || script.isEmpty()) {
      return new AnalysisResult(searchName, vpxFile, HighscoreType.UNKNOWN, "Could not extract script");
    }

    HighscoreType type = detectType(script);
    String evidence = extractEvidence(script, type);

    return new AnalysisResult(searchName, vpxFile, type, evidence);
  }

  private static HighscoreType detectType(String script) {
    if (NVRAM_PATTERN.matcher(script).find()) {
      return HighscoreType.NVRam;
    }
    if (VPREG_PATTERN.matcher(script).find()) {
      return HighscoreType.VPReg;
    }
    if (INI_PATTERN.matcher(script).find()) {
      return HighscoreType.Ini;
    }
    if (TEXT_HS_PATTERN.matcher(script).find()) {
      return HighscoreType.EM;
    }
    return HighscoreType.UNKNOWN;
  }

  /**
   * Returns the first matching line from the script as evidence for the detected type.
   */
  private static String extractEvidence(String script, HighscoreType type) {
    Pattern pattern;
    switch (type) {
      case NVRam:  pattern = NVRAM_PATTERN;   break;
      case VPReg:  pattern = VPREG_PATTERN;   break;
      case Ini:    pattern = INI_PATTERN;      break;
      case EM:     pattern = TEXT_HS_PATTERN;  break;
      default:     return "";
    }

    for (String line : script.split("\\r?\\n")) {
      Matcher m = pattern.matcher(line);
      if (m.find()) {
        return line.trim();
      }
    }
    return "";
  }

  // ── Fuzzy matching ───────────────────────────────────────────────────────────

  /** Recursively collect all .vpx files under root. */
  private static List<File> collectVpxFiles(File root) throws IOException {
    try (java.util.stream.Stream<java.nio.file.Path> stream = Files.walk(root.toPath())) {
      return stream
          .filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".vpx"))
          .map(java.nio.file.Path::toFile)
          .collect(Collectors.toList());
    }
  }

  /**
   * Finds all .vpx files that match {@code searchName} using a three-pass strategy:
   * <ol>
   *   <li>Exact normalized match (alphanumeric, lowercase)</li>
   *   <li>Substring: the search fuzzy-key is fully contained in the file fuzzy-key</li>
   *   <li>Token: every significant word from the search name appears in the file fuzzy-key</li>
   * </ol>
   * Returns results from the first pass that yields at least one hit.
   */
  private static List<File> findMatches(String searchName, List<File> allFiles) {
    String searchFuzzy = fuzzyKey(searchName);

    // Pass 1 – exact normalized match
    List<File> exact = allFiles.stream()
        .filter(f -> fuzzyKey(f.getName()).equals(searchFuzzy))
        .collect(Collectors.toList());
    if (!exact.isEmpty()) return exact;

    // Pass 2 – substring containment (search key inside file key)
    List<File> substring = allFiles.stream()
        .filter(f -> fuzzyKey(f.getName()).contains(searchFuzzy))
        .collect(Collectors.toList());
    if (!substring.isEmpty()) return substring;

    // Pass 3 – all significant tokens from search appear in the file key
    List<String> tokens = significantTokens(searchName);
    if (tokens.isEmpty()) return Collections.emptyList();

    return allFiles.stream()
        .filter(f -> {
          String fileKey = fuzzyKey(f.getName());
          return tokens.stream().allMatch(fileKey::contains);
        })
        .collect(Collectors.toList());
  }

  /**
   * Reduces a name to lowercase alphanumeric characters only, stripping
   * spaces, punctuation, version suffixes, and common noise words.
   * e.g. "Twilight Zone, The (Williams 1993) v2.1" → "twilightzonethe"
   */
  private static String fuzzyKey(String name) {
    String base = FilenameUtils.getBaseName(name);
    // Remove version tokens like v1.0, 1.03, (1993)
    base = base.replaceAll("(?i)\\bv?\\d+(\\.\\d+)+\\b", "");
    // Remove parenthesised/bracketed content (manufacturer, year)
    base = base.replaceAll("[\\(\\[\\{][^\\)\\]\\}]*[\\)\\]\\}]", "");
    // Keep only alphanumeric
    return base.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
  }

  /**
   * Splits a search name into meaningful word tokens (length >= 3) for token matching.
   * Skips purely numeric tokens and common noise words.
   */
  private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
      "the", "and", "for", "vpx", "vpu", "mod", "original"));

  private static List<String> significantTokens(String name) {
    String base = FilenameUtils.getBaseName(name);
    return Arrays.stream(base.split("[^a-zA-Z0-9]+"))
        .map(t -> t.toLowerCase(Locale.ROOT))
        .filter(t -> t.length() >= 3 && !STOP_WORDS.contains(t) && !t.matches("\\d+"))
        .collect(Collectors.toList());
  }

  // ── Reporting ────────────────────────────────────────────────────────────────

  private static void printReport(List<AnalysisResult> results) {
    System.out.println();
    System.out.printf("%-40s %-45s %-12s %s%n", "Search Name", "Matched File", "Type", "Evidence");
    System.out.println("-".repeat(160));
    for (AnalysisResult r : results) {
      String matchedFile = r.vpxFile != null ? r.vpxFile.getName() : "(not found)";
      System.out.printf("%-40s %-45s %-12s %s%n",
          truncate(r.searchName, 39),
          truncate(matchedFile, 44),
          r.type,
          truncate(r.evidence, 60));
    }
    System.out.println();

    // Summary counts
    Map<HighscoreType, Long> counts = results.stream()
        .collect(Collectors.groupingBy(r -> r.type, Collectors.counting()));
    System.out.println("Summary:");
    for (HighscoreType t : HighscoreType.values()) {
      System.out.printf("  %-12s : %d%n", t, counts.getOrDefault(t, 0L));
    }
    System.out.println();
  }

  private static void writeCsvReport(List<AnalysisResult> results, File out) throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("search_name,matched_file,vpx_path,highscore_type,evidence");
    for (AnalysisResult r : results) {
      lines.add(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
          r.searchName.replace("\"", "'"),
          r.vpxFile != null ? r.vpxFile.getName().replace("\"", "'") : "",
          r.vpxFile != null ? r.vpxFile.getAbsolutePath().replace("\"", "'") : "",
          r.type,
          r.evidence.replace("\"", "'").replace("\n", " ")));
    }
    Files.write(out.toPath(), lines);
    LOG.info("Report written to {}", out.getAbsolutePath());
  }

  private static String truncate(String s, int max) {
    if (s == null) return "";
    return s.length() > max ? s.substring(0, max - 1) + "…" : s;
  }

  // ── Data types ───────────────────────────────────────────────────────────────

  enum HighscoreType {
    NVRam, VPReg, Ini, EM, UNKNOWN, NOT_FOUND
  }

  static class AnalysisResult {
    final String searchName;   // name from the input list
    final File vpxFile;        // matched file (null if not found)
    final HighscoreType type;
    final String evidence;

    AnalysisResult(String searchName, File vpxFile, HighscoreType type, String evidence) {
      this.searchName = searchName;
      this.vpxFile = vpxFile;
      this.type = type;
      this.evidence = evidence != null ? evidence : "";
    }
  }
}
