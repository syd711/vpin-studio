package de.mephisto.vpin.ui.tables.drophandler;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.tables.dialogs.TableMediaUploadProgressModel;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TableMediaFileDropEventHandler implements EventHandler<DragEvent> {

  private final PopperScreen screen;
  private final List<String> suffixes;
  private final TablesSidebarController tablesSidebarController;

  public TableMediaFileDropEventHandler(TablesSidebarController tablesSidebarController, PopperScreen screen, String... suffix) {
    this.tablesSidebarController = tablesSidebarController;
    this.screen = screen;
    this.suffixes = Arrays.asList(suffix);
  }

  @Override
  public void handle(DragEvent event) {
    List<File> files = event.getDragboard().getFiles();

    Platform.runLater(() -> {
      GameRepresentation game = tablesSidebarController.getTablesController().getSelection();

      List<File> filtered = files.stream().filter(f -> {
        String suffix = FilenameUtils.getExtension(f.getName());
        return suffixes.contains(suffix);
      }).collect(Collectors.toList());

      if (filtered.isEmpty()) {
        WidgetFactory.showAlert(Studio.stage, "Error", "None of the selected is valid for this upload.",
            "Only files with extension(s) \"" + String.join("\", \"", suffixes) + "\" are accepted here.");
      }
      else {
        TableMediaUploadProgressModel model = new TableMediaUploadProgressModel(game.getId(),
            "Popper Media Upload", filtered, "popperMedia", screen);
        Dialogs.createProgressDialog(model);
      }
    });
  }
}
