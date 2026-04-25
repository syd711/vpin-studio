package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import org.jspecify.annotations.NonNull;

import java.time.OffsetDateTime;
import java.util.List;

public interface ScoreListAdapter {

  boolean isApplicable(@NonNull Game game);

  @NonNull
  List<Score> getScores(@NonNull Game game, @NonNull OffsetDateTime createdAt, @NonNull List<String> lines, boolean parseAll);
}
