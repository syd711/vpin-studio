package de.mephisto.vpin.server.uploader;

import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UploadAnalyzerTest {

  @Test
  public void testRootFolderResolving() throws IOException {
    File archive = new File("C:\\temp\\vpin-dropins\\Futurama PuP and media pack.zip");
//    File archive = new File("C:\\temp\\vpin-dropins\\Stranger Things 4 ALL IN 1 UPDATE.zip");
    if (archive.exists()) {
      System.out.println("Analyzing " + archive.getAbsolutePath());
      Frontend frontend = new Frontend();
      frontend.setFrontendType(FrontendType.Popper);

      UploaderAnalysis analysis = new UploaderAnalysis(true, archive);
      analysis.analyze();

      System.out.println("Root: " + analysis.getPupPackRootDirectory());
      System.out.println("ROM: " + analysis.getRomFromPupPack());
      System.out.println("DMD: " + analysis.getDMDPath());

      assertNotNull(analysis.getPupPackRootDirectory());
      assertNotNull(analysis.getRomFromPupPack());
      assertNotNull(analysis.getDMDPath());

//      File targetFolder = new File("C:\\temp\\PUPPackTest");
//      PupPackUtil.unpack(archive, targetFolder, analysis.getPupPackRootDirectory(), "StrangerThings4_Premium", null);
    }
  }
}
