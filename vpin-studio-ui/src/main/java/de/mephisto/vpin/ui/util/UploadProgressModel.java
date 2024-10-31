package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.restclient.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

abstract public class UploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(UploadProgressModel.class);

  private File file;
  private List<File> files;

  private Runnable finalizer;

  public UploadProgressModel(File file, String title, Runnable finalizer) {
    super(title);
    this.file = file;
    this.finalizer = finalizer;
  }

  public UploadProgressModel(List<File> files, String title, Runnable finalizer) {
    super(title);
    this.files = files;
    this.finalizer = finalizer;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    FileUtils.deleteIfTempFile(file);
    FileUtils.deleteIfTempFile(files);
    super.finalizeModel(progressResultModel);
    if (finalizer != null) {
      finalizer.run();
    }
  }
}
