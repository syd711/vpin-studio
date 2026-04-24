package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.listadapters.DefaultAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ScoreListFactory {
  private final static Logger LOG = LoggerFactory.getLogger(ScoreListFactory.class);

  private final static List<ScoreListAdapter> adapters = new ArrayList<>();
 
  public static void registerScoreListAdapter(ScoreListAdapter adapter) {
    adapters.add(adapter);
  }

  public static void unregisterScoreListAdapter(ScoreListAdapter adapter) {
    adapters.remove(adapter);
  }

  //-------------------------------------------------------

  public static List<Score> create(@NonNull String raw, @NonNull Date createdAt, @Nullable Game game, @NonNull ScoringDB scoringDB) {
    return create(raw, createdAt, game, scoringDB, false);
  }

  /**
   * The parseAll flag, when false (by default), filters on high scores only
   */
  public static List<Score> create(@NonNull String raw, @NonNull Date createdAt, @Nullable Game game, @NonNull ScoringDB scoringDB, boolean parseAll) {
    List<Score> scores = new ArrayList<>();

    try {
      LOG.debug("Parsing Highscore text: {}", raw);
      List<String> lines = Arrays.asList(raw.split("\\n"));
      if (lines.isEmpty()) {
        return scores;
      }

      if (game != null) {
        for (ScoreListAdapter adapter : adapters) {
          if (adapter.isApplicable(game)) {
//            LOG.info("Using score list adapter {}", adapter.getClass().getSimpleName());
            return adapter.getScores(game, createdAt, lines, parseAll);
          }
        }
      }
      // fall back adapter 
      DefaultAdapter adapter = new DefaultAdapter(scoringDB);
      return adapter.getScores(game, createdAt, lines, parseAll);
    }
    catch (Exception e) {
      LOG.error("Failed to parse highscore: {}\nRaw Data:\n==================================\n{}", e.getMessage(), raw, e);
    }
    return scores;
  }
}
