package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationProvider;
import de.mephisto.vpin.restclient.vpu.VPUSettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class VPUPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(VPUPreferencesController.class);
  public static final int DEBOUNCE_MS = 300;

  @FXML
  private TextField loginText;

  @FXML
  private PasswordField passwordText;

  @FXML
  private Button saveBtn;

  @FXML
  private void onConnectionTest() {
    String error = null;
    try {
      loginText.setDisable(true);
      passwordText.setDisable(true);
      saveBtn.setDisable(true);

      error = client.getAuthenticationService().login(AuthenticationProvider.VPU, loginText.getText().trim(), passwordText.getText().trim());
    }
    catch (Exception e) {
      error = e.getMessage();
    }
    finally {
      loginText.setDisable(false);
      passwordText.setDisable(false);
      saveBtn.setDisable(false);
    }
    // report to user
    if (error == null) {
      WidgetFactory.showInformation(stage, Messages.get("dialog.vpu_account"), Messages.get("dialog.login_test_successful"));

    }
    else {
      WidgetFactory.showAlert(stage, Messages.get("dialog.vpu_account_error"), Messages.get("dialog.login_test_not_successful"), error);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    VPUSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.VPU_SETTINGS, VPUSettings.class);

    loginText.setText(settings.getLogin());
    passwordText.setText(settings.getPassword());
    passwordText.setPromptText("<enter password to change it>");
  }
}
