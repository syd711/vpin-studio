package de.mephisto.vpin.restclient.system;

import java.io.File;

public class FileLoaderForTest {

  public static File load(String filename) {
    File file = new File(filename);
    if (!file.exists()) {
      file = new File("..", filename);
    }
    return file;
  }

}
