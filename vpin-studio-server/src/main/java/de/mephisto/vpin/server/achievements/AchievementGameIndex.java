package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.vps.VpsService;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Index of the installed games by ROM, with the manufacturer of each ROM (VPS manufacturer, fallback to the
 * Watcher's ROM prefix table). Cached for a few minutes.
 */
@Component
public class AchievementGameIndex {
  private static final Logger LOG = LoggerFactory.getLogger(AchievementGameIndex.class);
  private static final long TTL_MS = 5 * 60 * 1000;

  @Autowired
  private GameService gameService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private AchievementRuleStore ruleStore;

  private volatile Snapshot snapshot;

  public static class Snapshot {
    final long createdAt = System.currentTimeMillis();
    /** rom (lower case) -> games */
    public final Map<String, List<Game>> gamesByRom = new LinkedHashMap<>();
    /** rom (lower case) -> manufacturer */
    public final Map<String, String> manufacturerByRom = new HashMap<>();
    /** roms with a NVRAM map */
    public final Set<String> romsWithMap = new HashSet<>();
  }

  @NonNull
  public Snapshot get() {
    Snapshot s = snapshot;
    if (s == null || System.currentTimeMillis() - s.createdAt > TTL_MS) {
      s = build();
      snapshot = s;
    }
    return s;
  }

  public void invalidate() {
    snapshot = null;
  }

  private Snapshot build() {
    Snapshot s = new Snapshot();
    try {
      for (Game game : gameService.getGames()) {
        String rom = romOf(game);
        if (rom == null) {
          continue;
        }
        s.gamesByRom.computeIfAbsent(rom, r -> new ArrayList<>()).add(game);
        if (!s.manufacturerByRom.containsKey(rom)) {
          String mfr = manufacturerOf(game);
          if (mfr != null) {
            s.manufacturerByRom.put(rom, mfr);
          }
        }
        if (ruleStore.getAuditReader().isSupported(rom)) {
          s.romsWithMap.add(rom);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to build achievement game index: {}", e.getMessage(), e);
    }
    return s;
  }

  @Nullable
  public static String romOf(@Nullable Game game) {
    if (game == null || StringUtils.isEmpty(game.getRom())) {
      return null;
    }
    return game.getRom().trim().toLowerCase();
  }

  @Nullable
  private String manufacturerOf(Game game) {
    try {
      if (!StringUtils.isEmpty(game.getExtTableId())) {
        VpsTable table = vpsService.getTableById(game.getExtTableId());
        if (table != null && !StringUtils.isEmpty(table.getManufacturer())) {
          return ManufacturerUtil.normalize(table.getManufacturer());
        }
      }
    }
    catch (Exception e) {
      //ignore
    }
    return ManufacturerUtil.fromRom(game.getRom());
  }

  @Nullable
  public String manufacturerOfRom(@NonNull String rom) {
    Snapshot s = get();
    String m = s.manufacturerByRom.get(rom.toLowerCase());
    return m != null ? m : ManufacturerUtil.fromRom(rom);
  }

  /**
   * Installed ROMs with a NVRAM map for a manufacturer, "__any__" for all.
   */
  @NonNull
  public Set<String> installedRoms(@NonNull String manufacturer) {
    Snapshot s = get();
    Set<String> result = new HashSet<>();
    for (String rom : s.romsWithMap) {
      if ("__any__".equals(manufacturer) || manufacturer.equals(s.manufacturerByRom.get(rom))) {
        result.add(rom);
      }
    }
    return result;
  }

  @NonNull
  public List<Game> gamesOfRom(@NonNull String rom) {
    List<Game> games = get().gamesByRom.get(rom.toLowerCase());
    return games != null ? games : Collections.emptyList();
  }

  @Nullable
  public String tableName(@Nullable String rom) {
    if (rom == null) {
      return null;
    }
    List<Game> games = gamesOfRom(rom);
    return games.isEmpty() ? null : games.get(0).getGameDisplayName();
  }
}
