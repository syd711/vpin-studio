package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DMDPositionController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionController.class);

  @FXML
  private CheckBox aspectRationCheckbox;

  @FXML
  private Spinner<Integer> xSpinner;
  @FXML
  private Spinner<Integer> ySpinner;
  @FXML
  private Spinner<Integer> widthSpinner;
  @FXML
  private Spinner<Integer> heightSpinner;

  @FXML
  private ImageView fullDMDImage;

  private GameRepresentation game;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {

  }

  @FXML
  private void onAutoPosition() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    SpinnerValueFactory.IntegerSpinnerValueFactory factoryX = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4000, 0);
    xSpinner.setValueFactory(factoryX);
    SpinnerValueFactory.IntegerSpinnerValueFactory factoryY = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4000, 0);
    ySpinner.setValueFactory(factoryY);
    SpinnerValueFactory.IntegerSpinnerValueFactory factoryWidth = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4000, 0);
    ySpinner.setValueFactory(factoryWidth);
    SpinnerValueFactory.IntegerSpinnerValueFactory factoryHeight = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4000, 0);
    ySpinner.setValueFactory(factoryHeight);
  }

  @Override
  public void onDialogCancel() {

  }

  public void setGame(GameRepresentation gameRepresentation) {
    this.game = gameRepresentation;

//    DMDInfo dmdInfo = client.getDmdPositionService().getDMDInfo(this.game.getId());
    Image image = new Image("http://localhost:8089/api/v1/dmdposition/background/-1"); //TODO
    fullDMDImage.setImage(image);
    fullDMDImage.setPreserveRatio(true);
  }
}
