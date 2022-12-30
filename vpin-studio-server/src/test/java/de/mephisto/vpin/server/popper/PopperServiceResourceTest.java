package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.PinUPControl;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.server.VPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PopperServiceResourceTest extends VPinServerTest {


  @Autowired
  private PopperServiceResource popperResource;

  @Test
  public void testControl() {
    PinUPControl control = popperResource.getPinUPControlFor(PopperScreen.Other2.name());
    assertEquals(control.getDescription(), "Show Other");

    control = popperResource.getPinUPControlFor(PopperScreen.GameHelp.name());
    assertEquals(control.getDescription(), "Game Help");

    control = popperResource.getPinUPControlFor(PopperScreen.GameInfo.name());
    assertEquals(control.getDescription(), "Game Info/Flyer");
  }
}
