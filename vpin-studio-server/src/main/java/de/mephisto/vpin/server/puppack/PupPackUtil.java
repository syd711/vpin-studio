package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PupPackUtil {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackUtil.class);

  public static JobExecutionResult unpack(@NonNull File archiveFile, @NonNull File destinationDir, @NonNull String rom) {
    if (archiveFile.getName().toLowerCase().endsWith(".zip")) {
      return unzip(archiveFile, destinationDir, rom);
    }
    else if (archiveFile.getName().toLowerCase().endsWith(".rar")) {
//      return unrar(archiveFile, destinationDir);
    }
    throw new UnsupportedOperationException("Unsupported archive format for PUP pack " + archiveFile.getName());
  }

//  public static JobExecutionResult unrar(File archiveFile, File destinationDir, String rom, String pupPackName) {
//    try {
//      RandomAccessFile randomAccessFile = new RandomAccessFile(archiveFile, "r");
//      RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
//      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);
//
//      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
//        String name = item.getPath().replaceAll("\\\\", "/");
//        File newFile = new File(destinationDir, toTargetName(name, rom, pupPackName));
//        boolean isInPupPack = name.contains(pupPackName + "/") || name.contains(rom + "/");
//        if (!isInPupPack) {
//          LOG.info("Skipping extraction of " + newFile.getAbsolutePath());
//        }
//        else if (item.isFolder()) {
//          if (!newFile.exists() && !newFile.mkdirs()) {
//            throw new IOException("Failed to create directory " + newFile);
//          }
//        }
//        else {
//          File parent = newFile.getParentFile();
//          if (!parent.isDirectory() && !parent.mkdirs()) {
//            throw new IOException("Failed to create directory " + parent);
//          }
//
//          RandomAccessFile rafOut = new RandomAccessFile(newFile, "rw");
//          RandomAccessFileOutStream fos = new RandomAccessFileOutStream(rafOut);
//          ExtractOperationResult result = item.extractSlow(fos);
//          LOG.info("Unrar \"" + newFile.getAbsolutePath() + "\":" + result.name());
//          fos.close();
//          rafOut.close();
//        }
//      }
//
//      inArchive.close();
//      randomAccessFileStream.close();
//      randomAccessFile.close();
//    } catch (Exception e) {
//      LOG.error("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
//      return JobExecutionResultFactory.error("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage());
//    }
//    return JobExecutionResultFactory.empty();
//  }

  public static JobExecutionResult unzip(@NonNull File archiveFile, @NonNull File destinationDir, @NonNull String rom) {
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        if (zipEntry.isDirectory()) {
          zis.closeEntry();
          zipEntry = zis.getNextEntry();
          continue;
        }

        String name = zipEntry.getName();
        File newFile = toTargetFile(destinationDir, rom, name);
        if (newFile != null) {
          newFile.getParentFile().mkdirs();
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
    }
    catch (Exception e) {
      LOG.error("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return JobExecutionResultFactory.error("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage());
    }
    return JobExecutionResultFactory.empty();
  }

  @Nullable
  private static File toTargetFile(@NonNull File packPackDir, @NonNull String rom, @NonNull String name) {
    File folder = new File(packPackDir, rom);

    if (name.contains(rom)) {
      String fileName = name.substring(name.indexOf(rom) + rom.length());
      File targetFile = new File(folder, fileName);
      return targetFile;
    }
    return null;
  }
}
