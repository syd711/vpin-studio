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

  public UploadProgressModel(File file, String title) {
    super(title);
    this.file = file;
  }

  public UploadProgressModel(List<File> files, String title) {
    super(title);
    this.files = files;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    FileUtils.deleteIfTempFile(file);
    FileUtils.deleteIfTempFile(files);
    super.finalizeModel(progressResultModel);
  }
}
