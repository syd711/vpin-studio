package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.dropins.DropInManager;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

abstract public class UploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private File file;
  private List<File> files;
  private Iterator<File> iterator;

  public UploadProgressModel(File file, String title) {
    super(title);
    this.file = file;
    this.iterator = Collections.singletonList(this.file).iterator();
  }

  public UploadProgressModel(List<File> files, String title) {
    super(title);
    this.files = files;
    this.iterator = files.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return files != null ? files.size() : 1;
  }

  @Override
  public File getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(File file) {
    return files != null? file.getName() : "Uploading " + file.getName();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    FileUtils.deleteIfTempFile(file);
    FileUtils.deleteIfTempFile(files);

    super.finalizeModel(progressResultModel);

    if (file != null && DropInManager.getInstance().isDropInFile(file)) {
      Platform.runLater(() -> {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete \"" + file.getAbsolutePath() + "\"?", "The file will be moved to the trash bin.");
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
          Desktop.getDesktop().moveToTrash(file);
        }
      });
    }
  }
}
