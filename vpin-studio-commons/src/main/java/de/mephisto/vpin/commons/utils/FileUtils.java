package de.mephisto.vpin.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class FileUtils {
  private final static Logger LOG = LoggerFactory.getLogger(FileUtils.class);

  public static boolean delete(File file) {
    if(file.exists()) {
      if(file.delete()) {
        LOG.info("Deleted " + file.getAbsolutePath());
      }
      else {
        LOG.warn("Failed to delete " + file.getAbsolutePath());
        return false;
      }
    };
    return true;
  }

  public static String readableFileSize(long size) {
    if(size <= 0) return "0";
    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }

  public static void writeBatch(String name, String content) throws IOException {
    File path = new File("./" + name);
    if(path.exists()) {
      path.delete();
    }
    Files.write( path.toPath(), content.getBytes());
  }

  public static boolean deleteFolder(File folder) {
    if(!folder.exists()) {
      return true;
    }
    try {
      org.apache.commons.io.FileUtils.deleteDirectory(folder);
    } catch (IOException e) {
      return false;
    }
    return true;
  }
}
