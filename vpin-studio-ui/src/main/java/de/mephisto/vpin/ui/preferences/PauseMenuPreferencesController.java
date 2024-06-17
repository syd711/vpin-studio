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

public class PauseMenuPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PauseMenuPreferencesController.class);

  @FXML
  private ComboBox<String> pauseMenuKeyCombo;

  @FXML
  private ComboBox<PopperScreen> tutorialScreenCombo;

  @FXML
  private ComboBox<PauseMenuStyle> pauseMenuStyleCombo;

  @FXML
  private CheckBox pauseMenuCheckbox;

  @FXML
  private TextField videoAuthorsAllowList;


  @FXML
  private void onPauseTest() {
    PreferencesDialogs.openPauseMenuTestDialog();
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<String> keyNames = Keys.getKeyNames();
    keyNames.add(0, "");
    pauseMenuKeyCombo.setItems(FXCollections.observableList(keyNames));
    pauseMenuStyleCombo.setItems(FXCollections.observableList(Arrays.asList(PauseMenuStyle.values())));

    PauseMenuSettings pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    pauseMenuCheckbox.setSelected(pauseMenuSettings.isUseOverlayKey());
    pauseMenuCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setUseOverlayKey(newValue);
      pauseMenuKeyCombo.setDisable(newValue);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    });

    pauseMenuKeyCombo.setValue(pauseMenuSettings.getKey());
    pauseMenuKeyCombo.setDisable(pauseMenuCheckbox.isSelected());
    pauseMenuKeyCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setKey(newValue);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    });

    pauseMenuStyleCombo.setValue(pauseMenuSettings.getStyle());
    pauseMenuStyleCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setStyle(newValue);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    });

    tutorialScreenCombo.setItems(FXCollections.observableList(Arrays.asList(PopperScreen.Audio, PopperScreen.DMD, PopperScreen.GameHelp, PopperScreen.GameInfo, PopperScreen.Menu, PopperScreen.Other2, PopperScreen.Topper)));
    tutorialScreenCombo.setValue(pauseMenuSettings.getVideoScreen());
    tutorialScreenCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setVideoScreen(newValue);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    });

    videoAuthorsAllowList.setText(pauseMenuSettings.getAuthorAllowList());
    videoAuthorsAllowList.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(PreferenceNames.PAUSE_MENU_SETTINGS, () -> {
      pauseMenuSettings.setAuthorAllowList(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    }, 300));
  }
}
