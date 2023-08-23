package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PopperResourceTest extends AbstractVPinServerTest {

  @Test
  public void testStartExit() {
    super.setupSystem();

    boolean result = popperResource.gameLaunch(EM_TABLE_NAME);
    assertTrue(result);

    result = popperResource.gameLaunch("bubu");
    assertFalse(result);

    assertTrue(popperResource.popperLaunch());
  }
}
