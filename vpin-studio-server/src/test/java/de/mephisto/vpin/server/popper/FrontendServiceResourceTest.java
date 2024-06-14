package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.frontend.FrontendControl;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FrontendServiceResourceTest extends AbstractVPinServerTest {

  @Test
  public void testControl() {
    FrontendControl control = frontendServiceResource.getPinUPControlFor(VPinScreen.Other2.name());
    assertEquals(control.getDescription(), "Show Other");

    control = frontendServiceResource.getPinUPControlFor(VPinScreen.GameHelp.name());
    assertEquals(control.getDescription(), "Game Help");

    control = frontendServiceResource.getPinUPControlFor(VPinScreen.GameInfo.name());
    assertEquals(control.getDescription(), "Game Info/Flyer");
  }
}
