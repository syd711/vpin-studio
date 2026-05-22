package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.listadapters.DefaultAdapter;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;


    public class ScoreListFactory {
        private final static Logger LOG = LoggerFactory.getLogger(de.mephisto.vpin.server.highscores.parsing.ScoreListFactory.class);

        private final static List<ScoreListAdapter> adapters = new ArrayList<>();

  public static void registerScoreListAdapter(ScoreListAdapter adapter) {
    adapters.add(adapter);
  }

  //-------------------------------------------------------

        //-------------------------------------------------------

        public static List<Score> create(@NonNull String raw, @NonNull Instant createdAt, @Nullable Game game, @NonNull ScoringDB scoringDB) {
            return create(raw, createdAt, game, scoringDB, false);
        }

        /**
         * The parseAll flag, when false (by default), filters on high scores only
         */
        public static List<Score> create(@NonNull String raw, @NonNull Instant createdAt, @Nullable Game game, @NonNull ScoringDB scoringDB, boolean parseAll) {
            List<Score> scores = getScores(raw, createdAt, game, scoringDB, parseAll);
            List<Score> filteredScores = new ArrayList<>();
            int position = 1;
            for (Score sc : scores) {
                if (filteredScores.stream().anyMatch(score -> Objects.equals(score.getScore(), sc.getScore()) && Strings.CI.equals(score.getPlayerInitials(), sc.getPlayerInitials()))) {
                    continue;
                }
                sc.setPosition(position++);
                filteredScores.add(sc);
            }
            return filteredScores;
        }


        private static List<Score> getScores(@NonNull String raw, @NonNull Instant createdAt, @Nullable Game game, @NonNull ScoringDB scoringDB, boolean parseAll) {
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
                            List<Score> scoreList = adapter.getScores(game, createdAt, lines, parseAll);
                            if (!scoreList.isEmpty()) {
                                return scoreList;
                            }
                        }
                    }
                }

                // fall back adapter, for non nvrams
                DefaultAdapter adapter = new DefaultAdapter(scoringDB);
                return adapter.getScores(game, createdAt, lines, parseAll);
            }
            catch (Exception e) {
                LOG.error("Failed to parse highscore: {}\nRaw Data:\n==================================\n{}", e.getMessage(), raw, e);
            }
            return scores;
        }
    }
