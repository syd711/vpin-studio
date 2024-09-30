package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
      EventManager.getInstance().notifyJobFinished(JobType.TABLE_IMPORT);
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
      GameEmulatorRepresentation selectedEmu = this.emulatorCombo.getSelectionModel().getSelectedItem();
      this.emulatorCombo.valueProperty().addListener(new ChangeListener<GameEmulatorRepresentation>() {
        @Override
        public void changed(ObservableValue<? extends GameEmulatorRepresentation> observable, GameEmulatorRepresentation oldValue, GameEmulatorRepresentation newValue) {
          if (newValue != null) {
            refreshEmulator(newValue);
          }
          saveBtn.setDisable(newValue == null);
        }
      });

      List<GameEmulatorRepresentation> emulators = new ArrayList<>(client.getFrontendService().getGameEmulatorsUncached());
      List<GameEmulatorRepresentation> filtered = emulators.stream().filter(e -> !uiSettings.getIgnoredEmulatorIds().contains(Integer.valueOf(e.getId()))).collect(Collectors.toList());
      this.emulatorCombo.setItems(FXCollections.observableList(filtered));

      if (selectedEmu == null && !filtered.isEmpty()) {
        this.emulatorCombo.getSelectionModel().selectFirst();
      }
    }
    catch (Exception e) {
      LOG.error("Failed to init import dialog: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to read import list: " + e.getMessage());
    }
  }

  private void refreshEmulator(GameEmulatorRepresentation emulator) {
    try {
      GameList importableTables = client.getFrontendService().getImportableTables(emulator.getId());
      saveBtn.setDisable(importableTables.getItems().isEmpty());
      checkBoxes.clear();
      tableBox.getChildren().removeAll(tableBox.getChildren());

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
          CheckBox checkBox = new CheckBox(item.getName());
          checkBox.setUserData(item);
          checkBox.setStyle("-fx-font-size: 14px;");
          checkBox.setPrefHeight(30);
          tableBox.getChildren().add(checkBox);
          checkBoxes.add(checkBox);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to init import dialog: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to read import list: " + e.getMessage());
    }
  }

  public void setData(GameEmulatorRepresentation emulatorRepresentation) {
    this.emulatorCombo.setValue(emulatorRepresentation);
  }


  @Override
  public void onDialogCancel() {

  }
}
