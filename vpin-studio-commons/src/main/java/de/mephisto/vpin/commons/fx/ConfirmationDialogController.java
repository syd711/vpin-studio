package de.mephisto.vpin.commons.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Optional;

public class ConfirmationDialogController implements DialogController {

  @FXML
  private Label textLabel;

  @FXML
  private Label helpLabel1;

  @FXML
  private Label helpLabel2;

  @FXML
  private Button altButton;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  private Stage stage;

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  public void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  @FXML
  private void onAltButton() {
    result = Optional.of(ButtonType.APPLY);
    stage.close();
  }

  public void initDialog(Stage stage, String text, String helpText1, String helpText2) {
    initDialog(stage, null, null, text, helpText1, helpText2);
  }

  public void initDialog(Stage stage, String altText, String okText, String text, String helpText1, String helpText2) {
    this.stage = stage;
    this.textLabel.setText(text);

    if(altText != null) {
      this.altButton.setText(altText);
    }

    if (helpText1 != null) {
      this.helpLabel1.setText(helpText1);
    }
    else {
      this.helpLabel1.setText("");
    }

    if (helpText2 != null) {
      this.helpLabel2.setText(helpText2);
    }
    else {
      this.helpLabel2.setText("");
    }

    if(okText != null) {
      okButton.setText(okText);
    }
  }

  public Optional<ButtonType> getResult() {
    return result;
  }

  public void hideCancel() {
    this.cancelButton.setVisible(false);
  }
}
