package de.mephisto.vpin.server.dmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DMDInstallationUtil {
  private final static Logger LOG = LoggerFactory.getLogger(DMDInstallationUtil.class);

  public static void unzip(File archiveFile, File tablesFolder) {
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        if (zipEntry.isDirectory()) {
          continue;
        }

        String name = zipEntry.getName().replaceAll("\\\\", "/");
        if (name.contains("DMD/")) {
          String[] split = name.split("/");
          boolean append = false;
          File targetFile = tablesFolder;
          for (String segment : split) {
            if (segment.endsWith("DMD")) {
              append = true;
            }

            if (append) {
              targetFile = new File(targetFile, segment);
            }
          }
          targetFile.getParentFile().mkdirs();
          if (targetFile.exists() && !targetFile.delete()) {
            LOG.error("Failed to delete existing DMD file " + targetFile.getAbsolutePath());
            continue;
          }

          FileOutputStream fos = new FileOutputStream(targetFile);
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
          fos.close();
          LOG.info("Written " + targetFile.getAbsolutePath());
        }

        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    }
    catch (Exception e) {
      LOG.error("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
  }
}
