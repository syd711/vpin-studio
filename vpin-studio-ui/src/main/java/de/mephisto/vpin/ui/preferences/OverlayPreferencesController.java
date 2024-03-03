package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.preferences.PauseMenuStyle;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.Keys;
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
import java.util.*;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class OverlayPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayPreferencesController.class);

  @FXML
  private ComboBox<String> overlayKeyCombo;

  @FXML
  private ComboBox<String> pauseMenuKeyCombo;

  @FXML
  private ComboBox<PauseMenuStyle> pauseMenuStyleCombo;

  @FXML
  private CheckBox showOverlayOnStartupCheckbox;

  @FXML
  private CheckBox pauseMenuCheckbox;

  @FXML
  private CheckBox userInternalBrowserCheckbox;

  @FXML
  private RadioButton radioA;

  @FXML
  private RadioButton radioB;

  @FXML
  private RadioButton radioC;

  @FXML
  private RadioButton radioD;

  @FXML
  private CheckBox autoplayCheckbox;

  @FXML
  private CheckBox renderTutorialLinks;

  @FXML
  private TextField videoAuthorsAllowList;

  @FXML
  private Button externalPageButton;

  @FXML
  private TextField externalPageUrl;

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

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<String> keyNames = Keys.getKeyNames();
    keyNames.add(0, "");
    overlayKeyCombo.setItems(FXCollections.observableList(keyNames));
    pauseMenuKeyCombo.setItems(FXCollections.observableList(keyNames));
    pauseMenuStyleCombo.setItems(FXCollections.observableList(Arrays.asList(PauseMenuStyle.values())));

    BindingUtil.bindCheckbox(showOverlayOnStartupCheckbox, PreferenceNames.SHOW_OVERLAY_ON_STARTUP, false);
    BindingUtil.bindComboBox(overlayKeyCombo, PreferenceNames.OVERLAY_KEY);

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
    pauseMenuCheckbox.setSelected(pauseMenuSettings.isUseOverlayKey());
    pauseMenuCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setUseOverlayKey(newValue);
      pauseMenuKeyCombo.setDisable(newValue);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    });

    userInternalBrowserCheckbox.setDisable(!pauseMenuSettings.isRenderTutorialLinks());
    autoplayCheckbox.setDisable(!pauseMenuSettings.isRenderTutorialLinks());
    videoAuthorsAllowList.setDisable(!pauseMenuSettings.isRenderTutorialLinks());

    renderTutorialLinks.setSelected(pauseMenuSettings.isRenderTutorialLinks());
    renderTutorialLinks.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setRenderTutorialLinks(newValue);
      userInternalBrowserCheckbox.setDisable(!newValue);
      autoplayCheckbox.setDisable(!newValue);
      videoAuthorsAllowList.setDisable(!newValue);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    });

    userInternalBrowserCheckbox.setSelected(pauseMenuSettings.isUseInternalBrowser());
    userInternalBrowserCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setUseInternalBrowser(newValue);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    });

    autoplayCheckbox.setSelected(pauseMenuSettings.isAutoplay());
    autoplayCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setAutoplay(newValue);
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

    videoAuthorsAllowList.setText(pauseMenuSettings.getAuthorAllowList());
    videoAuthorsAllowList.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(PreferenceNames.PAUSE_MENU_SETTINGS, () -> {
      pauseMenuSettings.setAuthorAllowList(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    }, 300));
  }
}
