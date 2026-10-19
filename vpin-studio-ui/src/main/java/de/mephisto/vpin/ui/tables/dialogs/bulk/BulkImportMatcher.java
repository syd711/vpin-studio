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
  // version numbers and years, plain numbers are kept as they are part of names like "F-14" or "Batman 66"
  private static final Pattern VERSION = Pattern.compile("^(v\\d+(\\.\\d+)*[a-z]?|\\d+(\\.\\d+)+[a-z]?|(19|20)\\d{2})$");
  private static final Pattern APOSTROPHE = Pattern.compile("['’`]");
  private static final Pattern BRACKETS = Pattern.compile("[(\\[][^)\\]]*[)\\]]");
  private static final Pattern CAMEL_CASE = Pattern.compile("(?<=\\p{Ll})(?=\\p{Lu})");
  private static final Set<String> NOISE = new HashSet<>(Arrays.asList(
      "the", "a", "an", "and", "of", "vpx", "vpw", "fp", "mod", "vr", "vpu", "vps", "table", "instruction", "instructions",
      "card", "cards", "help", "rules", "rule", "png", "jpg", "pdf", "mp4", "mp3", "wheel", "backglass", "dmd", "topper",
      "playfield", "loading", "apron", "menu", "audio", "info", "bg", "pf", "b2s", "by", "sound"));
  // noise words that are often written together with the table name, e.g. "aceofspeed" or "theaddamsfamily"
  private static final List<String> FILLERS = Arrays.asList("the", "and", "of", "an", "a");
  private static final int MIN_UNALIGNED_LENGTH = 4;

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
  private final Map<GameRepresentation, List<Set<String>>> gameTokens = new HashMap<>();

  public BulkImportMatcher(List<GameRepresentation> games) {
    this.games = games;
    for (GameRepresentation game : games) {
      // the display name and the file name are matched separately, the better one counts
      List<Set<String>> variants = new ArrayList<>();
      addVariant(variants, game.getGameDisplayName());
      addVariant(variants, FilenameUtils.getBaseName(game.getGameFileName()));
      gameTokens.put(game, variants);
    }
  }

  /**
   * Adds the tokens of a table name without bracket parts like "(Williams 1992)", which only dilute the score.
   */
  private static void addVariant(List<Set<String>> variants, @Nullable String name) {
    if (StringUtils.isEmpty(name)) {
      return;
    }
    Set<String> tokens = tokenize(BRACKETS.matcher(name).replaceAll(" "));
    if (tokens.isEmpty()) {
      tokens = tokenize(name);
    }
    if (!tokens.isEmpty() && !variants.contains(tokens)) {
      variants.add(tokens);
    }
  }

  public static Set<String> tokenize(@Nullable String s) {
    Set<String> result = new LinkedHashSet<>();
    if (StringUtils.isEmpty(s)) {
      return result;
    }
    // split camel case: "AddamsFamily" -> "Addams Family"
    // "Ted's" -> "teds"
    String prepared = CAMEL_CASE.matcher(APOSTROPHE.matcher(s).replaceAll("")).replaceAll(" ");
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

      double fileScore = 0;
      double folderScore = 0;
      for (Set<String> gt : gameTokens.get(game)) {
        fileScore = Math.max(fileScore, score(fileTokens, gt));
        folderScore = Math.max(folderScore, score(folderTokens, gt));
      }
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
   * Symmetric overlap of the names: the share of the table name found in the file name, combined with the share of
   * the file name explained by the table name. File words are split into table words, so names written together
   * like "attackfrommars" match "Attack from Mars". The file words are also tried written together, for "fun_house"
   * against "Funhouse".
   */
  static double score(Set<String> fileTokens, Set<String> gameTokens) {
    if (fileTokens.isEmpty() || gameTokens.isEmpty()) {
      return 0;
    }
    double result = coverageScore(fileTokens, gameTokens);
    if (fileTokens.size() > 1) {
      result = Math.max(result, coverageScore(Collections.singleton(String.join("", fileTokens)), gameTokens));
    }
    return result;
  }

  private static double coverageScore(Set<String> fileTokens, Set<String> gameTokens) {
    Map<String, Integer> credited = new HashMap<>();
    int fileChars = 0;
    int covered = 0;
    for (String f : fileTokens) {
      fileChars += f.length();
      int p = 0;
      while (p < f.length()) {
        String token = longestTokenAt(f, p, gameTokens);
        if (token != null) {
          covered += token.length();
          credited.merge(token, token.length(), Math::max);
          p += token.length();
          continue;
        }

        // abbreviated end of the word: "addam" for "addams"
        String rest = f.substring(p);
        if (rest.length() >= MIN_UNALIGNED_LENGTH) {
          Optional<String> abbreviated = gameTokens.stream().filter(g -> g.startsWith(rest)).findFirst();
          if (abbreviated.isPresent()) {
            covered += rest.length();
            credited.merge(abbreviated.get(), rest.length(), Math::max);
            break;
          }
        }

        String filler = longestTokenAt(f, p, FILLERS);
        if (filler != null) {
          covered += filler.length(); // explains the file name, but is no table word found
          p += filler.length();
          continue;
        }

        // nothing starts here, continue with the next longer table word inside the word
        p = nextUnalignedMatch(f, p + 1, gameTokens);
      }
    }

    if (credited.isEmpty()) {
      return 0;
    }
    int gameChars = gameTokens.stream().mapToInt(String::length).sum();
    double recall = (double) credited.values().stream().mapToInt(Integer::intValue).sum() / gameChars;
    double precision = (double) covered / fileChars;
    return 2 * recall * precision / (recall + precision);
  }

  @Nullable
  private static String longestTokenAt(String word, int position, Collection<String> tokens) {
    String best = null;
    for (String t : tokens) {
      if (word.startsWith(t, position) && (best == null || t.length() > best.length())) {
        best = t;
      }
    }
    return best;
  }

  private static int nextUnalignedMatch(String word, int from, Set<String> tokens) {
    for (int p = from; p < word.length(); p++) {
      for (String t : tokens) {
        if (t.length() >= MIN_UNALIGNED_LENGTH && word.startsWith(t, p)) {
          return p;
        }
      }
    }
    return word.length();
  }
}
