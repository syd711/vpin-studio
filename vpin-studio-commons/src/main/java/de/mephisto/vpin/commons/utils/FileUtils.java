package de.mephisto.vpin.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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
}
