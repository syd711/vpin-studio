package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;

public class AltSound2Test {

  @Test
  public void testLoadSave() {
    AltSoundLoaderFactory loaderFactory = new AltSoundLoaderFactory(new File("../testsystem/vPinball/VisualPinball/VPinMAME/altsound/cftbl_l4"));
    AltSound altSound = loaderFactory.load();
    assertNotNull(altSound);
  }
}
