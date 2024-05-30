package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.ZipUtil;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.DnDOverlayController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ZipProgressModel;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipOutputStream;

public class TableOverviewDragDropHandler {
  private final static Logger LOG = LoggerFactory.getLogger(TableOverviewDragDropHandler.class);
  private final TableOverviewController tableOverviewController;
  private final TableView tableView;
  private final StackPane loaderStack;
  private final TablesController tablesController;

  private DnDOverlayController controller;

  private Parent dndLoadingOverlay;

  private final List<String> suffixes = Arrays.asList("vpx", "zip", "ini", "pov", "directb2s", "vni", "pal", "pac", "crz", "cfg", "nv");

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

          if (file.isFile()) {
            String extension = FilenameUtils.getExtension(file.getName());
            if (!suffixes.contains(extension.toLowerCase())) {
              return;
            }
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
        if (event.getDragboard().hasFiles() && event.getDragboard().getFiles().size() == 1) {
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
        final File file = files.get(0);
        LOG.info("Dropped file " + file.getAbsolutePath());

        try {
          File systemTmpFolder = new File(System.getProperty("java.io.tmpdir"));
          if (file.getAbsolutePath().startsWith(systemTmpFolder.getAbsolutePath())) {
            if (file.isFile()) {
              dispatchTemporaryFile(file);
            }
            else if (file.isDirectory()) {
              dispatchTemporaryFolder(systemTmpFolder, file);
            }
          }
          else if (file.isFile()) {
            dispatchDroppedFile(file);
          }
          else if (file.isDirectory()) {
            dispatchDroppedFolder(file);
          }
        }
        catch (Exception e) {
          LOG.info("Failed to dispatch dropped file: " + e.getMessage(), e);
          Platform.runLater(() -> {
            WidgetFactory.showAlert(Studio.stage, "Error", "Failed to drop file: " + e.getMessage());
          });
        }
      }
    });
  }

  private void dispatchDroppedFolder(File file) throws Exception {
    File systemTmpFolder = new File(System.getProperty("java.io.tmpdir"));
    File tempFile = new File(systemTmpFolder, file.getName() + ".zip");
    tempFile.deleteOnExit();

    if (tempFile.exists() && !tempFile.delete()) {
      throw new Exception("Failed to delete existing temp file " + tempFile.getAbsolutePath());
    }

    LOG.info("Zipping " + file.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
    Platform.runLater(() -> {
      ProgressDialog.createProgressDialog(new ZipProgressModel("Bundling \"" + file.getName() + "\"", file, tempFile));
      LOG.info("Created separate temp zip file \"" + tempFile.getAbsolutePath() + "\" for dropped folder \"" + file.getAbsolutePath() + "\"");
      dispatchDroppedFile(tempFile);
    });
  }

  private void dispatchTemporaryFolder(File systemTmpFolder, File file) throws Exception {
    //immediately create a copy of the dropped folder, otherwise the OS will clean these
    File tempFolder = new File(systemTmpFolder, file.getName());
    File zipTempFolder = new File(tempFolder, file.getName());
    zipTempFolder.mkdirs();
    zipTempFolder.deleteOnExit();
    FileUtils.copyDirectory(file, zipTempFolder);

    File tempFile = new File(systemTmpFolder, file.getName() + ".zip");
    if (tempFile.exists() && !tempFile.delete()) {
      LOG.error("Failed to delete existing temp file " + tempFile.getAbsolutePath());
//      tempFile = de.mephisto.vpin.commons.utils.FileUtils.uniqueFile(tempFile);
      throw new Exception("Failed to delete existing temp file " + tempFile.getAbsolutePath());
    }

    File temporaryUploadFile = tempFile;

    LOG.info("Zipping " + tempFolder.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
    Platform.runLater(() -> {
      ProgressDialog.createProgressDialog(new ZipProgressModel("Bundling " + file.getName(), zipTempFolder, temporaryUploadFile));
      LOG.info("Created separate temp zip file for dropped folder: " + file.getAbsolutePath());
      try {
        if (file.getAbsolutePath().startsWith(systemTmpFolder.getAbsolutePath())) {
          FileUtils.deleteDirectory(file);
          LOG.info("Delete temp folder " + file.getAbsolutePath());
        }
      }
      catch (IOException e) {
        LOG.error("Failed to delete temp folder " + file.getAbsolutePath(), e);
      }

      dispatchDroppedFile(temporaryUploadFile);
    });
  }

  private void dispatchTemporaryFile(File file) throws IOException {
    File tempFile = de.mephisto.vpin.commons.utils.FileUtils.createMatchingTempFile(file);
    tempFile.deleteOnExit();
    FileUtils.copyFile(file, tempFile);
    LOG.info("Created separate temp file for dropped archive file: " + tempFile.getAbsolutePath());
    if (tempFile.length() > 0) {
      dispatchDroppedFile(tempFile);
    }
    else {
      LOG.info("Skipped drop of " + tempFile.getAbsolutePath() + ", because the file is empty.");
    }
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
