package de.mephisto.vpin.server.achievements.bundle;

import de.mephisto.vpin.server.achievements.nvram.NvramAuditReader;
import de.mephisto.vpin.server.achievements.rules.AchievementRule;
import de.mephisto.vpin.server.achievements.rules.AchievementRuleSet;
import de.mephisto.vpin.server.achievements.rules.RomRuleGenerator;
import org.jspecify.annotations.NonNull;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Developer tool that adds new ROMs to the achievement rule bundle ({@link RuleBundle}). Run it before a release
 * with the current NVRAM maps:
 * <pre>
 *   RuleBundleBuilder &lt;map folder&gt; &lt;map version, e.g. the commit id&gt; [bundle folder]
 * </pre>
 * The bundle only grows: ROMs that are already bundled are never changed, even if their map changed since. They are
 * reported as "held back"; changing them would change the titles of achievements that players already unlocked.
 * Maps and platform files are stored under their content hash, so different versions of a map can coexist.
 */
public class RuleBundleBuilder {

  public static final String DEFAULT_BUNDLE_FOLDER = "vpin-studio-server/src/main/resources/achievements/bundle";

  private static final ObjectMapper COMPACT = JsonMapper.builder().build();

  public static class Result {
    public final List<String> added = new ArrayList<>();
    /** bundled ROMs that would get different rules from the source maps, they keep their frozen map and rules */
    public final List<String> heldBack = new ArrayList<>();
    /** bundled ROMs whose map file changed without changing the rules, they keep their frozen map too */
    public final List<String> mapChanged = new ArrayList<>();
    /** ROMs of the source maps without achievement rules (no usable fields) */
    public final List<String> skipped = new ArrayList<>();
    public int revision;
  }

