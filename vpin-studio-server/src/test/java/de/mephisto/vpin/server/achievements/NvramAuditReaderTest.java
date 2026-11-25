package de.mephisto.vpin.server.achievements;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import de.mephisto.vpin.server.achievements.nvram.NvramAuditReader;
import de.mephisto.vpin.server.achievements.nvram.NvramSnapshot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class NvramAuditReaderTest {

  static final File NVRAMS = new File("../resources/nvrams");
  static NvramAuditReader reader;

  @BeforeAll
  static void setup() {
    reader = new NvramAuditReader(RomRuleGeneratorGoldenTest.MAPS);
  }

  @Test
  void resolvesFamilyFallback() {
    assertThat(reader.resolveMapRom("afm_113b")).isEqualTo("afm_113b");
    assertThat(reader.resolveMapRom("twd_160")).isEqualTo("twd_156");
    assertThat(reader.resolveMapRom("doesnotexist")).isNull();
  }

  /**
   * The Java decoder respects the platform memory layout, the Watcher decodes raw file offsets with heuristics.
   * For WPC both agree on the field set; numeric values are compared and the agreement is reported.
   */
  @ParameterizedTest
  @ValueSource(strings = {"afm_113b", "mm_109c", "tz_94h", "taf_l7", "tom_14h", "ss_15", "sttng_l7"})
  void decodesWpcAudits(String rom) throws Exception {
    NvramSnapshot snapshot = reader.read(rom, new File(NVRAMS, rom + ".nv"));
    LinkedHashMap<String, Object> watcher = new ObjectMapper().readValue(new File(RomRuleGeneratorGoldenTest.GOLDEN, rom + ".audits.json"), new TypeReference<>() {
    });
    assertThat(snapshot.audits()).isNotEmpty();
    int same = 0;
    int numeric = 0;
    for (Map.Entry<String, Object> e : watcher.entrySet()) {
      if (e.getValue() instanceof Number n) {
        numeric++;
        Object j = snapshot.audits().get(e.getKey());
        if (j instanceof Number jn && jn.longValue() == n.longValue()) {
          same++;
        }
      }
    }
    System.out.println(rom + ": java fields=" + snapshot.audits().size() + ", watcher fields=" + watcher.size()
        + ", equal numeric values " + same + "/" + numeric + ", players=" + snapshot.playerCount());
    assertThat(snapshot.audits().keySet()).containsAll(watcher.keySet().stream().filter(k -> watcher.get(k) instanceof Number).toList());
  }

  /**
   * bbb108/bbb109 have a list valued "recent_scores" entry in the game state, which failed to parse in nvmaps 1.0.1.
   */
  @Test
  void testListValuedGameState() throws Exception {
    net.nvrams.mapping.map.NVRamMapParser parser = new net.nvrams.mapping.map.NVRamMapParser(new File("../resources/maps").getAbsolutePath() + File.separator);
    assertThat(parser.getMap("bbb109").getGameState().getPlayerCount()).isNotNull();
    assertThat(parser.getMap("bbb108").getGameState().getPlayerCount()).isNotNull();
  }
}
