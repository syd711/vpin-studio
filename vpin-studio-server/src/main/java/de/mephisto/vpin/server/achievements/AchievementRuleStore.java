package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.commons.SystemInfo;
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
 * Global and per ROM achievement rules. Per ROM rules are generated once (like the Watcher, on the first time
 * the audits of the ROM are available) and stored as {@code resources/achievements/rom/<rom>.ach.json} in the
 * Watcher's format. A {@code resources/achievements/global_achievements.json} overrides the bundled global rules.
 */
@Component
public class AchievementRuleStore {
  private static final Logger LOG = LoggerFactory.getLogger(AchievementRuleStore.class);

  private final Map<String, List<AchievementRule>> romRules = new ConcurrentHashMap<>();
  private volatile List<AchievementRule> globalRules;
  private volatile NvramAuditReader auditReader;

  @NonNull
  public File getFolder() {
    return new File(SystemInfo.RESOURCES, "achievements");
  }

  @NonNull
  public File getRomFolder() {
    return new File(getFolder(), "rom");
  }

  @NonNull
  public synchronized NvramAuditReader getAuditReader() {
    if (auditReader == null) {
      auditReader = new NvramAuditReader(new File(SystemInfo.RESOURCES, "maps"));
    }
    return auditReader;
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
   * @return the stored rules of the ROM, null if they have not been generated yet.
   */
  @Nullable
  public List<AchievementRule> getRomRules(@NonNull String rom) {
    String key = rom.toLowerCase();
    List<AchievementRule> cached = romRules.get(key);
    if (cached != null) {
      return cached;
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
   * Returns the rules of the ROM and generates them from the given audits if they do not exist yet
   * (port of {@code _ensure_rom_specific}).
   */
  @NonNull
  public List<AchievementRule> ensureRomRules(@NonNull String rom, @Nullable NvramSnapshot audits) {
    List<AchievementRule> existing = getRomRules(rom);
    if (existing != null) {
      return existing;
    }
    if (audits == null || audits.isEmpty()) {
      return Collections.emptyList();
    }
    String key = rom.toLowerCase();
    synchronized (this) {
      existing = getRomRules(key);
      if (existing != null) {
        return existing;
      }
      List<AchievementRule> rules = RomRuleGenerator.generate(key, audits.audits(), getAuditReader().getFields(key));
      if (rules.isEmpty()) {
        return Collections.emptyList();
      }
      try {
        new AchievementRuleSet(new ArrayList<>(rules)).save(getRomRuleFile(key));
        LOG.info("Created achievement rules for {} with {} rules", key, rules.size());
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
   * Stores rule files imported from an Achievement Watcher installation, existing files are kept.
   */
  public boolean importRomRules(@NonNull String rom, @NonNull File watcherRuleFile) {
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
