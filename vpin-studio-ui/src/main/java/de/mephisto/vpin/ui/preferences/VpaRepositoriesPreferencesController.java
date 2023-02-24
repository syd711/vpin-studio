package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.VpaSourceType;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.VpaSourceRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
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

public class VpaRepositoriesPreferencesController implements Initializable {
  public final static long DEFAULT_VPA_SOURCE_ID = -1;

  @FXML
  private TableView<VpaSourceRepresentation> tableView;

  @FXML
  private TableColumn<VpaSourceRepresentation, String> nameColumn;

  @FXML
  private TableColumn<VpaSourceRepresentation, String> urlColumn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button editBtn;

  @FXML
  private void onEdit() {
    VpaSourceRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      if(selectedItem.getId() == DEFAULT_VPA_SOURCE_ID) {
        return;
      }

      VpaSourceRepresentation sourceRepresentation = null;
      VpaSourceType vpaSourceType = VpaSourceType.valueOf(selectedItem.getType());
      switch (vpaSourceType) {
        case File: {
          sourceRepresentation = Dialogs.openVpaSourceFileDialog(selectedItem);
          break;
        }
        default: {
          sourceRepresentation = Dialogs.openVpaSourceHttpDialog(selectedItem);
          break;
        }
      }

      if (sourceRepresentation != null) {
        try {
          client.saveVpaSource(sourceRepresentation);
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Error saving repository: " + e.getMessage());
        }
        onReload();
      }
    }
  }

//  @FXML
//  private void onFolderAdd() {
//    VpaSourceRepresentation sourceRepresentation = Dialogs.openVpaSourceFileDialog(null);
//    if (sourceRepresentation != null) {
//      try {
//        client.saveVpaSource(sourceRepresentation);
//      } catch (Exception e) {
//        WidgetFactory.showAlert(Studio.stage, "Error", "Error saving repository: " + e.getMessage());
//      }
//      onReload();
//    }
//  }

  @FXML
  private void onHttpAdd() {
    VpaSourceRepresentation sourceRepresentation = Dialogs.openVpaSourceHttpDialog(null);
    if (sourceRepresentation != null) {
      try {
        client.saveVpaSource(sourceRepresentation);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Error saving repository: " + e.getMessage());
      }
      onReload();
    }
  }

  @FXML
  private void onDelete() {
    VpaSourceRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Repository \"" + selectedItem.getName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.deleteVpaSource(selectedItem.getId());
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Error deleting \"" + selectedItem.getName() + "\": " + e.getMessage());
        } finally {
          onReload();
        }
      }
    }
  }

  private void onReload() {
    tableView.setItems(FXCollections.observableList(client.getVpaSources()));
    EventManager.getInstance().notifyVpaSourceUpdate();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("              No table repository found.\nAdd a table repository to download tables from."));
    deleteBtn.setDisable(true);
    editBtn.setDisable(true);

    nameColumn.setCellValueFactory(cellData -> {
      VpaSourceRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(value.getName());
    });

    urlColumn.setCellValueFactory(cellData -> {
      VpaSourceRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(value.getLocation());
    });

    List<VpaSourceRepresentation> vpaSources = client.getVpaSources();
    tableView.setItems(FXCollections.observableList(vpaSources));
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean disable = newSelection == null || newSelection.getId() == -1;
      deleteBtn.setDisable(disable);
      editBtn.setDisable(disable);
    });

    tableView.setRowFactory(tv -> {
      TableRow<VpaSourceRepresentation> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          onEdit();
        }
      });
      return row;
    });
  }
}
