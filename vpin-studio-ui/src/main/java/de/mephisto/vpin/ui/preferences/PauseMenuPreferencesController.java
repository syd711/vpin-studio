package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class PauseMenuPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PauseMenuPreferencesController.class);

  @FXML
  private CheckBox pauseMenuCheckbox;

  @FXML
  private Pane maniaScoresBox;

  @FXML
  private CheckBox maniaScoresCheckbox;

  @FXML
  private Pane iScoredScoresBox;

  @FXML
  private CheckBox iScoredScoresCheckbox;

  @FXML
  private CheckBox pauseMenuMuteCheckbox;

  @FXML
  private CheckBox tutorialsCheckbox;

  @FXML
  private Spinner<Integer> delaySpinner;

  @FXML
  private ComboBox<MonitorInfo> screenInfoComboBox;

  @FXML
  private void onPauseTest() {
    PreferencesDialogs.openPauseMenuTestDialog();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    PauseMenuSettings pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    maniaScoresBox.managedProperty().bindBidirectional(maniaScoresBox.visibleProperty());
    maniaScoresBox.setVisible(Features.MANIA_ENABLED && pauseMenuSettings.isShowManiaScores());
    iScoredScoresBox.managedProperty().bindBidirectional(iScoredScoresBox.visibleProperty());
    iScoredScoresBox.setVisible(Features.ISCORED_ENABLED && pauseMenuSettings.isShowIscoredScores());

    screenInfoComboBox.setItems(FXCollections.observableList(client.getSystemService().getSystemSummary().getScreenInfos()));
    if (pauseMenuSettings.getPauseMenuScreenId() == -1) {
      screenInfoComboBox.setValue(client.getSystemService().getSystemSummary().getPrimaryScreen());
    }
    else {
      screenInfoComboBox.setValue(client.getSystemService().getSystemSummary().getScreenInfo(pauseMenuSettings.getPauseMenuScreenId()));
    }
    screenInfoComboBox.valueProperty().addListener(new ChangeListener<MonitorInfo>() {
      @Override
      public void changed(ObservableValue<? extends MonitorInfo> observable, MonitorInfo oldValue, MonitorInfo newValue) {
        pauseMenuSettings.setPauseMenuScreenId(newValue.getId());
        client.getPreferenceService().setJsonPreference(pauseMenuSettings);
      }
    });

    iScoredScoresCheckbox.setSelected(pauseMenuSettings.isShowIscoredScores());
    iScoredScoresCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setShowIscoredScores(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    maniaScoresCheckbox.setSelected(pauseMenuSettings.isShowManiaScores());
    maniaScoresCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setShowManiaScores(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    tutorialsCheckbox.setSelected(pauseMenuSettings.isShowTutorials());
    tutorialsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setShowTutorials(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    pauseMenuCheckbox.setSelected(pauseMenuSettings.isUseOverlayKey());
    pauseMenuCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setUseOverlayKey(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    pauseMenuMuteCheckbox.setSelected(pauseMenuSettings.isMuteOnPause());
    pauseMenuMuteCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setMuteOnPause(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(500, 10000, pauseMenuSettings.getUnpauseDelay());
    delaySpinner.setValueFactory(factory1);
    factory1.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("delaySpinner", () -> {
      pauseMenuSettings.setUnpauseDelay(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 300));
  }
}
