package de.mephisto.vpin.restclient.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
  private final static Logger LOG = LoggerFactory.getLogger(ZipUtil.class);

  public static boolean unzip(File archiveFile, File destinationDir) {
    return unzip(archiveFile, destinationDir, false);
  }

  public static boolean unzip(File archiveFile, File destinationDir, boolean log) {
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
          LOG.info("Unpacked {}", newFile.getAbsolutePath());
        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();

      LOG.info("Successfully extracted " + archiveFile.getAbsolutePath());
      return true;
    }
    catch (Exception e) {
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
        if (zipEntry.isDirectory()) {
          //not directory creation here!
        }
        else {
          String entryName = zipEntry.getName().toLowerCase().replaceAll("\\\\", "/");
          if (zipEntry.getName().endsWith(name) || entryName.equalsIgnoreCase(name)) {
            //folder creation
            File parent = targetFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
              throw new IOException("Failed to create directory " + parent);
            }

            FileOutputStream fos = new FileOutputStream(targetFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
            fos.close();
            written = true;
            LOG.info("Extracted '{}' to target: '{}'", entryName, targetFile.getAbsolutePath());
          }
        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();

        if (written) {
          zipEntry = null;
        }
      }
      fileInputStream.close();
      if (!written) {
        zis.closeEntry();
      }
      zis.close();
    }
    catch (Exception e) {
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


  public static void zipFile(File fileToZip, String fileName, net.lingala.zip4j.ZipFile zipOut) throws IOException {
    if (fileToZip.isHidden()) {
      return;
    }

    if (fileToZip.isDirectory()) {
      LOG.info("Zipping " + fileToZip.getCanonicalPath());

      if (!fileName.endsWith("/")) {
        fileName = fileName + "/";
      }

      File[] children = fileToZip.listFiles();
      if (children != null) {
        for (File childFile : children) {
          ZipParameters zipParameters = new ZipParameters();
          zipParameters.setEncryptFiles(true);
          zipParameters.setCompressionLevel(CompressionLevel.HIGHER);
          zipParameters.setEncryptionMethod(EncryptionMethod.AES);
          zipParameters.setFileNameInZip(fileName + childFile.getName());
          zipOut.addFile(childFile, zipParameters);
        }
      }
      return;
    }

    ZipParameters zipParameters = new ZipParameters();
    zipParameters.setEncryptFiles(true);
    zipParameters.setCompressionLevel(CompressionLevel.HIGHER);
    zipParameters.setEncryptionMethod(EncryptionMethod.AES);
    zipParameters.setFileNameInZip(fileName);
    zipOut.addFile(fileToZip, zipParameters);
  }

  public static void zipFolder(File sourceDirPath, File targetZip, ZipProgressable progressable) throws IOException {
    Path p = targetZip.toPath();
    OutputStream outputStream = null;
    try {
      outputStream = Files.newOutputStream(p);
      ZipOutputStream zs = new ZipOutputStream(outputStream);
      Path pp = sourceDirPath.toPath();
      Files.walk(pp)
          .filter(path -> !Files.isDirectory(path))
          .forEach(path -> {
            String zipEntryPath = sourceDirPath.getName() + "/" + pp.relativize(path);
            ZipEntry zipEntry = new ZipEntry(zipEntryPath);
            try {
              zs.putNextEntry(zipEntry);
              progressable.zipping(path.toFile(), zipEntryPath);
              Files.copy(path, zs);
              zs.closeEntry();
            }
            catch (IOException e) {
              LOG.error("Zip failed: " + e.getMessage(), e);
            }
          });
      zs.close();
    }
    finally {
      if (outputStream != null) {
        outputStream.close();
      }
    }
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
    }
    catch (Exception e) {
      LOG.error("Reading of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }

    if (!descriptorFound) {
      LOG.info("The selected archive does not contain file \"" + filename + "\"");
    }
    return fileToString;
  }

  public static boolean writeZippedFile(@NonNull File zipFile, @NonNull String filename, @NonNull File target) {
    boolean descriptorFound = false;
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(zipFile);
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
            LOG.info("Unzipped \"" + target.getAbsolutePath() + "\" from zip file \"" + zipFile.getAbsolutePath() + "\"");
          }
        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();

      return true;
    }
    catch (Exception e) {
      LOG.error("Reading of " + zipFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }

    if (!descriptorFound) {
      LOG.info("The selected archive does not contain file \"" + filename + "\"");
    }
    return false;
  }

  public static String contains(@NonNull File file, @NonNull String suffix) {
    String contains = containsWithPath(file, suffix);
    if (contains != null) {
      while (contains.contains("/")) {
        contains = contains.substring(contains.indexOf("/") + 1);
      }
    }
    return contains;
  }

  public static String containsWithPath(@NonNull File file, @NonNull String suffix) {
    long start = System.currentTimeMillis();
    String fileFound = null;
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
          if (name.toLowerCase().endsWith(suffix.toLowerCase())) {
            fileFound = name;
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
    }
    catch (Exception e) {
      LOG.error("Search of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return null;
    }
    finally {
      LOG.info("Contains check for \"" + file.getAbsolutePath() + "\" took " + (System.currentTimeMillis() - start) + "ms.");
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
          if (zipEntry.getName().equals(name)) {
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
    }
    catch (Exception e) {
      LOG.error("Search of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return null;
    }

    return fileFound;
  }

  public static byte[] readFile(File file, String name) {
    byte[] bytes = null;
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
          String entryName = zipEntry.getName();
          if (entryName.equals(name)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            while ((len = zis.read(buffer)) > 0) {
              baos.write(buffer, 0, len);
            }
            baos.close();
            bytes = baos.toByteArray();
          }
        }
        zis.closeEntry();

        if (bytes != null) {
          break;
        }

        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    }
    catch (Exception e) {
      LOG.error("Reading of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }

    return bytes;
  }
}
