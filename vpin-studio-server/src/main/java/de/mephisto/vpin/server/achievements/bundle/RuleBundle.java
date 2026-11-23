package de.mephisto.vpin.server.achievements.bundle;

import de.mephisto.vpin.server.achievements.nvram.NvramAuditReader;
import de.mephisto.vpin.server.achievements.rules.AchievementRule;
import de.mephisto.vpin.server.achievements.rules.AchievementRuleSet;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The per ROM achievement rules shipped with VPin Studio, together with the frozen NVRAM maps they were generated
 * from. Bundled ROMs are always decoded with their frozen map, so the rules stay reachable when the live maps in
 * {@code resources/maps} change, and every cabinet has the same rules for a ROM.
 * <p>
 * Layout (classpath {@code /achievements/bundle/}, see {@link RuleBundleBuilder}):
 * <ul>
 *   <li>{@code bundle.json} – {@link RuleBundleInfo}, written last, used to detect a new bundle</li>
 *   <li>{@code index.json} – ROM to frozen map, in the format of the map repository</li>
 *   <li>{@code maps/...<hash>.map.json} – frozen maps, the platform renamed to its frozen file</li>
 *   <li>{@code platforms/<name>.<hash>.json} – frozen platform files</li>
 *   <li>{@code rules/...<hash>.ach.json} – the rules per frozen map, titles prefixed with {@link #ROM_PLACEHOLDER}</li>
 * </ul>
 * The parser only reads files, so the bundle is extracted to {@code resources/achievements/bundle} on first use.
 */
public class RuleBundle {
  private static final Logger LOG = LoggerFactory.getLogger(RuleBundle.class);

  public static final String CLASSPATH_ROOT = "/achievements/bundle/";
  public static final String BUNDLE_JSON = "bundle.json";
  public static final String INDEX_JSON = "index.json";
  public static final String ROM_PLACEHOLDER = "{rom}";

  private final File folder;
  private final NvramAuditReader reader;
  private final Map<String, List<AchievementRule>> templates = new ConcurrentHashMap<>();

  private RuleBundle(@NonNull File folder) {
    this.folder = folder;
    this.reader = new NvramAuditReader(folder);
  }

  /**
   * Extracts the bundle shipped on the classpath to the given folder (if it changed) and opens it.
   *
   * @return null if no bundle is shipped or it could not be extracted.
   */
  @Nullable
  public static RuleBundle install(@NonNull File folder) {
    try {
      byte[] info = readClasspath(BUNDLE_JSON);
      if (info == null) {
        LOG.info("No achievement rule bundle available.");
        return null;
      }
      File marker = new File(folder, BUNDLE_JSON);
      if (!marker.exists() || !Arrays.equals(info, Files.readAllBytes(marker.toPath()))) {
        extract(folder, info);
      }
      RuleBundle bundle = new RuleBundle(folder);
      LOG.info("Achievement rule bundle: {} ROMs", bundle.reader.getSupportedRomCount());
      return bundle;
    }
    catch (Exception e) {
      LOG.error("Failed to install the achievement rule bundle: {}", e.getMessage(), e);
      return null;
    }
  }

  private static void extract(@NonNull File folder, byte[] info) throws IOException {
    LOG.info("Extracting achievement rule bundle to {}", folder.getAbsolutePath());
    if (folder.exists()) {
      FileUtils.deleteDirectory(folder);
    }
    byte[] index = readClasspath(INDEX_JSON);
    if (index == null) {
      throw new IOException("bundle index.json missing");
    }
    Map<String, String> roms = RuleBundleInfo.MAPPER.readValue(index, new TypeReference<LinkedHashMap<String, String>>() {
    });
    RuleBundleInfo bundleInfo = RuleBundleInfo.MAPPER.readValue(info, RuleBundleInfo.class);

    Set<String> files = new LinkedHashSet<>();
    files.add(INDEX_JSON);
    for (Map.Entry<String, String> e : roms.entrySet()) {
      if (!e.getKey().startsWith("_")) {
        files.add(e.getValue());
        files.add(rulesPath(e.getValue()));
      }
    }
    files.addAll(bundleInfo.getPlatforms());
    for (String file : files) {
      byte[] data = readClasspath(file);
      if (data == null) {
        throw new IOException("bundle file missing: " + file);
      }
      write(new File(folder, file), data);
    }
    write(new File(folder, BUNDLE_JSON), info);
  }

  private static void write(File target, byte[] data) throws IOException {
    target.getParentFile().mkdirs();
    Files.write(target.toPath(), data);
  }

  @Nullable
  private static byte[] readClasspath(String path) throws IOException {
    try (InputStream in = RuleBundle.class.getResourceAsStream(CLASSPATH_ROOT + path)) {
      return in != null ? in.readAllBytes() : null;
    }
  }

  /**
   * "maps/williams/wpc/afm_113.1a2b3c4d.map.json" -> "rules/williams/wpc/afm_113.1a2b3c4d.ach.json"
   */
  @NonNull
  public static String rulesPath(@NonNull String mapPath) {
    String p = mapPath.startsWith("maps/") ? mapPath.substring("maps/".length()) : mapPath;
    if (p.endsWith(".map.json")) {
      p = p.substring(0, p.length() - ".map.json".length());
    }
    return "rules/" + p + ".ach.json";
  }

  /**
   * Replaces the {@link #ROM_PLACEHOLDER} of the bundled titles with the ROM.
   */
  @NonNull
  public static List<AchievementRule> forRom(@NonNull List<AchievementRule> template, @NonNull String rom) {
    List<AchievementRule> rules = new ArrayList<>(template.size());
    for (AchievementRule r : template) {
      String title = r.getTitle();
      if (title != null && title.startsWith(ROM_PLACEHOLDER)) {
        title = rom + title.substring(ROM_PLACEHOLDER.length());
      }
      rules.add(new AchievementRule(title, r.getCondition(), r.getScope()));
    }
    return rules;
  }

  @NonNull
  public NvramAuditReader getReader() {
    return reader;
  }

  @NonNull
  public File getFolder() {
    return folder;
  }

  public boolean contains(@Nullable String rom) {
    return reader.isSupported(rom);
  }

  /**
   * @return the bundled rules of the ROM, null if the ROM is not part of the bundle.
   */
  @Nullable
  public List<AchievementRule> getRules(@NonNull String rom) {
    String mapPath = reader.getMapPath(rom);
    if (mapPath == null) {
      return null;
    }
    List<AchievementRule> template = templates.computeIfAbsent(mapPath, p -> {
      File file = new File(folder, rulesPath(p));
      try {
        return Collections.unmodifiableList(AchievementRuleSet.load(file).getRules());
      }
      catch (Exception e) {
        LOG.error("Failed to read bundled rules {}: {}", file.getAbsolutePath(), e.getMessage());
        return Collections.emptyList();
      }
    });
    if (template.isEmpty()) {
      return null;
    }
    return Collections.unmodifiableList(forRom(template, rom.toLowerCase()));
  }
}
