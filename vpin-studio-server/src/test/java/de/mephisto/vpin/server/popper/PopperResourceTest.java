package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PopperResourceTest extends AbstractVPinServerTest {


  @Autowired
  private PopperResource popperResource;

  @Test
  public void testStartExit() {
    boolean result = popperResource.gameLaunch("bubu");
    assertFalse(result);

    result = popperResource.gameExit("bubu");
    assertFalse(result);

//    result = popperResource.gameLaunch(AbstractVPinServerTest.TEST_GAME_FILENAME);
//    assertTrue(result);

    assertTrue(popperResource.popperLaunch());
  }
}
