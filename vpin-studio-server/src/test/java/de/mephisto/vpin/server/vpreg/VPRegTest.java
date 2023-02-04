package de.mephisto.vpin.server.vpreg;

import org.apache.poi.poifs.filesystem.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class VPRegTest {

  @Test
  public void readFile() throws IOException {
    // This is the most memory efficient way to open the FileSystem
//    try (POIFSFileSystem fs = new POIFSFileSystem(new File("C:\\vPinball\\VisualPinball\\User\\VPReg.stg"))) {
//      DirectoryEntry root = fs.getRoot();
//      System.out.println(root.getName());
//      Iterator<Entry> entries = root.getEntries();
//      while (entries.hasNext()) {
//        Entry next = entries.next();
//        System.out.println("- " + next.getName());
//      }
//
//      fs.close();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }


  }
}
