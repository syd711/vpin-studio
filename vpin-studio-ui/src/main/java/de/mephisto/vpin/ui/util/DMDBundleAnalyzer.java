package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.restclient.dmd.DMDPackageTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class DMDBundleAnalyzer {
  private final static Logger LOG = LoggerFactory.getLogger(DMDBundleAnalyzer.class);

  private boolean canceled = false;

  public String analyze(File archiveFile, ProgressResultModel progressResultModel) {
    boolean folderFound = false;
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
          if (!folderFound) {
            String folderName = name;
            if (folderName.contains(DMDPackageTypes.FlexDMD.name()) || folderName.contains(DMDPackageTypes.UltraDMD.name())) {
              LOG.info("Found DMD folder in DMD archive.");
              folderFound = true;
            }
          }
        }
        zis.closeEntry();

        if (folderFound) {
          break;
        }

        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (
      Exception e) {
      return "Reading of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage();
    }

    if (!folderFound) {
      progressResultModel.getResults().add("The selected file is not a valid DMD bundle.");
    }
    return null;
  }

  public void cancel() {
    canceled = true;
  }
}
