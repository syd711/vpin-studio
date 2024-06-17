package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.preferences.PauseMenuStyle;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import de.mephisto.vpin.ui.util.Keys;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class InputPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(InputPreferencesController.class);

  @FXML
  private Button recordBtn;

  @FXML
  private Spinner<Integer> inputDebounceSpinner;


  @FXML
  private void onButtonRecord() {
    PreferencesDialogs.openButtonRecorder();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    recordBtn.setDisable(!client.getSystemService().isLocal());
    PauseMenuSettings pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000, pauseMenuSettings.getInputDebounceMs());
    factory.setAmountToStepBy(100);
    inputDebounceSpinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("inputDebounce", () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      pauseMenuSettings.setInputDebounceMs(value1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    }, 1000));
  }
}
