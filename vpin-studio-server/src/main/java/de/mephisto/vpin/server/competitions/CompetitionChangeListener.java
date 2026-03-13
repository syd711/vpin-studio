package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface CompetitionChangeListener {

  default void competitionStarted(@NonNull Competition competition) {

  }

  default void competitionCreated(@NonNull Competition competition) {

  }

  default void competitionChanged(@NonNull Competition competition) {

  }

  default void competitionFinished(@NonNull Competition competition, @Nullable Player winner, @NonNull ScoreSummary scoreSummary) {

  }

  default void competitionDeleted(@NonNull Competition competition) {

  }
}
