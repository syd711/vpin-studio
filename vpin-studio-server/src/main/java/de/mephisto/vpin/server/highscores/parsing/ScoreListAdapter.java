package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Date;
import java.util.List;

public interface ScoreListAdapter {

  boolean isApplicable(@NonNull Game game);

  @NonNull
  List<Score> getScores(@NonNull Game game, @NonNull Date createdAt, @NonNull List<String> lines);
}
