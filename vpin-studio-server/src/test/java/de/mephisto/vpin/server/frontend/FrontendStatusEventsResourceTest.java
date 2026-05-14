package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FrontendStatusEventsResourceTest extends AbstractVPinServerTest {

  @Test
  public void testStartExit() {
    super.setupSystem();

    boolean result = frontendStatusEventsResource.gameLaunch(EM_TABLE_NAME, null);
//    assertTrue(result);
//
//    result = frontendStatusEventsResource.gameLaunch("bubu");
//    assertFalse(result);
//
//    assertTrue(frontendStatusEventsResource.popperLaunch());
  }
}
