package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.JobExecutionResult;
import de.mephisto.vpin.restclient.JobExecutionResultFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class PupPackUtil {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackUtil.class);

  public static JobExecutionResult unzip(File archiveFile, File destinationDir, String rom) {
    boolean folderMatch = false;
    try {
      ZipFile zf = new ZipFile(archiveFile);
      int totalCount = zf.size();
      zf.close();

      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        String name = zipEntry.getName();
        if (zipEntry.isDirectory()) {
          if(!folderMatch) {
            String folderName = name;
            if (folderName.contains("/")) {
              String[] segments = folderName.split("/");
              folderName = segments[segments.length-1];
            }

            if (folderName.equals(rom)) {
              folderMatch = true;
              LOG.info("Found matching ROM \"" + rom + "\" in pup pack archive.");
            }
            else {
              LOG.info("Missing ROM name match: " + folderName);
            }
          }
        }
        else {
          if (folderMatch) {
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
      return JobExecutionResultFactory.create("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage());
    }

    if (!folderMatch) {
      return JobExecutionResultFactory.create("Selected PUP pack is not applicable for ROM '" + rom + "'");
    }
    return JobExecutionResultFactory.empty();
  }
}
