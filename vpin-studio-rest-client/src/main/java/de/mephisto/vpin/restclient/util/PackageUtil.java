package de.mephisto.vpin.restclient.util;

import de.mephisto.vpin.restclient.backups.VpaArchiveUtil;

import java.io.File;
import java.util.Arrays;

public class PackageUtil {
  public static String ARCHIVE_RAR = "rar";
  public static String ARCHIVE_7Z = "7z";
  public static String ARCHIVE_ZIP = "zip";
  public static String ARCHIVE_VPA = "vpa";
  public static String[] ARCHIVE_SUFFIXES = {ARCHIVE_ZIP, ARCHIVE_RAR, ARCHIVE_7Z, ARCHIVE_VPA};

  public static boolean isSupportedArchive(String suffix) {
    return Arrays.asList(ARCHIVE_SUFFIXES).contains(suffix);
  }

  public static String contains(File file, String suffix) {
    String fileName = file.getName().toLowerCase();
    if (fileName.endsWith(".zip")) {
      return ZipUtil.contains(file, suffix);
    }
    else if(fileName.endsWith(".vpa")) {
      return VpaArchiveUtil.contains(file, suffix);
    }
    else if (fileName.endsWith(".rar") || fileName.endsWith(".7z")) {
      return RarUtil.contains(file, suffix);
    }
    throw new UnsupportedOperationException("No package support for " + file.getName());
  }

  public static boolean unpackTargetFile(File archiveFile, File targetFile, String name) {
    String archiveName = archiveFile.getName().toLowerCase();
    if (archiveName.endsWith(".zip")) {
      return ZipUtil.unzipTargetFile(archiveFile, targetFile, name);
    }
    else if (archiveName.endsWith(".vpa")) {
      return VpaArchiveUtil.extractFile(archiveFile, targetFile, name);
    }
    else if (archiveName.endsWith(".rar") || archiveName.endsWith(".7z")) {
      return RarUtil.unrarTargetFile(archiveFile, targetFile, name);
    }
    throw new UnsupportedOperationException("No package support for " + archiveFile.getName());
  }

  public static byte[] readFile(File file, String name) {
    String archiveName = file.getName().toLowerCase();
    if (archiveName.endsWith(".zip") || archiveName.endsWith(".vpa")) {
      return ZipUtil.readFile(file, name);
    }
    if (archiveName.endsWith(".rar") || archiveName.endsWith(".7z")) {
      return RarUtil.readFile(file, name);
    }
    throw new UnsupportedOperationException("No package support for " + file.getName());
  }
}
