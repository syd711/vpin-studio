package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.preferences.dialogs.IScoredGameRoomLoadingProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

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
  private TableColumn<IScoredGameRoomModel, String> syncColumn;

  @FXML
  private TableColumn<IScoredGameRoomModel, String> readApiColumn;

  @FXML
  private TableColumn<IScoredGameRoomModel, String> scoreApiColumn;

  @FXML
  private Button deleteBtn;

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private Button editBtn;

  @FXML
  private Pane enabledBox;

  private IScoredSettings iScoredSettings;

  @FXML
  private void onReload() {
    reload(true);
  }

  @FXML
  private void onEdit() {
    IScoredGameRoomModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      TableDialogs.openIScoredGameRoomDialog(iScoredSettings, selectedItem.iScoredGameRoom);
      PreferencesController.markDirty(PreferenceType.competitionSettings);
      reload(false);
    }
  }

  @FXML
  private void onAdd() {
    TableDialogs.openIScoredGameRoomDialog(iScoredSettings, null);
    PreferencesController.markDirty(PreferenceType.competitionSettings);
    reload(false);
  }

  @FXML
  private void onDelete() {
    IScoredGameRoomModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      PreferencesController.markDirty(PreferenceType.competitionSettings);
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete iScored game room \"" + selectedItem.iScoredGameRoom.getUrl() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        IScoredSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
        settings.remove(selectedItem.iScoredGameRoom);
        Platform.runLater(() -> {
          ProgressDialog.createProgressDialog(new PreferencesSavingModel("Synchronizing iScored", settings));
          reload(true);
          EventManager.getInstance().notifyPreferenceChanged(PreferenceType.competitionSettings);
        });

      }
    }
  }

  private void reload(boolean forceReload) {
    client.getPreferenceService().clearCache(PreferenceNames.ISCORED_SETTINGS);
    iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    List<IScoredGameRoom> sets = iScoredSettings.getGameRooms();
    ProgressDialog.createProgressDialog(new IScoredGameRoomLoadingProgressModel(sets, forceReload));

    List<IScoredGameRoomModel> models = sets.stream().map(gr -> new IScoredGameRoomModel(gr, IScored.getGameRoom(gr.getUrl(), false))).collect(Collectors.toList());
    tableView.setItems(FXCollections.observableList(models));
    tableView.refresh();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);

    enabledBox.managedProperty().bindBidirectional(enabledBox.visibleProperty());
    enabledBox.setVisible(iScoredSettings.isEnabled());

    enabledCheckbox.setSelected(iScoredSettings.isEnabled());
    enabledCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        iScoredSettings.setEnabled(newValue);

        Platform.runLater(() -> {
          ProgressDialog.createProgressDialog(new PreferencesSavingModel("Synchronizing iScored", iScoredSettings));

          PreferencesController.markDirty(PreferenceType.competitionSettings);
          enabledBox.setVisible(iScoredSettings.isEnabled());
          if (iScoredSettings.isEnabled()) {
            reload(false);
          }
        });
      }
    });

    tableView.setPlaceholder(new Label("              No game rooms found.\nCreate an iScored game room to submit highscores to."));
    deleteBtn.setDisable(true);
    editBtn.setDisable(true);

    nameColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomModel value = cellData.getValue();
      return new SimpleObjectProperty(value.iScoredGameRoom.getUrl());
    });

    syncColumn.setCellValueFactory(cellData -> {
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

    if (iScoredSettings.isEnabled()) {
      reload(false);
    }
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
