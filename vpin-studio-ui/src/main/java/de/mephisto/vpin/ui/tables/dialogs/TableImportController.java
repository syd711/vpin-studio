package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TableImportController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableImportController.class);

  @FXML
  private Label text1Description;
  @FXML
  private Label text2Description;
  @FXML
  private Label text3Description;
  @FXML
  private Label text4Description;

  @FXML
  private VBox tableBox;

  @FXML
  private Button saveBtn;

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
        JobExecutionResult jobResult = (JobExecutionResult) result;
        if (jobResult.isErrorneous()) {
          LOG.error("Table import failed: " + jobResult.getError());
          WidgetFactory.showAlert(Studio.stage, "Table Import Failed", "One ore more imports failed", jobResult.getMessage());
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
  }

  public void setEmulator(GameEmulatorRepresentation emulator) {
    try {
      Frontend frontend = client.getFrontendService().getFrontend();
      GameList importableTables = client.getFrontendService().getImportableTables(emulator.getId());
      saveBtn.setDisable(importableTables.getItems().isEmpty());

      if (importableTables.getItems().isEmpty()) {
        Label label = new Label("No tables found for [Frontend].");
        FrontendUtil.replaceName(label, frontend);
        label.setStyle("-fx-font-size: 14px;");
        tableBox.getChildren().add(label);
      }
      else {
        FrontendUtil.replaceNames(text1Description, frontend, emulator.getName());
        FrontendUtil.replaceNames(text2Description, frontend, emulator.getName());
        FrontendUtil.replaceNames(text3Description, frontend, emulator.getName());
        FrontendUtil.replaceNames(text4Description, frontend, emulator.getName());

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

  @Override
  public void onDialogCancel() {

  }
}
