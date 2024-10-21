package de.mephisto.vpin.server.puppacks;

import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.puppack.PupPackUtil;
import de.mephisto.vpin.server.puppack.ScreenEntry;
import de.mephisto.vpin.server.puppack.ScreensPub;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PupPackTest {

  @Test
  public void testScreensPup() {
    File screens = new File("../testsystem/vPinball/PinUPSystem/PUPVideos/twst_405/screens.pup");
    ScreensPub s = new ScreensPub(screens);
    List<ScreenEntry> entries = s.getEntries();

    assertTrue(s.exists());
    assertNotNull(s.getScreenMode(VPinScreen.Loading));
    assertFalse(entries.isEmpty());
  }

}
