package de.mephisto.vpin.server.nvrams.parser;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.nvram.NvRamOutputToScoreTextConverter;
import de.mephisto.vpin.server.pinemhi.PINemHiService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 */
public class NVRamToolDecoder {
  private final static Logger LOG = LoggerFactory.getLogger(NVRamParser.class);

  private static final int MAX_NUMBER_BYTES = 16;

  private static ScoringDB scoringDB = ScoringDB.load();

  private static final String[] LABELS = {"First Place", "Second Place", "Third Place", "Fourth Place",
    "Fifth Place", "Sixth Place", "Seventh Place", "Eighth Place"
  };
  private static final String[] SHORT_LABELS = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th" };


  public static void main(String[] args) throws Exception {
    NVRamToolDecoder decoder = new NVRamToolDecoder();
    decoder.checkRom("alpok_b6");
  }

  private void checkRom(String rom) throws Exception {
    LOG.info("Decoding {}...", rom);

    boolean useHexForPosition = true;

    File mainFolder = new File("./testsystem/vPinball/VisualPinball/VPinMAME/nvram/");

    // other nvram folders for validation of scores
    File[] testFolders = new File[] { 
      new File("C:/Github/py-pinmame-nvmaps/test/nvram"),
      new File("C:/Github/vpin-studio/resources/nvrams")    // resetted nvrams
    };

    // load pinhemi and parse scores
    File entry = new File(mainFolder, rom + ".nv");
    List<Score> scores = getScoresFromPinemhi(entry, rom, true);
    if (scores.isEmpty()) {
      LOG.warn("Found empty highscore for nvram {} !!!", entry.getAbsolutePath());
      return;
    }
    // cache of parsed pinemhi scores (nvram > scores)
    Map<File, List<Score>> cacheScores = new HashMap<>();

    //-------------------------------------------

    // Load nvram file and parse it
    byte[] bytes = Files.readAllBytes(entry.toPath());
    // an array to mark bytes that are consumed
    boolean[] used = new boolean[bytes.length];

    LinkedHashMap<Score, SearchResult> selectedScores = new LinkedHashMap<>();

    for (int s = 0; s < scores.size(); s++) {
      final Score sc = scores.get(s);
      final int scPos = s;

      LOG.info("...checking score \"{}\", position {}", sc, scPos);

      // search using INITIALS followed by SCORE pattern
      SearchResult result = search(bytes, sc.getPlayerInitials(), sc.getScore(), MAX_NUMBER_BYTES);
      
      // not found try not contiguous
      if (result == null) {
        // search all position for initials and scores
        List<Integer> initials = searchString(bytes, sc.getPlayerInitials());
        List<SearchResult> positions = searchNumber(bytes, sc.getScore(), -1, MAX_NUMBER_BYTES);
        // remove positions that conflict with previously selected SearchResult
        CollectionUtils.filter(positions, p -> {
          for (int i = 0; i < p.scoreLength; i++) {
            if (used[p.scorePosition + i]) {
              return false;
            }
          }
          return true;
        });

        result = findOrContinue(initials, positions, () -> {
          // check in alternative nvrams
          for (File testFolder : testFolders) {
            File altentry = new File(testFolder, rom + ".nv");
            LOG.warn("Several positions, check with alternative nvrams {}...", altentry.getAbsolutePath());

            // parse and cache for alternative nvrams
            List<Score> altscores = cacheScores.get(altentry);
            if (altscores == null) {
              altscores = getScoresFromPinemhi(altentry, rom, false);
              cacheScores.put(altentry, altscores);
            }
            // check nvrams are compatible, else ignore
            if (altscores.size() == scores.size()) {
              // get the associated scores in this alternate nvram
              Score altsc = altscores.get(scPos);
              // Load alternate nvram file
              try {
                byte[] altbytes = Files.readAllBytes(altentry.toPath());

                // eliminate in initials and positions the ones that are not common
                for (Iterator<Integer> it = initials.iterator(); it.hasNext();) {
                  Integer pos = it.next();
                  if (!StringUtils.equals(decodeString(altbytes, pos, altsc.getPlayerInitials().length()), altsc.getPlayerInitials())) {
                    it.remove();
                  }
                }
                for (Iterator<SearchResult> it = positions.iterator(); it.hasNext();) {
                  SearchResult pos = it.next();
                  if (decodeBCD(altbytes, pos.scorePosition, pos.scoreLength) != altsc.getScore()) {
                    it.remove();
                  }
                }
              }
              catch (IOException ioe) {
                LOG.warn("error while reading nvram {}, ignored and continue...", altentry.getAbsolutePath());
              }
            }
            else {
              LOG.warn("nvram {} has different number of scores {} compared to {}, ignored and continue...", altentry.getAbsolutePath(), altscores.size(), scores.size());
            }
          }
          //-------------------------------------------
          return findOrContinue(initials, positions, () -> {
            LOG.warn(">>>>>>>> still different positions so return first one...");
            SearchResult res = positions.get(0);
            if (initials.size() > 0) {
              res.initialPosition = initials.get(0);
            }
            return res;
          });
        });
      }
      if (result != null) {
        selectedScores.put(sc, result);
        if (result.initialPosition >= 0) {
          for (int i = 0; i < 3; i++) {
            used[result.initialPosition + i] = true;
          }
        }
        for (int i = 0; i < result.scoreLength; i++) {
          used[result.scorePosition + i] = true;
        }
      }
      else {
        LOG.warn("Score not found: {}\n", sc);
      }
    }


    //-----------------------
    // now generate the map

    JsonObject map = new JsonObject();

    JsonArray notes = new JsonArray();
    notes.add("Compiled from brute force scan of scores by vpin-studio.");
    map.add("_notes", notes);

    map.addProperty("_fileformat", 0.8);

    JsonArray highScores = new JsonArray();
    
    int index = 1;
    for (Entry<Score, SearchResult> sr : selectedScores.entrySet()) {
      Score sc = sr.getKey();
      SearchResult result = sr.getValue();

      String label = sc.getLabel();
      String shortLabel;
      if (label != null) {
        label = capitalize(label);
        shortLabel = abbreviate(label);
      }
      else {
        label = index < LABELS.length ? LABELS[index] : "Position " + index;
        shortLabel = index < SHORT_LABELS.length ? SHORT_LABELS[index] : index + "th";
        index++;
      }

      JsonObject score = new JsonObject();
      score.addProperty("label", label);
      score.addProperty("short_label", shortLabel);
      score.add("initials", createMapping(result.initialPosition, 3, "ch", useHexForPosition));
      score.add("score", createMapping(result.scorePosition, result.scoreLength, "bcd", useHexForPosition));
      highScores.add(score);
    }
    map.add("high_scores", highScores);

    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setIndent("  ");
    jsonWriter.setLenient(true);
    Streams.write(map, jsonWriter);
    System.out.println(stringWriter.toString());
  }

