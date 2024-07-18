package de.mephisto.vpin.commons.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

public class InputDialogController implements DialogController {

  @FXML
  private Label textLabel;

  @FXML
  private Label descriptionLabel;

  @FXML
  private Label helpLabel;

  @FXML
  private Button cancelButton;

  @FXML
  private TextField textField;

  private String inputText;

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

  public void initDialog(Stage stage, String innerTitle, String description, String helpText, String defaultValue) {
    this.stage = stage;
    this.textLabel.setText(innerTitle);
    textField.requestFocus();

    if(description != null) {
      this.descriptionLabel.setText(description);
    }
    else {
      this.descriptionLabel.setText("");
    }

    if(helpText != null) {
      this.helpLabel.setText(helpText);
    }
    else {
      this.helpLabel.setText("");
    }

    if(defaultValue != null) {
      this.textField.setText(defaultValue);
      this.textField.selectAll();
      this.inputText = defaultValue;
    }

    textField.textProperty().addListener((observable, oldValue, newValue) -> inputText = newValue);
  }

  public Optional<ButtonType> getResult() {
    return result;
  }

  public String getText() {
    return inputText;
  }
}
