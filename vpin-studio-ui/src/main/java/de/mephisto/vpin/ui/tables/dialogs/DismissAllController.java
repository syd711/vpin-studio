package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.util.DismissalUtil;
import de.mephisto.vpin.ui.util.LocalizedValidation;
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

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DismissAllController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private VBox dismissalList;

  private List<CheckBox> checkBoxes = new ArrayList<>();

  private GameRepresentation game;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    List<Integer> dismissals = new ArrayList<>();
    for (CheckBox checkBox : checkBoxes) {
      if(checkBox.isSelected()) {
        ValidationState validationState = (ValidationState) checkBox.getUserData();
        dismissals.add(validationState.getCode());
      }
    }
    DismissalUtil.dismissSelection(game, dismissals);
    EventManager.getInstance().notifyTableChange(game.getId(), game.getRom());
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  @Override
  public void onDialogCancel() {

  }

  public void setGame(GameRepresentation gameRepresentation) {
    this.game = gameRepresentation;

    List<ValidationState> validations = Studio.client.getGameService().getValidations(gameRepresentation.getId());
    for (ValidationState validation : validations) {
      LocalizedValidation validationResult = GameValidationTexts.getValidationResult(gameRepresentation, validation);
      CheckBox checkbox = new CheckBox(validationResult.getLabel());
      checkBoxes.add(checkbox);

      checkbox.getStyleClass().add("default-text");
      checkbox.setStyle("-fx-font-weight: bold;");
      checkbox.setUserData(validation);
      checkbox.setSelected(true);

      Label text = new Label(validationResult.getText());
      text.getStyleClass().add("default-text");
      text.setStyle("-fx-padding: 0 0 12 25;");

      dismissalList.getChildren().add(checkbox);
      dismissalList.getChildren().add(text);
    }

  }
}
