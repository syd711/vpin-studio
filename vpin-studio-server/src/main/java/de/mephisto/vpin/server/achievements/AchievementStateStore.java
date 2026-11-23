package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.server.achievements.model.AchievementPlayerState;
import de.mephisto.vpin.server.achievements.model.AchievementPlayerStateRepository;
import de.mephisto.vpin.server.achievements.model.AchievementUnlock;
import de.mephisto.vpin.server.achievements.model.AchievementUnlockRepository;
import de.mephisto.vpin.server.achievements.rules.PlayerProgress;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Reads and writes the JSON columns of {@link AchievementPlayerState}.
 */
@Component
public class AchievementStateStore {
  private static final Logger LOG = LoggerFactory.getLogger(AchievementStateStore.class);

  public static final ObjectMapper JSON = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

  @Autowired
  private AchievementPlayerStateRepository stateRepository;

  @Autowired
  private AchievementUnlockRepository unlockRepository;

  @NonNull
  public synchronized AchievementPlayerState getState(long playerId) {
    Optional<AchievementPlayerState> state = stateRepository.findById(playerId);
    if (state.isPresent()) {
      return state.get();
    }
    AchievementPlayerState s = new AchievementPlayerState();
    s.setPlayerId(playerId);
    s.setTrackingSince(Instant.now());
    return stateRepository.saveAndFlush(s);
  }

  public synchronized AchievementPlayerState save(@NonNull AchievementPlayerState state) {
    return stateRepository.saveAndFlush(state);
  }

  @NonNull
  public PlayerProgress toProgress(@NonNull AchievementPlayerState state, @NonNull List<AchievementUnlock> unlocks) {
    PlayerProgress p = new PlayerProgress();
    for (AchievementUnlock u : unlocks) {
      p.unlockedTitles.add(u.getTitle());
    }
    p.globalTally = getTally(state);
    p.romsPlayed = new LinkedHashSet<>(getRomsPlayed(state));
    return p;
  }

  public void applyProgress(@NonNull AchievementPlayerState state, @NonNull PlayerProgress progress) {
    state.setGlobalTally(write(progress.globalTally));
    state.setRomsPlayed(write(new ArrayList<>(progress.romsPlayed)));
  }

  @NonNull
  public Map<String, PlayerProgress.Tally> getTally(@NonNull AchievementPlayerState state) {
    Map<String, PlayerProgress.Tally> m = read(state.getGlobalTally(), new TypeReference<>() {
    });
    return m != null ? new HashMap<>(m) : new HashMap<>();
  }

  @NonNull
  public List<String> getRomsPlayed(@NonNull AchievementPlayerState state) {
    List<String> m = read(state.getRomsPlayed(), new TypeReference<>() {
    });
    return m != null ? m : new ArrayList<>();
  }

  @NonNull
  public Set<String> getBadges(@NonNull AchievementPlayerState state) {
    List<String> m = read(state.getBadges(), new TypeReference<>() {
    });
    return m != null ? new LinkedHashSet<>(m) : new LinkedHashSet<>();
  }

  public void setBadges(@NonNull AchievementPlayerState state, @NonNull Set<String> badges) {
    state.setBadges(write(new ArrayList<>(badges)));
  }

  /**
   * rom -> field -> best delta in one game
   */
  @NonNull
  public Map<String, Map<String, Long>> getFieldBest(@NonNull AchievementPlayerState state) {
    Map<String, Map<String, Long>> m = read(state.getFieldBest(), new TypeReference<>() {
    });
    return m != null ? new HashMap<>(m) : new HashMap<>();
  }

  public void updateFieldBest(@NonNull AchievementPlayerState state, @NonNull String rom, @NonNull Map<String, Long> deltas) {
    Map<String, Map<String, Long>> all = getFieldBest(state);
    Map<String, Long> romBest = new HashMap<>(all.getOrDefault(rom, new HashMap<>()));
    for (Map.Entry<String, Long> e : deltas.entrySet()) {
      romBest.merge(e.getKey(), e.getValue(), Math::max);
    }
    all.put(rom, romBest);
    state.setFieldBest(write(all));
  }

  @Nullable
  public static <T> T read(@Nullable String json, TypeReference<T> type) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      return JSON.readValue(json, type);
    }
    catch (Exception e) {
      LOG.warn("Failed to read achievement state json: {}", e.getMessage());
      return null;
    }
  }

  @Nullable
  public static String write(@Nullable Object value) {
    if (value == null) {
      return null;
    }
    try {
      return JSON.writeValueAsString(value);
    }
    catch (Exception e) {
      LOG.warn("Failed to write achievement state json: {}", e.getMessage());
      return null;
    }
  }

  public List<AchievementUnlock> getUnlocks(long playerId) {
    return unlockRepository.findByPlayerId(playerId);
  }
}
