package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.preferences.PauseMenuStyle;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import de.mephisto.vpin.ui.util.Keys;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class OverlayPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayPreferencesController.class);

  @FXML
  private Button recordBtn;

  @FXML
  private ComboBox<String> overlayKeyCombo;

  @FXML
  private ComboBox<String> pauseMenuKeyCombo;

  @FXML
  private ComboBox<PopperScreen> tutorialScreenCombo;

  @FXML
  private ComboBox<PauseMenuStyle> pauseMenuStyleCombo;

  @FXML
  private CheckBox showOverlayOnStartupCheckbox;

  @FXML
  private CheckBox pauseMenuCheckbox;

  @FXML
  private RadioButton radioA;

  @FXML
  private RadioButton radioB;

  @FXML
  private RadioButton radioC;

  @FXML
  private RadioButton radioD;

  @FXML
  private TextField videoAuthorsAllowList;

  @FXML
  private Spinner<Integer> inputDebounceSpinner;

  @FXML
  private TextField externalPageUrl;

  @FXML
  private void onPauseTest() {
    PreferencesDialogs.openPauseMenuTestDialog();
  }

  @FXML
  private void onOpenExternalPage() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    String url = this.externalPageUrl.getText();
    boolean open = url != null && url.startsWith(url);
    if (open && desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI(url));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onButtonRecord() {
    PreferencesDialogs.openButtonRecorder();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<String> keyNames = Keys.getKeyNames();
    keyNames.add(0, "");
    overlayKeyCombo.setItems(FXCollections.observableList(keyNames));
    pauseMenuKeyCombo.setItems(FXCollections.observableList(keyNames));
    pauseMenuStyleCombo.setItems(FXCollections.observableList(Arrays.asList(PauseMenuStyle.values())));

    recordBtn.setDisable(!client.getSystemService().isLocal());

    PreferenceBindingUtil.bindCheckbox(showOverlayOnStartupCheckbox, PreferenceNames.SHOW_OVERLAY_ON_STARTUP, false);
    PreferenceBindingUtil.bindComboBox(overlayKeyCombo, PreferenceNames.OVERLAY_KEY);

    radioA.setUserData("");
    radioB.setUserData("-hs-plrs-offline");
    radioC.setUserData("-hs");
    radioD.setUserData("-hs-plrs-iframe");

    PreferenceEntryRepresentation preference = Studio.client.getPreference(PreferenceNames.OVERLAY_DESIGN);
    String value = preference.getValue();
    radioA.setSelected(true);

    ToggleGroup toggleGroup = new ToggleGroup();
    radioA.setToggleGroup(toggleGroup);
    radioB.setToggleGroup(toggleGroup);
    radioC.setToggleGroup(toggleGroup);
    radioD.setToggleGroup(toggleGroup);

    if (StringUtils.isEmpty(value)) {
      value = "";
    }

    externalPageUrl.setDisable(true);
    switch (value) {
      case "": {
        toggleGroup.selectToggle(radioA);
        break;
      }
      case "-hs-plrs-offline": {
        toggleGroup.selectToggle(radioB);
        break;
      }
      case "-hs": {
        toggleGroup.selectToggle(radioC);
        break;
      }
      case "-hs-plrs-iframe": {
        toggleGroup.selectToggle(radioD);
        externalPageUrl.setDisable(false);
        break;
      }
    }

    toggleGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
      String type = (String) t1.getUserData();
      externalPageUrl.setDisable(!type.contains("iframe"));
      Studio.client.getPreferenceService().setPreference(PreferenceNames.OVERLAY_DESIGN, type);
    });

    String externalPageUrlValue = client.getPreferenceService().getPreference(PreferenceNames.OVERLAY_PAGE_URL).getValue();
    externalPageUrl.setText(externalPageUrlValue);

    externalPageUrl.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(PreferenceNames.OVERLAY_PAGE_URL, () -> {
      client.getPreferenceService().setPreference(PreferenceNames.OVERLAY_PAGE_URL, t1);
    }, 300));


    PauseMenuSettings pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000, pauseMenuSettings.getInputDebounceMs());
    factory.setAmountToStepBy(100);
    inputDebounceSpinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("inputDebounce", () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      pauseMenuSettings.setInputDebounceMs(value1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    }, 1000));

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
