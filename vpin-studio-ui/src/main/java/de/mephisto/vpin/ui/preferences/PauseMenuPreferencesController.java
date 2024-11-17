package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.preferences.PauseMenuStyle;
import de.mephisto.vpin.restclient.system.ScreenInfo;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
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
  private ComboBox<VPinScreen> tutorialScreenCombo;

  @FXML
  private ComboBox<PauseMenuStyle> pauseMenuStyleCombo;

  @FXML
  private CheckBox pauseMenuCheckbox;

  @FXML
  private TextField videoAuthorsAllowList;

  @FXML
  private ComboBox<ScreenInfo> screenInfoComboBox;

  @FXML
  private void onPauseTest() {
    PreferencesDialogs.openPauseMenuTestDialog();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    pauseMenuStyleCombo.setItems(FXCollections.observableList(Arrays.asList(PauseMenuStyle.values())));

    PauseMenuSettings pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);
    screenInfoComboBox.setItems(FXCollections.observableList(client.getSystemService().getSystemSummary().getScreenInfos()));
    if (pauseMenuSettings.getPauseMenuScreenId() == -1) {
      screenInfoComboBox.setValue(client.getSystemService().getSystemSummary().getPrimaryScreen());
    }
    else {
      screenInfoComboBox.setValue(client.getSystemService().getSystemSummary().getScreenInfo(pauseMenuSettings.getPauseMenuScreenId()));
    }
    screenInfoComboBox.valueProperty().addListener(new ChangeListener<ScreenInfo>() {
      @Override
      public void changed(ObservableValue<? extends ScreenInfo> observable, ScreenInfo oldValue, ScreenInfo newValue) {
        pauseMenuSettings.setPauseMenuScreenId(newValue.getId());
        client.getPreferenceService().setJsonPreference(pauseMenuSettings);
      }
    });

    pauseMenuCheckbox.setSelected(pauseMenuSettings.isUseOverlayKey());
    pauseMenuCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setUseOverlayKey(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    pauseMenuStyleCombo.setValue(pauseMenuSettings.getStyle());
    pauseMenuStyleCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setStyle(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    tutorialScreenCombo.setItems(FXCollections.observableList(Arrays.asList(VPinScreen.Audio, VPinScreen.DMD, VPinScreen.GameHelp, VPinScreen.GameInfo, VPinScreen.Menu, VPinScreen.Other2, VPinScreen.Topper)));
    tutorialScreenCombo.setValue(pauseMenuSettings.getVideoScreen());
    tutorialScreenCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setVideoScreen(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    videoAuthorsAllowList.setText(pauseMenuSettings.getAuthorAllowList());
    videoAuthorsAllowList.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(PreferenceNames.PAUSE_MENU_SETTINGS, () -> {
      pauseMenuSettings.setAuthorAllowList(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 300));
  }
}
