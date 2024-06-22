package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.frontend.pinballx.PinballXSettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class PinballXSettingsPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PinballXSettingsPreferencesController.class);
  public static final int DEBOUNCE_MS = 500;

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private TextField gameExMailText;

  @FXML
  private PasswordField gameExPasswordText;

  @FXML
  private CheckBox gameExEnabledCheckbox;

  private PinballXSettings pinballXSettings;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    pinballXSettings = client.getFrontendService().getSettings(PinballXSettings.class);

    gameExMailText.setText(pinballXSettings.getGameExMail());
    gameExMailText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("gameExMailText", () -> {
      pinballXSettings.setGameExMail(t1);
      saveSettings();
    }, DEBOUNCE_MS));

    gameExPasswordText.setPromptText("<enter password to change it>");
    gameExPasswordText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("gameExPasswordText", () -> {
      pinballXSettings.setGameExPassword(t1);
      saveSettings();
    }, DEBOUNCE_MS));

    gameExEnabledCheckbox.setSelected(pinballXSettings.isGameExEnabled());
    gameExEnabledCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      pinballXSettings.setGameExEnabled(t1);
      saveSettings();
    });
  }

  private void saveSettings() {
    try {
      client.getFrontendService().saveSettings(pinballXSettings);
    }
    catch (Exception e) {
      LOG.error("Failed to update frontend settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Error", "Failed to update frontend settings: " + e.getMessage());
    }
  }

}
