package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.ManiaAccountRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManiaAccountDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaAccountDialogController.class);

  @FXML
  private Button okBtn;

  @FXML
  private TextField initialsText;

  @FXML
  private TextField displayNameText;
  private ManiaAccountRepresentation account;

  @FXML
  private void onCreate(ActionEvent e) {
    if (this.account == null) {
      this.account = new ManiaAccountRepresentation();
    }
    account.setDisplayName(displayNameText.getText());
    account.setInitials(initialsText.getText());

    try {
      Studio.client.getManiaService().saveAccount(this.account);
    } catch (Exception ex) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save account: " + ex.getMessage());
    } finally {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      stage.close();
    }
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.okBtn.setDisable(true);
    initialsText.textProperty().addListener((observable, oldValue, newValue) -> validateInputs());
    displayNameText.textProperty().addListener((observable, oldValue, newValue) -> validateInputs());
  }

  private void validateInputs() {
    this.okBtn.setDisable(true);
    if (StringUtils.isEmpty(displayNameText.getText())) {
      return;
    }

    if (StringUtils.isEmpty(initialsText.getText())) {
      return;
    }
    if (initialsText.getText().length() > 3) {
      initialsText.setText(initialsText.getText().substring(0, 3));
    }

    //max length of 25 because of iscored.info
    if (displayNameText.getText().length() > 25) {
      displayNameText.setText(displayNameText.getText().substring(0, 25));
    }

    if (initialsText.getText().length() != 3) {
      return;
    }

    String regex = "^[a-zA-Z0-9]+$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(initialsText.getText());
    if (!matcher.matches()) {
      return;
    }

    this.okBtn.setDisable(false);
  }

  public void setAccount(ManiaAccountRepresentation accountRepresentation) {
    this.account = accountRepresentation;
    if (accountRepresentation != null) {
      okBtn.setText("Save");
      displayNameText.setText(accountRepresentation.getDisplayName());
      initialsText.setText(accountRepresentation.getInitials());
    }
  }

  @Override
  public void onDialogCancel() {
  }
}
