package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TableImportController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableImportController.class);

  @FXML
  private Label text2Description;

  @FXML
  private VBox tableBox;

  @FXML
  private Button saveBtn;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  private final List<CheckBox> checkBoxes = new ArrayList<>();

  private Stage stage;

  @FXML
  private void onSaveClick(ActionEvent e) {
    GameList importList = new GameList();
    for (CheckBox checkBox : checkBoxes) {
      if (checkBox.isSelected()) {
        GameListItem item = (GameListItem) checkBox.getUserData();
        importList.getItems().add(item);
      }
    }

    if (importList.getItems().isEmpty()) {
      return;
    }

    Platform.runLater(() -> {
      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(Studio.stage, new TableImportProgressModel(importList));
      List<Object> results = progressDialog.getResults();

      for (Object result : results) {
        JobDescriptor jobResult = (JobDescriptor) result;
        if (jobResult.getError() != null) {
          LOG.error("Table import failed: " + jobResult.getError());
          WidgetFactory.showAlert(Studio.stage, "Table Import Failed", "One ore more imports failed", jobResult.getError());
          break;
        }
      }
      EventManager.getInstance().notifyJobFinished(JobType.TABLE_IMPORT, 0, false, true);
    });

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }


  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

      Frontend frontend = client.getFrontendService().getFrontendCached();
      // should always be done
      FrontendUtil.replaceNames(text2Description, frontend, frontend.getName());

      List<GameEmulatorRepresentation> filtered = new ArrayList<>(client.getEmulatorService().getFilteredEmulatorsWithAllVpx(uiSettings));
      this.emulatorCombo.setItems(FXCollections.observableList(filtered));

      this.emulatorCombo.valueProperty().addListener(new ChangeListener<GameEmulatorRepresentation>() {
        @Override
        public void changed(ObservableValue<? extends GameEmulatorRepresentation> observable, GameEmulatorRepresentation oldValue, GameEmulatorRepresentation newValue) {
          if (newValue != null) {
            refreshEmulator(newValue);
          }
          else {
            saveBtn.setDisable(true);
          }
        }
      });
    }
    catch (Exception e) {
      LOG.error("Failed to init import dialog: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to read import list: " + e.getMessage());
    }
  }

  private void refreshEmulator(GameEmulatorRepresentation emulator) {
    saveBtn.setDisable(true);
    emulatorCombo.setDisable(true);

    checkBoxes.clear();
    tableBox.getChildren().removeAll(tableBox.getChildren());

    Label loading = new Label("loading...");
    loading.setStyle("-fx-font-size: 14px;");
    tableBox.getChildren().add(loading);

    JFXFuture.supplyAsync(() -> {
          if (client.getEmulatorService().isAllVpx(emulator)) {
            return client.getFrontendService().getImportableTablesVpx();
          }
          else {
            return client.getFrontendService().getImportableTables(emulator.getId());
          }
        })
        .onErrorSupply(e -> {
          LOG.error("Failed to init import dialog: " + e.getMessage(), e);
          Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, "Error", "Failed to read import list: " + e.getMessage()));
          return new GameList();
        })
        .thenAcceptLater(importableTables -> {
          tableBox.getChildren().remove(loading);

          if (importableTables.getItems().isEmpty()) {
            Label label = new Label("No tables found for \"" + emulator.getName() + "\" that have not been imported yet.");
            label.setStyle("-fx-font-size: 14px;");
            tableBox.getChildren().add(label);
          }
          else {
            CheckBox selectCheckbox = new CheckBox("Select All");
            selectCheckbox.setStyle("-fx-font-size: 14px;-fx-font-weight: bold;");
            selectCheckbox.setPrefHeight(50);
            selectCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
              for (CheckBox checkBox : checkBoxes) {
                checkBox.setSelected(newValue);
              }
            });
            tableBox.getChildren().add(selectCheckbox);

            for (GameListItem item : importableTables.getItems()) {
              HBox row = new HBox();

              CheckBox checkBox = new CheckBox(item.getName() + " (" + FileUtils.readableFileSize(item.getFileSize()) + ")");
              checkBox.setPrefWidth(520);
              checkBox.setUserData(item);
              checkBox.setStyle("-fx-font-size: 14px;");
              checkBox.setPrefHeight(30);
              row.getChildren().add(checkBox);

              if (emulator.isVpxEmulator() || emulator.isFpEmulator()) {
                Button deleteBtn = new Button();
                deleteBtn.setGraphic(WidgetFactory.createIcon("mdi2d-delete-outline"));
                row.getChildren().add(deleteBtn);

                deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
                  @Override
                  public void handle(ActionEvent event) {
                    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete file \"" + item.getName() + "\"?");
                    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                      client.getGameService().deleteGameFile(item.getEmuId(), item.getName());
                      refreshEmulator(emulator);
                    }
                  }
                });

              }
              tableBox.getChildren().add(row);
              checkBoxes.add(checkBox);
            }
          }
          saveBtn.setDisable(importableTables.getItems().isEmpty());
          emulatorCombo.setDisable(false);
        });
  }

  public void setData(Stage stage, GameEmulatorRepresentation emulatorRepresentation) {
    this.stage = stage;
    this.emulatorCombo.setValue(emulatorRepresentation);
  }


  @Override
  public void onDialogCancel() {

  }
}
