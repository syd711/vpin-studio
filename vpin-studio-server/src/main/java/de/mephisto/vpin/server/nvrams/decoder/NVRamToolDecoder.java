package de.mephisto.vpin.server.nvrams.decoder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.ScoreListFactory;
import de.mephisto.vpin.server.highscores.parsing.nvram.NvRamOutputToScoreTextConverter;
import de.mephisto.vpin.server.nvrams.NVRamMapService;
import de.mephisto.vpin.server.nvrams.decoder.SimpleLogger.LEVEL;
import de.mephisto.vpin.server.nvrams.map.ChecksumMapping;
import de.mephisto.vpin.server.nvrams.map.NVRamMap;
import de.mephisto.vpin.server.nvrams.map.NVRamScore;
import de.mephisto.vpin.server.nvrams.map.SparseMemory;
import de.mephisto.vpin.server.nvrams.tools.NVRamToolDump;
import de.mephisto.vpin.server.nvrams.tools.NVRamToolHexDump;
import de.mephisto.vpin.server.pinemhi.PINemHiService;

/**
 * A tool that "guess" a nvram map by taking a nvram file, the result of the pinemhi decoding 
 * and guess the map by searching scores and initials in the nvram
 */
public class NVRamToolDecoder {
  private final static SimpleCaptureLogger LOG = //LoggerFactory.getLogger(NVRamToolDecoder.class);
    new SimpleCaptureLogger(NVRamToolDecoder.class, LEVEL.INFO);

  // The max number of bytes used for BCD encoding, 8 means 16 digits..., generally it is 5
  private static final int MAX_LENGTH = 8;

  private static ScoringDB scoringDB;
  private static VPS vps;

  private NVRamMapService parser = new NVRamMapService();


  File mainFolder = new File("C:/Visual Pinball/VPinMAME/nvram");

  static {
    scoringDB = ScoringDB.load();
    vps = new VPS();
    vps.reload();
  }

  public static void main(String[] args) throws Exception {
    NVRamToolDecoder decoder = new NVRamToolDecoder();
    //decoder.decodeAll(); 
    
    decoder.decodeTest("agent777", null);
    //decoder.decodeTest("godzilla", "Godzilla (Sega 1998)"); 
    //decoder.decodeTest("afm_113b", "Attack from Mars (Bally 1995)"); 
    //decoder.decodeTest("bdk_294", null);
  }

  private void decodeAll() throws Exception {

    Files.list(mainFolder.toPath()).forEach(p -> {
      String rom = FilenameUtils.getBaseName(p.getFileName().toString());
      try {
        if (!rom.endsWith(".nv")) {
          decode(p.toFile(), rom, null);
          LOG.warn("----------------------");
        }
      }
      catch (Exception e) {
        LOG.error("!!!!! Cannot process {} : {}", rom, e.getMessage());
      }
    });
  }

  private void decodeTest(String rom, String tablename) throws Exception {
    File entry = new File(mainFolder, rom + ".nv");
    decode(entry, rom, tablename);
    test(entry, rom);
  }

  private void test(String rom, String tablename) throws Exception {
    File entry = new File(mainFolder, rom + ".nv");

    getScoresFromPinemhi(entry, rom, true);
    test(entry, rom);
  }


  private void test(File entry, String rom) throws IOException {
    // now do some tests
    File mapfile = new File(NVRamToolMapGenerator.DECODED_ROOT, rom + ".map.json");
    NVRamMap map = parser.getLocalMap(mapfile, rom);

    if (map != null) {
      byte[] bytes = Files.readAllBytes(entry.toPath());
      SparseMemory memory = parser.getMemory(map, bytes);
      
      NVRamToolHexDump hexdump = new NVRamToolHexDump();
      String hex = hexdump.hexDump(map, memory, Locale.ENGLISH);
      LOG.info("\n\n==================================");
      LOG.info(hex);
      LOG.info("==================================");

      LOG.info("Dump of nv {} is :", entry.getAbsolutePath());
      NVRamToolDump dump = new NVRamToolDump();
      String txt = dump.dump(map, memory, Locale.ENGLISH, true);
      LOG.info(txt);

      if (!map.getHighScores().isEmpty()) {
        NVRamScore score = map.getHighScores().get(0);
        score.reset(777777);
      }

      for (ChecksumMapping c : map.getChecksumEntries()) {
        
      }

    }
  }


