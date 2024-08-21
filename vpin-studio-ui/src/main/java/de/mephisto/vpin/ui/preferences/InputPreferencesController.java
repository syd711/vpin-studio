package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class InputPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(InputPreferencesController.class);

  @FXML
  private Button recordBtn;

//  @FXML
//  private CheckBox ignoreInputs;

  @FXML
  private TextField filterListText;

  @FXML
  private Spinner<Integer> inputDebounceSpinner;


  @FXML
  private void onButtonRecord() {
    PreferencesDialogs.openButtonRecorder();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    PauseMenuSettings pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

//    recordBtn.setDisable(!client.getSystemService().isLocal() || pauseMenuSettings.isIgnoreInputs());
//    filterListText.setDisable(pauseMenuSettings.isIgnoreInputs());
//    inputDebounceSpinner.setDisable(pauseMenuSettings.isIgnoreInputs());

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000, pauseMenuSettings.getInputDebounceMs());
    factory.setAmountToStepBy(100);
    inputDebounceSpinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("inputDebounce", () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      pauseMenuSettings.setInputDebounceMs(value1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    }, 1000));

//    ignoreInputs.setSelected(pauseMenuSettings.isIgnoreInputs());
//    ignoreInputs.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
//      pauseMenuSettings.setIgnoreInputs(t1);
//      filterListText.setDisable(t1);
//      recordBtn.setDisable(t1);
//      inputDebounceSpinner.setDisable(t1);
//      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
//    });

    filterListText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("filterList", () -> {
      pauseMenuSettings.setInputFilterList(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    }, 500));
  }
}
