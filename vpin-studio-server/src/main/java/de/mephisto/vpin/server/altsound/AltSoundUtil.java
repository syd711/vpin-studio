package de.mephisto.vpin.server.altsound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class AltSoundUtil {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundUtil.class);

  public static void unzip(File archiveFile, File destinationDir) {
    try {
      ZipFile zf = new ZipFile(archiveFile);
      int totalCount = zf.size();
      zf.close();

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
          if (name.endsWith(".ogg") || name.endsWith(".mp3") || name.endsWith(".csv")) {
            // fix for Windows-created archives
            if (name.contains("/")) {
              name = name.substring(name.lastIndexOf("/") + 1);
            }
            File target = new File(destinationDir, name);
            FileOutputStream fos = new FileOutputStream(target);
            int len;
            while ((len = zis.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
            fos.close();
            LOG.info("Written " + target.getAbsolutePath());
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
    }
  }
}
