package de.mephisto.vpin.server.util;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RarUtil {

  public static String analyzeRar(File archiveFile, List<String> romNames) {
    try {
      Map<InputStream, String> extractedMap = new HashMap<>();

      RandomAccessFile randomAccessFile = new RandomAccessFile(archiveFile, "r");
      RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);

      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
        if (!item.isFolder()) {
          System.out.println(item.getPath());
        }
        else {
          System.out.println(item.getPath());
        }
      }

      return null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    RarUtil.analyzeRar(new File("E:\\downloads\\diner_l4.rar"), Collections.emptyList());
  }
}
