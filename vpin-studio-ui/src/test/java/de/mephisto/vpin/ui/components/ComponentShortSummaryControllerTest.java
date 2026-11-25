package de.mephisto.vpin.ui.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentShortSummaryControllerTest {

  @Test
  void repositoryNameFromGithubUrl() {
    assertEquals("vpinball/pinmame", ComponentShortSummaryController.toRepositoryName("https://github.com/vpinball/pinmame/releases"));
    assertEquals("freezy/dmd-extensions", ComponentShortSummaryController.toRepositoryName("https://github.com/freezy/dmd-extensions"));
    assertEquals("https://example.com/x", ComponentShortSummaryController.toRepositoryName("https://example.com/x"));
    assertEquals("-", ComponentShortSummaryController.toRepositoryName(null));
  }

  @Test
  void latestVpxVersion() {
    assertTrue(ComponentShortSummaryController.isLatestVpxVersion("10.8.1.5436", "v10.8.1-5436-af26b2d93"));
    assertFalse(ComponentShortSummaryController.isLatestVpxVersion("10.8.0.1807", "v10.8.1-5436-af26b2d93"));
  }
}
