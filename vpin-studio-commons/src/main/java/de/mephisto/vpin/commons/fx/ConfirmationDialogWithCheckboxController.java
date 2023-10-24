package de.mephisto.vpin.commons.fx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Optional;

public class ConfirmationDialogWithCheckboxController implements DialogController {

  @FXML
  private Label textLabel;

  @FXML
  private Label helpLabel1;

  @FXML
  private Label helpLabel2;

  @FXML
  private Button altButton;

  @FXML
  private CheckBox checkBox;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  private Stage stage;

  private boolean checked = false;

  private boolean checkMandatory = false;

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

  public void initDialog(Stage stage, String text, String helpText1, String helpText2, String checkboxText) {
    initDialog(stage, null, null, text, helpText1, helpText2, checkboxText);
  }

  public void initDialog(Stage stage, String altText, String okText, String text, String helpText1, String helpText2, String checkboxText) {
    this.stage = stage;
    this.textLabel.setText(text);
    this.checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      checked = newValue;
      okButton.setDisable(checkMandatory && !newValue);
    });

    if (altText != null) {
      this.altButton.setText(altText);
    }
    else {
      this.altButton.setText("Cancel");
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

    if (okText != null) {
      okButton.setText(okText);
    }

    this.checkBox.setText(checkboxText);
  }

  public ConfirmationResult getResult() {
    ConfirmationResult r = new ConfirmationResult();
    r.setChecked(checked);
    r.setApplied(result.isPresent() && result.get().equals(ButtonType.APPLY));
    return r;
  }

  public boolean isChecked() {
    return checked;
  }

  public void hideCancel() {
    this.cancelButton.setVisible(false);
  }

  public void setChecked(boolean b) {
    this.checkBox.setSelected(b);
  }

  public void setCheckboxMandatory() {
    this.checkMandatory = true;
    this.okButton.setDisable(true);
    this.checkBox.setSelected(false);
  }
}
