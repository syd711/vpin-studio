package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.server.achievements.bundle.RuleBundle;
import de.mephisto.vpin.server.achievements.nvram.NvramAuditReader;
import de.mephisto.vpin.server.achievements.nvram.NvramSnapshot;
import de.mephisto.vpin.server.achievements.rules.AchievementRule;
import de.mephisto.vpin.server.achievements.rules.AchievementRuleSet;
import de.mephisto.vpin.server.achievements.rules.RomRuleGenerator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global and per ROM achievement rules.
 * <p>
 * Per ROM rules come from the {@link RuleBundle} shipped with VPin Studio; bundled ROMs are also decoded with the
 * frozen map of the bundle. ROMs that are not bundled yet (maps added after the release) get their rules generated
 * from the live map in {@code resources/maps} and stored as {@code resources/achievements/rom/<rom>.ach.json} in the
 * Watcher's format; the next bundle normally contains the same rules, since they only depend on the map.
 * A {@code resources/achievements/global_achievements.json} overrides the bundled global rules.
 */
@Component
public class AchievementRuleStore {
  private static final Logger LOG = LoggerFactory.getLogger(AchievementRuleStore.class);

  private final Map<String, List<AchievementRule>> romRules = new ConcurrentHashMap<>();
  private volatile List<AchievementRule> globalRules;
  private volatile NvramAuditReader auditReader;
  private volatile RuleBundle bundle;
  private volatile boolean bundleLoaded;

  @NonNull
  public File getFolder() {
    return new File(SystemInfo.RESOURCES, "achievements");
  }

  @NonNull
  public File getRomFolder() {
    return new File(getFolder(), "rom");
  }

  /**
   * The reader for the live maps in {@code resources/maps}.
   */
  @NonNull
  public synchronized NvramAuditReader getLiveAuditReader() {
    if (auditReader == null) {
      auditReader = new NvramAuditReader(new File(SystemInfo.RESOURCES, "maps"));
    }
    return auditReader;
  }

  /**
   * @return the bundled rules and maps, null if no bundle is available.
   */
  @Nullable
  public RuleBundle getBundle() {
    if (!bundleLoaded) {
      synchronized (this) {
        if (!bundleLoaded) {
          bundle = RuleBundle.install(new File(getFolder(), "bundle"));
          bundleLoaded = true;
        }
      }
    }
    return bundle;
  }

  public boolean isBundled(@Nullable String rom) {
    RuleBundle b = getBundle();
    return b != null && b.contains(rom);
  }

  /**
   * The reader to decode the ROM with: the frozen map of the bundle for bundled ROMs, the live map otherwise.
   */
  @NonNull
  public NvramAuditReader getAuditReader(@Nullable String rom) {
    RuleBundle b = getBundle();
    if (b != null && b.contains(rom)) {
      return b.getReader();
    }
    return getLiveAuditReader();
  }

  public boolean isSupported(@Nullable String rom) {
    return getAuditReader(rom).isSupported(rom);
  }

  @Nullable
  public String resolveMapRom(@Nullable String rom) {
    return getAuditReader(rom).resolveMapRom(rom);
  }

  @NonNull
  public NvramSnapshot read(@NonNull String rom, @Nullable File nvFile) {
    return getAuditReader(rom).read(rom, nvFile);
  }

  @NonNull
  public List<AchievementRule> getGlobalRules() {
    if (globalRules == null) {
      synchronized (this) {
        if (globalRules == null) {
          globalRules = loadGlobalRules();
        }
      }
    }
    return globalRules;
  }

  private List<AchievementRule> loadGlobalRules() {
    File override = new File(getFolder(), "global_achievements.json");
    try {
      if (override.exists()) {
        LOG.info("Using global achievements from {}", override.getAbsolutePath());
        return Collections.unmodifiableList(AchievementRuleSet.load(override).getRules());
      }
      try (InputStream in = AchievementRuleStore.class.getResourceAsStream("/achievements/global_achievements.json")) {
        return Collections.unmodifiableList(AchievementRuleSet.load(in).getRules());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to load global achievements: {}", e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  public File getRomRuleFile(@NonNull String rom) {
    return new File(getRomFolder(), rom.toLowerCase() + ".ach.json");
  }

  /**
   * @return the bundled or stored rules of the ROM, null if the ROM is not bundled and its rules have not been
   * generated yet.
   */
  @Nullable
  public List<AchievementRule> getRomRules(@NonNull String rom) {
    String key = rom.toLowerCase();
    List<AchievementRule> cached = romRules.get(key);
    if (cached != null) {
      return cached;
    }
    RuleBundle b = getBundle();
    if (b != null && b.contains(key)) {
      List<AchievementRule> rules = b.getRules(key);
      if (rules != null) {
        romRules.put(key, rules);
      }
      return rules;
    }
    File file = getRomRuleFile(key);
    if (file.exists()) {
      try {
        List<AchievementRule> rules = Collections.unmodifiableList(AchievementRuleSet.load(file).getRules());
        romRules.put(key, rules);
        return rules;
      }
      catch (Exception e) {
        LOG.error("Failed to read {}: {}", file.getAbsolutePath(), e.getMessage());
      }
    }
    return null;
  }

  /**
   * Returns the rules of the ROM. ROMs that are not bundled get their rules generated once from the live map
   * (the audits of an empty NVRAM, see {@link NvramAuditReader#getSyntheticAudits(String)}).
   */
  @NonNull
  public List<AchievementRule> ensureRomRules(@NonNull String rom) {
    List<AchievementRule> existing = getRomRules(rom);
    if (existing != null) {
      return existing;
    }
    String key = rom.toLowerCase();
    NvramAuditReader live = getLiveAuditReader();
    if (!live.isSupported(key)) {
      return Collections.emptyList();
    }
    synchronized (this) {
      existing = getRomRules(key);
      if (existing != null) {
        return existing;
      }
      List<AchievementRule> rules = RomRuleGenerator.generate(key, live.getSyntheticAudits(key), live.getFields(key));
      if (rules.isEmpty()) {
        // remembered until the next restart, the map may be usable after a map update
        romRules.put(key, Collections.emptyList());
        return Collections.emptyList();
      }
      try {
        new AchievementRuleSet(new ArrayList<>(rules)).save(getRomRuleFile(key));
        LOG.info("Created achievement rules for {} (not bundled) with {} rules", key, rules.size());
      }
      catch (Exception e) {
        LOG.error("Failed to write achievement rules for {}: {}", key, e.getMessage());
      }
      List<AchievementRule> result = Collections.unmodifiableList(rules);
      romRules.put(key, result);
      return result;
    }
  }

  /**
   * Stores rule files imported from an Achievement Watcher installation, existing files are kept. Bundled ROMs
   * always use the bundled rules (generated from the same map version as the Watcher's).
   */
  public boolean importRomRules(@NonNull String rom, @NonNull File watcherRuleFile) {
    if (isBundled(rom)) {
      return false;
    }
    File target = getRomRuleFile(rom);
    if (target.exists()) {
      return false;
    }
    try {
      AchievementRuleSet set = AchievementRuleSet.load(watcherRuleFile);
      set.save(target);
      romRules.remove(rom.toLowerCase());
      return true;
    }
    catch (Exception e) {
      LOG.warn("Failed to import rule file {}: {}", watcherRuleFile, e.getMessage());
      return false;
    }
  }
}
