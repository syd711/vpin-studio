package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Iterator;

import static de.mephisto.vpin.ui.Studio.Features;

public class UploadDispatchAnalysisRarProgressModel extends ProgressModel<ISimpleInArchiveItem> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final RandomAccessFile randomAccessFile;
  private final RandomAccessFileInStream randomAccessFileStream;
  private final ISimpleInArchiveItem[] archiveItems;
  private final IInArchive inArchive;
  private int size = 0;
  private Iterator<ISimpleInArchiveItem> iterator;

  private UploaderAnalysis uploaderAnalysis;

  public UploadDispatchAnalysisRarProgressModel(File file) throws IOException {
    super("Analyzing Archive");


    randomAccessFile = new RandomAccessFile(file, "r");
    randomAccessFileStream = new RandomAccessFileInStream(randomAccessFile);
    inArchive = SevenZip.openInArchive(null, randomAccessFileStream);

    archiveItems = inArchive.getSimpleInterface().getArchiveItems();
    size = archiveItems.length;
    iterator = Arrays.stream(archiveItems).iterator();

    uploaderAnalysis = new UploaderAnalysis(Features.PUPPACKS_ENABLED, file);
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    try {
      randomAccessFileStream.close();
      randomAccessFile.close();

      progressResultModel.getResults().add(uploaderAnalysis);
    }
    catch (IOException e) {
      LOG.error("Error finalizing zip file: " + e.getMessage());
    }
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return size;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public ISimpleInArchiveItem getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(ISimpleInArchiveItem entry) {
    try {
      return "Analyzing \"" + entry.getPath() + "\"";
    }
    catch (SevenZipException e) {
      LOG.error("Failed to read entry: " + e.getMessage(), e);
    }
    return "";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, ISimpleInArchiveItem next) {
    try {
      uploaderAnalysis.analyze(inArchive, next, next.getPath(), next.isFolder(), next.getSize());
    }
    catch (Exception e) {
      LOG.error("Error reading zip file: " + e.getMessage(), e);
    }
  }
}
