package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.ArchiveSourceType;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.archiving.ArchiveSourceRepresentation;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.archiving.ArchivingDialogs;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TableRepositoriesPreferencesController implements Initializable {
  public final static long DEFAULT_VPA_SOURCE_ID = -1;

  @FXML
  private TableView<ArchiveSourceRepresentation> tableView;

  @FXML
  private TableColumn<ArchiveSourceRepresentation, String> nameColumn;

  @FXML
  private TableColumn<ArchiveSourceRepresentation, String> urlColumn;

  @FXML
  private TableColumn<ArchiveSourceRepresentation, String> enabledColumn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button editBtn;

  @FXML
  private void onEdit() {
    ArchiveSourceRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      if (selectedItem.getId() < 0) {
        return;
      }

      ArchiveSourceRepresentation sourceRepresentation = null;
      ArchiveSourceType archiveSourceType = ArchiveSourceType.valueOf(selectedItem.getType());
      switch (archiveSourceType) {
        case Folder: {
          sourceRepresentation = ArchivingDialogs.openArchiveSourceFolderDialog(selectedItem);
          break;
        }
        default: {
          sourceRepresentation = ArchivingDialogs.openArchiveSourceHttpDialog(selectedItem);
          break;
        }
      }

      if (sourceRepresentation != null) {
        try {
          client.getArchiveService().saveArchiveSource(sourceRepresentation);
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Error saving repository: " + e.getMessage());
        }
        onReload();
      }
    }
  }

  @FXML
  private void onHttpAdd() {
    ArchiveSourceRepresentation sourceRepresentation = ArchivingDialogs.openArchiveSourceHttpDialog(null);
    if (sourceRepresentation != null) {
      try {
        client.getArchiveService().saveArchiveSource(sourceRepresentation);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Error saving repository: " + e.getMessage());
      }
      onReload();
    }
  }

  @FXML
  private void onFolderAdd() {
    ArchiveSourceRepresentation sourceRepresentation = ArchivingDialogs.openArchiveSourceFolderDialog(null);
    if (sourceRepresentation != null) {
      try {
        client.getArchiveService().saveArchiveSource(sourceRepresentation);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Error saving repository: " + e.getMessage());
      }
      onReload();
    }
  }

  @FXML
  private void onDelete() {
    ArchiveSourceRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Repository \"" + selectedItem.getName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.getArchiveService().deleteArchiveSource(selectedItem.getId());
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Error deleting \"" + selectedItem.getName() + "\": " + e.getMessage());
        } finally {
          onReload();
        }
      }
    }
  }

  private void onReload() {
    List<ArchiveSourceRepresentation> sources = client.getArchiveService().getArchiveSources();
    tableView.setItems(FXCollections.observableList(sources));
    tableView.refresh();
    EventManager.getInstance().notifyRepositoryUpdate();
    PreferencesController.markDirty(PreferenceType.backups);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("              No table repository found.\nAdd a table repository to download tables from."));
    deleteBtn.setDisable(true);
    editBtn.setDisable(true);

    nameColumn.setCellValueFactory(cellData -> {
      ArchiveSourceRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(value.getName());
    });

    urlColumn.setCellValueFactory(cellData -> {
      ArchiveSourceRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(value.getLocation());
    });

    enabledColumn.setCellValueFactory(cellData -> {
      ArchiveSourceRepresentation value = cellData.getValue();
      if (value.isEnabled()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
      }
      return new SimpleObjectProperty(WidgetFactory.createExclamationIcon());
    });

    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean disable = newSelection == null || newSelection.getId() < 0;
      deleteBtn.setDisable(disable);
      editBtn.setDisable(disable);
    });

    tableView.setRowFactory(tv -> {
      TableRow<ArchiveSourceRepresentation> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          onEdit();
        }
      });
      return row;
    });

    onReload();
  }
}
