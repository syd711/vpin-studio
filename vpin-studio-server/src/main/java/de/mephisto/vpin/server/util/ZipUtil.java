package de.mephisto.vpin.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
  private final static Logger LOG = LoggerFactory.getLogger(ZipUtil.class);

  public static boolean unzip(File archiveFile, File destinationDir) {
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        File newFile = new File(destinationDir, zipEntry.getName());
        LOG.info("Writing " + newFile.getAbsolutePath());
        if (zipEntry.isDirectory()) {
          if (!newFile.isDirectory() && !newFile.mkdirs()) {
            throw new IOException("Failed to create directory " + newFile);
          }
        }
        else {
          // fix for Windows-created archives
          File parent = newFile.getParentFile();
          if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory " + parent);
          }
          FileOutputStream fos = new FileOutputStream(newFile);
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
          fos.close();
        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();

      LOG.info("Successfully extracted " + archiveFile.getAbsolutePath());
      return true;
    } catch (Exception e) {
      LOG.error("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return false;
    }
  }

  public static boolean unzipTargetFile(File archiveFile, File targetFile, String name) {
    boolean written = false;
    File destinationDir = targetFile.getParentFile();
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        File newFile = new File(destinationDir, zipEntry.getName());
        if (zipEntry.isDirectory()) {
          //not directory creation here!
        }
        else {
          // fix for Windows-created archives
          File parent = newFile.getParentFile();
          if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory " + parent);
          }

          if (zipEntry.getName().endsWith(name)) {
            FileOutputStream fos = new FileOutputStream(targetFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
            fos.close();
            written = true;
            LOG.info("Extracted file " + targetFile.getAbsolutePath());
          }
        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();

        if (written) {
          zipEntry = null;
        }
      }
      fileInputStream.close();
      if(!written) {
        zis.closeEntry();
      }
      zis.close();
    } catch (Exception e) {
      LOG.error("Unzipping of " + targetFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
    return written;
  }

  public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
    if (fileToZip.isHidden()) {
      return;
    }

    if (fileToZip.isDirectory()) {
      LOG.info("Zipping " + fileToZip.getCanonicalPath());

      if (fileName.endsWith("/")) {
        zipOut.putNextEntry(new ZipEntry(fileName));
        zipOut.closeEntry();
      }
      else {
        zipOut.putNextEntry(new ZipEntry(fileName + "/"));
        zipOut.closeEntry();
      }

      File[] children = fileToZip.listFiles();
      if (children != null) {
        for (File childFile : children) {
          zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
        }
      }
      return;
    }

    FileInputStream fis = new FileInputStream(fileToZip);
    ZipEntry zipEntry = new ZipEntry(fileName);
    zipOut.putNextEntry(zipEntry);
    byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
      zipOut.write(bytes, 0, length);
    }
    zipOut.closeEntry();
    fis.close();
  }


  public static String readZipFile(@NonNull File file, @NonNull String filename) {
    boolean descriptorFound = false;
    String fileToString = null;
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
          if (name.equals(filename)) {
            descriptorFound = true;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            while ((len = zis.read(buffer)) > 0) {
              baos.write(buffer, 0, len);
            }
            baos.close();
            fileToString = baos.toString();
          }
        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (Exception e) {
      LOG.error("Reading of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }

    if (!descriptorFound) {
      LOG.info("The selected archive does not contain file \"" + filename + "\"");
    }
    return fileToString;
  }

  public static boolean writeZippedFile(@NonNull File file, @NonNull String filename, @NonNull File target) {
    boolean descriptorFound = false;
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
          if (name.equalsIgnoreCase(filename)) {
            descriptorFound = true;

            FileOutputStream fileOutputStream = new FileOutputStream(target);
            int len;
            while ((len = zis.read(buffer)) > 0) {
              fileOutputStream.write(buffer, 0, len);
            }
            fileOutputStream.close();
            LOG.info("Unzipped \"" + target.getAbsolutePath() + "\" from zip file \"" + file.getAbsolutePath() + "\"");
          }
        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();

      return true;
    } catch (Exception e) {
      LOG.error("Reading of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }

    if (!descriptorFound) {
      LOG.info("The selected archive does not contain file \"" + filename + "\"");
    }
    return false;
  }

  public static String contains(@NonNull File file, @NonNull String suffix) {
    String fileFound = null;
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
          if (name.toLowerCase().endsWith(suffix.toLowerCase())) {
            fileFound = name;
            while (fileFound.contains("/")) {
              fileFound = fileFound.substring(fileFound.indexOf("/") + 1);
            }
          }
        }
        zis.closeEntry();

        if (fileFound != null) {
          break;
        }

        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (Exception e) {
      LOG.error("Search of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return null;
    }

    return fileFound;
  }

  public static String containsFolder(@NonNull File file, @NonNull String name) {
    String fileFound = null;
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(file);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        if (zipEntry.isDirectory()) {
          if(zipEntry.getName().equals(name)) {
            fileFound = zipEntry.getName();
          }
        }
        else {
          String entryName = zipEntry.getName();
          if (entryName.contains(name + "/")) {
            fileFound = entryName;
          }
        }
        zis.closeEntry();

        if (fileFound != null) {
          break;
        }

        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (Exception e) {
      LOG.error("Search of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return null;
    }

    return fileFound;
  }
}
