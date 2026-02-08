package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.VPXZSettings;
import de.mephisto.vpin.restclient.vpxz.VPXZSourceRepresentation;
import de.mephisto.vpin.restclient.vpxz.VPXZSourceType;
import de.mephisto.vpin.restclient.vpxz.VPXZType;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.vpxz.VPXZDialogs;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class VPXZRepositoriesPreferencesController implements Initializable {

  @FXML
  private TableView<VPXZSourceRepresentation> tableView;

  @FXML
  private TableColumn<VPXZSourceRepresentation, String> nameColumn;

  @FXML
  private TableColumn<VPXZSourceRepresentation, String> urlColumn;

  @FXML
  private TableColumn<VPXZSourceRepresentation, String> enabledColumn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button editBtn;

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private void onEdit() {
    VPXZSourceRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      VPXZSourceRepresentation sourceRepresentation = null;
      VPXZSourceType sourceType = VPXZSourceType.valueOf(selectedItem.getType());
      switch (sourceType) {
        case Folder: {
          sourceRepresentation = VPXZDialogs.openVpxzSourceFolderDialog(selectedItem);
          break;
        }
        default: {
//          sourceRepresentation = VPXZDialogs.openArchiveSourceHttpDialog(selectedItem);
          break;
        }
      }

      if (sourceRepresentation != null) {
        try {
          client.getVpxzService().saveVPXZSource(sourceRepresentation);
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Error saving vpxz repository: " + e.getMessage());
        }
        onReload();
      }
    }
  }

  @FXML
  private void onHttpAdd() {
    VPXZSourceRepresentation sourceRepresentation = null; //BackupDialogs.openArchiveSourceHttpDialog(null);
    if (sourceRepresentation != null) {
      try {
        client.getVpxzService().saveVPXZSource(sourceRepresentation);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Error saving vpxz repository: " + e.getMessage());
      }
      onReload();
    }
  }

  @FXML
  private void onFolderAdd() {
    VPXZSourceRepresentation sourceRepresentation = VPXZDialogs.openVpxzSourceFolderDialog(null);
    if (sourceRepresentation != null) {
      try {
        client.getVpxzService().saveVPXZSource(sourceRepresentation);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Error saving VPXZ repository: " + e.getMessage());
      }
      onReload();
    }
  }

  @FXML
  private void onDelete() {
    VPXZSourceRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Repository \"" + selectedItem.getName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.getVpxzService().deleteVPXZSource(selectedItem.getId());
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
    List<VPXZSourceRepresentation> sources = client.getVpxzService().getVPXZSources();
    tableView.setItems(FXCollections.observableList(sources));
    tableView.refresh();
    EventManager.getInstance().notifyRepositoryUpdate();
    PreferencesController.markDirty(PreferenceType.vpxz);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("              No VPXZ repository found.\nAdd a VPXZ repository to manage .vpxz files."));
    deleteBtn.setDisable(true);
    editBtn.setDisable(true);

    VPXZSettings vpxzSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VPXZ_SETTINGS, VPXZSettings.class);

    enabledCheckbox.selectedProperty().setValue(vpxzSettings.isEnabled());
    enabledCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        vpxzSettings.setEnabled(newValue);
        client.getPreferenceService().setJsonPreference(vpxzSettings);
      }
    });

    nameColumn.setCellValueFactory(cellData -> {
      VPXZSourceRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(value.getName());
    });

    urlColumn.setCellValueFactory(cellData -> {
      VPXZSourceRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(value.getLocation());
    });

    enabledColumn.setCellValueFactory(cellData -> {
      VPXZSourceRepresentation value = cellData.getValue();
      if (value.isEnabled()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
      }
      return new SimpleObjectProperty(WidgetFactory.createExclamationIcon());
    });

    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean disable = newSelection == null;
      deleteBtn.setDisable(disable || tableView.getItems().size() == 1);
//      deleteBtn.setDisable(false);
      editBtn.setDisable(disable);
    });

    tableView.setRowFactory(tv -> {
      TableRow<VPXZSourceRepresentation> row = new TableRow<>();
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
