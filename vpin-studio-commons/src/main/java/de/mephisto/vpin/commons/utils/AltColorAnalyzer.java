package de.mephisto.vpin.commons.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AltColorAnalyzer {
  private final static Logger LOG = LoggerFactory.getLogger(AltColorAnalyzer.class);

  public final static String PAL_SUFFIX = ".pal";
  public final static String VNI_SUFFIX = ".vni";
  public final static String PAC_SUFFIX = ".pac";
  public final static String SERUM_SUFFIX = ".cRZ";


  public static String analyze(@NonNull File file) {
    boolean altColorFound = false;
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
          if (name.endsWith(PAC_SUFFIX) || name.endsWith(SERUM_SUFFIX) || name.endsWith(PAL_SUFFIX)) {
            altColorFound = true;
          }
        }
        zis.closeEntry();

        if(altColorFound) {
          break;
        }

        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (Exception e) {
      LOG.error("Unzipping of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return "Unzipping of " + file.getAbsolutePath() + " failed: " + e.getMessage();
    }

    if (!altColorFound) {
      return "The selected archive does not contain ALT Color files.";
    }
    return null;
  }
}
