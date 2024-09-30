package de.mephisto.vpin.server.system;


import de.mephisto.vpin.commons.utils.WinRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WinRegistryTest {

  @Test
  public void testDotNet() throws Exception {
    boolean dotNetInstalled = WinRegistry.isDotNetInstalled();
    assertTrue(dotNetInstalled);

    assertTrue(WinRegistry.isValidDotNetVersion("v4"));
    assertTrue(WinRegistry.isValidDotNetVersion("v4.0"));
    assertTrue(WinRegistry.isValidDotNetVersion("v3.5"));
    assertFalse(WinRegistry.isValidDotNetVersion("v2.0.4"));
  }
}
