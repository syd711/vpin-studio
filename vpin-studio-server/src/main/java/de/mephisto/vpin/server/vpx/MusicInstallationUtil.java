package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MusicInstallationUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MusicInstallationUtil.class);

  public static boolean unpack(@NonNull File archiveFile, @NonNull File musicFolder, @NonNull UploaderAnalysis analysis, @Nullable String rom, @Nullable String relativePath) throws IOException {
    if (!musicFolder.exists()) {
      LOG.error("Music upload failed, no music folder found for default emulator.");
    }

    LOG.info("Extracting music pack into \"" + musicFolder.getAbsolutePath() + "\" with ROM value \"" + rom + "\", relative path: {}", relativePath);
    String suffix = FilenameUtils.getExtension(archiveFile.getName());
    if (suffix.equalsIgnoreCase(AssetType.ZIP.name())) {
      unzipIntoMusicFolder(archiveFile, musicFolder, analysis, rom, relativePath);
    }
    else if (suffix.equalsIgnoreCase(AssetType.RAR.name()) || suffix.equalsIgnoreCase("7z")) {
      unrarIntoMusicFolder(archiveFile, musicFolder, analysis, rom, relativePath);
    }
    return true;
  }

  private static void unzipIntoMusicFolder(@NonNull File archiveFile, @NonNull File musicFolder, @NonNull UploaderAnalysis analysis, @Nullable String rom, @Nullable String relativePath) throws IOException {
    int count = 0;
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
        if (name.toLowerCase().contains("macosx")) {
          zis.closeEntry();
          zipEntry = zis.getNextEntry();
          continue;
        }

        if (relativePath == null && !name.toLowerCase().contains("music/")) {
          zis.closeEntry();
          zipEntry = zis.getNextEntry();
          continue;
        }


        String suffix = FilenameUtils.getExtension(name);
        if (suffix.equalsIgnoreCase("mp3") || suffix.equalsIgnoreCase("ogg") || suffix.equalsIgnoreCase("wav")) {
          File target = null;
          if (StringUtils.isEmpty(relativePath)) {
            String relativeName = name.substring(name.toLowerCase().lastIndexOf("music/") + "music/".length());
            target = new File(musicFolder, relativeName);
          }
          else {
            String fileName = name;
            if (fileName.contains("/")) {
              fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }
            fileName = relativePath + "/" + fileName;
            target = new File(musicFolder, fileName);
          }

          target.getParentFile().mkdirs();
          if (target.exists() && !target.delete()) {
            LOG.warn("Failed to overwrite existing music file \"" + target.getAbsolutePath() + "\"");
          }
          else {
            FileOutputStream fos = new FileOutputStream(target);
            int len;
            while ((len = zis.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
            fos.close();
            LOG.info("Written music pack file " + target.getAbsolutePath());
            count++;
          }
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
      throw e;
    }
    finally {
      LOG.info("Music bundle was installed with " + count + " files, used relative path '" + relativePath + "'");
    }
  }

  private static void unrarIntoMusicFolder(@NonNull File archiveFile, @NonNull File musicFolder, @NonNull UploaderAnalysis analysis, @Nullable String rom, @Nullable String relativePath) throws IOException {
    int count = 0;
    File destinationDir = archiveFile.getParentFile();
    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(archiveFile, "r");
      RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);

      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
        if (item.isFolder()) {
          continue;
        }
        String name = item.getPath().replaceAll("\\\\", "/");
        if (name.toLowerCase().contains("macosx")) {
          continue;
        }
        if (relativePath == null && !name.toLowerCase().contains("music/")) {
          continue;
        }

        String suffix = FilenameUtils.getExtension(name);
        if (suffix.equalsIgnoreCase("mp3") || suffix.equalsIgnoreCase("ogg") ||suffix.equalsIgnoreCase("wav")) {
          File target = null;
          if (StringUtils.isEmpty(relativePath)) {
            String relativeName = name.substring(name.toLowerCase().lastIndexOf("music/") + "music/".length());
            target = new File(musicFolder, relativeName);
          }
          else {
            String fileName = name;
            if (fileName.contains("/")) {
              fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }
            fileName = relativePath + "/" + fileName;
            target = new File(musicFolder, fileName);
          }

          target.getParentFile().mkdirs();
          if (target.exists() && !target.delete()) {
            LOG.warn("Failed to overwrite existing music file \"" + target.getAbsolutePath() + "\"");
          }
          else {
            RandomAccessFile rafOut = new RandomAccessFile(target, "rw");
            RandomAccessFileOutStream fos = new RandomAccessFileOutStream(rafOut);
            ExtractOperationResult result = item.extractSlow(fos);

            LOG.info("Unrar \"" + target.getAbsolutePath() + "\": " + result.name());
            fos.close();
            rafOut.close();
            count++;
          }
        }
      }

      inArchive.close();
      randomAccessFileStream.close();
      randomAccessFile.close();
    }
    catch (Exception e) {
      LOG.error("Unrar of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
    finally {
      LOG.info("Music bundle was installed with " + count + " files, used relative path '" + relativePath + "'");
    }
  }
}
