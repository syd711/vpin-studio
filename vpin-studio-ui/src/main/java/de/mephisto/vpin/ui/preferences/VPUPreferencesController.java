package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
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

public class VPUPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(VPUPreferencesController.class);
  public static final int DEBOUNCE_MS = 300;

  @FXML
  private TextField loginText;

  @FXML
  private PasswordField passwordText;

  @FXML
  private Button saveBtn;

  private VPUSettings settings;

  @FXML
  private void onConnectionTest() {
    String error = null;
    try {
      loginText.setDisable(true);
      passwordText.setDisable(true);
      saveBtn.setDisable(true);

      settings.setLogin(loginText.getText().trim());
      settings.setPassword(passwordText.getText().trim());
      saveSettings();
      error = client.getVpsService().checkInstallLogin("vpuniverse.com");
    } catch (Exception e) {
      error = e.getMessage();
    }
    finally {
      loginText.setDisable(false);
      passwordText.setDisable(false);
      saveBtn.setDisable(false);
    }
    // report to user
    if (error == null) {
      WidgetFactory.showInformation(stage, "VPU Account", "Login test successful!");

    }
    else {
      WidgetFactory.showAlert(stage, "VPU Account Error", "Login test not successful!", error);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    settings = client.getPreferenceService().getJsonPreference(PreferenceNames.VPU_SETTINGS, VPUSettings.class);

    loginText.setText(settings.getLogin());
    passwordText.setPromptText("<enter password to change it>");
  }

  private void saveSettings() {
    try {
      client.getPreferenceService().setJsonPreference(settings);
    }
    catch (Exception e) {
      LOG.error("Failed to update VPU settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Error", "Failed to update VP Universe settings: " + e.getMessage());
    }
  }

}
