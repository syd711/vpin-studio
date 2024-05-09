package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;
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

abstract public class UploadDispatchAnalysisZipProgressModel extends ProgressModel<ZipEntry> {
  private final static Logger LOG = LoggerFactory.getLogger(UploadDispatchAnalysisZipProgressModel.class);
  private final GameRepresentation game;
  private final File file;
  private ZipEntry zipEntry;
  private final ZipInputStream zis;
  private final FileInputStream fileInputStream;
  private int size = 0;

  public UploadDispatchAnalysisZipProgressModel(GameRepresentation game, File file) throws IOException {
    super("Analyzing Archive");
    this.game = game;
    this.file = file;
    ZipFile zipFile = new ZipFile(file);
    size = zipFile.size();

    fileInputStream = new FileInputStream(file);
    zis = new ZipInputStream(fileInputStream);
    zipEntry = zis.getNextEntry();
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    try {
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (IOException e) {
      LOG.error("Error finalizing zip file: " + e.getMessage(), e);
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
      return zipEntry != null;
    } catch (IOException e) {
      LOG.error("Error reading zip file " + file.getAbsolutePath() + ": " + e.getMessage(), e);
      return false;
    }
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

    } catch (Exception e) {
      LOG.error("Error reading zip file: " + e.getMessage(), e);
    }
  }

  abstract protected void analyze(ZipEntry entry);
}
