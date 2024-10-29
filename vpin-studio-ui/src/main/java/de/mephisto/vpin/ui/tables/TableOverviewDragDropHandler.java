package de.mephisto.vpin.ui.tables;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

public class TableOverviewDragDropHandler extends BaseDragDropHandler {
  
  private final TableOverviewController tableController;


  public TableOverviewDragDropHandler(TableOverviewController tableController, TableView<?> tableView, StackPane loaderStack) {
    super(tableView, loaderStack, false);
    this.tableController = tableController;
  }

  //@Override
  protected GameRepresentation getSelectedGame() {
    return tableController.getSelection();
  }

  @Override
  protected boolean acceptFile(File file) {
    String extension = FilenameUtils.getExtension(file.getName());
    return AssetType.isInstallable(extension);
  }

  @Override
  protected void processDroppedFile(File file) {
    GameRepresentation selection = getSelectedGame();
    UploadAnalysisDispatcher.dispatch(file, selection, null);
  }
}
