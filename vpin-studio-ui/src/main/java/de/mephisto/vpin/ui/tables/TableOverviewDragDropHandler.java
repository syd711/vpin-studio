package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.DnDOverlayController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
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
  private DnDOverlayController controller;

  private Parent dndLoadingOverlay;

  private List<String> suffixes = Arrays.asList("vpx", "zip", "rar");

  public TableOverviewDragDropHandler(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;

    try {
      FXMLLoader loader = new FXMLLoader(DnDOverlayController.class.getResource("overlay-dnd.fxml"));
      dndLoadingOverlay = loader.load();
      controller = loader.getController();
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    TableView tableView = tableOverviewController.getTableView();
    StackPane loaderStack = tableOverviewController.getLoaderStack();
    tableView.setOnDragOver(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        if (files == null || files.size() > 1) {
          return;
        }

        for (File file : files) {
          String extension = FilenameUtils.getExtension(file.getName());
          if (!suffixes.contains(extension)) {
            return;
          }
        }

        if (event.getDragboard().hasFiles()) {
          event.acceptTransferModes(TransferMode.COPY);
        }
        else {
          event.consume();
        }

        if (!loaderStack.getChildren().contains(dndLoadingOverlay)) {
          tableView.setVisible(false);
          dndLoadingOverlay.setTranslateX(tableView.getTranslateX());
          dndLoadingOverlay.setTranslateY(tableView.getTranslateY());

          controller.setViewParams(tableView.getWidth(), tableView.getHeight());
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

        Platform.runLater(() -> {
          tableView.setVisible(true);
          loaderStack.getChildren().remove(dndLoadingOverlay);

          GameRepresentation selection = tableOverviewController.getSelection();
          UploadDispatcher.dispatch(files.get(0), selection);
        });
      }
    });
  }
}
