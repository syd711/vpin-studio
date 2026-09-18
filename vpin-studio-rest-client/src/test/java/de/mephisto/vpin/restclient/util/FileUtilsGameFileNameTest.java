package de.mephisto.vpin.restclient.util;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileUtilsGameFileNameTest {

  @Test
  public void testJoinGameFileNameUsesPlatformSeparator() {
    assertEquals("Twister" + File.separator + "Twister.vpx", FileUtils.joinGameFileName("Twister", "Twister.vpx"));
  }

  @Test
  public void testLastSeparatorIndexAcceptsBothSeparators() {
    assertEquals(-1, FileUtils.lastSeparatorIndex("Twister.vpx"));
    assertEquals(7, FileUtils.lastSeparatorIndex("Twister\\Twister.vpx"));
    assertEquals(7, FileUtils.lastSeparatorIndex("Twister/Twister.vpx"));
    assertEquals(9, FileUtils.lastSeparatorIndex("Bally\\Old/Twister.vpx"));
  }
}
