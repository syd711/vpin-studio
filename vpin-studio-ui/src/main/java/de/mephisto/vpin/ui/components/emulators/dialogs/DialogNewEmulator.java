package de.mephisto.vpin.ui.components.emulators.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DialogNewEmulator implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DialogNewEmulator.class);

  @FXML
  private Button installBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private VBox emulatorFieldsList;

  @FXML
  private ComboBox<EmulatorType> emulatorTypeComboBox;

  @FXML
  private TextField nameField;

  @FXML
  private TextField descriptionField;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onInstall(ActionEvent event) {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  @Override
  public void onDialogCancel() {

  }
}
