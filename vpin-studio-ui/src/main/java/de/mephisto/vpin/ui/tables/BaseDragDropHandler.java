package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.DnDOverlayController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ZipProgressModel;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseDragDropHandler {
  private final static Logger LOG = LoggerFactory.getLogger(BaseDragDropHandler.class);

  private final TableView<?> tableView;

  protected DnDOverlayController overlayController;

  /**
   * @param tableView   The Table that is the drop zone
   * @param loaderStack The Stack onto which adding the DndOverlay
   * @param isInDialog  Whether this helper is used in a dialog like Backglass Manager or not
   */
  public BaseDragDropHandler(TableView<?> tableView, StackPane loaderStack, boolean isInDialog) {
    this.tableView = tableView;

    // only one file supported
    overlayController = DnDOverlayController.load(loaderStack, tableView, true);

    tableView.setOnDragOver(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        try {
          List<Window> open = Stage.getWindows().stream().filter(Window::isShowing).filter(s -> s instanceof ContextMenu).collect(Collectors.toList());
          if (open.size() > (isInDialog ? 2 : 1)) {
            return;
          }

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
              if (!acceptFile(file)) {
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

          overlayController.showOverlay(event);
        }
        catch (Exception e) {
          LOG.info("Dragging failed: {}", e.getMessage(), e);
        }
      }
    });


    overlayController.setOnDragDropped(new EventHandler<DragEvent>() {
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
//      tempFile = de.mephisto.vpin.restclient.util.FileUtils.uniqueFile(tempFile);
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
    File tempFile = de.mephisto.vpin.restclient.util.FileUtils.createMatchingTempFile(file);
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
      overlayController.hideOverlay();
      processDroppedFile(file);
    });
  }

  //------------------------------

  // OLE commented as Game area in overlay is invisible
  //protected abstract GameRepresentation getSelectedGame();

  protected abstract boolean acceptFile(File file);

  protected abstract void processDroppedFile(File file);

}
