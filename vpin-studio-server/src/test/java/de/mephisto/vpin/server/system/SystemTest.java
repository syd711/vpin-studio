package de.mephisto.vpin.server.system;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SystemTest extends AbstractVPinServerTest {

  @Test
  public void testMaintenanceMode() {
    assertTrue(systemService.getFrontendInstallationFolder().exists());
    assertTrue(systemService.getPinemhiCommandFile().exists());
    assertTrue(systemService.getBackupFolder().exists());
    assertNotNull(systemService.getArchiveType());

    assertFalse(systemService.getScreenInfos().isEmpty());
    assertFalse(systemService.getCompetitionBadges().isEmpty());
  }
}
