package de.mephisto.vpin.server.altsound;

import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AltSoundUtil {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundUtil.class);

  public static void unpack(File archiveFile, File destinationDir) {
    if (archiveFile.getName().toLowerCase().endsWith(".rar")) {
      unrar(archiveFile, destinationDir);
    }
    else {
      unzip(archiveFile, destinationDir);
    }
  }

  private static void unrar(File archiveFile, File destinationDir) {
    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(archiveFile, "r");
      RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);

      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
        if (item.isFolder()) {
          //ignore
        }
        else {
          String name = item.getPath().replaceAll("\\\\", "/");
          if (isValidAltSoundFile(name)) {
            final int[] hash = new int[] { 0 };

            // fix for Windows-created archives
            if (name.contains("/")) {
              name = name.substring(name.lastIndexOf("/") + 1);
            }
            File target = new File(destinationDir, name);
            RandomAccessFile rafOut = new RandomAccessFile(target, "rw");
            RandomAccessFileOutStream fos = new RandomAccessFileOutStream(rafOut);
            ExtractOperationResult result = item.extractSlow(fos);
            LOG.info("Unrar \"" + target.getAbsolutePath() + "\":" + result.name());
            fos.close();
            rafOut.close();
          }
        }
      }
      inArchive.close();
      randomAccessFileStream.close();
      randomAccessFile.close();
    } catch (Exception e) {
      LOG.error("Unrar of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
  }

  private static void unzip(File archiveFile, File destinationDir) {
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        if (zipEntry.isDirectory()) {
          //ignore
        }
        else {
          String name = zipEntry.getName();
          if (isValidAltSoundFile(name)) {
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
    }
  }

  private static boolean isValidAltSoundFile(String name) {
    return name.endsWith(".ogg") || name.endsWith(".mp3") || name.endsWith(".csv") || name.endsWith(".ini");
  }
}
