package de.mephisto.vpin.server.puppacks;

import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.server.puppack.ScreenEntry;
import de.mephisto.vpin.server.puppack.ScreensPub;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PupPackTest {

  @Test
  public void testScreensPup() {
    File screens = new File("../testsystem/vPinball/PinUPSystem/PUPVideos/twst_405/screens.pup");
    ScreensPub s = new ScreensPub(screens);
    List<ScreenEntry> entries = s.getEntries();

    assertTrue(s.exists());
    assertNotNull(s.getScreenMode(PopperScreen.Loading));
    assertFalse(entries.isEmpty());
  }
}
