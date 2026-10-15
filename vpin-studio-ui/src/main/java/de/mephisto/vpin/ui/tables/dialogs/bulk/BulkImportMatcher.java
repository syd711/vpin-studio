package de.mephisto.vpin.ui.tables.dialogs.bulk;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Fuzzy matching of files to tables and detection of the target screen from file or folder names.
 */
public class BulkImportMatcher {

  public enum MatchMode {
    FILENAME, FOLDER, FILENAME_AND_FOLDER
  }

  public static class Match {
    public final GameRepresentation game;
    public final double score;
    public final String source;

    Match(GameRepresentation game, double score, String source) {
      this.game = game;
      this.score = score;
      this.source = source;
    }
  }

  private static final Pattern NON_ALNUM = Pattern.compile("[^\\p{L}\\p{N}]+");
  private static final Pattern VERSION = Pattern.compile("^v?\\d+(\\.\\d+)*[a-z]?$");
  private static final Pattern CAMEL_CASE = Pattern.compile("(?<=\\p{Ll})(?=\\p{Lu})");
  private static final Set<String> NOISE = new HashSet<>(Arrays.asList(
      "the", "a", "an", "and", "of", "vpx", "vpw", "fp", "mod", "vr", "vpu", "vps", "table", "instruction", "instructions",
      "card", "cards", "help", "rules", "rule", "png", "jpg", "pdf", "mp4", "mp3", "wheel", "backglass", "dmd", "topper",
      "playfield", "loading", "apron", "menu", "audio", "info", "bg", "pf", "b2s", "by", "sound"));

  private static final Map<String, VPinScreen> SCREEN_KEYWORDS = new LinkedHashMap<>();

  static {
    SCREEN_KEYWORDS.put("audiolaunch", VPinScreen.AudioLaunch);
    SCREEN_KEYWORDS.put("backglass", VPinScreen.BackGlass);
    SCREEN_KEYWORDS.put("b2s", VPinScreen.BackGlass);
    SCREEN_KEYWORDS.put("bg", VPinScreen.BackGlass);
    SCREEN_KEYWORDS.put("wheel", VPinScreen.Wheel);
    SCREEN_KEYWORDS.put("logo", VPinScreen.Logo);
    SCREEN_KEYWORDS.put("dmd", VPinScreen.DMD);
    SCREEN_KEYWORDS.put("topper", VPinScreen.Topper);
    SCREEN_KEYWORDS.put("playfield", VPinScreen.PlayField);
    SCREEN_KEYWORDS.put("pf", VPinScreen.PlayField);
    SCREEN_KEYWORDS.put("loading", VPinScreen.Loading);
    SCREEN_KEYWORDS.put("apron", VPinScreen.Menu);
    SCREEN_KEYWORDS.put("menu", VPinScreen.Menu);
    SCREEN_KEYWORDS.put("help", VPinScreen.GameHelp);
    SCREEN_KEYWORDS.put("instruction", VPinScreen.GameHelp);
    SCREEN_KEYWORDS.put("instructions", VPinScreen.GameHelp);
    SCREEN_KEYWORDS.put("rules", VPinScreen.GameHelp);
    SCREEN_KEYWORDS.put("gamehelp", VPinScreen.GameHelp);
    SCREEN_KEYWORDS.put("info", VPinScreen.GameInfo);
    SCREEN_KEYWORDS.put("gameinfo", VPinScreen.GameInfo);
    SCREEN_KEYWORDS.put("other2", VPinScreen.Other2);
    SCREEN_KEYWORDS.put("gameselect", VPinScreen.Other2);
    SCREEN_KEYWORDS.put("audio", VPinScreen.Audio);
    SCREEN_KEYWORDS.put("sound", VPinScreen.Audio);
  }

  private final List<GameRepresentation> games;
  private final Map<GameRepresentation, Set<String>> gameTokens = new HashMap<>();

  public BulkImportMatcher(List<GameRepresentation> games) {
    this.games = games;
    for (GameRepresentation game : games) {
      Set<String> tokens = new LinkedHashSet<>(tokenize(game.getGameDisplayName()));
      if (tokens.size() < 2) {
        tokens.addAll(tokenize(FilenameUtils.getBaseName(game.getGameFileName())));
      }
      gameTokens.put(game, tokens);
    }
  }

