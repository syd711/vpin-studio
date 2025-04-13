package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableDialogs;
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

public class IScoredPreferencesController implements Initializable {

  @FXML
  private TableView<IScoredGameRoom> tableView;

  @FXML
  private TableColumn<IScoredGameRoom, String> nameColumn;

  @FXML
  private TableColumn<IScoredGameRoom, String> enabledColumn;

  @FXML
  private TableColumn<IScoredGameRoom, String> syncColumn;

  @FXML
  private TableColumn<IScoredGameRoom, String> readApiColumn;

  @FXML
  private TableColumn<IScoredGameRoom, String> scoreApiColumn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button editBtn;


  private IScoredSettings iScoredSettings;

  @FXML
  private void onReload() {
    reload();
  }

  @FXML
  private void onEdit() {
    IScoredGameRoom selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      TableDialogs.openIScoredGameRoomDialog(iScoredSettings, selectedItem);
      reload();
    }
  }

  @FXML
  private void onAdd() {
    TableDialogs.openIScoredGameRoomDialog(iScoredSettings, null);
    reload();
  }

  @FXML
  private void onDelete() {
    IScoredGameRoom selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete iScored game room \"" + selectedItem.getUrl() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          IScoredSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
          settings.remove(selectedItem);
          client.getPreferenceService().setJsonPreference(settings);
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Error deleting game room \"" + selectedItem.getUrl() + "\": " + e.getMessage());
        } finally {
          reload();
        }
      }
    }
  }

  private void reload() {
    client.getPreferenceService().clearCache(PreferenceNames.ISCORED_SETTINGS);
    iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    List<IScoredGameRoom> sets = iScoredSettings.getGameRooms();
    tableView.setItems(FXCollections.observableList(sets));
    tableView.refresh();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);

    tableView.setPlaceholder(new Label("              No game rooms found.\nCreate an iScored game room to submit highscores to."));
    deleteBtn.setDisable(true);
    editBtn.setDisable(true);

    nameColumn.setCellValueFactory(cellData -> {
      IScoredGameRoom value = cellData.getValue();
      return new SimpleObjectProperty(value.getUrl());
    });

    enabledColumn.setCellValueFactory(cellData -> {
      IScoredGameRoom value = cellData.getValue();
      if (value.isSynchronize()) {
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
      TableRow<IScoredGameRoom> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          onEdit();
        }
      });
      return row;
    });

    reload();
  }
}
