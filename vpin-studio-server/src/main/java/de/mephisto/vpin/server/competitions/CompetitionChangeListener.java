package de.mephisto.vpin.server.competitions;

public interface CompetitionChangeListener {

  void competitionCreated(Competition competition);

  void competitionChanged(Competition competition);

  void competitionFinished(Competition competition);

  void competitionDeleted(Competition competition);
}
