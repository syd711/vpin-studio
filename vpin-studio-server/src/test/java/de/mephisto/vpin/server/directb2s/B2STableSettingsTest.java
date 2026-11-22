package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.server.VPinStudioException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    serializer.serializeXml(settings, b2sFile, true);

    String after = FileUtils.readFileToString(b2sFile, Charset.defaultCharset());
    Assertions.assertEquals(before, after);
  }

  @Test
  public void testCorruptedFileIsNotOverwritten(@TempDir File tmp) throws Exception {
    File b2sFile = new File(tmp, "B2STableSettings.xml");
    String corrupted = "<avr_200><HideGrill>1</HideGrill></avr_200>\n<B2STableSettings>\n  <ArePluginsOn>1</ArePluginsOn>";
    FileUtils.writeStringToFile(b2sFile, corrupted, StandardCharsets.UTF_8);

    DirectB2STableSettings settings = new DirectB2STableSettings();
    settings.setRom("test_rom");
    B2STableSettingsSerializer serializer = new B2STableSettingsSerializer();
    Assertions.assertThrows(VPinStudioException.class, () -> serializer.serializeXml(settings, b2sFile, true));

    Assertions.assertEquals(corrupted, FileUtils.readFileToString(b2sFile, StandardCharsets.UTF_8));
  }

  @Test
  public void testMissingFileIsCreated(@TempDir File tmp) throws Exception {
    File b2sFile = new File(tmp, "B2STableSettings.xml");
    DirectB2STableSettings settings = new DirectB2STableSettings();
    settings.setRom("test_rom");
    new B2STableSettingsSerializer().serializeXml(settings, b2sFile, true);

    B2STableSettingsParser parser = new B2STableSettingsParser();
    parser.parseSettingsXml(tmp, b2sFile);
    Assertions.assertNotNull(parser.getEntry("test_rom"));
    Assertions.assertFalse(new File(tmp, "B2STableSettings.xml.tmp").exists());
  }

  @Test
  public void testConcurrentSavesKeepFileValid(@TempDir File tmp) throws Exception {
    File source = new File("../testsystem/vPinball/VisualPinball/Tables/B2STableSettings.xml");
    File b2sFile = new File(tmp, "B2STableSettings.xml");
    FileUtils.copyFile(source, b2sFile);

    int threads = 8;
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    try {
      List<Future<?>> futures = new ArrayList<>();
      for (int i = 0; i < threads; i++) {
        String rom = "concurrent_" + i;
        futures.add(executor.submit(() -> {
          for (int j = 0; j < 10; j++) {
            DirectB2STableSettings settings = new DirectB2STableSettings();
            settings.setRom(rom);
            settings.setSolenoidsSkipFrames(j);
            new B2STableSettingsSerializer().serializeXml(settings, b2sFile, true);
          }
          return null;
        }));
      }
      for (Future<?> f : futures) {
        f.get();
      }
    }
    finally {
      executor.shutdownNow();
    }

    B2STableSettingsParser parser = new B2STableSettingsParser();
    parser.parseSettingsXml(tmp, b2sFile);
    Assertions.assertNotNull(parser.getEntry("avr_200"));
    for (int i = 0; i < threads; i++) {
      DirectB2STableSettings entry = parser.getEntry("concurrent_" + i);
      Assertions.assertNotNull(entry);
      Assertions.assertEquals(9, entry.getSolenoidsSkipFrames());
    }
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
    serializer.serializeXml(settings, b2sFile, true);

    String after = FileUtils.readFileToString(b2sFile, Charset.defaultCharset());
    Assertions.assertEquals(before, after);
  }
}
