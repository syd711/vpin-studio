package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import de.mephisto.vpin.commons.utils.JFXFuture;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class InputPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(InputPreferencesController.class);

  @FXML
  private Button recordBtn;

  @FXML
  private TextField filterListText;

  @FXML
  private Pane fullScreenWarning;

  @FXML
  private Spinner<Integer> inputDebounceSpinner;

  @FXML
  private void onButtonRecord() {
    PreferencesDialogs.openButtonRecorder();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    fullScreenWarning.managedProperty().bindBidirectional(fullScreenWarning.visibleProperty());

    PauseMenuSettings pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000, pauseMenuSettings.getInputDebounceMs());
    factory.setAmountToStepBy(100);
    inputDebounceSpinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("inputDebounce", () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      pauseMenuSettings.setInputDebounceMs(value1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 1000));

    filterListText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("filterList", () -> {
      pauseMenuSettings.setInputFilterList(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 500));

    if (client.getFrontendService().getFrontendCached().getFrontendType().equals(FrontendType.Popper)) {
      JFXFuture.supplyAsync(() -> {
        List<GameEmulatorRepresentation> gameEmulators = client.getEmulatorService().getVpxGameEmulators();
        for (GameEmulatorRepresentation emulator : gameEmulators) {
          String script = emulator.getLaunchScript().getScript();
          if (!StringUtils.isEmpty(script)) {
            String[] split = script.split("\n");
            for (String line : split) {
              if (line.contains("FSMODE=EnableTrueFullScreen") && !line.toLowerCase().contains("rem")) {
                return true;
              }
            }
          }
        }
        return false;
      }).thenAcceptLater(visible -> {
        fullScreenWarning.setVisible(visible);
      });
    }

  }
}
