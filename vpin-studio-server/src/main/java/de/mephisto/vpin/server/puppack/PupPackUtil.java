package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PupPackUtil {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackUtil.class);

  public static JobExecutionResult unzip(File archiveFile, File destinationDir, String rom, String tableName) {
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        String name = zipEntry.getName();
        File newFile = new File(destinationDir, toTargetName(name, rom, tableName));
        boolean isInPupPack = name.contains(rom + "/") || (!StringUtils.isEmpty(tableName) && name.contains(tableName));
        if (!isInPupPack) {
          LOG.info("Skipping extraction of " + newFile.getAbsolutePath());
        }
        else if (zipEntry.isDirectory()) {
          if (!newFile.exists() && !newFile.mkdirs()) {
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
          LOG.info("Written " + newFile.getAbsolutePath());
        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (Exception e) {
      LOG.error("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return JobExecutionResultFactory.error("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage());
    }
    return JobExecutionResultFactory.empty();
  }

  @NonNull
  private static String toTargetName(String name, String rom, String tableName) {
    String targetFolder = name;
    while (!targetFolder.startsWith(rom + "/") && targetFolder.contains("/")) {
      targetFolder = targetFolder.substring(targetFolder.indexOf("/") + 1);
    }

    if (!targetFolder.startsWith(rom) && !StringUtils.isEmpty(tableName)) {
      targetFolder = name;
      while (!targetFolder.startsWith(tableName + "/") && targetFolder.contains("/")) {
        targetFolder = targetFolder.substring(targetFolder.indexOf("/") + 1);
      }
    }

    return targetFolder;
  }
}
