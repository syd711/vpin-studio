package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.representations.*;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface OverlayClient {
  List<CompetitionRepresentation> getFinishedCompetitions(int limit);

  List<CompetitionRepresentation> getActiveOfflineCompetitions();

  GameRepresentation getGame(int id);

  ScoreSummaryRepresentation getGameScores(int id);

  GameMediaRepresentation getGameMedia(int id);

  ByteArrayInputStream getCompetitionBackground(long gameId);

  ScoreListRepresentation getCompetitionScores(int id);

  ByteArrayInputStream getAsset(String uuid);

  List<GameRepresentation> getRecentlyPlayedGames(int count);

  ByteArrayInputStream getGameMediaItem(int id, PopperScreen screen);
}
