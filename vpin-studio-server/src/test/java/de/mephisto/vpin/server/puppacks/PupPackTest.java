package de.mephisto.vpin.server.puppacks;

import de.mephisto.vpin.server.puppack.ScreenEntry;
import de.mephisto.vpin.server.puppack.ScreensPub;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

public class PupPackTest {

  @Test
  public void testScreensPup() {
    File screens = new File("C:\\vPinball\\PinUPSystem\\PUPVideos\\cftbl_l4\\screens.pup");
    ScreensPub s = new ScreensPub(screens);
    List<ScreenEntry> entries = s.getEntries();
    for (ScreenEntry entry : entries) {
      System.out.println(entry.getScreenNum() + ":" + entry.getScreenMode());
    }
  }
}
