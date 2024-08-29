package de.mephisto.vpin.ui.backglassmanager;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.mephisto.vpin.ui.tables.BaseDragDropHandler;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

public class BackglassManagerDragDropHandler extends BaseDragDropHandler {
  
  BackglassManagerController controller;

  private final List<String> suffixes = Arrays.asList( "zip", "rar", "directb2s" );

  public BackglassManagerDragDropHandler(BackglassManagerController controller, TableView<?> directb2sList, StackPane tableStack) {
    super(directb2sList, tableStack, true);
    this.controller = controller;
    this.overlayController.setMessage("Drop Backglass here...");
  }

  @Override
  protected boolean acceptFile(File file) {
    String extension = FilenameUtils.getExtension(file.getName());
    return suffixes.contains(extension);
  }

  @Override
  protected void processDroppedFile(File file) {
    UploadAnalysisDispatcher.dispatch(file, controller.getGame());
    // when done, force refresh
    controller.refreshBackglass();
  }
}
