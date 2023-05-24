package de.mephisto.vpin.server.vpx;

import org.junit.jupiter.api.Test;

import java.io.File;


public class VPXUtilTest {

  @Test
  public void testStringRead() {
    File table = new File("C:\\vPinball\\VisualPinball\\Tables\\Hayburners (WIlliams 1951).vpx");
    String script = VPXUtil.readScript(table);
    System.out.println(script);
  }
}