  public static Set<String> tokenize(@Nullable String s) {
    Set<String> result = new LinkedHashSet<>();
    if (StringUtils.isEmpty(s)) {
      return result;
    }
    // split camel case: "AddamsFamily" -> "Addams Family"
    String prepared = CAMEL_CASE.matcher(s).replaceAll(" ");
    for (String t : NON_ALNUM.split(prepared.toLowerCase())) {
      if (t.isEmpty() || NOISE.contains(t) || VERSION.matcher(t).matches()) {
        continue;
      }
      result.add(t);
    }
    return result;
  }

  /**
   * Returns the screen implied by keywords in the name, or null.
   */
  @Nullable
  public static VPinScreen detectScreen(@Nullable String name) {
    if (StringUtils.isEmpty(name)) {
      return null;
    }
    String prepared = CAMEL_CASE.matcher(name).replaceAll(" ");
    for (String t : NON_ALNUM.split(prepared.toLowerCase())) {
      VPinScreen screen = SCREEN_KEYWORDS.get(t);
      if (screen != null) {
        return screen;
      }
    }
    return null;
  }

  /**
   * Screen for a file: filename keywords win over folder keywords, audio extensions imply Audio.
   */
  @Nullable
  public static VPinScreen detectScreen(File file, @Nullable File root) {
    VPinScreen screen = detectScreen(FilenameUtils.getBaseName(file.getName()));
    if (screen == null) {
      File parent = file.getParentFile();
      while (screen == null && parent != null) {
        if (root != null && parent.equals(root.getParentFile())) {
          break;
        }
        screen = detectScreen(parent.getName());
        if (root != null && parent.equals(root)) {
          break;
        }
        parent = parent.getParentFile();
      }
    }
    if (screen == null) {
      String ext = FilenameUtils.getExtension(file.getName()).toLowerCase();
      if (ext.equals("mp3") || ext.equals("wav") || ext.equals("ogg")) {
        screen = VPinScreen.Audio;
      }
    }
    return screen;
  }

  public List<Match> suggest(File file, @Nullable File root, MatchMode mode, int limit) {
    Set<String> fileTokens = new LinkedHashSet<>();
    Set<String> folderTokens = new LinkedHashSet<>();
    String fileName = null;
    String folderName = null;
    if (mode != MatchMode.FOLDER) {
      fileName = FilenameUtils.getBaseName(file.getName());
      fileTokens.addAll(tokenize(fileName));
    }
    if (mode != MatchMode.FILENAME) {
      File parent = file.getParentFile();
      // only folders below the chosen root carry table information
      if (parent != null && (root == null || !parent.equals(root))) {
        folderName = parent.getName();
        folderTokens.addAll(tokenize(folderName));
      }
    }

    List<Match> matches = new ArrayList<>();
    for (GameRepresentation game : games) {
      // a name equal to the VPS table id is an exact match
      String vpsTableId = game.getExtTableId();
      if (StringUtils.isNotBlank(vpsTableId)
          && (vpsTableId.equalsIgnoreCase(StringUtils.trim(fileName)) || vpsTableId.equalsIgnoreCase(StringUtils.trim(folderName)))) {
        matches.add(new Match(game, 1, "vps"));
        continue;
      }

      Set<String> gt = gameTokens.get(game);
      double fileScore = score(fileTokens, gt);
      double folderScore = score(folderTokens, gt);
      double best = Math.max(fileScore, folderScore);
      if (best <= 0) {
        continue;
      }
      String source = folderScore > fileScore ? "folder" : "filename";
      matches.add(new Match(game, best, source));
    }
    matches.sort((a, b) -> Double.compare(b.score, a.score));
    return matches.size() > limit ? new ArrayList<>(matches.subList(0, limit)) : matches;
  }

  /**
   * Symmetric token overlap: every table token should be found, and few file tokens should be left over.
   * Tokens match when equal or when one is a prefix of the other (min length 4), e.g. "addams" / "addam".
   */
  static double score(Set<String> fileTokens, Set<String> gameTokens) {
    if (fileTokens.isEmpty() || gameTokens.isEmpty()) {
      return 0;
    }
    int hits = 0;
    for (String g : gameTokens) {
      for (String f : fileTokens) {
        if (g.equals(f) || (Math.min(g.length(), f.length()) >= 4 && (g.startsWith(f) || f.startsWith(g)))) {
          hits++;
          break;
        }
      }
    }
    if (hits == 0) {
      return 0;
    }
    double recall = (double) hits / gameTokens.size();
    double precision = (double) Math.min(hits, fileTokens.size()) / fileTokens.size();
    return 2 * recall * precision / (recall + precision);
  }
}
