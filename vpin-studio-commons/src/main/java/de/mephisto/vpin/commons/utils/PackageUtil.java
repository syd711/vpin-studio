package de.mephisto.vpin.commons.utils;

import java.io.File;

public class PackageUtil {
  public static boolean isSupportedArchive(String suffix) {
    return suffix.equalsIgnoreCase("zip") || suffix.equalsIgnoreCase("rar");
  }

  public static String contains(File file, String suffix) {
    if(file.getName().toLowerCase().endsWith(".zip")) {
      return ZipUtil.contains(file, suffix);
    }
    if(file.getName().toLowerCase().endsWith(".rar")) {
      return RarUtil.contains(file, suffix);
    }
    throw new UnsupportedOperationException("No package support for " + file.getName());
  }

  public static boolean unpackTargetFile(File archiveFile, File targetFile, String name) {
    if(archiveFile.getName().toLowerCase().endsWith(".zip")) {
      return ZipUtil.unzipTargetFile(archiveFile, targetFile, name);
    }
    if(archiveFile.getName().toLowerCase().endsWith(".rar")) {
      return RarUtil.unrarTargetFile(archiveFile, targetFile, name);
    }
    throw new UnsupportedOperationException("No package support for " + archiveFile.getName());
  }
}
