package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreListRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.players.RankedPlayerRepresentation;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface OverlayClient {
  DiscordServer getDiscordServer(long serverId);

  List<CompetitionRepresentation> getFinishedCompetitions(int limit);

  CompetitionRepresentation getActiveCompetition(CompetitionType type);

  GameRepresentation getGame(int id);

  GameRepresentation getGameCached(int id);

  FrontendMediaRepresentation getFrontendMedia(int id);

  InputStream getCachedUrlImage(String url);

  default InputStream getPersistentCachedUrlImage(String cache, String url) {
    return null;
  }

  ScoreSummaryRepresentation getCompetitionScore(long id);

  ByteArrayInputStream getCompetitionBackground(long gameId);

  ScoreListRepresentation getCompetitionScoreList(long id);

  ByteArrayInputStream getAsset(AssetType assetType, String uuid);

  ScoreSummaryRepresentation getRecentScores(int count);

  ByteArrayInputStream getGameMediaItem(int id, VPinScreen screen);

  PreferenceEntryRepresentation getPreference(String key);

  <T> T getJsonPreference(String key, Class<T> clazz);

  List<RankedPlayerRepresentation> getRankedPlayers();
}
