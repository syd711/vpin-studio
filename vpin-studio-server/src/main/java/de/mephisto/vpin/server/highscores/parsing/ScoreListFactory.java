package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.listadapters.DefaultAdapter;
import de.mephisto.vpin.server.highscores.parsing.listadapters.SortedScoreAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ScoreListFactory {
  private final static Logger LOG = LoggerFactory.getLogger(ScoreListFactory.class);

  private final static List<ScoreListAdapter> adapters = new ArrayList<>();

  // Adapters are used to return a list of scores from the raw highscore
  // If an adapter is applicable, this is the only adapter used to return scores
  // (adapters don't "stack")
  static {
    adapters.add(new SortedScoreAdapter("tf_180"));
    adapters.add(new DefaultAdapter());
  }

  public static List<Score> create(@NonNull String raw, @NonNull Date createdAt, @Nullable Game game, List<String> titles) {
    List<Score> scores = new ArrayList<>();

    try {
      LOG.debug("Parsing Highscore text: " + raw);
     List<String> lines = Arrays.asList(raw.split("\\n"));
      if (lines.isEmpty()) {
        return scores;
      }

      for (ScoreListAdapter adapter : adapters) {
        if (adapter.isApplicable(game)) {
          // LOG.info("Using score list adapter {}", adapter.getClass().getSimpleName());
          return adapter.getScores(game, createdAt, lines, titles);
        }
      }
    } catch (Exception e) {
      LOG.error(
          "Failed to parse highscore: " + e.getMessage() + "\nRaw Data:\n==================================\n" + raw, e);
    }
    return scores; // return an empty score
  }
}
