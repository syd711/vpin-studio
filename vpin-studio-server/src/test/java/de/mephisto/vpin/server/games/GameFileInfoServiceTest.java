package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.system.FileInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class GameFileInfoServiceTest {

  @TempDir
  File tables;

  @Test
  public void testFileFallbackIsClosestExistingFolder() {
    // VPX 10.8.1 table folder without a pinmame/nvram folder yet
    File tableFolder = new File(tables, "Twister");
    assertTrue(new File(tableFolder, "pinmame").mkdirs());
    File nvram = new File(tableFolder, "pinmame/nvram/twst_405.nv");

    FileInfo info = GameFileInfoService.file(nvram, nvram.getParentFile());
    assertNotNull(info);
    assertTrue(info.isFile());
    assertEquals(nvram, info.getFile());
    assertEquals(new File(tableFolder, "pinmame"), info.getFallback());
  }

  @Test
  public void testMissingFolderIsReplacedByFallback() {
    File tableFolder = new File(tables, "Twister");
    assertTrue(tableFolder.mkdirs());

    FileInfo info = GameFileInfoService.folder(new File(tableFolder, "altsound/twst_405"), tableFolder);
    assertNotNull(info);
    assertFalse(info.isFile());
    assertEquals(tableFolder, info.getFile());
  }

  @Test
  public void testExistingFolderIsKept() {
    File altsound = new File(tables, "Twister/altsound/twst_405");
    assertTrue(altsound.mkdirs());

    FileInfo info = GameFileInfoService.folder(altsound, new File(tables, "Twister"));
    assertNotNull(info);
    assertEquals(altsound, info.getFile());
  }

  @Test
  public void testMissingFolderWithoutFallbackUsesClosestParent() {
    FileInfo info = GameFileInfoService.folder(new File(tables, "Twister/altsound"), null);
    assertNotNull(info);
    assertEquals(tables, info.getFile());
    assertNull(GameFileInfoService.folder(null, null));
  }
}
