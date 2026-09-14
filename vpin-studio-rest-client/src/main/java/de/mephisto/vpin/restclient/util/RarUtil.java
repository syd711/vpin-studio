package de.mephisto.vpin.restclient.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import net.sf.sevenzipjbinding.util.ByteArrayStream;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RarUtil {

  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

  public static boolean unrar(File archiveFile, File targetFolder) {
    return unrar(archiveFile, targetFolder, null, Collections.emptyList());
  }

  public static boolean unrar(@NonNull File archiveFile, @NonNull File targetFolder, @Nullable String archiveFolder, @NonNull List<String> suffixAllowList) {
    return unrar(archiveFile, targetFolder, archiveFolder, suffixAllowList, null);
  }

  public static boolean unrar(@NonNull File archiveFile, @NonNull File targetFolder, @Nullable String archiveFolder, @NonNull List<String> suffixAllowList, @Nullable UnzipChangeListener listener) {
    boolean success = true;
    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(archiveFile, "r");
      RandomAccessFileInStream randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
      IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileStream);
      int total = inArchive.getNumberOfItems();
      int index = 0;

      //first a cheap metadata-only pass to work out which entries to extract and where they go,
      //so cancellation and progress reporting stay fast even on huge archives
      Map<Integer, File> targetsByIndex = new LinkedHashMap<>();
      boolean cancelled = false;

      for (ISimpleInArchiveItem item : inArchive.getSimpleInterface().getArchiveItems()) {
        if (item.isFolder()) {
          //ignore
        }
        else {
          if (listener != null) {
            boolean continueOp = listener.unzipping(item.getPath(), index, total);
            if (!continueOp) {
              cancelled = true;
              break;
            }
          }
          index++;

          String entryName = item.getPath().replaceAll("\\\\", "/");
          String suffix = FilenameUtils.getExtension(entryName);

//          if (!entryName.startsWith("/")) {
//            entryName = "/" + entryName;
//          }
          boolean isTargetFolder = archiveFolder == null || entryName.startsWith(archiveFolder);
          LOG.info("Checking extraction of {}", entryName);
          if (suffixAllowList.isEmpty() || suffixAllowList.contains(suffix.toLowerCase()) || isTargetFolder) {
            String itempath = entryName;
            if (archiveFolder != null) {
              if (!itempath.startsWith(archiveFolder)) {
                continue;
              }
              itempath = itempath.substring(archiveFolder.length());
              if (itempath.startsWith("/") || itempath.startsWith("\\")) {
                itempath = itempath.substring(1);
              }
            }
            File target = new File(targetFolder, itempath);
            // delete existing file and don't simply write in it
            // that would corrupt the file in case conten tto be comied is smaller than previous size
            if (target.isFile() && target.exists() && !target.delete()) {
              LOG.error("Failed to delete existing unrar target file {}", target.getAbsolutePath());
            }
            else {
              // folder creation
              File parent = target.getParentFile();
              if (!parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("Failed to create directory " + parent);
              }
            }

            targetsByIndex.put(item.getItemIndex(), target);
          }
        }
      }

      //extract every matching entry in a single sequential pass through the archive stream instead of calling
      //extractSlow() once per file: for a solid archive, extractSlow() re-decodes the preceding data from the
      //start of the solid block on every call, which turns extraction of many files into a near-quadratic operation
      if (!cancelled && !targetsByIndex.isEmpty()) {
        int[] indices = targetsByIndex.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
        inArchive.extract(indices, false, new BulkExtractCallback(targetsByIndex));
      }

      inArchive.close();
      randomAccessFileStream.close();
      randomAccessFile.close();
    }
    catch (Exception e) {
      success = false;
      if (listener != null) {
        listener.onError("Unzipping of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage());
      }
      LOG.error("Unrar of " + archiveFile.getAbsolutePath() + " failed: " + e.getMessage(), e);
    }
    return success;
  }

  /**
   * Delivers the output for a batch {@link IInArchive#extract} call to the target file resolved for
   * each requested item index. Used instead of {@link ISimpleInArchiveItem#extractSlow} so that extracting
   * many entries out of the same (possibly solid) archive only decodes the underlying stream once.
   */
  private static class BulkExtractCallback implements IArchiveExtractCallback {
    private final Map<Integer, File> targetsByIndex;
    private File currentTarget;
    private RandomAccessFile currentRandomAccessFile;
    private RandomAccessFileOutStream currentOutStream;

    private BulkExtractCallback(Map<Integer, File> targetsByIndex) {
      this.targetsByIndex = targetsByIndex;
    }

    @Override
    public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
      currentTarget = targetsByIndex.get(index);
      if (currentTarget == null || extractAskMode != ExtractAskMode.EXTRACT) {
        return null;
      }
      try {
        currentRandomAccessFile = new RandomAccessFile(currentTarget, "rw");
        currentOutStream = new RandomAccessFileOutStream(currentRandomAccessFile);
        return currentOutStream;
      }
      catch (IOException e) {
        throw new SevenZipException("Failed to open unrar target file " + currentTarget.getAbsolutePath(), e);
      }
    }

    @Override
    public void prepareOperation(ExtractAskMode extractAskMode) {
      //no-op
    }

    @Override
    public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
      if (currentOutStream == null) {
        return;
      }
      try {
        currentOutStream.close();
        currentRandomAccessFile.close();
      }
      catch (IOException e) {
        throw new SevenZipException("Failed to close unrar target file " + currentTarget.getAbsolutePath(), e);
      }
      LOG.info("Unrar \"{}\": {}", currentTarget.getAbsolutePath(), extractOperationResult.name());
      currentOutStream = null;
      currentRandomAccessFile = null;
      currentTarget = null;
    }

    @Override
    public void setTotal(long total) {
      //no-op, only per-file progress is reported via UnzipChangeListener during the metadata pass
    }

    @Override
    public void setCompleted(long complete) {
      //no-op
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
