package de.mephisto.vpin.tools;

import de.mephisto.vpin.restclient.system.ScoringDB;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class PinemHINvRamExtractor {

  public static void main(String[] args) throws IOException {

    ScoringDB db = ScoringDB.load();
    List<String> strings = FileUtils.readLines(new File("C:\\workspace\\vpin-studio-next\\resources\\pinemhi\\list.txt"), Charset.defaultCharset());
    for (String string : strings) {
      string = string.trim();
      if (!string.isEmpty()) {
        if (!db.getSupportedNvRams().contains(string)) {
          System.out.println("\"" + string + "\",");
        }
      }
    }

  }
}
