package de.mephisto.vpin.server.frontend.popper;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.server.frontend.popper.pupgames.PUPGameImporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PUPGameImporterTest {

  @Test
  public void testImport() {
    SystemInfo.RESOURCES = "../resources/";
    Assertions.assertNotEquals(0, PUPGameImporter.read(EmulatorType.Zaccaria, 0).size());
    Assertions.assertNotEquals(0, PUPGameImporter.read(EmulatorType.ZenFX, 0));
    Assertions.assertNotEquals(0, PUPGameImporter.read(EmulatorType.ZenFX3, 0));
    Assertions.assertNotEquals(0, PUPGameImporter.read(EmulatorType.PinballM, 0));
  }
}
