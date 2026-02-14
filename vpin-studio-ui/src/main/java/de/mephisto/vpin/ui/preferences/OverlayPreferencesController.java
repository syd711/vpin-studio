package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.OverlaySettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private ComboBox<MonitorInfo> screenInfoComboBox;

  @FXML
  private TextField externalPageUrl;

  @FXML
  private void onOpenExternalPage() {
    String url = this.externalPageUrl.getText();
    Studio.browse(url);
  }

  @FXML
  private void onButtonRecord() {
    PreferencesDialogs.openButtonRecorder();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    OverlaySettings overlaySettings = client.getPreferenceService().getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);

    screenInfoComboBox.setItems(FXCollections.observableList(client.getSystemService().getSystemSummary().getMonitorInfos()));
    if (overlaySettings.getOverlayScreenId() == -1) {
      screenInfoComboBox.setValue(client.getSystemService().getSystemSummary().getPrimaryMonitor());
    }
    else {
      screenInfoComboBox.setValue(client.getSystemService().getSystemSummary().getMonitorInfo(overlaySettings.getOverlayScreenId()));
    }
    screenInfoComboBox.valueProperty().addListener(new ChangeListener<MonitorInfo>() {
      @Override
      public void changed(ObservableValue<? extends MonitorInfo> observable, MonitorInfo oldValue, MonitorInfo newValue) {
        overlaySettings.setOverlayScreenId(newValue.getId());
        client.getPreferenceService().setJsonPreference(overlaySettings);
      }
    });

    showOverlayOnStartupCheckbox.setSelected(overlaySettings.isShowOnStartup());
    showOverlayOnStartupCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      overlaySettings.setShowOnStartup(newValue);
      client.getPreferenceService().setJsonPreference(overlaySettings);
    });

    radioA.setUserData("");
    radioB.setUserData("-hs-plrs-offline");
    radioC.setUserData("-hs");
    radioD.setUserData("-hs-plrs-iframe");

    String value = overlaySettings.getDesignType();
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
      overlaySettings.setDesignType(type);
      client.getPreferenceService().setJsonPreference(overlaySettings);
    });

    externalPageUrl.setText(overlaySettings.getPageUrl());
    externalPageUrl.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("overlayPageUrl", () -> {
      overlaySettings.setPageUrl(t1);
      client.getPreferenceService().setJsonPreference(overlaySettings);
    }, 300));
  }
}
