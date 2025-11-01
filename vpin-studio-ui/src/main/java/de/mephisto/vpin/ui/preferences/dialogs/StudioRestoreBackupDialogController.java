package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.backups.StudioBackupDescriptor;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class StudioRestoreBackupDialogController implements Initializable, DialogController, ChangeListener<Boolean> {
  private final static Logger LOG = LoggerFactory.getLogger(StudioRestoreBackupDialogController.class);

  @FXML
  private TextField fileNameField;

  @FXML
  private Button cancelBtn;

  @FXML
  private Button createBtn;

  @FXML
  private CheckBox preferencesCheckbox;

  @FXML
  private CheckBox playersCheckbox;

  @FXML
  private CheckBox vpsNotesCheckbox;

  @FXML
  private CheckBox gameNotesCheckbox;

  @FXML
  private CheckBox gamesCheckbox;

  @FXML
  private CheckBox gamesVpsMappingCheckbox;

  @FXML
  private CheckBox gamesCardSettingsCheckbox;

  @FXML
  private CheckBox gamesVersionCheckbox;

  private Stage stage;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onCreateClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    StudioBackupDescriptor descriptor = new StudioBackupDescriptor();
    descriptor.setGameComments(gameNotesCheckbox.isSelected());
    descriptor.setPreferences(preferencesCheckbox.isSelected());
    descriptor.setVpsComments(vpsNotesCheckbox.isSelected());
    descriptor.setPlayers(playersCheckbox.isSelected());

    descriptor.setGames(gamesCheckbox.isSelected());
    descriptor.setGameCardSettings(gamesCardSettingsCheckbox.isSelected());
    descriptor.setGameVpsMapping(gamesVpsMappingCheckbox.isSelected());
    descriptor.setGameComments(gameNotesCheckbox.isSelected());
    descriptor.setGameVersion(gamesVersionCheckbox.isSelected());

    File file = new File(fileNameField.getText());

    Platform.runLater(() -> {
      stage.close();
    });

    ProgressDialog.createProgressDialog(new StudioBackupRestoreProgressModel(file, descriptor));
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    this.createBtn.setDisable(true);

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select VPin Studio Backup File");

    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("VPin Studio Backup", "*.json"));
    File selection = fileChooser.showOpenDialog(stage);
    if (selection != null) {
      this.fileNameField.setText(selection.getAbsolutePath());
    }
    refresh();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    vpsNotesCheckbox.selectedProperty().addListener(this);
    gamesCheckbox.selectedProperty().addListener(this);
    preferencesCheckbox.selectedProperty().addListener(this);
    playersCheckbox.selectedProperty().addListener(this);

    refresh();
  }

  @Override
  public void onDialogCancel() {
  }

  @Override
  public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
    refresh();
  }

  private void refresh() {
    boolean enabled = vpsNotesCheckbox.isSelected() ||
        gamesCheckbox.isSelected() ||
        preferencesCheckbox.isSelected() ||
        playersCheckbox.isSelected();

    createBtn.setDisable(!enabled || StringUtils.isEmpty(fileNameField.getText()));

    gamesVersionCheckbox.setDisable(!gamesCheckbox.isSelected());
    gameNotesCheckbox.setDisable(!gamesCheckbox.isSelected());
    gamesVpsMappingCheckbox.setDisable(!gamesCheckbox.isSelected());
    gamesCardSettingsCheckbox.setDisable(!gamesCheckbox.isSelected());
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }
}
