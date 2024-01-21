package de.mephisto.vpin.ui.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class PupPackAnalyzer {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackAnalyzer.class);

  private boolean canceled = false;

  public String analyze(File archiveFile, List<String> romNames, ProgressResultModel progressResultModel) {
    if (archiveFile.getName().toLowerCase().endsWith(".zip")) {
      return analyzeZip(archiveFile, romNames, progressResultModel);
    }

    return null;
  }

  private String analyzeZip(File archiveFile, List<String> romNames, ProgressResultModel progressResultModel) {
    boolean foundFolderMatchingRom = false;
    boolean screensPupFound = false;
    try {
      ZipFile zf = new ZipFile(archiveFile);
      int totalCount = zf.size();
      zf.close();

      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      int count = 0;
      while (zipEntry != null && !canceled) {
        count++;
        String name = zipEntry.getName();

        double progress = count * 100 / totalCount;
        progressResultModel.setProgress(progress / 100);

        if (zipEntry.isDirectory()) {
          if (!foundFolderMatchingRom) {
            String folderName = name;
            if (folderName.contains("/")) {
              String[] segments = folderName.split("/");
              folderName = segments[segments.length - 1];
            }

            for (String romName : romNames) {
              if (!StringUtils.isEmpty(romName) && folderName.equals(romName)) {
                foundFolderMatchingRom = true;
                LOG.info("Found matching ROM \"" + romName + "\" in pup pack archive.");
                break;
              }
            }
          }
        }
        else {
          if (name.contains("screens.pup")) {
            screensPupFound = true;
          }
        }
        zis.closeEntry();

        if (screensPupFound && foundFolderMatchingRom) {
          break;
        }

        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (Exception e) {
      return "Reading of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage();
    }

    if (!screensPupFound) {
      progressResultModel.getResults().add("The selected file is not a valid PUP pack.");
    }

    if (!foundFolderMatchingRom) {
      progressResultModel.getResults().add("Selected PUP pack is not applicable for names \"" + String.join(", ", romNames) + "\".");
    }

    return null;
  }

  public void cancel() {
    canceled = true;
  }
}
