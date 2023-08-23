package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PopperServiceResourceTest extends AbstractVPinServerTest {

  @Test
  public void testControl() {
    PinUPControl control = popperServiceResource.getPinUPControlFor(PopperScreen.Other2.name());
    assertEquals(control.getDescription(), "Show Other");

    control = popperServiceResource.getPinUPControlFor(PopperScreen.GameHelp.name());
    assertEquals(control.getDescription(), "Game Help");

    control = popperServiceResource.getPinUPControlFor(PopperScreen.GameInfo.name());
    assertEquals(control.getDescription(), "Game Info/Flyer");
  }
}
