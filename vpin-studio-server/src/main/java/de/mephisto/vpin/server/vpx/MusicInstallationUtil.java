package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.utils.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MusicInstallationUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MusicInstallationUtil.class);

  public static boolean unzip(File archiveFile, File musicFolder) {
    if (musicFolder == null || !musicFolder.exists()) {
      LOG.error("Music upload failed, no music folder found for default emulator.");
    }

    boolean hasMusicFolder = ZipUtil.containsFolder(archiveFile, "Music") != null;

    if (hasMusicFolder) {
      extractWithMusicFolder(archiveFile, musicFolder);
    }
    else {
      extractIntoMusicFolder(archiveFile, musicFolder);
      return true;
    }

    return false;
  }

  private static void extractWithMusicFolder(File archiveFile, File musicFolder) {
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        if (zipEntry.getName().contains("Music/")) {
          if (zipEntry.isDirectory()) {
            String name = zipEntry.getName();
            File newFolder = new File(musicFolder, formatWithMusicFolder(name));
            if (!newFolder.exists() && !newFolder.mkdirs()) {
              fileInputStream.close();
              zis.closeEntry();
              zis.close();

              throw new IOException("Failed to create directory " + newFolder.getAbsolutePath());
            }
          }
          else {
            String name = zipEntry.getName();
            File target = new File(musicFolder, formatWithMusicFolder(name));
            if (target.exists()) {
              target.delete();
            }

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

  private static String formatWithMusicFolder(String name) {
    String targetFolder = name;
    while (targetFolder.contains("Music/")) {
      targetFolder = targetFolder.substring(targetFolder.indexOf("/") + 1);
    }
    return targetFolder;
  }

  private static void extractIntoMusicFolder(File archiveFile, File musicFolder) {
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        if (zipEntry.isDirectory()) {
          String name = zipEntry.getName();
          File newFolder = new File(musicFolder, name);
          if (!newFolder.exists() && !newFolder.mkdirs()) {
            fileInputStream.close();
            zis.closeEntry();
            zis.close();

            throw new IOException("Failed to create directory " + newFolder.getAbsolutePath());
          }
        }
        else {
          String name = zipEntry.getName();
          File target = new File(musicFolder, name);
          if (target.exists()) {
            target.delete();
          }

          FileOutputStream fos = new FileOutputStream(target);
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
          fos.close();
          LOG.info("Written " + target.getAbsolutePath());
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
