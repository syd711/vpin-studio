package de.mephisto.vpin.server.pinemhi;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PINemHiTest {

  @Test
  public void testIniReadWrite() throws Exception {
    INIConfiguration iniConfiguration = new INIConfiguration();
    iniConfiguration.setCommentLeadingCharsUsedInInput(";");
    iniConfiguration.setSeparatorUsedInOutput("=");
    iniConfiguration.setSeparatorUsedInInput("=");
    File f = new File("../" + PINemHiService.PINEMHI_FOLDER, PINemHiService.PINEMHI_INI);

    try (FileReader fileReader = new FileReader(f)) {
      iniConfiguration.read(fileReader);
    }

    String original = FileUtils.readFileToString(f, Charset.defaultCharset());

    iniConfiguration.write(new FileWriter(f));
    String updated = FileUtils.readFileToString(f, Charset.defaultCharset());
    assertEquals(original, updated);
  }
}
