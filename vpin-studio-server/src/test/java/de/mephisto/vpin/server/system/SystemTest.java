package de.mephisto.vpin.server.system;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SystemTest extends AbstractVPinServerTest {

  @Test
  public void testMaintenanceMode() {
    assertTrue(systemService.getNvramFolder().exists());
    assertTrue(systemService.getPinUPSystemFolder().exists());
    assertTrue(systemService.getMameFolder().exists());
    assertTrue(systemService.getVPXTablesFolder().exists());
    assertTrue(systemService.getVisualPinballInstallationFolder().exists());
    assertTrue(systemService.getMameRomFolder().exists());
    assertTrue(systemService.getAltColorFolder().exists());
    assertTrue(systemService.getAltSoundFolder().exists());
    assertTrue(systemService.getVisualPinballUserFolder().exists());
    assertTrue(systemService.getVPXExe().exists());
    assertTrue(systemService.getVPMAliasFile().exists());
    assertTrue(systemService.getVPXMusicFolder().exists());
    assertTrue(systemService.getVPRegFile().exists());
    assertTrue(systemService.getPinUPDatabaseFile().exists());
    assertTrue(systemService.getPinemhiCommandFile().exists());
    assertTrue(systemService.getBackupFolder().exists());

    assertNotNull(systemService.getSystemSummary());
    assertNotNull(systemService.getArchiveType());

    assertFalse(systemService.getScreenInfos().isEmpty());
    assertFalse(systemService.getCompetitionBadges().isEmpty());
  }
}
