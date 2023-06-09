package de.mephisto.vpin.commons.fx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class OutputDialogController implements DialogController {

  @FXML
  private Label textLabel;

  @FXML
  private Label descriptionLabel;

  @FXML
  private TextArea textArea;

  private String textValue;

  private Stage stage;

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  public void onDialogSubmit() {
    stage.close();
  }

  public void initDialog(Stage stage, String innerTitle, String description, String text) {
    this.stage = stage;
    this.textLabel.setText(innerTitle);
    textArea.requestFocus();

    if(description != null) {
      this.descriptionLabel.setText(description);
    }
    else {
      this.descriptionLabel.setText("");
    }

    if(text != null) {
      this.textArea.setText(text);
      this.textValue = text;
    }

    textArea.textProperty().addListener((observable, oldValue, newValue) -> textValue = newValue);
  }

  public String getText() {
    return textValue;
  }
}