  private record FrozenMap(String mapPath, byte[] map, String platformPath, byte[] platform) {
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("Usage: RuleBundleBuilder <map folder> <map version> [bundle folder, default " + DEFAULT_BUNDLE_FOLDER + "]");
      System.exit(1);
    }
    File bundleFolder = new File(args.length > 2 ? args[2] : DEFAULT_BUNDLE_FOLDER);
    Result result = build(new File(args[0]), args[1], bundleFolder);
    System.out.println("Bundle revision " + result.revision + ": " + result.added.size() + " ROM(s) added, "
        + result.heldBack.size() + " held back, " + result.mapChanged.size() + " with a changed map but the same rules, "
        + result.skipped.size() + " without rules.");
    if (!result.heldBack.isEmpty()) {
      System.out.println("Held back (the current map would change the rules, the bundled map and rules are kept): "
          + String.join(", ", result.heldBack));
    }
    if (!result.skipped.isEmpty()) {
      System.out.println("Without rules: " + String.join(", ", result.skipped));
    }
  }

  @NonNull
  public static Result build(@NonNull File sourceMaps, @NonNull String mapsVersion, @NonNull File bundleFolder) throws IOException {
    Result result = new Result();
    File indexFile = new File(bundleFolder, RuleBundle.INDEX_JSON);
    File infoFile = new File(bundleFolder, RuleBundle.BUNDLE_JSON);

    TreeMap<String, String> index = new TreeMap<>();
    if (indexFile.exists()) {
      index.putAll(readIndex(indexFile));
    }
    RuleBundleInfo info = infoFile.exists() ? RuleBundleInfo.MAPPER.readValue(infoFile, RuleBundleInfo.class) : new RuleBundleInfo();
    TreeSet<String> platforms = new TreeSet<>(info.getPlatforms());

    Map<String, String> sourceIndex = readIndex(new File(sourceMaps, RuleBundle.INDEX_JSON));
    NvramAuditReader source = new NvramAuditReader(sourceMaps);
    Map<String, FrozenMap> frozen = new HashMap<>();
    Map<String, String> addedRules = new LinkedHashMap<>();

    for (String rom : new TreeSet<>(sourceIndex.keySet())) {
      String sourcePath = sourceIndex.get(rom);
      FrozenMap map = frozen.get(sourcePath);
      if (map == null) {
        map = freeze(sourceMaps, sourcePath);
        frozen.put(sourcePath, map);
      }
      List<AchievementRule> rules = RomRuleGenerator.generate(RuleBundle.ROM_PLACEHOLDER, source.getSyntheticAudits(rom), source.getFields(rom));
      if (index.containsKey(rom)) {
        String bundledPath = index.get(rom);
        if (!bundledPath.equals(map.mapPath())) {
          String bundledRules = Files.readString(new File(bundleFolder, RuleBundle.rulesPath(bundledPath)).toPath());
          if (bundledRules.equals(COMPACT.writeValueAsString(new AchievementRuleSet(rules)))) {
            result.mapChanged.add(rom);
          }
          else {
            result.heldBack.add(rom);
          }
        }
        continue;
      }
      if (rules.isEmpty()) {
        result.skipped.add(rom);
        continue;
      }
      writeIfMissing(new File(bundleFolder, map.mapPath()), map.map());
      if (map.platformPath() != null) {
        writeIfMissing(new File(bundleFolder, map.platformPath()), map.platform());
        platforms.add(map.platformPath());
      }
      String rulesJson = COMPACT.writeValueAsString(new AchievementRuleSet(rules));
      writeIfMissing(new File(bundleFolder, RuleBundle.rulesPath(map.mapPath())), rulesJson.getBytes(StandardCharsets.UTF_8));
      index.put(rom, map.mapPath());
      addedRules.put(rom, rulesJson);
      result.added.add(rom);
    }

    result.revision = info.getRevision();
    if (result.added.isEmpty()) {
      return result;
    }

    Map<String, String> indexJson = new LinkedHashMap<>();
    indexJson.put("_note", "Generated by RuleBundleBuilder, do not edit. ROMs are only added, never changed.");
    indexJson.putAll(index);
    bundleFolder.mkdirs();
    RuleBundleInfo.MAPPER.writeValue(indexFile, indexJson);

    verify(bundleFolder, addedRules);

    result.revision = info.getRevision() + 1;
    info.setRevision(result.revision);
    info.getSources().add(new RuleBundleInfo.Source(result.revision, mapsVersion, result.added.size()));
    info.setPlatforms(new ArrayList<>(platforms));
    RuleBundleInfo.MAPPER.writeValue(infoFile, info);
    return result;
  }

  /**
   * The frozen maps must produce exactly the rules generated from the source maps, and the stored rule file of a
   * map must match (it may have been written for another ROM of the same map).
   */
  private static void verify(File bundleFolder, Map<String, String> addedRules) throws IOException {
    NvramAuditReader bundled = new NvramAuditReader(bundleFolder);
    for (Map.Entry<String, String> e : addedRules.entrySet()) {
      String rom = e.getKey();
      String json = COMPACT.writeValueAsString(new AchievementRuleSet(
          RomRuleGenerator.generate(RuleBundle.ROM_PLACEHOLDER, bundled.getSyntheticAudits(rom), bundled.getFields(rom))));
      String mapPath = bundled.getMapPath(rom);
      String stored = mapPath != null ? Files.readString(new File(bundleFolder, RuleBundle.rulesPath(mapPath)).toPath()) : null;
      if (!e.getValue().equals(json) || !e.getValue().equals(stored)) {
        throw new IllegalStateException("The frozen map of " + rom + " (" + mapPath + ") does not reproduce its rules.");
      }
    }
  }

  /**
   * Compacts the map and points it to the frozen copy of its platform file; both are named by their content hash.
   */
  private static FrozenMap freeze(File sourceMaps, String sourcePath) throws IOException {
    ObjectNode map = (ObjectNode) COMPACT.readTree(new File(sourceMaps, sourcePath));
    String platformPath = null;
    byte[] platform = null;
    JsonNode metadata = map.get("_metadata");
    if (metadata instanceof ObjectNode meta && meta.hasNonNull("platform")) {
      String name = meta.get("platform").asString();
      JsonNode platformJson = COMPACT.readTree(new File(sourceMaps, "platforms/" + name + ".json"));
      platform = COMPACT.writeValueAsBytes(platformJson);
      String frozenName = name + "." + hash(platform);
      platformPath = "platforms/" + frozenName + ".json";
      meta.put("platform", frozenName);
    }
    byte[] data = COMPACT.writeValueAsBytes(map);
    String base = sourcePath.endsWith(".map.json") ? sourcePath.substring(0, sourcePath.length() - ".map.json".length()) : sourcePath;
    if (!base.startsWith("maps/")) {
      base = "maps/" + base;
    }
    return new FrozenMap(base + "." + hash(data) + ".map.json", data, platformPath, platform);
  }

  private static Map<String, String> readIndex(File file) throws IOException {
    Map<String, String> index = RuleBundleInfo.MAPPER.readValue(file, new TypeReference<LinkedHashMap<String, String>>() {
    });
    index.keySet().removeIf(k -> k.startsWith("_"));
    return index;
  }

  private static void writeIfMissing(File file, byte[] data) throws IOException {
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      Files.write(file.toPath(), data);
    }
  }

  private static String hash(byte[] data) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)).substring(0, 8);
    }
    catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
