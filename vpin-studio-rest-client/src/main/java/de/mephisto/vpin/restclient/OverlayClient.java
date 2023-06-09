package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface OverlayClient {
  DiscordServer getDiscordServer(long serverId);

  List<CompetitionRepresentation> getFinishedCompetitions(int limit);

  CompetitionRepresentation getActiveCompetition(CompetitionType type);

  GameRepresentation getGame(int id);

  InputStream getCachedUrlImage(String url);

  ScoreSummaryRepresentation getCompetitionScore(long id);

  ByteArrayInputStream getCompetitionBackground(long gameId);

  ScoreListRepresentation getCompetitionScoreList(long id);

  ByteArrayInputStream getAsset(AssetType assetType, String uuid);

  ScoreSummaryRepresentation getRecentlyPlayedGames(int count);

  ByteArrayInputStream getGameMediaItem(int id, PopperScreen screen);

  PreferenceEntryRepresentation getPreference(String key);

  List<RankedPlayerRepresentation> getRankedPlayers();
}
