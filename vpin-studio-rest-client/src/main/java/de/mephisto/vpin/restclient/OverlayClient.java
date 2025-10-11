package de.mephisto.vpin.restclient;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.highscores.ScoreListRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.players.RankedPlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.system.FeaturesInfo;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface OverlayClient {
  
  DiscordServer getDiscordServer(long serverId);

  List<CompetitionRepresentation> getFinishedCompetitions(int limit);

  List<CompetitionRepresentation> getIScoredSubscriptions();

  CompetitionRepresentation getActiveCompetition(CompetitionType type);

  //----------------------------------
  
  GameRepresentation getGame(int gameId);

  GameRepresentation getGameCached(int gameId);

  GameEmulatorRepresentation getGameEmulator(int emulatorId);

  GameScoreValidation getGameScoreValidation(int gameId);

  AlxSummary getAlxSummary(int gameId);

  CardTemplate getHighscoreCardTemplate(GameRepresentation game);

  /** get the CardData with streams */
  CardData getCardData(GameRepresentation game, CardTemplate template);

  //------------------------------

  FrontendPlayerDisplay getScreenDisplay(VPinScreen tutorialScreen);

  FrontendMediaRepresentation getFrontendMedia(int id);

  String getURL(String url);

  InputStream getCachedUrlImage(String url);

  InputStream getPersistentCachedUrlImage(String cache, String url);

  ScoreSummaryRepresentation getCompetitionScore(long id);

  ByteArrayInputStream getCompetitionBackground(long gameId);

  ScoreListRepresentation getCompetitionScoreList(long id);

  ByteArrayInputStream getAsset(AssetType assetType, String uuid);

  ScoreSummaryRepresentation getRecentScores(int count);

  ScoreSummaryRepresentation getRecentScoresByGame(int count, int gameId);

  ByteArrayInputStream getWheelIcon(int id, boolean skipApng);

  //--------------------------

  GameStatus startPause();

  GameStatus getPauseStatus();

  GameStatus finishPause();

  //--------------------------

  void clearPreferenceCache();

  PreferenceEntryRepresentation getPreference(String key);

  <T> T getJsonPreference(String key, Class<T> clazz);

  List<RankedPlayerRepresentation> getRankedPlayers();

  //---------------------------

  VpsTable getVpsTable(String extTableId);

  VpsTableVersion getVpsTableVersion(@Nullable String tableId, @Nullable String versionId);

  GameRepresentation getGameByVpsId(@Nullable String vpsTableId, @Nullable String vpsTableVersionId);

  //---------------------------

  FeaturesInfo getFeatures();

  MonitorInfo getScreenInfo(int screenId);

}
