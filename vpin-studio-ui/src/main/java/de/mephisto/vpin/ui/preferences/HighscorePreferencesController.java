package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.system.NVRamsInfo;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class HighscorePreferencesController implements Initializable {

  @FXML
  private CheckBox filterCheckbox;

  @FXML
  private CheckBox monitorCheckbox;

  @FXML
  private void onNvRamReset() {
    ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new NvRamDownloadProgressModel("NVRam Synchronization"));
    if (!progressDialog.getResults().isEmpty()) {
      NVRamsInfo nvRamsInfo = (NVRamsInfo) progressDialog.getResults().getFirst();
      WidgetFactory.showInformation(Studio.stage, Messages.get("dialog.nvram_reset"), Messages.get("dialog.resetted") + nvRamsInfo.getCount() + Messages.get("dialog.nvram_files"));
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    boolean filerEnabled = ServerFX.client.getPreferenceService().getPreference(PreferenceNames.HIGHSCORE_FILTER_ENABLED).getBooleanValue(false);
    filterCheckbox.setSelected(filerEnabled);
    filterCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      client.getPreferenceService().setPreference(PreferenceNames.HIGHSCORE_FILTER_ENABLED, t1);
    });


    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    monitorCheckbox.setSelected(serverSettings.isHighscoreMonitorEnabled());
    monitorCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setHighscoreMonitorEnabled(t1);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });
  }
}
