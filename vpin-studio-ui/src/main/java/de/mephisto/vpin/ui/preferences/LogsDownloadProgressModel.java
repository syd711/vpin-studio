package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class LogsDownloadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(LogsDownloadProgressModel.class);
  private final File targetFolder;
  private Iterator<File> fileIterator;
  private List<File> files;
  private boolean hasNext = true;

  public LogsDownloadProgressModel(String title, File targetFolder) {
    super(title);
    this.targetFolder = targetFolder;
    this.files = Arrays.asList(targetFolder);
    this.fileIterator = files.iterator();
  }

  @Override
  public int getMax() {
    return this.files.size();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public File getNext() {
    return this.fileIterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(File next) {
    return null;
  }

  @Override
  public boolean hasNext() {
    return fileIterator.hasNext();
  }

  public void processNext(ProgressResultModel progressResultModel, File item) {
    try {
      File f = new File(targetFolder, "vpin-studio-logs.zip");
      File file = FileUtils.uniqueFile(f);
      client.download("system/download/logs", file);
      progressResultModel.getResults().add(file);
    } catch (Exception e) {
      LOG.error("Logs download error: " + e.getMessage(), e);
    }
  }
}
