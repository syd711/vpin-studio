package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.logging.HighscoreEventLog;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class LoadingAsset2ScreenAssignmentController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private RadioButton screen0;

  @FXML
  private RadioButton screen1;

  @FXML
  private RadioButton screen2;

  @FXML
  private RadioButton screen3;

  @FXML
  private RadioButton screen4;

  private VPinScreen selection;

  @FXML
  private void onCancelClick(ActionEvent e) {
    selection = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }


  @FXML
  private void onApply(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    if (screen0.isSelected()) {
      selection = VPinScreen.Topper;
    }
    else if (screen1.isSelected()) {
      selection = VPinScreen.DMD;
    }
    else if (screen2.isSelected()) {
      selection = VPinScreen.BackGlass;
    }
    else if (screen3.isSelected()) {
      selection = VPinScreen.PlayField;
    }
    else if (screen4.isSelected()) {
      selection = VPinScreen.Menu;
    }
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  @Override
  public void onDialogCancel() {
    selection = null;
  }

  public VPinScreen getScreen() {
    return selection;
  }
}
