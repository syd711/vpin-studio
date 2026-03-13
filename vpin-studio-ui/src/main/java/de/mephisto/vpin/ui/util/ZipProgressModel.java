package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.restclient.util.ZipProgressable;
import de.mephisto.vpin.restclient.util.ZipUtil;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ZipProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private List<File> files;
  private final File folder;
  private final File targetZip;

  private final Iterator<File> fileIterator;
  private double count = 0;
  private double total = 0;

  public ZipProgressModel(String title, File folder, File targetZip) {
    super(title);
    this.files = Arrays.asList(folder);
    this.folder = folder;
    this.targetZip = targetZip;
    this.fileIterator = files.iterator();
    this.total = FileUtils.listFiles(folder, null, true).size();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public boolean hasNext() {
    return this.fileIterator.hasNext();
  }

  @Override
  public File getNext() {
    return fileIterator.next();
  }

  @Override
  public String nextToString(File f) {
    return "Preparing Upload Bundle";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File f) {
    try {
      ZipUtil.zipFolder(folder, targetZip, new ZipProgressable() {
        @Override
        public void zipping(File file, String path) {
          LOG.info("Zipped " + file.getAbsolutePath() + " [" + path + "]");
          Platform.runLater(() -> {
            count++;
            double percent = (count * 100 / total) / 100;
            progressResultModel.setProgress(percent);
          });
        }
      });
    }
    catch (Exception e) {
      LOG.error("Error zipping archive: " + e.getMessage(), e);
    }
  }
}