  private void decode(File entry, String rom, String tablename) throws Exception {
    // new rom to be decoded
    LOG.reset();

    LOG.warn("Decoding {}...", rom);

    String mapPath = parser.mapPathForRom(rom);
    if (mapPath != null) {
      LOG.warn("\nNVRam Map already exists: {} !", mapPath);
      return;
    }

    // First try to determine associated VpsTable
    VpsTable table = null;
    if (StringUtils.isNotEmpty(tablename)) {
      List<VpsTable> tables = vps.getTables().stream().filter(t -> StringUtils.containsIgnoreCase(t.getDisplayName(), tablename)).collect(Collectors.toList());
      if (tables.size() == 1) {
        table = tables.get(0);
          LOG.warn("Table found by name {}", table.getDisplayName());
      }
    }
    // table  not found by name, search in all tables by rom
    if (table == null) {
      List<VpsTable> tables = vps.getTables().stream()
        .filter(t -> t.getRomFiles() != null && t.getRomFiles().stream().anyMatch(r -> StringUtils.equalsIgnoreCase(rom, r.getVersion())))
        .collect(Collectors.toList());
      if (tables.size() == 1) {
        table = tables.get(0);
          LOG.warn("Table found by rom {}", table.getDisplayName());
      }
      else if (tables.size() > 1) {
          LOG.warn("Several tables found for rom {}, please choose one !", rom);
        for (VpsTable t : tables) {
            LOG.warn("=> {}", t.getDisplayName());
        }
        return;
      }
      else {
          LOG.warn("No Table associated with rom {} in VPS ??", rom);
      }
    }

    //---------------------------------

    // other nvram folders for validation of scores
    File[] testFolders = new File[] { 
      new File("./testsystem/vPinball/VisualPinball/VPinMAME/nvram/"),
      new File("C:/Github/py-pinmame-nvmaps/test/nvram"),
      new File("C:/temp/_NVRAMS/Matt"),
      new File("C:/temp/_NVRAMS/ed209"),
      new File("C:/temp/_NVRAMS/YabbaDabbaDoo"),
      new File("C:/temp/_NVRAMS/gonzonia"),
      new File("C:/temp/_NVRAMS/GerhardK"),
      new File("C:/temp/_NVRAMS/Buffdriver"),
      new File("C:/temp/_NVRAMS/BostonBuckeye"),
      new File("C:/temp/_NVRAMS/FuFu"),
      new File("C:/temp/_NVRAMS/Blap"),
      //new File("C:/Visual Pinball/VPinMAME/nvram"),         // OLE
      new File("C:/Github/vpin-studio/resources/nvrams")    // resetted nvrams
    };

    // load pinhemi and parse scores
    LOG.warn("...get scores from Pinemhi");
  
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

    // calculate score mappings, not presuming any score encoding length
    LinkedHashMap<Score, SearchResult> selectedScores = parseScores(bytes, rom, testFolders, scores, cacheScores, -1);

    NVRamToolMapGenerator generator = new NVRamToolMapGenerator();
    if (selectedScores.size() > 0) {

      LinkedHashMap<String, SearchResult> checksums = parseChecksum(bytes, selectedScores);

      // generate the map
      boolean useHexForPosition = false;
      generator.generateHighscores(rom, table, useHexForPosition, selectedScores, checksums);
    }
    generator.appendText(rom, "\n/*\n" + LOG.getText() + "\n*/\n");
  }

  //---------------------------------------------

