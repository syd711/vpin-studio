package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.BackupSourceType;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.backups.BackupSourceRepresentation;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationProvider;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationSettings;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.backups.BackupDialogs;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class BackupRepositoriesPreferencesController implements Initializable {

  @FXML
  private TableView<BackupSourceRepresentation> tableView;

  @FXML
  private TableColumn<BackupSourceRepresentation, String> nameColumn;

  @FXML
  private TableColumn<BackupSourceRepresentation, String> urlColumn;

  @FXML
  private TableColumn<BackupSourceRepresentation, String> enabledColumn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button providerBtn;

  @FXML
  private ComboBox<AuthenticationProvider> providerCombo;

  @FXML
  private Button editBtn;

  @FXML
  private void onEdit() {
    BackupSourceRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      if (selectedItem.getId() < 0) {
        return;
      }

      BackupSourceRepresentation sourceRepresentation = null;
      BackupSourceType backupSourceType = BackupSourceType.valueOf(selectedItem.getType());
      switch (backupSourceType) {
        case Folder: {
          sourceRepresentation = BackupDialogs.openArchiveSourceFolderDialog(selectedItem);
          break;
        }
        default: {
          sourceRepresentation = BackupDialogs.openArchiveSourceHttpDialog(selectedItem);
          break;
        }
      }

      if (sourceRepresentation != null) {
        try {
          client.getArchiveService().saveArchiveSource(sourceRepresentation);
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Error saving repository: " + e.getMessage());
        }
        onReload();
      }
    }
  }

  @FXML
  private void onHttpAdd() {
    BackupSourceRepresentation sourceRepresentation = BackupDialogs.openArchiveSourceHttpDialog(null);
    if (sourceRepresentation != null) {
      try {
        client.getArchiveService().saveArchiveSource(sourceRepresentation);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Error saving repository: " + e.getMessage());
      }
      onReload();
    }
  }

  @FXML
  private void onFolderAdd() {
    BackupSourceRepresentation sourceRepresentation = BackupDialogs.openArchiveSourceFolderDialog(null);
    if (sourceRepresentation != null) {
      try {
        client.getArchiveService().saveArchiveSource(sourceRepresentation);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Error saving repository: " + e.getMessage());
      }
      onReload();
    }
  }

  @FXML
  private void onDelete() {
    BackupSourceRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Repository \"" + selectedItem.getName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.getArchiveService().deleteArchiveSource(selectedItem.getId());
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

  @FXML
  private void onProvider() {
    AuthenticationProvider value = providerCombo.getValue();
    if (value != null) {
      switch (value) {
        case VPF: {
          PreferencesController.open("vpf");
          break;
        }
        case VPU: {
          PreferencesController.open("vpu");
          break;
        }
      }
    }
  }

  private void onReload() {
    List<BackupSourceRepresentation> sources = client.getArchiveService().getArchiveSources();
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

    List<AuthenticationProvider> providers = new ArrayList<>(Arrays.asList(AuthenticationProvider.VPF, AuthenticationProvider.VPU));
    providers.add(0, null);
    providerCombo.setItems(FXCollections.observableList(providers));

    AuthenticationSettings authenticationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.AUTHENTICATION_SETTINGS, AuthenticationSettings.class);
    providerCombo.setValue(authenticationSettings.getAuthenticationProvider());

    providerCombo.valueProperty().addListener(new ChangeListener<AuthenticationProvider>() {
      @Override
      public void changed(ObservableValue<? extends AuthenticationProvider> observable, AuthenticationProvider oldValue, AuthenticationProvider newValue) {
        authenticationSettings.setAuthenticationProvider(newValue);
        client.getPreferenceService().setJsonPreference(authenticationSettings);
      }
    });

    nameColumn.setCellValueFactory(cellData -> {
      BackupSourceRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(value.getName());
    });

    urlColumn.setCellValueFactory(cellData -> {
      BackupSourceRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(value.getLocation());
    });

    enabledColumn.setCellValueFactory(cellData -> {
      BackupSourceRepresentation value = cellData.getValue();
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
      TableRow<BackupSourceRepresentation> row = new TableRow<>();
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
