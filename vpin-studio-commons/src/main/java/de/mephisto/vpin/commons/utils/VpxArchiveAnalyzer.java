package de.mephisto.vpin.commons.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VpxArchiveAnalyzer {
  private final static Logger LOG = LoggerFactory.getLogger(VpxArchiveAnalyzer.class);

  public final static String VPX_SUFFIX = ".vpx";

  public static String analyze(@NonNull File file) {
    if(file.getName().toLowerCase().endsWith(".zip")) {
      return analyzeZip(file);
    }
    return null;
  }

  private static String analyzeZip(@NonNull File file) {
    boolean entryFound = false;
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(file);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        if (zipEntry.isDirectory()) {
          //ignore
        }
        else {
          String name = zipEntry.getName();
          if (name.endsWith(VPX_SUFFIX)) {
            entryFound = true;
          }
        }
        zis.closeEntry();

        if(entryFound) {
          break;
        }

        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (Exception e) {
      LOG.error("Analyzing of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return "Analyzing of " + file.getAbsolutePath() + " failed: " + e.getMessage();
    }

    if (!entryFound) {
      return "The selected archive does not contain a VPX file.";
    }
    return null;
  }
}
