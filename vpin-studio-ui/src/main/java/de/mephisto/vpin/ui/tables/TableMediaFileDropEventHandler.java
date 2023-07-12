package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.SystemSummary;
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

  private final PopperScreen screen;
  private final String suffix;
  private final SystemSummary systemSummary;
  private final TablesSidebarController tablesSidebarController;

  public TableMediaFileDropEventHandler(SystemSummary systemSummary, TablesSidebarController tablesSidebarController, PopperScreen screen, String suffix) {
    this.systemSummary = systemSummary;
    this.tablesSidebarController = tablesSidebarController;
    this.screen = screen;
    this.suffix = suffix;
  }

  @Override
  public void handle(DragEvent event) {
    GameRepresentation game = tablesSidebarController.getTablesController().getSelection();

    List<File> files = event.getDragboard().getFiles();
    List<File> filtered = files.stream().filter(f -> f.getName().toLowerCase().endsWith(suffix.toLowerCase())).collect(Collectors.toList());
    if (filtered.isEmpty()) {
      WidgetFactory.showAlert(Studio.stage, "Error", "None of the selected is valid for this upload.", "Files of the " + suffix + " are expected here.");
    }
    else {
      TableMediaUploadProgressModel model = new TableMediaUploadProgressModel(tablesSidebarController, game.getId(),
          "Popper Media Upload", filtered, "popperMedia", screen);
      Dialogs.createProgressDialog(model);
    }
  }
}
