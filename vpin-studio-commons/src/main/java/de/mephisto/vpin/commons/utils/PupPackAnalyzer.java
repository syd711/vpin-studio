package de.mephisto.vpin.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class PupPackAnalyzer {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackAnalyzer.class);

  private boolean canceled = false;

  public String analyze(File archiveFile, String rom) {
    boolean foundFolderMatchingRom = false;
    boolean screensPupFound = false;
    String actualPupPackRomName = null;
    try {
      ZipFile zf = new ZipFile(archiveFile);
      int totalCount = zf.size();
      zf.close();

      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null && !canceled) {
        String name = zipEntry.getName();
        if (zipEntry.isDirectory()) {
          if (!foundFolderMatchingRom) {
            String folderName = name;
            if (folderName.contains("/")) {
              String[] segments = folderName.split("/");
              folderName = segments[segments.length - 1];
            }

            if (folderName.equals(rom)) {
              foundFolderMatchingRom = true;
              LOG.info("Found matching ROM \"" + rom + "\" in pup pack archive.");
            }
          }
        }
        else {
          if (name.contains("screens.pup")) {
            screensPupFound = true;
            if (actualPupPackRomName == null || actualPupPackRomName.length() > name.length()) {
              actualPupPackRomName = name;
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
      return "Reading of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage();
    }

    if (!screensPupFound) {
      return "The selected file is not a valid PUP pack.";
    }

    if (!foundFolderMatchingRom) {
      String[] segments = actualPupPackRomName.split("/");
      String romFound = segments[segments.length - 2];
      return "Selected PUP pack is not applicable for ROM \"" + rom + "\", but for tables with ROM \"" + romFound + "\".";
    }

    return null;
  }

  public void cancel() {
    canceled = true;
  }
}
