package de.mephisto.vpin.server.system;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class SystemTest extends AbstractVPinServerTest {

  @Test
  public void testMaintenanceMode() {
    assertTrue(systemService.getPinemhiCommandFile().exists());
    assertTrue(systemService.getBackupFolder().exists());
    assertFalse(systemService.getCompetitionBadges().isEmpty());
  }


  @Test
  public void testDotNet() throws Exception {
    boolean dotNetInstalled = systemService.isDotNetInstalled();
    assertTrue(dotNetInstalled);

    assertTrue(systemService.isValidDotNetVersion("v4"));
    assertTrue(systemService.isValidDotNetVersion("v4.0"));
    assertTrue(systemService.isValidDotNetVersion("v3.5"));
    assertFalse(systemService.isValidDotNetVersion("v2.0.4"));
  }

  @Test
  public void testStickKeys() throws Exception {
    boolean stickyKeysEnabled = systemService.isStickyKeysEnabled();
    if(stickyKeysEnabled) {
      systemService.setStickyKeysEnabled(false);
      assertFalse(systemService.isStickyKeysEnabled());
    }
    else {
      systemService.setStickyKeysEnabled(true);
      assertTrue(systemService.isStickyKeysEnabled());
    }
  }

}
