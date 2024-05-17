package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.DnDOverlayController;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableOverviewDragDropHandler {
  private final static Logger LOG = LoggerFactory.getLogger(TableOverviewDragDropHandler.class);
  private final TableOverviewController tableOverviewController;
  private final TableView tableView;
  private final StackPane loaderStack;
  private final TablesController tablesController;

  private DnDOverlayController controller;

  private Parent dndLoadingOverlay;

  private final List<String> suffixes = Arrays.asList("vpx", "zip", "rar", "7z", "ini", "pov", "directb2s", "vni", "pal", "pac", "crz");

  public TableOverviewDragDropHandler(TablesController tablesController) {
    this.tablesController = tablesController;
    tableOverviewController = tablesController.getTableOverviewController();
    try {
      FXMLLoader loader = new FXMLLoader(DnDOverlayController.class.getResource("overlay-dnd.fxml"));
      dndLoadingOverlay = loader.load();
      controller = loader.getController();
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    tableView = tableOverviewController.getTableView();
    loaderStack = tableOverviewController.getLoaderStack();
    tableView.setOnDragOver(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        if (files == null || files.size() > 1) {
          return;
        }

        for (File file : files) {
          //zipped files
          if (file.length() == 0) {
            continue;
          }
          String extension = FilenameUtils.getExtension(file.getName());
          if (!suffixes.contains(extension.toLowerCase())) {
            return;
          }
        }

        if (event.getDragboard().hasFiles()) {
          event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        else {
          event.consume();
        }

        if (!loaderStack.getChildren().contains(dndLoadingOverlay)) {
          tableView.setVisible(false);
          dndLoadingOverlay.setTranslateX(tableView.getTranslateX());
          dndLoadingOverlay.setTranslateY(tableView.getTranslateY());

          double width = ((Pane) tableView.getParent()).getWidth();
          double height = tableView.getHeight();
          controller.setViewParams(width, height);
          controller.setGame(tablesController.getTableOverviewController().getSelection());
          loaderStack.getChildren().add(dndLoadingOverlay);
        }
      }
    });

    dndLoadingOverlay.setOnDragOver(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
          event.acceptTransferModes(TransferMode.COPY);
        }
        else {
          event.consume();
        }
      }
    });

    dndLoadingOverlay.setOnDragExited(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        tableView.setVisible(true);
        loaderStack.getChildren().remove(dndLoadingOverlay);
      }
    });

    dndLoadingOverlay.setOnDragDropped(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        event.consume();
        List<File> files = new ArrayList<>(event.getDragboard().getFiles());
        File file = files.get(0);
        LOG.info("Dropped file " + file.getAbsolutePath());

        String path = file.getAbsolutePath().toLowerCase();
        if (path.contains("user") && path.contains("temp")) {
          try {
            File tempFile = de.mephisto.vpin.commons.utils.FileUtils.createMatchingTempFile(file);
            tempFile.deleteOnExit();
            FileUtils.copyFile(file, tempFile);
            file = tempFile;
            LOG.info("Created separate temp file for dropped archive file: " + tempFile.getAbsolutePath());
          }
          catch (Exception e) {
            LOG.info("Failed to create temporary drop file: " + e.getMessage(), e);
            Platform.runLater(() -> {
              WidgetFactory.showAlert(Studio.stage, "Error", "Failed to create temporary drop file: " + e.getMessage());
            });
          }
        }
        dispatchDroppedFile(file);
      }
    });
  }


  private void dispatchDroppedFile(File file) {
    Platform.runLater(() -> {
      tableView.setVisible(true);
      loaderStack.getChildren().remove(dndLoadingOverlay);
      GameRepresentation selection = tableOverviewController.getSelection();
      UploadAnalysisDispatcher.dispatch(tablesController.getTablesSideBarController(), file, selection);
    });
  }
}
