package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetSourceType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
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

public class TableAssetSourcesPreferencesController implements Initializable {

  @FXML
  private TableView<TableAssetSource> tableView;

  @FXML
  private TableColumn<TableAssetSource, String> nameColumn;

  @FXML
  private TableColumn<TableAssetSource, String> urlColumn;

  @FXML
  private TableColumn<TableAssetSource, String> enabledColumn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button editBtn;

  @FXML
  private void onEdit() {
    TableAssetSource selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      TableAssetSource sourceRepresentation = null;
      TableAssetSourceType backupSourceType = selectedItem.getType();
      switch (backupSourceType) {
        case FileSystem: {
          sourceRepresentation = PreferencesDialogs.openMediaSourceFolderDialog(selectedItem);
          break;
        }
      }

      if (sourceRepresentation != null) {
        try {
          client.getMediaSourcesService().saveAssetSource(sourceRepresentation);
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Error saving media source: " + e.getMessage());
        }
        onReload();
      }
    }
  }

  @FXML
  private void onSourceAdd() {
    TableAssetSource sourceRepresentation = PreferencesDialogs.openMediaSourceFolderDialog(new TableAssetSource());
    if (sourceRepresentation != null) {
      try {
        client.getMediaSourcesService().saveAssetSource(sourceRepresentation);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Error saving media source: " + e.getMessage());
      }
      onReload();
    }
  }

  @FXML
  private void onDelete() {
    TableAssetSource selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Media Source \"" + selectedItem.getName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.getMediaSourcesService().deleteAssetSource(selectedItem.getId());
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Error deleting \"" + selectedItem.getName() + "\": " + e.getMessage());
        }
        finally {
          onReload();
        }
      }
    }
  }

  private void onReload() {
    List<TableAssetSource> sources = client.getMediaSourcesService().getAssetSources();
    tableView.setItems(FXCollections.observableList(sources));
    tableView.refresh();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("              No table repository found.\nAdd a table repository to download tables from."));
    deleteBtn.setDisable(true);
    editBtn.setDisable(true);

    nameColumn.setCellValueFactory(cellData -> {
      TableAssetSource value = cellData.getValue();
      return new SimpleObjectProperty(value.getName());
    });

    urlColumn.setCellValueFactory(cellData -> {
      TableAssetSource value = cellData.getValue();
      return new SimpleObjectProperty(value.getLocation());
    });

    enabledColumn.setCellValueFactory(cellData -> {
      TableAssetSource value = cellData.getValue();
      if (value.isEnabled()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
      }
      return new SimpleObjectProperty(WidgetFactory.createExclamationIcon());
    });

    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean disable = newSelection == null;
      deleteBtn.setDisable(disable);
      editBtn.setDisable(disable);
    });

    tableView.setRowFactory(tv -> {
      TableRow<TableAssetSource> row = new TableRow<>();
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
