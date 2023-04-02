package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.AltSound;
import de.mephisto.vpin.restclient.AltSoundEntry;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class AltSoundEditorController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundEditorController.class);

  private AltSound altSound;
  private boolean result = false;

  @FXML
  private TableView<AltSoundEntry> tableView;


  @FXML
  private TableColumn<AltSoundEntry, String> columnId;

  @FXML
  private TableColumn<AltSoundEntry, String> columnName;

  @FXML
  private TableColumn<AltSoundEntry, String> columnChannel;

  @FXML
  private TableColumn<AltSoundEntry, String> columnDuck;

  @FXML
  private TableColumn<AltSoundEntry, String> columnGain;

  @FXML
  private TableColumn<AltSoundEntry, String> columnLoop;

  @FXML
  private TableColumn<AltSoundEntry, String> columnStop;

  @FXML
  private TableColumn<AltSoundEntry, String> columnFilename;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onRestoreClick() {

  }

  @FXML
  private void onSaveClick() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    columnId.setCellValueFactory(cellData -> {
      AltSoundEntry value = cellData.getValue();
      return new SimpleStringProperty(value.getId());
    });
    columnName.setCellValueFactory(cellData -> {
      AltSoundEntry value = cellData.getValue();
      return new SimpleStringProperty(value.getName());
    });
    columnChannel.setCellValueFactory(cellData -> {
      AltSoundEntry value = cellData.getValue();
      return new SimpleStringProperty(String.valueOf(value.getChannel()));
    });
    columnDuck.setCellValueFactory(cellData -> {
      AltSoundEntry value = cellData.getValue();
      return new SimpleStringProperty(String.valueOf(value.getDuck()));
    });
    columnGain.setCellValueFactory(cellData -> {
      AltSoundEntry value = cellData.getValue();
      return new SimpleStringProperty(String.valueOf(value.getGain()));
    });
    columnLoop.setCellValueFactory(cellData -> {
      AltSoundEntry value = cellData.getValue();
      return new SimpleStringProperty(String.valueOf(value.getLoop()));
    });
    columnStop.setCellValueFactory(cellData -> {
      AltSoundEntry value = cellData.getValue();
      return new SimpleStringProperty(String.valueOf(value.getStop()));
    });
    columnFilename.setCellValueFactory(cellData -> {
      AltSoundEntry value = cellData.getValue();
      return new SimpleStringProperty(value.getFilename());
    });
  }

  public void setAltSound(AltSound altSound) {
    this.altSound = altSound;

    ObservableList<AltSoundEntry> entries = FXCollections.observableList(altSound.getEntries());
    tableView.setItems(entries);

  }

  @Override
  public void onDialogCancel() {
    result = false;
  }
}
