package de.mephisto.vpin.server.highscores.parsing.nvram;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.ScoreListAdapter;

import net.nvrams.mapping.NVRamParser;
import net.nvrams.mapping.NVRamScore;
import net.nvrams.mapping.map.NVRamMapParser;
import net.nvrams.mapping.pinemhi.PinemhiRamParser;
import net.nvrams.mapping.superhac.NVRamSuperhacParser;

/**
 * A bridge to NVRam map sub projects
 * The class is used
 * - as a NvRamOutputToRaw, to convert nvram to raw
 * - as a ScoreListAdapter, to parse raw to Scores
 */
public class NvRamParsingWithParser implements NvRamOutputToRaw, ScoreListAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(NvRamParsingWithParser.class);

  // the parser which most of the work is delegated to
  private NVRamParser parser;


  public static NvRamParsingWithParser createPinemhiParser(ScoringDB scoringDB) throws IOException {
    NVRamParser pinemhiParser = new PinemhiRamParser(SystemInfo.RESOURCES + "pinemhi/",
        scoringDB.getHighscoreTitles(), scoringDB.getHighscoreSkipTitlesCheck());
    return new NvRamParsingWithParser(pinemhiParser);
  }

  public static NvRamParsingWithParser createSuperhacParser(ScoringDB scoringDB) throws IOException {
    NVRamParser superhacParser = new NVRamSuperhacParser(SystemInfo.RESOURCES + "superhac/roms.json");
    return new NvRamParsingWithParser(superhacParser);
  }

  public static NvRamParsingWithParser createNvramMapParser(ScoringDB scoringDB) throws IOException {
    NVRamParser mapParser = new NVRamMapParser(SystemInfo.RESOURCES + "maps/");
    return new NvRamParsingWithParser(mapParser);
  }

  NvRamParsingWithParser(NVRamParser parser) throws IOException {
    this.parser = parser;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [" + this.parser.getClass().getSimpleName() + "]";
  }

  //---------------------------------------
  // implementation of NvRamOutputToRaw

  @Override
  public boolean isSupportedRom(String rom) {
    return parser.isSupportedRom(rom);
  }

  @Override
  public List<String> getRaw(String rom, File nvFile, Locale locale) throws IOException {
    return parser.getRaw(rom, nvFile, locale);
  }

  //---------------------------------------
  // implementation of ScoreListAdapter

  @Override
  public boolean isApplicable(Game game) {
    String rom = (game != null && game.getRom() != null) ? game.getRom().toLowerCase() : "<no rom>";
    return isSupportedRom(rom);
  }

    @Override
    public List<Score> getScores(Game game, Instant createdAt, List<String> lines, boolean parseAll) throws IOException {
        Locale locale = Locale.getDefault();
        String rom = game != null ? game.getRom().toLowerCase() : "<no rom>";
        if (isSupportedRom(rom)) {
            List<NVRamScore> nvRamScores = parser.parseRaw(rom, lines, locale, parseAll);
            List<Score> scores = new ArrayList<>();
            for (NVRamScore nvramScore : nvRamScores) {
                Score sc = new Score(createdAt, game.getId(),
                        nvramScore.getInitials(), null,
                        nvramScore.getRawScore(),
                        nvramScore.getScore(),
                        nvramScore.getPosition());
                sc.setSuffix(nvramScore.getSuffix());
                sc.setLabel(nvramScore.getLabel());
                scores.add(sc);
            }
            return scores;
        }
        return Collections.emptyList();
    }
}
