package de.mephisto.vpin.restclient.system;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FeaturesInfoTest {

  @Test
  public void testDisableWindowsOnlyFeatures() {
    FeaturesInfo features = new FeaturesInfo();
    features.disableWindowsOnlyFeatures();

    assertFalse(features.RECORDER);
    assertFalse(features.NVRAM_PARSING_USE_PINEMHI);
    assertFalse(features.DOF_TESTER_ENABLED);
    assertFalse(features.DMD_DEVICE_INI);
    assertFalse(features.HIGHSCORE_MONITORING);

    // the pure java nvram parsers keep working on every platform
    assertTrue(features.NVRAM_PARSING_USE_JAVAMAPS);
    assertTrue(features.NVRAM_PARSING_USE_SUPERHAC);
    assertTrue(features.BACKUPS_ENABLED);
  }
}
