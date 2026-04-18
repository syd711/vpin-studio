package de.mephisto.vpin.server.highscores.parsing.nvram;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.ScoreListAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import net.nvrams.mapping.NVRamParser;
import net.nvrams.mapping.NVRamScore;
import net.nvrams.mapping.map.NVRamMapParser;
import net.nvrams.mapping.superhac.NVRamSuperhacParser;

/**
 * A bridge to NVRam map sub projects
 * The class is used
 * - as a NvRamOutputToRaw, to convert nvram to raw
 * - as a ScoreListAdapter, to parse raw to Scores
 */
public class NvRamParsingWithParser implements NvRamOutputToRaw, ScoreListAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(NvRamOutputToRawWithPinemhi.class);

  // the parser which most of the work is delegated to
  private NVRamParser parser;
  // The cached list of supported roms to accelerate checks
  private Set<String> supportedRoms;


  public static NvRamParsingWithParser createSuperhacParser() throws IOException {
    NVRamParser superhacParser = new NVRamSuperhacParser();
    return new NvRamParsingWithParser(superhacParser);
  }

  public static NvRamParsingWithParser createNvramMapParser() throws IOException {
    NVRamParser mapParser = new NVRamMapParser();
    return new NvRamParsingWithParser(mapParser);
  }

  private NvRamParsingWithParser(NVRamParser parser) throws IOException {
    this.parser = parser;
    this.supportedRoms = new HashSet<>();
    for (String rom : parser.getSupportedNVRams()) {
      supportedRoms.add(rom.toLowerCase());
    }
  }

  public Set<String> getSupportedRoms() {
    return supportedRoms;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [" + this.parser.getClass().getSimpleName() + "]";
  }

  public boolean isSupportedRom(String rom) {
    return supportedRoms.contains(rom.toLowerCase());
  }

  //---------------------------------------
  // implementation of NvRamOutputToRaw

  //@Override
  public String convertOutputToRaw(@NonNull String nvRamFileName, File nvFile) throws Exception {
    Locale locale = Locale.getDefault();
    String rom = romFromNv(nvFile);
    if (isSupportedRom(rom)) {
      return String.join("\n", parser.getRaw(rom, nvFile, locale));
    }
    return null;
  }

  public String romFromNv(File nvFile) throws IOException {
    String rom = nvFile.getName();
    int dotIndex = rom.lastIndexOf('.');
    if (dotIndex > 0) rom = rom.substring(0, dotIndex);
    int hyphenIndex = rom.indexOf('-');
    if (hyphenIndex > 0) rom = rom.substring(0, hyphenIndex);
    return rom;
  }


  //---------------------------------------
  // implementation of ScoreListAdapter

  @Override
  public boolean isApplicable(Game game) {
    String rom = game != null ? game.getRom().toLowerCase() : "<no rom>";
    return isSupportedRom(rom);
  }

  @Override
  public List<Score> getScores(Game game, Date createdAt, List<String> lines, boolean parseAll) {
    Locale locale = Locale.getDefault();
    String rom = game != null ? game.getRom().toLowerCase() : "<no rom>";
    if (isSupportedRom(rom)) {
      try {
        List<NVRamScore> nvRamScores = parser.parseRaw(rom, lines, locale, parseAll);
        List<Score> scores = new ArrayList<>();
        for (NVRamScore nvramScore : nvRamScores) {
          Score sc = new Score(createdAt, game.getId(),
              nvramScore.getPlayerInitials(), null,
              nvramScore.getRawScore(),
              nvramScore.getScore(),
              nvramScore.getPosition());
          sc.setSuffix(nvramScore.getSuffix());
          sc.setLabel(nvramScore.getLabel());
          scores.add(sc);
        }
        return scores;
      }
      catch (Exception ioe) {
        LOG.error("Error while getting scores for game {}: {}", game, ioe.getMessage());
      }
    }
    return Collections.emptyList();
  }
}