  private LinkedHashMap<Score, SearchResult> parseScores(byte[] bytes, String rom, File[] testFolders,
      List<Score> scores, Map<File, List<Score>> cacheScores, int forcedScoreLength) {
    // an array to mark bytes that are consumed
    boolean[] used = new boolean[bytes.length];
    LinkedHashMap<Score, SearchResult> selectedScores = new LinkedHashMap<>();

    Score previousScore = null;
    SearchResult previousResult = null;

    for (int s = 0; s < scores.size(); s++) {
      final Score sc = scores.get(s);
      final int scPos = s;

      LOG.warn("...checking score \"{}\", position {}", sc, scPos);

      // search using INITIALS followed by SCORE pattern
      SearchResult result = search(bytes, sc.getPlayerInitials(), sc.getScore(), MAX_LENGTH);
      
      // not found try not contiguous
      if (result == null) {
        // search all position for initials and scores
        List<Integer> initials = searchString(bytes, sc.getPlayerInitials());
        List<SearchResult> positions = searchNumber(bytes, sc.getScore(), forcedScoreLength, MAX_LENGTH, true);
        // remove initials positions that conflict with previously selected SearchResult
        CollectionUtils.filter(initials, p -> {
          for (int i = 0; i < 3; i++) {
            if (used[p + i]) {
              return false;
            }
          }
          return true;
        });
        // remove positions that conflict with previously selected SearchResult
        CollectionUtils.filter(positions, p -> {
          for (int i = 0; i < p.scoreLength; i++) {
            if (used[p.scorePosition + i]) {
              return false;
            }
          }
          return true;
        });

        final Score _previousScore = previousScore;
        final SearchResult _previousResult = previousResult;
        result = findOrContinue(initials, positions, () -> {
          // if there is a position contiguous to the previous one for same score label, use this one
          List<Integer> filteredInitials = initials;
          if (_previousResult != null) {
            for (Integer pos : initials) {
              if (_previousResult.initialPosition + 3 == pos) {
                filteredInitials = List.of(pos);
                break;
              }
            }
            for (SearchResult pos : positions) {
              if (_previousResult.scorePosition + _previousResult.scoreLength + 1 == pos.scorePosition 
                    && StringUtils.equals(sc.getLabel(), _previousScore.getLabel())) {
                LOG.warn("  Find a contiguous score for same regions {}, use it..", sc.getLabel());
                return findOrContinue(filteredInitials, List.of(pos), null);
              }
            }
          }
          // else continue in trying to eliminate scores that have another position in alternative nvram files
          return findOrContinue(filteredInitials, positions, () -> {
            LOG.warn("  Several positions, check with alternative nvrams..");
            // check in alternative nvrams
            for (File testFolder : testFolders) {
              File altentry = new File(testFolder, rom + ".nv");
              // check that the file exists
              if (!altentry.exists()) {
                continue;
              }

              LOG.warn("    check with nvram {}...", altentry.getAbsolutePath());

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
                LOG.warn("nvram {} has different number of pinhemi scores {} compared to {}, ignored and continue...", altentry.getAbsolutePath(), altscores.size(), scores.size());
                getScoresFromPinemhi(altentry, rom, true);
              }
            }
            //-------------------------------------------
            return findOrContinue(initials, positions, () -> {
              LOG.warn("  >>>>>>>> still different positions so return first one...");
              SearchResult res = positions.get(0);
              if (initials.size() > 0) {
                res.initialPosition = initials.get(0);
              }
              return res;
            });
          });
        });
      }
      if (result != null) {
        selectedScores.put(sc, result);
        previousScore = sc;
        previousResult = result;
        // now flagged the bytes consumed by this result so that next scores don't use them
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

    // When parseScore method is called with forcedScoreLength=-1, the parser will try to guess the encoding length for each score
    // For number, when it encounters 00 00 10 00 00 00, it cannot determine easily the length, could be 4 5 or 6 bytes
    // The below code is to normalize length of scores by calculating the length that appears most time in the list 
    // and use it to normalize all scores by relaunching teh method, enforcing the score length
    if (forcedScoreLength < 0) {
      // calculate most frequent number and occurrence
      Map.Entry<Integer, Long> occur = selectedScores.values().stream()
              .collect(Collectors.groupingBy(sr -> sr.scoreLength, Collectors.counting()))
              .entrySet().stream()
              .max(Map.Entry.comparingByValue())
              .orElse(null);

      if (occur != null && occur.getValue() < selectedScores.size()) {
          LOG.warn("All scores not stored on same length, normalize to the most frequent length {}, appearring {} times on {}\n", 
            occur.getKey(), occur.getValue(), selectedScores.size());
          // now start again the computation forcing a score length
        return parseScores(bytes, rom, testFolders, scores, cacheScores, occur.getKey());
      }
    }
    // else we have our score mappings
    return selectedScores;
  }

  //------------------------------------------------

  private SearchResult findOrContinue(List<Integer> initials, List<SearchResult> positions, Supplier<SearchResult> continueChain) {
    if (positions.size() == 0) {
      return null;
    }
    else if (positions.size() == 1) {
      SearchResult result = positions.get(0);
      if (initials == null) {
        // nothing to do, do not even write message
      }
      else if (initials.size() == 0) {
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
      return continueChain != null? continueChain.get() : null;
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
        LOG.warn("\n" + raw);
      }
      return ScoreListFactory.create(raw, new Date(), null, scoringDB, true);
    } 
    catch (Exception e) {
      LOG.error("Cannot getscores from pinhemi", e);
      return Collections.emptyList();
    }
  }

  //--------------------------------------------------

  private LinkedHashMap<String, SearchResult> parseChecksum(byte[] bytes, LinkedHashMap<Score, SearchResult> selectedScores) {
    LinkedHashMap<String, SearchResult> results = new LinkedHashMap<>();

    // group all scores by the label of the score
    Map<String, List<Score>> groupByLabel = selectedScores.keySet().stream()
              .collect(Collectors.groupingBy(sc -> StringUtils.defaultString(sc.getLabel(), "High Scores"), Collectors.toList()));

    for (Map.Entry<String, List<Score>> e : groupByLabel.entrySet()) {
      String label = e.getKey();
      List<Score> scores = e.getValue();

      int startPosition = Integer.MAX_VALUE;
      int endPosition = 0;
      for (Score sc : scores) {
        SearchResult res = selectedScores.get(sc);
        if (res.initialPosition >= 0) {
          startPosition = Math.min(startPosition, res.initialPosition);
          endPosition = Math.max(endPosition, res.initialPosition + 3);
        }
        if (res.scorePosition >= 0) {
          startPosition = Math.min(startPosition, res.scorePosition);
          endPosition = Math.max(endPosition, res.scorePosition + res.scoreLength);
        }
      }

      // calculate checksum of the region
      int sum = 0xFFFF;
      for (int i = startPosition; i < endPosition; i++) {
          sum -= bytes[i] & 0XFF;
      }

      // try to find the checksum at the end of the section, it could be some extra contiguous digits
      while (sum != decodeInt(bytes, endPosition, 2) && endPosition < bytes.length) {
        sum -= bytes[endPosition] & 0XFF;
        endPosition++;
      }
      if (endPosition < bytes.length) {
        // we found our checksum 
        SearchResult checksumPos = new SearchResult(-1, startPosition, endPosition - startPosition + 1);
        results.put(label, checksumPos);
      }
      else {
        // search our checksum within the file
        List<SearchResult> res = searchNumber(bytes, sum, 2, 2, false);
        SearchResult checksumPos = findOrContinue(null, res, null);
        if (checksumPos != null) {
          results.put(label, checksumPos);
        }
      }
    }
    return results;
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
  public List<SearchResult> searchNumber(byte[] data, long number, int nbBytes, int maxNumberBytes, boolean useBcd) {
    List<SearchResult> positions = new ArrayList<>();
    for (int matchPos = 0; matchPos < data.length; matchPos++) {
      if (nbBytes < 0) {
        int len = isNumber(data, number, matchPos, maxNumberBytes, useBcd);
        if (len >= 0) {
          positions.add(new SearchResult(-1, matchPos, len));
        }
      } else {
        long nb = useBcd ? decodeBCD(data, matchPos, nbBytes): decodeInt(data, matchPos, nbBytes);
        if (nb == number) {
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
      int len = isNumber(data, targetNumber, afterString, maxNumberBytes, true);
      if (len >= 0) {
        return new SearchResult(matchPos, afterString, len);
      }
      startPos = matchPos + 1;
    }
  }

  /** Find if number is stored at position and if yes, return the length of encoding else -1 */
  public int isNumber(byte[] data, long number, int position, int maxNumberBytes, boolean useBcd) {
    // Try all possible byte lengths for the BCD-encoded number
    for (int len = 1; len <= maxNumberBytes; len++) {
      long decoded = useBcd ? decodeBCD(data, position, len): decodeInt(data, position, len);
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

  private long decodeInt(byte[] data, int offset, int length) {
    if (offset + length > data.length) {
      return -1;
    }

    long value = 0;
    for (int i = 0; i < length; i++) {
      int b = data[offset + i] & 0xFF;
      value = (value << 8) | (b & 0xFF);
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
