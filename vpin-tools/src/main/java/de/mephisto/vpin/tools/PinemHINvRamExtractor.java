package de.mephisto.vpin.tools;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class PinemHINvRamExtractor {

  public static void main(String[] args) throws IOException {

    List<String> strings = FileUtils.readLines(new File("C:\\Users\\syd71\\Downloads\\pinemhi\\list.txt"), Charset.defaultCharset());
    for (String string : strings) {
      string = string.trim();
      System.out.println("\"" + string + "\",");
    }

  }
}
