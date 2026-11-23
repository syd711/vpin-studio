package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.server.achievements.bundle.RuleBundle;
import de.mephisto.vpin.server.achievements.bundle.RuleBundleBuilder;
import de.mephisto.vpin.server.achievements.bundle.RuleBundleInfo;
import de.mephisto.vpin.server.achievements.nvram.NvramAuditReader;
import de.mephisto.vpin.server.achievements.rules.AchievementRule;
import de.mephisto.vpin.server.achievements.rules.AchievementRuleSet;
import de.mephisto.vpin.server.achievements.rules.RomRuleGenerator;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleBundleTest {

  static final File BUNDLE_SOURCE = new File("src/main/resources/achievements/bundle");
  static final ObjectMapper MAPPER = new ObjectMapper();

  @TempDir
  static File installFolder;
  static RuleBundle bundle;

  @BeforeAll
  static void install() {
    bundle = RuleBundle.install(installFolder);
  }

  /**
   * The first bundle revision was generated from the maps the Watcher shipped with (eb0d7cf), so the rules of
   * migrated players match the rule files the Watcher generated with these maps.
   */
  @ParameterizedTest
  @ValueSource(strings = {"afm_113b", "mm_109c", "tz_94h", "taf_l7", "cc_13", "tom_14h", "apollo13", "lw3_208", "baywatch",
      "godzilla", "mb_106b", "ss_15", "xenon", "centaur", "sttng_l7", "bop_l7", "rs_l6", "bk2k_l4"})
  void bundledRulesMatchWatcher(String rom) throws Exception {
    File golden = new File("src/test/resources/achievements/golden-eb0d7cf", rom + ".ach.json");
    List<AchievementRule> expected = AchievementRuleSet.load(golden).getRules();
    assertThat(bundle).isNotNull();
    assertThat(MAPPER.writeValueAsString(bundle.getRules(rom))).isEqualTo(MAPPER.writeValueAsString(expected));
  }

  @Test
  void familyFallbackUsesBundledMap() {
    assertThat(bundle.contains("twd_160")).isTrue();
    List<AchievementRule> rules = bundle.getRules("twd_160");
    assertThat(rules).isNotEmpty();
    assertThat(rules.get(0).getTitle()).startsWith("twd_160 ");
    assertThat(bundle.contains("doesnotexist")).isFalse();
    assertThat(bundle.getRules("doesnotexist")).isNull();
  }

  /**
   * Every bundled ROM: the frozen map reproduces the bundled rules, so the decoded fields match the rule conditions.
   */
  @Test
  void frozenMapsReproduceAllBundledRules() throws Exception {
    NvramAuditReader reader = bundle.getReader();
    Map<String, String> index = MAPPER.readValue(new File(installFolder, RuleBundle.INDEX_JSON), new TypeReference<LinkedHashMap<String, String>>() {
    });
    index.keySet().removeIf(k -> k.startsWith("_"));
    assertThat(index).hasSizeGreaterThan(1500);
    for (String rom : index.keySet()) {
      List<AchievementRule> generated = RomRuleGenerator.generate(rom, reader.getSyntheticAudits(rom), reader.getFields(rom));
      assertThat(MAPPER.writeValueAsString(generated)).as(rom).isEqualTo(MAPPER.writeValueAsString(bundle.getRules(rom)));
    }
  }

  @Test
  void installIsSkippedForTheSameBundle() throws Exception {
    File marker = new File(installFolder, RuleBundle.INDEX_JSON);
    long modified = marker.lastModified();
    assertThat(RuleBundle.install(installFolder)).isNotNull();
    assertThat(marker.lastModified()).isEqualTo(modified);
    assertThat(Files.readAllBytes(new File(installFolder, RuleBundle.BUNDLE_JSON).toPath()))
        .isEqualTo(Files.readAllBytes(new File(BUNDLE_SOURCE, RuleBundle.BUNDLE_JSON).toPath()));
  }

  /**
   * The builder only adds ROMs; a changed map of a bundled ROM never changes its bundled files.
   */
  @Test
  void builderIsAppendOnly(@TempDir File tmp) throws Exception {
    File maps = new File(tmp, "maps");
    File out = new File(tmp, "bundle");
    FileUtils.copyDirectory(RomRuleGeneratorGoldenTest.MAPS, maps);

    RuleBundleBuilder.Result first = RuleBundleBuilder.build(maps, "v1", out);
    assertThat(first.added).hasSize(20);
    assertThat(first.revision).isEqualTo(1);
    String afmRules = Files.readString(new File(out, RuleBundle.rulesPath(index(out).get("afm_113b"))).toPath());

    // rename a field of the afm map that is used by its rules
    File afmMap = new File(maps, index(maps).get("afm_113b"));
    String json = Files.readString(afmMap.toPath());
    String label = AchievementRuleSet.MAPPER.readValue(afmRules, AchievementRuleSet.class).getRules().stream()
        .filter(r -> r.getCondition() != null && r.getCondition().getField() != null)
        .findFirst().orElseThrow().getCondition().getField();
    assertThat(json).contains("\"" + label + "\"");
    Files.writeString(afmMap.toPath(), json.replace("\"" + label + "\"", "\"" + label + " (renamed)\""));

    RuleBundleBuilder.Result second = RuleBundleBuilder.build(maps, "v2", out);
    assertThat(second.added).isEmpty();
    assertThat(second.heldBack).contains("afm_113b");
    assertThat(second.revision).isEqualTo(1);
    assertThat(Files.readString(new File(out, RuleBundle.rulesPath(index(out).get("afm_113b"))).toPath())).isEqualTo(afmRules);

    // a new ROM is added with the current map, existing ROMs stay untouched
    File indexFile = new File(maps, RuleBundle.INDEX_JSON);
    ObjectNode idx = (ObjectNode) MAPPER.readTree(indexFile);
    idx.put("afm_999", idx.get("afm_113b").asString());
    MAPPER.writeValue(indexFile, idx);

    RuleBundleBuilder.Result third = RuleBundleBuilder.build(maps, "v3", out);
    assertThat(third.added).containsExactly("afm_999");
    assertThat(third.revision).isEqualTo(2);
    assertThat(index(out).get("afm_999")).isNotEqualTo(index(out).get("afm_113b"));
    assertThat(Files.readString(new File(out, RuleBundle.rulesPath(index(out).get("afm_113b"))).toPath())).isEqualTo(afmRules);
    RuleBundleInfo info = RuleBundleInfo.MAPPER.readValue(new File(out, RuleBundle.BUNDLE_JSON), RuleBundleInfo.class);
    assertThat(info.getSources()).extracting(RuleBundleInfo.Source::getMaps).containsExactly("v1", "v3");
  }

  private static Map<String, String> index(File folder) throws Exception {
    return MAPPER.readValue(new File(folder, RuleBundle.INDEX_JSON), new TypeReference<LinkedHashMap<String, String>>() {
    });
  }
}
