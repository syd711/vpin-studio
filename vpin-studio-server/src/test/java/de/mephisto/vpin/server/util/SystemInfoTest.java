package de.mephisto.vpin.server.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemInfoTest {

  @Test
  public void testSystemInfo() {
    SystemInfo info = SystemInfo.getInstance();
    assertNotNull(info.get7ZipCommand());
    assertNotNull(info.getMameRomFolder());
    assertTrue(info.getMameRomFolder().exists());
    assertTrue(info.getVPXTables().length > 0);
    assertTrue(info.getPinUPSystemFolder().exists());
    assertTrue(info.getVisualPinballInstallationFolder().exists());
  }

}
