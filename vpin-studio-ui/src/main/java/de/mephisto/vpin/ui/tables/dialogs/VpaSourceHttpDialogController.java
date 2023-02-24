package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.VpaSourceType;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.representations.VpaSourceRepresentation;
import de.mephisto.vpin.restclient.util.PasswordUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class VpaSourceHttpDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(VpaSourceHttpDialogController.class);
  private static File lastFolderSelection;

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private TextField urlField;

  @FXML
  private TextField loginField;

  @FXML
  private TextField passwordField;

  private VpaSourceRepresentation source;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.source = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    this.source.setType(VpaSourceType.Http.name());
    this.source.setName(nameField.getText());
    this.source.setLocation(urlField.getText());
    this.source.setLogin(loginField.getText());
    this.source.setAuthenticationType("basic");
    this.source.setPassword(PasswordUtil.encrypt(passwordField.getText()));

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    source = new VpaSourceRepresentation();

    nameField.textProperty().addListener((observableValue, s, t1) -> {
      source.setName(t1);
      validateInput();
    });
    urlField.textProperty().addListener((observableValue, s, t1) -> {
      source.setName(t1);
      validateInput();
    });
    this.validateInput();
    this.nameField.requestFocus();
  }

  private void validateInput() {
    String name = nameField.getText();
    String url = urlField.getText();
    saveBtn.setDisable(StringUtils.isEmpty(name) || StringUtils.isEmpty(url));
  }

  @Override
  public void onDialogCancel() {
    this.source = null;
  }

  public VpaSourceRepresentation getVpaSource() {
    return source;
  }

  public void setSource(VpaSourceRepresentation source) {
    if (source != null) {
      this.source = source;
      nameField.setText(source.getName());
      urlField.setText(source.getLocation());
      loginField.setText(source.getLogin());
      passwordField.setText(PasswordUtil.decrypt(source.getPassword()));
    }
    validateInput();
  }
}
