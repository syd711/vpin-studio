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
import java.util.List;
import java.util.Optional;

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
