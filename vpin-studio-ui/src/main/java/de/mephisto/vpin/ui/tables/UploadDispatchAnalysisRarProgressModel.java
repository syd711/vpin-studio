package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.i18n.Messages;
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
  private boolean readmeCounted = false;
  private boolean readmeStepDone = false;

  public UploadDispatchAnalysisRarProgressModel(File file) throws IOException {
    super(Messages.get("dialog.analyzing_archive"));


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
    if (iterator.hasNext()) {
      return true;
    }
    //once all archive entries have been scanned, run the (potentially slow) readme extraction
    //as one final, clearly labeled step instead of blocking the scan on whichever entry it happens to be
    return uploaderAnalysis.hasPendingReadme() && !readmeStepDone;
  }

  @Override
  public ISimpleInArchiveItem getNext() {
    if (iterator.hasNext()) {
      return iterator.next();
    }
    //sentinel value for the deferred readme extraction step, see processNext()/nextToString()
    return null;
  }

  @Override
  public String nextToString(ISimpleInArchiveItem entry) {
    if (entry == null) {
      return Messages.get("dialog.analyzing_quoted", "readme file");
    }
    try {
      return Messages.get("dialog.analyzing_quoted", entry.getPath());
    }
    catch (SevenZipException e) {
      LOG.error("Failed to read entry: " + e.getMessage(), e);
    }
    return "";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, ISimpleInArchiveItem next) {
    try {
      if (next == null) {
        uploaderAnalysis.extractPendingReadme();
        readmeStepDone = true;
        return;
      }

      uploaderAnalysis.analyze(inArchive, next, next.getPath(), next.isFolder(), next.getSize());
      if (!readmeCounted && uploaderAnalysis.hasPendingReadme()) {
        //account for the extra readme extraction step added at the end of the scan
        readmeCounted = true;
        size++;
      }
    }
    catch (Exception e) {
      LOG.error("Error reading zip file: " + e.getMessage(), e);
    }
  }
}
