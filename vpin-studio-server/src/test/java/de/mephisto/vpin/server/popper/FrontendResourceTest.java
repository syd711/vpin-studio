package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.frontend.FrontendControl;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FrontendResourceTest extends AbstractVPinServerTest {

  @Test
  public void testControl() {
    FrontendControl control = frontendResource.getPinUPControlFor(VPinScreen.Other2.name());
    assertEquals(control.getDescription(), "Show Other");

    control = frontendResource.getPinUPControlFor(VPinScreen.GameHelp.name());
    assertEquals(control.getDescription(), "Game Help");

    control = frontendResource.getPinUPControlFor(VPinScreen.GameInfo.name());
    assertEquals(control.getDescription(), "Game Info/Flyer");
  }
}