  //------------------------------------------------

  private SearchResult findOrContinue(List<Integer> initials, List<SearchResult> positions, Supplier<SearchResult> continueChain) {
    if (positions.size() == 0) {
      return null;
    }
    else if (positions.size() == 1) {
      SearchResult result = positions.get(0);
      if (initials.size() == 0) {
        LOG.warn(">>>>>>> No position for initials ???");
      } 
      else if (initials.size() == 1) {
        result.initialPosition = initials.get(0);
      }
      else {
        LOG.warn(">>>>>>> Several initials positions for score, use first one");
        result.initialPosition = initials.get(0);
      }
      return result;
    }
    else {
      return continueChain.get();
    }
  }

  private @NotNull List<Score> getScoresFromPinemhi(File nvramFile, String rom, boolean displayResult) {
    if (!nvramFile.exists()) {
      return Collections.emptyList();
    }

    // load pinhemi and parse scores
    PINemHiService.adjustVPPathForEmulator(nvramFile.getParentFile(), PINemHiService.getPinemhiIni(), true);
    try {
      String raw = NvRamOutputToScoreTextConverter.convertNvRamTextToMachineReadable(PINemHiService.getPinemhiExe(), nvramFile);
      if (displayResult) {
        LOG.info(raw);
      }
      return parseRaw(raw);
    } 
    catch (Exception e) {
      LOG.error("Cannot getscores from pinhemi", e);
      return Collections.emptyList();
    }
  }

  private List<Score> parseRaw(String raw) {
    List<Score> scores = new ArrayList<>();
    List<String> lines = Arrays.asList(raw.split("\\n"));
    if (lines.isEmpty()) {
      return scores;
    }

    List<String> titles = scoringDB.getHighscoreTitles();

    String currentTitle = null;
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);

      //Check if there is a highscore title, in that case...
      if (titles.contains(line.trim()) && ((i + 1) < titles.size())) {
        if (i + 1 >= lines.size()) {
          continue;
        }

        String scoreLine = lines.get(i + 1);
        int idx = isScoreLine(scoreLine);

        //the next line could be a raw score without a positions
        if (idx < 0) {
          Score score = createTitledScore(line.trim(), scoreLine);
          if (score != null) {
            scores.add(score);
          }
          //do not increase index, as we still search for #1
          continue;
        }
      }

      int idx = isScoreLine(line);
      if (idx > 0) {
        Score score = createScore(currentTitle, line);
        if (score != null) {
          score.setPosition(idx);
          scores.add(score);
        }
      }
      else if (StringUtils.isEmpty(currentTitle)) {
        currentTitle = line;
      }

