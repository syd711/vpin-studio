package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class OverlayPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayPreferencesController.class);

  @FXML
  private CheckBox showOverlayOnStartupCheckbox;

  @FXML
  private RadioButton radioA;

  @FXML
  private RadioButton radioB;

  @FXML
  private RadioButton radioC;

  @FXML
  private RadioButton radioD;

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
      }
      catch (Exception e) {
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
    PreferenceBindingUtil.bindCheckbox(showOverlayOnStartupCheckbox, PreferenceNames.SHOW_OVERLAY_ON_STARTUP, false);

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

  }
}
