package de.mephisto.vpin.server.vpx;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Set;

import static de.mephisto.vpin.server.AbstractVPinServerTest.TABLE_NAMES;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class VPXUtilTest {

  @Test
  public void testScript() {
    for (String tableName : TABLE_NAMES) {
      File table = new File("../testsystem/vPinball/VisualPinball/Tables/" + tableName);
      String script = VPXUtil.readScript(table);
      assertNotNull(script);
    }
  }

  @Test
  public void testInfo() throws Exception {
    for (String tableName : TABLE_NAMES) {
      File table = new File("../testsystem/vPinball/VisualPinball/Tables/" + tableName);
      Map<String, Object> data = VPXUtil.readTableInfo(table);
      Set<Map.Entry<String, Object>> entries = data.entrySet();
      for (Map.Entry<String, Object> entry : entries) {
        System.out.println(entry.getKey() + " => " + entry.getValue());
      }

      assertNotNull(data);
    }
  }

//  @Test
//  public void testInfo2() throws Exception {
//    File table = new File("C:\\vPinball\\VisualPinball\\Tables\\Attack from Mars 2.0.1.vpx");
//    Map<String, Object> data = VPXUtil.readTableInfo(table);
//    Set<Map.Entry<String, Object>> entries = data.entrySet();
//    for (Map.Entry<String, Object> entry : entries) {
////      System.out.println(entry.getKey() + " => " + entry.getValue());
//      if (entry.getKey().equals("Screenshot")) {
//        File file = new File("E:\\temp\\out.png");
//        file.delete();
//        Object value = entry.getValue();
//        IOUtils.write((byte[])value, new FileOutputStream(file));
//        System.out.println("Written " + file.getName());
//      }
//    }
//
//    assertNotNull(data);
//  }
}
