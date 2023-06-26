package de.mephisto.vpin.server.altcolor;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static de.mephisto.vpin.commons.utils.AltColorAnalyzer.*;

public class AltColorUtil {
  private final static Logger LOG = LoggerFactory.getLogger(AltColorUtil.class);

  public static String unzip(File archiveFile, File destinationDir) {
    boolean unpacked = unzip(archiveFile, destinationDir, SERUM_SUFFIX);
    String result = null;
    if (!unpacked) {
      unpacked = unzip(archiveFile, destinationDir, PAC_SUFFIX);
    }
    if (!unpacked) {
      unpacked = unzip(archiveFile, destinationDir, PAL_SUFFIX, ".vni");
    }

    if (!unpacked) {
      result = "No matching ALT Color files found.";
    }

    return result;
  }

  private static boolean unzip(File archiveFile, File destinationDir, String... suffixes) {
    boolean unpacked = false;
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        if (zipEntry.isDirectory()) {
          //ignore
        }
        else {
          String name = zipEntry.getName();
          for (String suffix : suffixes) {
            if (name.endsWith(suffix)) {
              // fix for Windows-created archives
              if (name.contains("/")) {
                name = name.substring(name.lastIndexOf("/") + 1);
              }

              String baseName = FilenameUtils.getBaseName(name);
              if (!baseName.equals("pin2dmd") && !suffix.equals(SERUM_SUFFIX)) {
                name = "pin2dmd" + suffix;
              }

              File target = new File(destinationDir, name);
              if(target.exists()) {
                target.delete();
              }

              FileOutputStream fos = new FileOutputStream(target);
              int len;
              while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
              }
              fos.close();
              LOG.info("Written " + target.getAbsolutePath());
              unpacked = true;
            }
          }

        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (Exception e) {
      LOG.error("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    } finally {
      return unpacked;
    }
  }
}
