package de.mephisto.vpin.ui.components.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DialogExclusionList implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DialogExclusionList.class);

  @FXML
  private Button installBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private VBox exclusionList;

  private File selection;

  private boolean result = false;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onInstall(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }
}
