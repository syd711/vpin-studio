package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
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
  private VBox tableBox;

  @FXML
  private Button saveBtn;

  private final List<CheckBox> checkBoxes = new ArrayList<>();

  @FXML
  private void onSaveClick(ActionEvent e) {
    GameList importList = new GameList();
    for (CheckBox checkBox : checkBoxes) {
      if (checkBox.isSelected()) {
        GameListItem item = new GameListItem();
        item.setName(checkBox.getText());
        item.setEmuId((Integer) checkBox.getUserData());
        importList.getItems().add(item);
      }
    }

    if (importList.getItems().isEmpty()) {
      return;
    }

    this.onCancelClick(e);
    try {
      JobExecutionResult jobExecutionResult = client.getPinUPPopperService().importTables(importList);
      if (!jobExecutionResult.isErrorneous()) {
        EventManager.getInstance().notifyJobFinished(JobType.TABLE_IMPORT);
      }
    } catch (Exception ex) {
      LOG.error("Table import failed: " + ex.getMessage(), ex);
      WidgetFactory.showAlert(Studio.stage, "Table Import Failed", ex.getMessage());
    }
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    GameList importableTables = client.getPinUPPopperService().getImportableTables();
    saveBtn.setDisable(importableTables.getItems().isEmpty());

    if (importableTables.getItems().isEmpty()) {
      Label label = new Label("No tables found.");
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
        checkBox.setUserData(item.getEmuId());
        checkBox.setStyle("-fx-font-size: 14px;");
        checkBox.setPrefHeight(30);
        tableBox.getChildren().add(checkBox);
        checkBoxes.add(checkBox);
      }
    }

  }

  @Override
  public void onDialogCancel() {

  }
}
