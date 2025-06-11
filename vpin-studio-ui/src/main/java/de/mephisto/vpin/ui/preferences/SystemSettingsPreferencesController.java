package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class SystemSettingsPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(SystemSettingsPreferencesController.class);

  @FXML
  private CheckBox stickyKeysCheckbox;

  @FXML
  private Spinner<Integer> idleSpinner;

  @FXML
  private Button shutdownBtn;

  @FXML
  private void onShutdown() {
    Optional<ButtonType> result = WidgetFactory.showAlertOption(Studio.stage, "Remote System Shutdown", "Cancel", "Shutdown System", "Are you sure you want to shutdown the remote system?", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getSystemService().systemShutdown();
      WidgetFactory.showInformation(Studio.stage, "Remote System Shutdown", "The remote system will shutdown in less than a minute.");
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    shutdownBtn.setDisable(client.getSystemService().isLocal());

    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    PreferenceEntryRepresentation idle = ServerFX.client.getPreference(PreferenceNames.IDLE_TIMEOUT);
    int timeout = idle.getIntValue();
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60, timeout);
    idleSpinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      client.getPreferenceService().setPreference(PreferenceNames.IDLE_TIMEOUT, String.valueOf(value1));
    }, 500));

    stickyKeysCheckbox.setSelected(!serverSettings.isStickyKeysEnabled());
    stickyKeysCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setStickyKeysEnabled(!t1);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });
  }
}
