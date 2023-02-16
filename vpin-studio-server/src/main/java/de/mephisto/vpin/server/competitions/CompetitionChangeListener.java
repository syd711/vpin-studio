package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface CompetitionChangeListener {

  void competitionStarted(@NonNull Competition competition);

  void competitionCreated(@NonNull Competition competition);

  void competitionChanged(@NonNull Competition competition);

  void competitionFinished(@NonNull Competition competition, @Nullable Player winner, @NonNull ScoreSummary scoreSummary);

  void competitionDeleted(@NonNull Competition competition);
}
