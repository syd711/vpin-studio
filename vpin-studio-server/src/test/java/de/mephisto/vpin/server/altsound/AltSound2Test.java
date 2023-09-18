package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSound2DuckingProfile;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AltSound2Test {

  @Test
  public void testLoadSave() {
    AltSoundLoaderFactory loaderFactory = new AltSoundLoaderFactory(new File("../testsystem/vPinball/VisualPinball/VPinMAME/altsound/cftbl_l4"));
    AltSound altSound = loaderFactory.load();
    assertNotNull(altSound);

    List<AltSound2DuckingProfile> profiles = altSound.getOverlayDuckingProfiles();
    assertEquals(profiles.size(), 2);

    AltSound2Writer writer = new AltSound2Writer(new File("../testsystem/vPinball/VisualPinball/VPinMAME/altsound/cftbl_l4"));
    writer.write(altSound);
  }
}
