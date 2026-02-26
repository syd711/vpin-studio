package de.mephisto.vpin.server.nvrams;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.mephisto.vpin.server.nvrams.parser.NVRamMap;
import de.mephisto.vpin.server.nvrams.parser.NVRamMapping;
import de.mephisto.vpin.server.nvrams.parser.NVRamParser;
import de.mephisto.vpin.server.nvrams.parser.NVRamToolDump;
import de.mephisto.vpin.server.nvrams.parser.SparseMemory;

/**
 * Uses test data from https://github.com/tomlogic/py-pinmame-nvmaps
 * This project uses a snapshot version of https://github.com/tomlogic/pinmame-nvram-maps/
 */
public class NVRamParserTest {

  /** Root url for test, see above comment  */
  public static final String MAPS_ROOT = "https://raw.githubusercontent.com/tomlogic/pinmame-nvram-maps/d7b9d881753def5f92e7858bea8bec84a52bc77e/";

  public static final String TEST_ROOT = "https://github.com/tomlogic/py-pinmame-nvmaps/raw/refs/heads/main/test/";

  @Test
  public void testAllDump() throws IOException {
    NVRamParser parser = new NVRamParser(MAPS_ROOT);
    String indexJsonUrl = MAPS_ROOT + "index.json";

    // optional ROM, to start with, leave null for all
    String romStart = "t2_l8";

    parser.download(indexJsonUrl, in -> {
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> values = mapper.readValue(in, new TypeReference<Map<String, Object>>() {});
      for (String key : values.keySet()) {
        if (romStart == null || key.compareTo(romStart) >= 0) {
          checkRom(parser, key, false);
        }
      }
      return null;
    });
  }

  @Test
  public void testOneDump() throws IOException {
    NVRamParser parser = new NVRamParser(MAPS_ROOT);
    checkRom(parser, " t2_l8", true);
  }

  private void checkRom(NVRamParser parser, String rom, boolean runAssert) throws IOException {
    System.out.println("checking " + rom + "...");
    parseNVRam(parser, rom, (mapJson, memory) -> {
      String result = TEST_ROOT + "expected/" + rom + ".nv.txt";
      try {
        parser.download(result, res -> {

          NVRamToolDump tool = new NVRamToolDump();
          String dump = tool.dump(mapJson, memory, Locale.ENGLISH, true);

          String expected = IOUtils.toString(res, StandardCharsets.UTF_8);
          // check files
          if (runAssert) {
            Assertions.assertEquals(expected, dump);
          }
          else {
            if (!expected.equals(dump)) {
              System.out.println("=> file are different for " + rom);
            }
          }

          return null;
        });
      }
      catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    });
  }

  //-------------------------------------------

  @Test
  public void testDumpPlayerCount() throws IOException {
    String rom = "bcats_l5";

    NVRamParser parser = new NVRamParser(MAPS_ROOT);
    parseNVRam(parser, rom, (mapJson, memory) -> {
        NVRamMapping m = mapJson.getGameState().getPlayerCount();
        String e = m.formatEntry(mapJson, memory, Locale.ENGLISH);
        assertEquals("1", e);
    });
  }

  //-------------------------------------------

  private void parseNVRam(NVRamParser parser, String rom, BiConsumer<NVRamMap, SparseMemory> consumer) throws IOException {
    String testnv = TEST_ROOT + "nvram/" + rom + ".nv";
    parser.download(testnv, in -> {
      byte[] bytes = IOUtils.toByteArray(in);
      NVRamMap mapJson = parser.getMap(rom + ".nv", null);
      SparseMemory memory = parser.setNvram(mapJson, bytes);
      consumer.accept(mapJson, memory);
      return null;
    });
  }
}
