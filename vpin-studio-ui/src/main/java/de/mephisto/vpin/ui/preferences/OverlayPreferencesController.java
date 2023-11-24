package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.Keys;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class OverlayPreferencesController implements Initializable {
  @FXML
  private ComboBox<String> overlayKeyCombo;

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
  private WebView preview;

  @FXML
  private TextField externalPageUrl;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<String> keyNames = Keys.getKeyNames();
    keyNames.add(0, "");
    overlayKeyCombo.setItems(FXCollections.observableList(keyNames));

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
    preview.setZoom(0.25);
    preview.setVisible(false);
    if (!StringUtils.isEmpty(externalPageUrlValue)) {
      preview.setVisible(true);
      preview.getEngine().load(externalPageUrlValue);
    }

    externalPageUrl.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(PreferenceNames.OVERLAY_PAGE_URL, () -> {
      client.getPreferenceService().setPreference(PreferenceNames.OVERLAY_PAGE_URL, t1);
      preview.setVisible(!StringUtils.isEmpty(externalPageUrlValue));

      if (!StringUtils.isEmpty(externalPageUrlValue)) {
        preview.getEngine().load(externalPageUrlValue);
      }
    }, 300));
  }
}
