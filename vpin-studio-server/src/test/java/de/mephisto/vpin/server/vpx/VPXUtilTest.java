package de.mephisto.vpin.server.vpx;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class VPXUtilTest {

  @Test
  public void testScript() {
    File table = new File("C:\\vPinball\\VisualPinball\\Tables\\Hayburners (WIlliams 1951).vpx");
    String script = VPXUtil.readScript(table);
    assertNotNull(script);
  }

  @Test
  public void testSourceRead() {
//    File table = new File("../vpin-studio-server/src/test/resources/Aces High (1965).vpx");
//    File table = new File("C:\\vPinball\\VisualPinball\\Tables\\Baseball (1970).vpx");
    File table = new File("C:\\vPinball\\VisualPinball\\Tables\\Batman 66.vpx");
    byte[] bytes = VPXUtil.readBytes(table);
    String source = new String(bytes);
    source = source.substring(source.indexOf("CODE")+7);

    byte[] formatted = source.getBytes();
    while(formatted[0] == 0) {
      formatted = Arrays.copyOfRange(formatted, 1, source.length());
    }

    System.out.println(source.indexOf("\4"));
    assertNotNull(bytes);
  }

  @Test
  public void testWrite() throws IOException {
//    File table = new File("../vpin-studio-server/src/test/resources/Aces High (1965).vpx");
    File copy = new File("E:/temp/2 in 1 (Bally 1964) - Kopie.vpx");
    File table = new File("E:/temp/2 in 1 (Bally 1964).vpx");
    table.delete();
    FileUtils.copyFile(copy, table);
    if (table.exists()) {
      long length = table.length();
      String script = VPXUtil.readScript(table);
      VPXUtil.writeGameData(table, script.getBytes());
      assertEquals(length, table.length());
    }
  }
}
