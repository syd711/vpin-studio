package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
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

public class HighscorePreferencesController implements Initializable {

  @FXML
  private CheckBox filterCheckbox;

  @FXML
  private void onNvRamReset() {
    ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new NvRamDownloadProgressModel("NVRam Synchronization"));
    if (!progressDialog.getResults().isEmpty()) {
      NVRamsInfo nvRamsInfo = (NVRamsInfo) progressDialog.getResults().get(0);
      WidgetFactory.showInformation(Studio.stage, "NVRam Reset", "Resetted " + nvRamsInfo.getCount() + " nvram files.");
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    boolean filerEnabled = ServerFX.client.getPreferenceService().getPreference(PreferenceNames.HIGHSCORE_FILTER_ENABLED).getBooleanValue(false);
    filterCheckbox.setSelected(filerEnabled);
    filterCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      client.getPreferenceService().setPreference(PreferenceNames.HIGHSCORE_FILTER_ENABLED, t1);
    });
  }
}
