package de.mephisto.vpin.ui.vpxz.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.util.PasswordUtil;
import de.mephisto.vpin.restclient.vpxz.VPXZSourceRepresentation;
import de.mephisto.vpin.restclient.vpxz.VPXZSourceType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class VPXZSourceHttpDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZSourceHttpDialogController.class);
  private static File lastFolderSelection;

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private TextField urlField;

  @FXML
  private CheckBox basicAuthCheckbox;

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private TextField loginField;

  @FXML
  private TextField passwordField;

  private VPXZSourceRepresentation source;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.source = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    this.source.setType(VPXZSourceType.Http.name());
    this.source.setName(nameField.getText());
    this.source.setLocation(urlField.getText());
    this.source.setLogin(loginField.getText());
    this.source.setEnabled(enabledCheckbox.isSelected());

    if(basicAuthCheckbox.isSelected()) {

    }

    this.source.setPassword(PasswordUtil.encrypt(passwordField.getText()));

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    source = new VPXZSourceRepresentation();
    source.setSettings(null);
    source.setEnabled(true);
    enabledCheckbox.setSelected(true);
    loginField.setDisable(true);
    passwordField.setDisable(true);

    nameField.textProperty().addListener((observableValue, s, t1) -> {
      source.setName(t1);
      validateInput();
    });
    urlField.textProperty().addListener((observableValue, s, t1) -> {
      source.setName(t1);
      validateInput();
    });

    basicAuthCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      this.loginField.setDisable(!newValue);
      this.passwordField.setDisable(!newValue);
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

  public VPXZSourceRepresentation getVpxzSource() {
    return source;
  }

  public void setSource(VPXZSourceRepresentation source) {
    if (source != null) {
      this.source = source;
      nameField.setText(source.getName());
      urlField.setText(source.getLocation());
      enabledCheckbox.setSelected(source.isEnabled());
      loginField.setText(source.getLogin());
      passwordField.setText(PasswordUtil.decrypt(source.getPassword()));
    }
    validateInput();
  }
}
