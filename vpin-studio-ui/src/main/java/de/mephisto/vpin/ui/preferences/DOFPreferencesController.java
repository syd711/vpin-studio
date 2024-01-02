package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DOFPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(DOFPreferencesController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private TextField apiKeyText;

  @FXML
  private TextField installationFolderText;

  @FXML
  private Button downloadBtn;

  private DOFSettings settings;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    settings = client.getDofService().getSettings();
    installationFolderText.setText(settings.getInstallationPath());

    apiKeyText.setText(settings.getApiKey());
    apiKeyText.textProperty().addListener((observableValue, integer, t1) -> {
      downloadBtn.setDisable(StringUtils.isEmpty(apiKeyText.getText()));

      debouncer.debounce("apiKeyText", () -> {
        try {
          settings.setApiKey(t1);
          client.getDofService().saveSettings(settings);
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }, 300);
    });

    downloadBtn.setDisable(StringUtils.isEmpty(apiKeyText.getText()));
  }

  @FXML
  private void onLink() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    try {
      desktop.browse(new URI("http://configtool.vpuniverse.com"));
    } catch (Exception e) {
      LOG.error("Failed to open link: " + e.getMessage());
    }
  }

  @FXML
  private void onDownload() {
    try {
      ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new DOFSyncProgressModel());
      List<Object> results = resultModel.getResults();
      if (!results.isEmpty()) {
        Object o = results.get(0);
        if (o instanceof String) {
          WidgetFactory.showAlert(Studio.stage, "Error", "DOF config download failed: " + o);
        }
        else {
          JobExecutionResult result = (JobExecutionResult) resultModel.getResults().get(0);
          if (result.isErrorneous()) {
            WidgetFactory.showAlert(Studio.stage, "Error", "DOF configuration download failed: ", result.getError());
          }
          else {
            WidgetFactory.showInformation(Studio.stage, "DOF configuration download finished successfully.", null);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to sync dof: " + e.getMessage());
    }
  }
}
