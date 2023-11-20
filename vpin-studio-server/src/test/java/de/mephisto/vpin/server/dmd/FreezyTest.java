package de.mephisto.vpin.server.dmd;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class FreezyTest {

  @Test
  public void testDmdIni() throws IOException {
    INIConfiguration iniConfiguration = new INIConfiguration();
    iniConfiguration.setCommentLeadingCharsUsedInInput(";");
    iniConfiguration.setSeparatorUsedInOutput("=");
    iniConfiguration.setSeparatorUsedInInput("=");

    File iniFile = new File("../testsystem/vPinball/VisualPinball/VPinMAME/DmdDevice.ini");
    FileReader fileReader = new FileReader(iniFile);
    try {
      iniConfiguration.read(fileReader);
    } catch (Exception e) {
      e.getMessage();
    } finally {
      fileReader.close();
    }

    Set<String> sections = iniConfiguration.getSections();

    System.out.println(iniConfiguration.getString("vni..key"));
    for (String section : sections) {
      System.out.println("Section " + section);
      SubnodeConfiguration section1 = iniConfiguration.getSection(section);
      Iterator<String> keys = section1.getKeys();
      while(keys.hasNext()) {
        String next = keys.next();
        System.out.println(section + ": " + next + "=" + section1.getString(next));
      }
    }

//    System.out.println(iniConfiguration.getString("vni.key"));

  }
}
