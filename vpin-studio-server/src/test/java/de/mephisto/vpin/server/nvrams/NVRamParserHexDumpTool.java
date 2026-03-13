package de.mephisto.vpin.server.nvrams;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import de.mephisto.vpin.server.nvrams.parser.NVRamToolHexDump;
import de.mephisto.vpin.server.nvrams.parser.SparseMemory;

/**
 * Uses test data from https://github.com/tomlogic/py-pinmame-nvmaps
 * This project uses a snapshot version of https://github.com/tomlogic/pinmame-nvram-maps/
 */
public class NVRamParserHexDumpTool {

  /** Root url for test, see above comment  */
  public static final String MAPS_ROOT = "https://raw.githubusercontent.com/tomlogic/pinmame-nvram-maps/d7b9d881753def5f92e7858bea8bec84a52bc77e/";

  public static final String TEST_ROOT = "https://github.com/tomlogic/py-pinmame-nvmaps/raw/refs/heads/main/test/";

  public static void main(String[] args) {
    NVRamParserHexDumpTool tool = new NVRamParserHexDumpTool();
    tool.run("alpok_l6");
  }

  public void run(String rom) {
    try  {
      File mainFolder = new File("./testsystem/vPinball/VisualPinball/VPinMAME/nvram/");
      File entry = new File(mainFolder, rom + ".nv");

      byte[] bytes = Files.readAllBytes(entry.toPath());

      NVRamParser parser = new NVRamParser();
      NVRamMap mapJson = new NVRamMap();
      SparseMemory memory = parser.setNvram(mapJson, bytes);
      NVRamToolHexDump dump = new NVRamToolHexDump();
      String txt = dump.hexDump(mapJson, memory, Locale.ENGLISH);

      System.out.println(txt);
    }
    catch (IOException ioe) {
    }
  }
}
