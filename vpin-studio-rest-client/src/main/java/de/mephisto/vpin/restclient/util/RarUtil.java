package de.mephisto.vpin.restclient.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import net.sf.sevenzipjbinding.util.ByteArrayStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

      inArchive.close();
      randomAccessFileStream.close();
      randomAccessFile.close();
      return fileFound;
    }
    catch (Exception e) {
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
          if (entryName.toLowerCase().endsWith(name.toLowerCase())) {
            // delete existing file and don't simply write in it 
            // that would corrupt the file in case conten tto be comied is smaller than previous size
            if (targetFile.exists()) {
              targetFile.delete();
            }
            else {
              //folder creation
              File parent = targetFile.getParentFile();
              if (!parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("Failed to create directory " + parent);
              }
            }

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
    }
    catch (Exception e) {
      LOG.error("Unrar of " + targetFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
    return written;
  }

  public static void unrar(File archiveFile, File targetFolder) {
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
          File target = new File(targetFolder, item.getPath());
          
          // delete existing file and don't simply write in it 
          // that would corrupt the file in case conten tto be comied is smaller than previous size
          if (target.exists()) {
            target.delete();
          }
          else {
            // folder creation 
            File parent = target.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
              throw new IOException("Failed to create directory " + parent);
            }
          }

          RandomAccessFile rafOut = new RandomAccessFile(target, "rw");
          RandomAccessFileOutStream fos = new RandomAccessFileOutStream(rafOut);
          ExtractOperationResult result = item.extractSlow(fos);

          LOG.info("Unrar \"" + target.getAbsolutePath() + "\": " + result.name());
          fos.close();
          rafOut.close();
        }
      }

      inArchive.close();
      randomAccessFileStream.close();
      randomAccessFile.close();
    }
    catch (Exception e) {
      LOG.error("Unrar of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
  }

  public static byte[] readFile(File file, String name) {
    byte[] bytes = null;
    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
      RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);

      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
        if (item.isFolder()) {
          //ignore
        }
        else {
          String entryName = item.getPath().replaceAll("\\\\", "/");
          if (entryName.equals(name)) {
            ByteArrayStream fos = new ByteArrayStream(Integer.MAX_VALUE);
            ExtractOperationResult result = item.extractSlow(fos);
            bytes = fos.getBytes();
            break;
          }
        }
      }
      inArchive.close();
      randomAccessFileStream.close();
      randomAccessFile.close();
    }
    catch (Exception e) {
      LOG.error("Unrar of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
    return bytes;
  }
}
