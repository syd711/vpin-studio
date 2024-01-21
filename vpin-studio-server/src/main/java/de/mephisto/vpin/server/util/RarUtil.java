package de.mephisto.vpin.server.util;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RarUtil {

  private final static Logger LOG = LoggerFactory.getLogger(RarUtil.class);

  public static String contains(@NonNull File archiveFile, @NonNull String suffix) {
    String fileFound = null;
    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(archiveFile, "r");
      RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);

      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
        if (item.isFolder()) {
          continue;
        }
        else {
          if (item.getPath().toLowerCase().endsWith(suffix)) {
            fileFound = item.getPath();
            break;
          }
        }
      }
      return fileFound;
    } catch (Exception e) {
      LOG.error("Failed to check rar file \"" + archiveFile.getAbsolutePath() + "\": " + e.getMessage(), e);
    }
    return null;
  }

  public static boolean unrarTargetFile(File archiveFile, File targetFile, String name) {
    boolean written = false;
    File destinationDir = targetFile.getParentFile();
    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(archiveFile, "r");
      RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);

      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
        if (item.isFolder()) {
          //ignore
        }
        else {
          String entryName = item.getPath().replaceAll("\\\\", "/");
          if (entryName.contains("/")) {
            entryName = entryName.substring(entryName.lastIndexOf("/") + 1);
          }

          File newFile = new File(destinationDir, entryName);
          File parent = newFile.getParentFile();
          if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory " + parent);
          }

          if (entryName.endsWith(name)) {
            RandomAccessFile rafOut = new RandomAccessFile(targetFile, "rw");
            RandomAccessFileOutStream fos = new RandomAccessFileOutStream(rafOut);
            ExtractOperationResult result = item.extractSlow(fos);

            LOG.info("Unrar \"" + targetFile.getAbsolutePath() + "\": " + result.name());
            fos.close();
            rafOut.close();

            written = true;
            break;
          }
        }
      }

      if (!written) {
        LOG.error("Failed to unrar file \"" + name + "\", the file has not been found.");
      }

      inArchive.close();
      randomAccessFileStream.close();
      randomAccessFile.close();
    } catch (Exception e) {
      LOG.error("Unrar of " + targetFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
    return written;
  }
}
