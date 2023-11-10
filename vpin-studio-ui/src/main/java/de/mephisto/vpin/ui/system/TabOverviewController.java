package de.mephisto.vpin.ui.system;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabOverviewController extends SystemTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabOverviewController.class);

  @FXML
  private ComboBox<String> presetCombo;

  @FXML
  private Button refreshBtn;

  @FXML
  private void onVersionRefresh() {
    refreshBtn.setDisable(true);

    new Thread(() -> {
      client.getComponentService().clearCache();

      Platform.runLater(() -> {
        EventManager.getInstance().notify3rdPartyVersionUpdate();
        refreshAll();
      });
      refreshBtn.setDisable(false);
    }).start();
  }

  private void refreshAll() {
    Platform.runLater(() -> {
//      refreshUpdate(ComponentType.vpinmame, mameTitleLabel, mameInstalledVersionLabel, mameLatestVersionLabel);
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
//    presetCombo.setItems(FXCollections.observableList(Arrays.asList(PreferenceNames.SYSTEM_PRESET_32_BIT, PreferenceNames.SYSTEM_PRESET_64_BIT)));
//
//    String preset = getSystemPreset();
//    presetCombo.setValue(preset);
//    presetCombo.valueProperty().addListener((observableValue, s, t1) -> client.getPreferenceService().setPreference(PreferenceNames.SYSTEM_PRESET, t1));
  }
}
