package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.server.achievements.progression.RarityTier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rarity counters per ROM as delivered by VPin Mania, filled by the cloud service and kept for 24h.
 */
@Component
public class AchievementRarityCache {

  private static final long TTL_MS = 24L * 3600 * 1000;

  public record Rarity(int players, @NonNull Map<String, Integer> counts, long fetchedAt) {

    @Nullable
    public RarityTier tierOf(@NonNull String achKey) {
      Integer count = counts.get(achKey);
      return RarityTier.of(count != null ? count : 0, players);
    }

    public double pctOf(@NonNull String achKey) {
      Integer count = counts.get(achKey);
      return RarityTier.pct(count != null ? count : 0, players);
    }
  }

  private final Map<String, Rarity> cache = new ConcurrentHashMap<>();

  @Nullable
  public Rarity get(@Nullable String rom) {
    if (rom == null) {
      return null;
    }
    return cache.get(rom.toLowerCase());
  }

  public boolean isStale(@NonNull String rom) {
    Rarity r = cache.get(rom.toLowerCase());
    return r == null || System.currentTimeMillis() - r.fetchedAt() > TTL_MS;
  }

  public void put(@NonNull String rom, int players, @Nullable Map<String, Integer> counts) {
    cache.put(rom.toLowerCase(), new Rarity(players, counts != null ? counts : Collections.emptyMap(), System.currentTimeMillis()));
  }

  public void clear() {
    cache.clear();
  }
}
