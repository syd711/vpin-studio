package de.mephisto.vpin.server.vpbm;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.archiving.adapters.vpbm.VpbmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class VPBMTest extends AbstractVPinServerTest {

  @Autowired
  private VpbmService vpbmService;

  @Test
  public void testGetVersion() {
    String version = vpbmService.getVersion();
    assertTrue(version.startsWith("Version: "));
  }

  @Test
  public void testUpdate() {
//    assertFalse(vpbmService.isUpdateAvailable());
  }
}
