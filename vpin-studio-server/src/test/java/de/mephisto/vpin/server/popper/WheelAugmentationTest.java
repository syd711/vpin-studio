package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.server.frontend.popper.WheelAugmenter;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class WheelAugmentationTest {

  @Test
  public void testWheelAugmentation() {
    File wheelIcon = new File("../testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Jaws.png");
    long initialSize = wheelIcon.length();
    assertTrue(wheelIcon.exists());

    File badge = new File("../resources/competition-badges/discord.png");
    assertTrue(badge.exists());

    WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);
    augmenter.deAugment();
    augmenter.augment(badge);

    assertNotEquals(augmenter.getBackupWheelIcon().length(), 0);
    assertNotEquals(augmenter.getBackupWheelIcon().length(), wheelIcon.length());
    augmenter.deAugment();

    assertTrue(wheelIcon.exists());
    assertEquals(initialSize, wheelIcon.length());
  }

}
