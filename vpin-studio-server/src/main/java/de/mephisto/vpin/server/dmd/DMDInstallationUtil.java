package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.dmd.DMDPackageTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DMDInstallationUtil {
  private final static Logger LOG = LoggerFactory.getLogger(DMDInstallationUtil.class);

  public static boolean unzip(File archiveFile, File tablesFolder) {
    boolean unpacked = false;
    File dmdFolder = null;
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        if (zipEntry.isDirectory()) {
          String folderName = zipEntry.getName();
          if (folderName.contains(DMDPackageTypes.FlexDMD.name()) || folderName.contains(DMDPackageTypes.UltraDMD.name())) {
            dmdFolder = new File(tablesFolder, folderName);
            dmdFolder.mkdirs();
            LOG.info("Created/Found DMD folder \"" + dmdFolder.getAbsolutePath() + "\"");
          }
        }
        else if (dmdFolder != null) {
          String name = zipEntry.getName();
          // fix for Windows-created archives
          if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
          }

          File target = new File(dmdFolder, name);
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
          unpacked = true;
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
    return unpacked;
  }
}
