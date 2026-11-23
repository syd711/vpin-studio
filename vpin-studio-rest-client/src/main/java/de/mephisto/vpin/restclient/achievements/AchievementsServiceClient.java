package de.mephisto.vpin.restclient.achievements;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*********************************************************************************************************************
 * Achievements
 ********************************************************************************************************************/
public class AchievementsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public AchievementsServiceClient(VPinStudioClient client) {
    super(client);
  }

  @NonNull
  public List<PlayerAchievements> getPlayers() {
    PlayerAchievements[] players = getRestClient().get(API + "achievements/players", PlayerAchievements[].class);
    return players != null ? Arrays.asList(players) : Collections.emptyList();
  }

  @Nullable
  public PlayerAchievements getPlayer(long playerId) {
    return getRestClient().get(API + "achievements/player/" + playerId, PlayerAchievements.class);
  }

  @NonNull
  public List<TableAchievements> getTables(long playerId) {
    TableAchievements[] tables = getRestClient().get(API + "achievements/player/" + playerId + "/tables", TableAchievements[].class);
    return tables != null ? Arrays.asList(tables) : Collections.emptyList();
  }

  @Nullable
  public TableAchievementsDetails getTable(long playerId, @NonNull String rom) {
    return getRestClient().get(API + "achievements/player/" + playerId + "/table/" + URLEncoder.encode(rom, StandardCharsets.UTF_8), TableAchievementsDetails.class);
  }

  public boolean selectBadge(long playerId, @Nullable String badgeId) {
    try {
      String param = badgeId != null ? "?badge=" + URLEncoder.encode(badgeId, StandardCharsets.UTF_8) : "";
      getRestClient().put(API + "achievements/player/" + playerId + "/badge" + param, java.util.Map.of());
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to select badge: {}", e.getMessage(), e);
      return false;
    }
  }

  @Nullable
  public AchievementsSyncResult sync() {
    return getRestClient().post(API + "achievements/sync", java.util.Map.of(), AchievementsSyncResult.class);
  }

  @Nullable
  public AchievementsMigrationResult migrate(@NonNull AchievementsMigrationRequest request) {
    return getRestClient().post(API + "achievements/migration", request, AchievementsMigrationResult.class);
  }

  @Nullable
  public String getDefaultWatcherFolder() {
    return getRestClient().get(API + "achievements/migration/watcherfolder", String.class);
  }
}
