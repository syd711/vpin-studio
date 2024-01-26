package de.mephisto.vpin.commons.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DirectB2SArchiveAnalyzer {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SArchiveAnalyzer.class);

  public static String analyze(@NonNull File file) {
    if (file.getName().toLowerCase().endsWith(".zip")) {
      return analyzeZip(file);
    }
    return null;
  }

  private static String analyzeZip(@NonNull File file) {
    boolean fileFound = false;
    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        if (zipEntry.isDirectory()) {
          //ignore
        }
        else {
          String name = zipEntry.getName();
          if (name.toLowerCase().endsWith(".directb2s")) {
            fileFound = true;
          }
        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (Exception e) {
      LOG.error("Unzipping of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return "Unzipping of " + file.getAbsolutePath() + " failed: " + e.getMessage();
    }

    if (!fileFound) {
      return "The selected archive does not contain a \"directb2s\" file.";
    }
    return null;
  }
}
