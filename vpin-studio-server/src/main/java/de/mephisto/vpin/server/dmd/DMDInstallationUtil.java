package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.util.ZipUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DMDInstallationUtil {
  private final static Logger LOG = LoggerFactory.getLogger(DMDInstallationUtil.class);

  public static void unzip(@NonNull File archiveFile, @NonNull File dmdFolder) {
    ZipUtil.unzip(archiveFile, dmdFolder, null);
  }
  //TODO replace with PackageUtil
  public static void unrar(File archiveFile, File dmdFolder) {
    try {

      if (dmdFolder.exists() && !dmdFolder.delete()) {
        LOG.error("Failed to delete existing DMD file " + dmdFolder.getAbsolutePath());
      }
      dmdFolder.mkdirs();
      String dmdFolderName = dmdFolder.getName();

      RandomAccessFile randomAccessFile = new RandomAccessFile(archiveFile, "r");
      RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);

      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
        if (item.isFolder()) {
          continue;
        }

        String name = item.getPath().replaceAll("\\\\", "/");
        if (name.toLowerCase().contains(dmdFolderName.toLowerCase())) {
          name = name.substring(name.toLowerCase().indexOf(dmdFolderName.toLowerCase()) + dmdFolderName.length());
        }
        if (name.startsWith("/")) {
          name = name.substring(1);
        }

        File targetFile = new File(dmdFolder, name);
        RandomAccessFile rafOut = new RandomAccessFile(targetFile, "rw");
        RandomAccessFileOutStream fos = new RandomAccessFileOutStream(rafOut);
        ExtractOperationResult result = item.extractSlow(fos);
        LOG.info("Unrar \"" + targetFile.getAbsolutePath() + "\":" + result.name());
        fos.close();
        rafOut.close();
      }
      inArchive.close();
      randomAccessFileStream.close();
      randomAccessFile.close();
    }
    catch (Exception e) {
      LOG.error("Unrar of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
  }
}