      if (StringUtils.isEmpty(line)) {
        // restart a possible new sequence
        currentTitle = null;
      }
    }

    return scores;
  }


  private static Pattern patternRank = Pattern.compile("^((\\d\\d?\\))|(#\\d\\d?)|(\\d\\d?#)|(\\d\\d?\\.:))[\\s \u00a0\u202f\ufffd\u00ff]");

  /**
   * Return the index if it is a score line, else -1 
   */
  public int isScoreLine(String line) {
    Matcher m = patternRank.matcher(line);
    if (m.find()) {
      String idx = m.group(1);
      idx = StringUtils.remove(idx, ')');
      idx = StringUtils.remove(idx, '#');
      idx = StringUtils.remove(idx, ".:");
      return Integer.parseInt(idx);
    }
    return -1;
  }

  /**
   * Parses score that are shown right behind a possible title.
   * These scores do not have a leading position number.
   */
  @Nullable
  protected Score createTitledScore(@NonNull String title, @NonNull String line) {
    String initials = "???";
    if (line.trim().length() >= 3) {
      initials = line.trim().substring(0, 3);

      String scoreString = line.substring(4).trim();
      String cleanScore = ScoreFormatUtil.cleanScore(scoreString);
      long scoreValue = Long.parseLong(cleanScore);
      if (scoreValue == -1) {
        return null;
      }

      Score sc = new Score(null, -1, initials, null, scoreString, scoreValue, 1);
      sc.setLabel(title);
      return sc;
    }

    String cleanScore = ScoreFormatUtil.cleanScore(line.trim());
    long scoreValue = Long.parseLong(cleanScore);
    if (scoreValue == -1) {
      return null;
    }

    return new Score(null, -1, initials, null, line.trim(), scoreValue, 1);
  }

  private static Pattern pattern = Pattern.compile("\\d?\\d?([., ?\u00a0\u202f\ufffd\u00ff]?\\d\\d\\d)*$");

  @Nullable
  public Score createScore(@Nullable String title, @NonNull String line) {
    if (line.indexOf(" ") < -1) {
      return null;
    }
    line = StringUtils.substringAfter(line, " ").trim();
    Matcher m = pattern.matcher(line);
    if (m.find()) {
      int p = m.start();
      String score = line.substring(p);
      String cleanScore = ScoreFormatUtil.cleanScore(score.trim());
      long v = Long.parseLong(cleanScore);
      if (v == -1) {
        return null;
      }
      String initials = p > 0 ? line.substring(0, p - 1).trim() : "";
      initials = ScoreFormatUtil.cleanInitials(initials);
      Score sc = new Score(null, -1, initials, null, score, v, -1);
      if (StringUtils.isNotEmpty(title)) {
        sc.setLabel(title);
      }
      return sc;
    }
    return null;
  }


  private JsonObject createMapping(int position, int length, String encoding, boolean useHexForPosition) {
    JsonObject mapping = new JsonObject();
    if (useHexForPosition) {
      mapping.addProperty("start", "0x" + Integer.toHexString(position).toUpperCase());
    } else {
      mapping.addProperty("start", position);
    }
    mapping.addProperty("encoding", encoding);
    mapping.addProperty("length", length);
    return mapping;
  }

  //--------------------------------------------------

  /**
   * Find all positions of the searchString in data
   */
  public List<Integer> searchString(byte[] data, String searchString) {
    byte[] pattern = searchString.getBytes(StandardCharsets.UTF_8);
    List<Integer> positions = new ArrayList<>();
    int matchPos = -1;
    while ((matchPos = findPattern(data, pattern, matchPos + 1)) != -1) {
      positions.add(matchPos);
    }
    return positions;
  }

  /**
   * Search a number in data
   * @param number The number to find
   * @param nbBytes optional length,, -1 to let the system calculate it
   * @param maxNumberBytes Max number of bytes for encoding when nbBytes is -1
   */
  public List<SearchResult> searchNumber(byte[] data, long number, int nbBytes, int maxNumberBytes) {
    List<SearchResult> positions = new ArrayList<>();
    for (int matchPos = 0; matchPos < data.length; matchPos++) {
      if (nbBytes < 0) {
        int len = isBCDNumber(data, number, matchPos, maxNumberBytes);
        if (len >= 0) {
          positions.add(new SearchResult(-1, matchPos, len));
        }
      } else {
        if (decodeBCD(data, matchPos, nbBytes) == number) {
          positions.add(new SearchResult(-1, matchPos, nbBytes));
        }
      }
    }
    return positions;
  }

  /**
   * Searches for a string followed by a BCD-encoded number in a byte array.
   * Since the number of encoding bytes is unknown, all sizes from 1 to maxNumberBytes are tried.
   *
   * @param data            The byte array to search in
   * @param searchString    The string to locate
   * @param targetNumber    The decimal number expected after the string
   * @param maxNumberBytes  Maximum number of bytes to try for the BCD-encoded number
   * @return                The search result if found, null otherwise
   */
  public SearchResult search(byte[] data, String searchString, long targetNumber, int maxNumberBytes) {
    byte[] pattern = searchString.getBytes(StandardCharsets.UTF_8);
    int startPos = 0;
    while (true) {
      int matchPos = findPattern(data, pattern, startPos);
      if (matchPos == -1) {
        return null; // String not found
      }

      int afterString = matchPos + pattern.length;
      int len = isBCDNumber(data, targetNumber, afterString, maxNumberBytes);
      if (len >= 0) {
        return new SearchResult(matchPos, afterString, len);
      }
      startPos = matchPos + 1;
    }
  }

  /** Find if number is stored at position and if yes, return the length of encoding else -1 */
  public int isBCDNumber(byte[] data, long number, int position, int maxNumberBytes) {
    // Try all possible byte lengths for the BCD-encoded number
    for (int len = 1; len <= maxNumberBytes; len++) {
      long decoded = decodeBCD(data, position, len);
      if (decoded < 0) {
        break;
      }
      if (decoded == number) {
        return len;
      }
    }
    return -1;
  }


  private String decodeString(byte[] data, int offset, int length) {
    if (offset + length > data.length) {
      return null;
    }
    byte[] bytes = new byte[length];
    System.arraycopy(data, offset, bytes, 0, length);
    return new String(bytes);
  }

  /**
   * Decodes a BCD-encoded number from raw bytes at the given offset.
   * Each byte holds 2 decimal digits (one per nibble), big-endian.
   * If the number of digits is odd, the high nibble of the first byte is 0 (padding).
   *
   * Examples:
   *   12345 -> 0x01 0x23 0x45  (3 bytes, leading nibble padded with 0)
   *   1234  -> 0x12 0x34     (2 bytes)
   *
   * @param data  The byte array
   * @param offset  Start position
   * @param length  Number of bytes to read
   * @return    The decoded decimal value, or -1 if an invalid BCD nibble is found
   */
  private long decodeBCD(byte[] data, int offset, int length) {
    if (offset + length > data.length) {
      return -1;
    }

    long value = 0;
    for (int i = 0; i < length; i++) {
      int b = data[offset + i] & 0xFF;
      int highNibble = (b >> 4) & 0x0F;
      int lowNibble  =  b     & 0x0F;

      // A nibble > 9 is invalid in BCD
      if (highNibble > 9 || lowNibble > 9) return -1;

      value = value * 10 + highNibble;
      value = value * 10 + lowNibble;
    }
    return value;
  }

  /**
   * Searches for a byte pattern in a byte array starting from a given position.
   *
   * @param data    The byte array to search in
   * @param pattern   The byte pattern to find
   * @param startPos  The index to start searching from
   * @return      The index of the first match, or -1 if not found
   */
  private static int findPattern(byte[] data, byte[] pattern, int startPos) {
    if (pattern.length == 0) return startPos;
    if (data.length < pattern.length) return -1;

    outer:
    for (int i = startPos; i <= data.length - pattern.length; i++) {
      for (int j = 0; j < pattern.length; j++) {
        if (data[i + j] != pattern[j]) {
          continue outer;
        }
      }
      return i;
    }
    return -1;
  }

  //------------------------------------

  public static String capitalize(String input) {
    return Arrays.stream(input.trim().split("\\s+"))
                 .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                 .collect(Collectors.joining(" "));
  }

  public static String abbreviate(String input) {
    return Arrays.stream(input.trim().split("\\s+"))
                 .map(word -> String.valueOf(word.charAt(0)))
                 .collect(Collectors.joining())
                 .toUpperCase();
  }

  /**
   * Search result containing position and match details
   */
  public static class SearchResult {
    public int initialPosition = -1;        // Start position of the initial in the array
    public int scorePosition = -1;          // Position of the score in the array
    public int scoreLength;                 // Number of bytes used to encode the score

    public SearchResult(int initialPosition, int numberOffset, int numberLength) {
      this.initialPosition = initialPosition;
      this.scorePosition = numberOffset;
      this.scoreLength = numberLength;
    }

    @Override
    public String toString() {
      return initialPosition < 0 ? 
        String.format("%d (%d)", scorePosition, scoreLength) :
        String.format("%d, %d (%d)", initialPosition, scorePosition, scoreLength);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof SearchResult) {
        SearchResult that = (SearchResult) o;
        return this.initialPosition == that.initialPosition && 
              this.scorePosition == that.scorePosition &&
              this.scoreLength == that.scoreLength;
      }
      return false;
    }
  }
}
