package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.vpf.VPFSettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class VPFPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(VPFPreferencesController.class);
  public static final int DEBOUNCE_MS = 300;

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private TextField loginText;

  @FXML
  private PasswordField passwordText;

  private VPFSettings settings;

  @FXML
  private void onConnectionTest() {
    String error = null;
    try {
      error = client.getVpsService().checkInstallLogin("vpforums.org");
    } catch (Exception e) {
      error = e.getMessage();
    }
    // report to user
    if (error == null) {
      WidgetFactory.showInformation(stage, "VPF Account", "Login test successful!");
      settings.setLogin(loginText.getText().trim());
      settings.setPassword(passwordText.getText().trim());
      saveSettings();
    }
    else {
      WidgetFactory.showAlert(stage, "VPF Account Error", "Login test not successful!", error);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    settings = client.getPreferenceService().getJsonPreference(PreferenceNames.VPF_SETTINGS, VPFSettings.class);

    loginText.setText(settings.getLogin());
    passwordText.setPromptText("<enter password to change it>");
  }

  private void saveSettings() {
    try {
      client.getPreferenceService().setJsonPreference(settings);
    }
    catch (Exception e) {
      LOG.error("Failed to update VPF settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Error", "Failed to update VP Forums settings: " + e.getMessage());
    }
  }

}
