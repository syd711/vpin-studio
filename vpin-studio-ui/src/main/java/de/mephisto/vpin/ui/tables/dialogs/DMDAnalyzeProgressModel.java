package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.ui.util.DMDBundleAnalyzer;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.PupPackAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;

public class DMDAnalyzeProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(DMDAnalyzeProgressModel.class);

  private final Iterator<File> iterator;
  private final File file;
  private DMDBundleAnalyzer analyzer;

  public DMDAnalyzeProgressModel(String title, File file) {
    super(title);
    this.file = file;
    this.iterator = Collections.singletonList(this.file).iterator();
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
  public File getNext() {
    return iterator.next();
  }

  @Override
  public void cancel() {
    super.cancel();
    this.analyzer.cancel();
  }

  @Override
  public String nextToString(File file) {
    return "Analyzing " + file.getName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      analyzer = new DMDBundleAnalyzer();
      analyzer.analyze(next, progressResultModel);
      progressResultModel.addProcessed();
    } catch (Exception e) {
      LOG.error("PUP pack upload failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
