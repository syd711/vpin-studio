package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.server.VPinStudioException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class B2STableSettingsTest {

  @Test
  public void testTable() {
    File b2sFolder = new File("../testsystem/vPinball/VisualPinball/Tables");
    File b2sFile = new File(b2sFolder, "B2STableSettings.xml");
    B2STableSettingsParser parser = new B2STableSettingsParser();
    parser.parseSettingsXml(b2sFolder, b2sFile);
    DirectB2STableSettings settings = parser.getEntry("avr_200");
    Assertions.assertNotNull(settings);

    Assertions.assertTrue(settings.getHideGrill() > 0);
    Assertions.assertEquals(3, settings.getSolenoidsSkipFrames());
  }

  @Test
  public void testTableSerializer() throws Exception {
    File b2sFolder = new File("../testsystem/vPinball/VisualPinball/Tables");
    File b2sFile = new File(b2sFolder, "B2STableSettings.xml");
    String before = FileUtils.readFileToString(b2sFile, Charset.defaultCharset());

    B2STableSettingsParser parser = new B2STableSettingsParser();
    parser.parseSettingsXml(b2sFolder, b2sFile);
    DirectB2STableSettings settings = parser.getEntry("avr_200");
    Assertions.assertNotNull(settings);

    B2STableSettingsSerializer serializer = new B2STableSettingsSerializer();
    serializer.serializeXml(settings, b2sFile);

    String after = FileUtils.readFileToString(b2sFile, Charset.defaultCharset());
    Assertions.assertEquals(before, after);
  }

  @Test
  public void testSettingsParser() {
    File b2sFolder = new File("../testsystem/vPinball/VisualPinball/Tables");
    File b2sFile = new File(b2sFolder, "B2STableSettings.xml");
    B2STableSettingsParser parser = new B2STableSettingsParser();
    parser.parseSettingsXml(b2sFolder, b2sFile);
    DirectB2ServerSettings settings = parser.getServerSettings();
    Assertions.assertNotNull(settings);
    Assertions.assertTrue(settings.isPluginsOn());
  }

  @Test
  public void testSettingsSerializer() throws IOException, VPinStudioException {
    File b2sFolder = new File("../testsystem/vPinball/VisualPinball/Tables");
    File b2sFile = new File(b2sFolder, "B2STableSettings.xml");
    String before = FileUtils.readFileToString(b2sFile, Charset.defaultCharset());

    B2STableSettingsParser parser = new B2STableSettingsParser();
    parser.parseSettingsXml(b2sFolder, b2sFile);
    DirectB2ServerSettings settings = parser.getServerSettings();
    Assertions.assertNotNull(settings);

    B2STableSettingsSerializer serializer = new B2STableSettingsSerializer();
    serializer.serializeXml(settings, b2sFile);

    String after = FileUtils.readFileToString(b2sFile, Charset.defaultCharset());
    Assertions.assertEquals(before, after);
  }
}
