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

  @Test
  public void testRootFolderResolving() throws IOException {
    File archive = new File("C:\\temp\\vpin-dropins\\Stranger Things 4 ALL IN 1 UPDATE.zip");
    if (archive.exists()) {
      System.out.println("Analyzing " + archive.getAbsolutePath());
      Frontend frontend = new Frontend();
      frontend.setFrontendType(FrontendType.Popper);

      UploaderAnalysis analysis = new UploaderAnalysis(frontend, archive);
      analysis.analyze();

      System.out.println("Root: " + analysis.getPupPackRootDirectory());
      System.out.println("ROM: " + analysis.getRomFromPupPack());

      assertNotNull(analysis.getPupPackRootDirectory());
      assertNotNull(analysis.getRomFromPupPack());

//      File targetFolder = new File("C:\\temp\\PUPPackTest");
//      PupPackUtil.unpack(archive, targetFolder, analysis.getPupPackRootDirectory(), "StrangerThings4_Premium", null);
    }
  }
}
