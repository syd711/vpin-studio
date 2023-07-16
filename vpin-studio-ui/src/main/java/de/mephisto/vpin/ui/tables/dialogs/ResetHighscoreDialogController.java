package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.descriptors.ResetHighscoreDescriptor;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ResetHighscoreDialogController implements DialogController {

  @FXML
  private Label textLabel;

  @FXML
  private Label descriptionLabel;

  @FXML
  private Button okButton;

  @FXML
  private TextField textField;

  private ResetHighscoreDescriptor result;
  private GameRepresentation game;

  @Override
  public void onDialogCancel() {
    result = null;
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    result = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDialogSubmit(ActionEvent e) {
    result = new ResetHighscoreDescriptor();
    result.setGameId(game.getId());

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    okButton.setDisable(true);

    this.textLabel.setText("Reset the highscore of \"" + game.getGameDisplayName() + "\"?");
    this.descriptionLabel.setText("Enter the ROM name (\"" + game.getEffectiveRom() + "\") to confirm the reset:");
    textField.requestFocus();
    textField.textProperty().addListener((observable, oldValue, newValue) -> {
      String romName = game.getEffectiveRom();
      boolean match = String.valueOf(newValue).trim().equals(romName);
      okButton.setDisable(!match);
    });
  }

  public ResetHighscoreDescriptor getDescriptor() {
    return result;
  }
}
