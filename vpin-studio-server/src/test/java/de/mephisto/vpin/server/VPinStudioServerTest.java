package de.mephisto.vpin.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VPinStudioServerTest {

  @Test
  public void testDisplayIsAssumedOnWindowsAndMac() {
    assertTrue(VPinStudioServer.isDisplayAvailable(null, true, null, null));
  }

  @Test
  public void testDisplayDependsOnTheEnvironmentElsewhere() {
    assertTrue(VPinStudioServer.isDisplayAvailable(null, false, ":0", null));
    assertTrue(VPinStudioServer.isDisplayAvailable(null, false, null, "wayland-0"));
    // no display: without this the server fails to start with an AWTError
    assertFalse(VPinStudioServer.isDisplayAvailable(null, false, null, null));
    assertFalse(VPinStudioServer.isDisplayAvailable(null, false, "", ""));
  }

  @Test
  public void testExplicitHeadlessPropertyWins() {
    assertFalse(VPinStudioServer.isDisplayAvailable("true", true, ":0", null));
    assertTrue(VPinStudioServer.isDisplayAvailable("false", false, null, null));
  }
}
