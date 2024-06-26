package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.commons.utils.WinRegistry;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public class B2STableSettingsTest {

  @Test
  public void testTable() {
    File b2sFile = new File("../testsystem/vPinball/VisualPinball/Tables/B2STableSettings.xml");
    B2STableSettingsParser parser = new B2STableSettingsParser(b2sFile);
    DirectB2STableSettings settings = parser.getEntry("avr_200");
    Assertions.assertNotNull(settings);

    Assertions.assertTrue(settings.getHideGrill() > 0);
    Assertions.assertEquals(3, settings.getSolenoidsSkipFrames());
  }

//  @Test
  public void testWinRegRead() {
    Map<String, Object> values = WinRegistry.getClassesValues(".res\\b2sserver.res\\ShellNew");
    Assertions.assertFalse(values.isEmpty());
  }

  @Test
  public void testTableSerializer() throws Exception {
    File b2sFile = new File("../testsystem/vPinball/VisualPinball/Tables/B2STableSettings.xml");
    String before = FileUtils.readFileToString(b2sFile, Charset.defaultCharset());

    B2STableSettingsParser parser = new B2STableSettingsParser(b2sFile);
    DirectB2STableSettings settings = parser.getEntry("avr_200");
    Assertions.assertNotNull(settings);

    B2STableSettingsSerializer serializer = new B2STableSettingsSerializer(b2sFile);
    serializer.serialize(settings);

    String after = FileUtils.readFileToString(b2sFile, Charset.defaultCharset());
    Assertions.assertEquals(before, after);
  }

  @Test
  public void testSettingsParser() {
    File b2sFile = new File("../testsystem/vPinball/VisualPinball/Tables/B2STableSettings.xml");
    B2SServerSettingsParser parser = new B2SServerSettingsParser(b2sFile);
    DirectB2ServerSettings settings = parser.getSettings();
    Assertions.assertNotNull(settings);
    Assertions.assertTrue(settings.isPluginsOn());
  }

  @Test
  public void testSettingsSerializer() throws IOException {
    File b2sFile = new File("../testsystem/vPinball/VisualPinball/Tables/B2STableSettings.xml");
    String before = FileUtils.readFileToString(b2sFile, Charset.defaultCharset());

    B2SServerSettingsParser parser = new B2SServerSettingsParser(b2sFile);
    DirectB2ServerSettings settings = parser.getSettings();
    Assertions.assertNotNull(settings);

    B2SServerSettingsSerializer serializer = new B2SServerSettingsSerializer(b2sFile);
    serializer.serialize(settings);

    String after = FileUtils.readFileToString(b2sFile, Charset.defaultCharset());
    Assertions.assertEquals(before, after);
  }
}
