package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface ScoreListAdapter {

    boolean isApplicable(@NonNull Game game);

  @NonNull
  List<Score> getScores(@NonNull Game game, @NonNull Instant createdAt, @NonNull List<String> lines, boolean parseAll) throws IOException;
}
