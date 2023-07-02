package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.dialogs.TableMediaUploadProgressModel;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class TableMediaFileDropEventHandler implements EventHandler<DragEvent> {

  private final File targetFolder;
  private final TablesSidebarController sidebarController;
  private final PopperScreen screen;
  private final String suffix;

  public TableMediaFileDropEventHandler(File targetFolder, TablesSidebarController sidebarController, PopperScreen screen, String suffix) {
    this.targetFolder = targetFolder;
    this.sidebarController = sidebarController;
    this.screen = screen;
    this.suffix = suffix;
  }

  @Override
  public void handle(DragEvent event) {
    GameRepresentation game = sidebarController.getTablesController().getSelection();

    List<File> files = event.getDragboard().getFiles();
    List<File> filtered = files.stream().filter(f -> f.getName().toLowerCase().endsWith(suffix.toLowerCase())).collect(Collectors.toList());
    if (filtered.isEmpty()) {
      WidgetFactory.showAlert(Studio.stage, "Error", "None of the selected is valid for this upload.", "Files of the " + suffix + " are expected here.");
    }
    else {
      TableMediaUploadProgressModel model = new TableMediaUploadProgressModel(sidebarController, game.getId(),
          "Popper Media Upload", filtered, "popperMedia", screen);
      Dialogs.createProgressDialog(model);
    }
  }
}
