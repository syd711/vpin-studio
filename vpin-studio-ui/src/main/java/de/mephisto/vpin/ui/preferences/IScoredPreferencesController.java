package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tournaments.dialogs.IScoredGameRoomProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class IScoredPreferencesController implements Initializable {

  @FXML
  private TableView<IScoredGameRoomModel> tableView;

  @FXML
  private TableColumn<IScoredGameRoomModel, String> nameColumn;

  @FXML
  private TableColumn<IScoredGameRoomModel, String> enabledColumn;

  @FXML
  private TableColumn<IScoredGameRoomModel, String> syncColumn;

  @FXML
  private TableColumn<IScoredGameRoomModel, String> readApiColumn;

  @FXML
  private TableColumn<IScoredGameRoomModel, String> scoreApiColumn;

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
    IScoredGameRoomModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      TableDialogs.openIScoredGameRoomDialog(iScoredSettings, selectedItem.iScoredGameRoom);
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
    IScoredGameRoomModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete iScored game room \"" + selectedItem.iScoredGameRoom.getUrl() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          IScoredSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
          settings.remove(selectedItem.iScoredGameRoom);
          client.getPreferenceService().setJsonPreference(settings);
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Error deleting game room \"" + selectedItem.gameRoom.getUrl() + "\": " + e.getMessage());
        }
        finally {
          reload();
        }
      }
    }
  }

  private void reload() {
    client.getPreferenceService().clearCache(PreferenceNames.ISCORED_SETTINGS);
    iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    List<IScoredGameRoom> sets = iScoredSettings.getGameRooms();
    ProgressDialog.createProgressDialog(new IScoredGameRoomProgressModel(sets));

    List<IScoredGameRoomModel> models = sets.stream().map(gr -> new IScoredGameRoomModel(gr, IScored.getGameRoom(gr.getUrl()))).collect(Collectors.toList());
    tableView.setItems(FXCollections.observableList(models));
    tableView.refresh();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);

    tableView.setPlaceholder(new Label("              No game rooms found.\nCreate an iScored game room to submit highscores to."));
    deleteBtn.setDisable(true);
    editBtn.setDisable(true);

    nameColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomModel value = cellData.getValue();
      return new SimpleObjectProperty(value.iScoredGameRoom.getUrl());
    });

    enabledColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomModel value = cellData.getValue();
      if (value.iScoredGameRoom.isSynchronize()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
      }
      return new SimpleObjectProperty(WidgetFactory.createExclamationIcon());
    });

    readApiColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomModel value = cellData.getValue();
      if (value.gameRoom.getSettings().isApiReadingEnabled()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
      }
      return new SimpleObjectProperty(WidgetFactory.createExclamationIcon());
    });

    scoreApiColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomModel value = cellData.getValue();
      if (value.gameRoom.getSettings().isPublicScoreEnteringEnabled()) {
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
      TableRow<IScoredGameRoomModel> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          onEdit();
        }
      });
      return row;
    });

    reload();
  }

  class IScoredGameRoomModel {
    IScoredGameRoom iScoredGameRoom;
    GameRoom gameRoom;

    IScoredGameRoomModel(IScoredGameRoom iScoredGameRoom, GameRoom gameRoom) {
      this.iScoredGameRoom = iScoredGameRoom;
      this.gameRoom = gameRoom;
    }
  }
}
