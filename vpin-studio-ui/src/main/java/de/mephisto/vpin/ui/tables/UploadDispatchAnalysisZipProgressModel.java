package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static de.mephisto.vpin.ui.Studio.Features;

public class UploadDispatchAnalysisZipProgressModel extends ProgressModel<ZipEntry> {
  private final static Logger LOG = LoggerFactory.getLogger(UploadDispatchAnalysisZipProgressModel.class);
  private ZipEntry zipEntry;
  private final ZipInputStream zis;
  private final FileInputStream fileInputStream;
  private int size = 0;

  private UploaderAnalysis uploaderAnalysis;

  public UploadDispatchAnalysisZipProgressModel(File file) throws IOException {
    super("Analyzing Archive");
    ZipFile zipFile = new ZipFile(file);
    size = zipFile.size();
    zipFile.close();

    fileInputStream = new FileInputStream(file);
    zis = new ZipInputStream(fileInputStream);

    uploaderAnalysis = new UploaderAnalysis(Features.PUPPACKS_ENABLED, file);
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    try {
      zis.closeEntry();
      zis.close();
      fileInputStream.close();

      progressResultModel.getResults().add(uploaderAnalysis);
    } catch (IOException e) {
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
    try {
      zipEntry = zis.getNextEntry();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return zipEntry != null;
  }

  @Override
  public ZipEntry getNext() {
    return zipEntry;
  }

  @Override
  public String nextToString(ZipEntry zipEntry) {
    return "Analyzing \"" + zipEntry.getName() + "\"";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, ZipEntry next) {
    try {
      uploaderAnalysis.analyze(zis, next, next.getName(), next.isDirectory(), next.getSize());
    } catch (Exception e) {
      LOG.error("Error reading zip file: " + e.getMessage(), e);
    }
  }
}
