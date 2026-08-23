package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.VPXZSettings;
import de.mephisto.vpin.restclient.vpxz.VPXZSourceRepresentation;
import de.mephisto.vpin.restclient.vpxz.VPXZSourceType;
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
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

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
  private TextField serverHost;

  @FXML
  private TextField serverPort;

  @FXML
  private Button testBtn;

  @FXML
  private void onTest() {
    testBtn.setDisable(true);
    JFXFuture.supplyAsync(() -> {
      return client.getVpxzService().ping();
    }).thenAcceptLater(ping -> {
      testBtn.setDisable(false);
      if (ping == null) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.ping_failed"), Messages.get("dialog.make_sure_that_the_ip_matches_with"), Messages.get("dialog.the_vpx_application_must_be_open_and"));
      }
      else {
        WidgetFactory.showInformation(Studio.stage, Messages.get("dialog.ping_successful"), Messages.get("dialog.version") + ping.getVersion());
      }
    }).onErrorLater(e -> {
        testBtn.setDisable(false);
        WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.ping_failed"), Messages.get("dialog.an_error_occurred_during_the_ping"), e.getMessage());
    });
  }

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
          WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.error_saving_vpxz_repository") + e.getMessage());
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
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.error_saving_vpxz_repository") + e.getMessage());
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
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.error_saving_vpxz_repository_2") + e.getMessage());
      }
      onReload();
    }
  }

  @FXML
  private void onDelete() {
    VPXZSourceRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.delete_repository") + selectedItem.getName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.getVpxzService().deleteVPXZSource(selectedItem.getId());
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.error_deleting") + selectedItem.getName() + Messages.get("dialog.item") + e.getMessage());
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
    tableView.setPlaceholder(new Label("              " + Messages.get("pref.vpxz_repositories.no_vpxz_repository_found")));
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

    serverHost.setText(vpxzSettings.getWebserverHost());
    serverHost.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        vpxzSettings.setWebserverHost(newValue.trim());
        client.getPreferenceService().setJsonPreference(vpxzSettings);
        updateTestBtn();
      }
    });

    serverPort.setText(String.valueOf(vpxzSettings.getWebserverPort()));
    serverPort.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        try {
          vpxzSettings.setWebserverPort(Integer.parseInt(newValue));
          client.getPreferenceService().setJsonPreference(vpxzSettings);
        }
        catch (NumberFormatException e) {
          //ignore
        }
        updateTestBtn();
      }
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
    updateTestBtn();
  }

  private void updateTestBtn() {
    this.testBtn.setDisable(StringUtils.isEmpty(serverHost.getText()) || StringUtils.isEmpty(serverPort.getText()));
  }
}
