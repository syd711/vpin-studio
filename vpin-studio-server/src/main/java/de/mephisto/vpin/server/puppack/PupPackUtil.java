package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
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

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PupPackUtil {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackUtil.class);

  public static JobExecutionResult unpack(File archiveFile, File destinationDir, String rom, String pupPackName) {
    if (archiveFile.getName().toLowerCase().endsWith(".zip")) {
      return unzip(archiveFile, destinationDir, rom, pupPackName);
    }
    else if (archiveFile.getName().toLowerCase().endsWith(".rar")) {
      return unrar(archiveFile, destinationDir, rom, pupPackName);
    }
    throw new UnsupportedOperationException("Unsupported archive format for PUP pack " + archiveFile.getName());
  }

  public static JobExecutionResult unrar(File archiveFile, File destinationDir, String rom, String pupPackName) {
    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(archiveFile, "r");
      RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);

      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
        String name = item.getPath().replaceAll("\\\\", "/");
        File newFile = new File(destinationDir, toTargetName(name, rom, pupPackName));
        boolean isInPupPack = name.contains(pupPackName + "/") || name.contains(rom + "/");
        if (!isInPupPack) {
          LOG.info("Skipping extraction of " + newFile.getAbsolutePath());
        }
        else if (item.isFolder()) {
          if (!newFile.exists() && !newFile.mkdirs()) {
            throw new IOException("Failed to create directory " + newFile);
          }
        }
        else {
          File parent = newFile.getParentFile();
          if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory " + parent);
          }

          RandomAccessFile rafOut = new RandomAccessFile(newFile, "rw");
          RandomAccessFileOutStream fos = new RandomAccessFileOutStream(rafOut);
          ExtractOperationResult result = item.extractSlow(fos);
          LOG.info("Unrar \"" + newFile.getAbsolutePath() + "\":" + result.name());
          fos.close();
          rafOut.close();
        }
      }

      inArchive.close();
      randomAccessFileStream.close();
      randomAccessFile.close();
    } catch (Exception e) {
      LOG.error("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return JobExecutionResultFactory.error("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage());
    }
    return JobExecutionResultFactory.empty();
  }

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
  private static String toTargetName(String name, String rom, String pupPackName) {
    String targetFolder = name;
    if (!StringUtils.isEmpty(pupPackName)) {
      while (!targetFolder.startsWith(pupPackName + "/") && targetFolder.contains("/")) {
        targetFolder = targetFolder.substring(targetFolder.indexOf("/") + 1);
      }

      return targetFolder;
    }

    if (!targetFolder.startsWith(pupPackName) && !StringUtils.isEmpty(rom)) {
      targetFolder = name;
      while (!targetFolder.startsWith(rom + "/") && targetFolder.contains("/")) {
        targetFolder = targetFolder.substring(targetFolder.indexOf("/") + 1);
      }
    }

    return targetFolder;
  }
}
