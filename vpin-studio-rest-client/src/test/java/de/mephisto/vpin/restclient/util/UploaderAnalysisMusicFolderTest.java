package de.mephisto.vpin.restclient.util;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UploaderAnalysisMusicFolderTest {

  private UploaderAnalysis newAnalysis(String... fileNames) {
    UploaderAnalysis analysis = new UploaderAnalysis(true, new File("dummy.zip"));
    for (String fileName : fileNames) {
      analysis.analyze(fileName, false, 0);
    }
    return analysis;
  }

  @Test
  public void testMusicFolderOutsidePupPackIsResolved() {
    UploaderAnalysis analysis = newAnalysis(
        "MyPack/screens.pup",
        "MyPack/options.bat",
        "MyGame/music/theme.mp3");

    assertNotNull(analysis.getPupPackRootDirectory());
    String musicFolder = analysis.getMusicFolder();
    assertNotNull(musicFolder);
    assertFalse(FileUtils.isFileBelowFolder(analysis.getPupPackRootDirectory(), musicFolder + "/"),
        "Resolved music folder must not be below the pup pack folder");
  }

  @Test
  public void testMusicFileInsidePupPackFolderIsNotResolvedAsMusicFolder() {
    UploaderAnalysis analysis = newAnalysis(
        "MyPack/screens.pup",
        "MyPack/options.bat",
        "MyPack/theme.mp3");

    assertNotNull(analysis.getPupPackRootDirectory());
    String musicFolder = analysis.getMusicFolder();
    if (musicFolder != null) {
      assertFalse(FileUtils.isFileBelowFolder(analysis.getPupPackRootDirectory(), musicFolder + "/"),
          "Resolved music folder must not be below the pup pack folder");
    }
    else {
      assertNull(musicFolder);
    }
  }
}
