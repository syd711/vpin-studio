package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.representations.*;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface OverlayClient {
  List<CompetitionRepresentation> getFinishedCompetitions(int limit);

  List<CompetitionRepresentation> getActiveCompetitions();

  GameRepresentation getGame(int id);

  ScoreSummaryRepresentation getGameScores(int id);

  ByteArrayInputStream getCompetitionBackground(long gameId);

  ScoreListRepresentation getCompetitionScores(long id);

  ByteArrayInputStream getAsset(String uuid);

  ScoreSummaryRepresentation getRecentlyPlayedGames(int count);

  ByteArrayInputStream getGameMediaItem(int id, PopperScreen screen);

  PreferenceEntryRepresentation getPreference(String key);
}
