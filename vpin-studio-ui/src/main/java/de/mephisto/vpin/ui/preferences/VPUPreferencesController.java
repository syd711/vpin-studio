package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.pinballx.PinballXSettings;
import de.mephisto.vpin.restclient.vpu.VPUSettings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class VPUPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(VPUPreferencesController.class);
  public static final int DEBOUNCE_MS = 300;

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private TextField loginText;

  @FXML
  private PasswordField passwordText;

  private VPUSettings settings;

  @FXML
  private void onConnectionTest() {
    boolean connected = false;
    String error = null;
    try {
     //TODO
    } catch (Exception e) {
      error = e.getMessage();
    }
    // report to user
    if (connected) {
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
    loginText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("loginText", () -> {
      settings.setLogin(t1);
      saveSettings();
    }, DEBOUNCE_MS));

    passwordText.setPromptText("<enter password to change it>");
    passwordText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("passwordText", () -> {
      settings.setPassword(t1);
      saveSettings();
    }, DEBOUNCE_MS));
  }

  private void saveSettings() {
    try {
      client.getPreferenceService().setJsonPreference(PreferenceNames.VPU_SETTINGS, settings);
    }
    catch (Exception e) {
      LOG.error("Failed to update VPU settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Error", "Failed to update frontend settings: " + e.getMessage());
    }
  }

}
