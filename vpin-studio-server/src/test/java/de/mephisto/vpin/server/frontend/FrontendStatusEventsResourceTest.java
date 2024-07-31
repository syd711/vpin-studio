package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FrontendStatusEventsResourceTest extends AbstractVPinServerTest {

  @Test
  public void testStartExit() {
    super.setupSystem();

    boolean result = frontendStatusEventsResource.gameLaunch(EM_TABLE_NAME);
//    assertTrue(result);
//
//    result = frontendStatusEventsResource.gameLaunch("bubu");
//    assertFalse(result);
//
//    assertTrue(frontendStatusEventsResource.popperLaunch());
  }
}
