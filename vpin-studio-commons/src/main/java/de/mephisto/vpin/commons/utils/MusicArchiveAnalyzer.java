package de.mephisto.vpin.commons.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MusicArchiveAnalyzer {
  private final static Logger LOG = LoggerFactory.getLogger(MusicArchiveAnalyzer.class);


  public static String analyze(@NonNull File file) {
    String fileName = file.getName().toLowerCase();
    if (fileName.endsWith(".zip")) {
      return analyzeZip(file);
    }
    if (fileName.endsWith(".rar") || fileName.endsWith(".7z")) {
      return analyzeRar(file);
    }
    return null;
  }

  private static String analyzeRar(@NonNull File file) {
    boolean fileFound = false;
    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
      RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);

      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
        if (item.isFolder()) {
          continue;
        }

        String name = item.getPath().toLowerCase();
        if (name.endsWith(".mp3") || name.endsWith(".ogg") || name.endsWith(".wav")) {
          fileFound = true;
          break;
        }
      }

      inArchive.close();
      randomAccessFileStream.close();
      randomAccessFile.close();
    }
    catch (Exception e) {
      LOG.error("Failed to check rar file \"" + file.getAbsolutePath() + "\": " + e.getMessage(), e);
      return "Failed to check rar file \"" + file.getAbsolutePath() + "\": " + e.getMessage();
    }

    if (!fileFound) {
      return "The selected archive does not contain any audio files.";
    }
    return null;
  }

  private static String analyzeZip(@NonNull File file) {
    boolean entryFound = false;
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(file);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        if (zipEntry.isDirectory()) {
          //ignore
        }
        else {
          String name = zipEntry.getName().toLowerCase();
          if (name.endsWith(".mp3") || name.endsWith(".ogg") || name.endsWith(".wav")) {
            entryFound = true;
          }
        }
        zis.closeEntry();

        if (entryFound) {
          break;
        }

        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    }
    catch (Exception e) {
      LOG.error("Analyzing of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return "Analyzing of " + file.getAbsolutePath() + " failed: " + e.getMessage();
    }

    if (!entryFound) {
      return "The selected archive does not contain any audio files.";
    }
    return null;
  }
}
