package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.listadapters.DefaultAdapter;
import de.mephisto.vpin.server.highscores.parsing.listadapters.SortedScoreAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreListFactory {
  private final static Logger LOG = LoggerFactory.getLogger(ScoreListFactory.class);

  private final static List<ScoreListAdapter> adapters = new ArrayList<>();

  // Adapters are used to return a list of scores from the raw highscore
  // If an adapter is applicable, this is the only adapter used to return scores
  // (adapters don't "stack")
  static {
    adapters.add(new SortedScoreAdapter("tf_180"));
  }

  public static List<Score> create(@NonNull String raw, @NonNull Date createdAt, @Nullable Game game, List<String> titles) {
    List<Score> scores = new ArrayList<>();

    try {
      LOG.debug("Parsing Highscore text: " + raw);
      List<String> lines = Arrays.asList(raw.split("\\n"));
      if (lines.isEmpty()) {
        return scores;
      }

      if (game != null) {
        for (ScoreListAdapter adapter : adapters) {
          if (adapter.isApplicable(game)) {
//            LOG.info("Using score list adapter {}", adapter.getClass().getSimpleName());
            return adapter.getScores(game, createdAt, lines, titles);
          }
        }
      }
      // fall back adapter 
      DefaultAdapter adapter = new DefaultAdapter();
      return adapter.getScores(game, createdAt, lines, titles);
    }
    catch (Exception e) {
      LOG.error("Failed to parse highscore: " + e.getMessage() + "\nRaw Data:\n==================================\n" + raw, e);
    }
    return scores;
  }

}