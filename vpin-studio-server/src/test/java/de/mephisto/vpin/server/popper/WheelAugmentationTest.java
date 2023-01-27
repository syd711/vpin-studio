package de.mephisto.vpin.server.popper;

import org.junit.jupiter.api.Test;

import java.io.File;

public class WheelAugmentationTest {

  @Test
  public void testWheelAugmentation() {
    File wheelIcon = new File("C:\\vPinball\\PinUPSystem\\POPMedia\\Visual Pinball X\\Wheel\\AC-DC (2012).png");
    File badge = new File("E:\\Development\\workspace\\vpin-studio\\resources\\competition-badges\\discord.png");

    WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);
    augmenter.deAugment();
    augmenter.augment(badge);
    augmenter.deAugment();
  }

}
