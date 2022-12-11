package de.mephisto.vpin.server.popper;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;

public class PinUPControlTest {

  @Test
  public void testFunctions() throws FileNotFoundException {
//    SqliteConnector connector = new SqliteConnector(new RomManager());
//    List<PinUPControl> functions = connector.getControls();
//    assertFalse(functions.isEmpty());
  }

  @Test
  public void testWheelAugmentation() {
    File wheelIcon = new File("C:\\vPinball\\PinUPSystem\\POPMedia\\Visual Pinball X\\Wheel\\Aliens 2.0.png");
    File badge = new File("E:\\Development\\workspace\\vpin-studio\\resources\\competition-badges\\trophy-1.png");

    WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);
    augmenter.deAugment();
    augmenter.augment(badge);
  }

}
