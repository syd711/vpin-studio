package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.system.FolderRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.FolderChooserDialog;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  @FXML
  private Button folderBtn;

  @FXML
  private Label dofFolderErrorLabel;

  @FXML
  private Spinner<Integer> syncInterval;

  @FXML
  private CheckBox syncCheckbox;

  private DOFSettings settings;

  @FXML
  private void onFolder(ActionEvent event) {
    FolderRepresentation folder = FolderChooserDialog.open(installationFolderText.getText());
    if (folder != null) {
      this.installationFolderText.setText(folder.getPath());
    }
  }

  @FXML
  private void onLink() {
    Studio.browse("http://configtool.vpuniverse.com");
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
          JobDescriptor result = (JobDescriptor) resultModel.getResults().get(0);
          if (result.getError() != null) {
            WidgetFactory.showAlert(Studio.stage, "Error", "DOF configuration download failed: ", result.getError());
          }
          else {
            WidgetFactory.showInformation(Studio.stage, "DOF configuration download finished successfully.", null);
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to sync dof: " + e.getMessage());
    }
  }

  private void refresh() {
    settings = client.getDofService().getSettings();
    downloadBtn.setDisable(StringUtils.isEmpty(apiKeyText.getText()) || !settings.isValidDOFFolder());
    dofFolderErrorLabel.setVisible(!settings.isValidDOFFolder());

    client.getPreferenceService().notifyPreferenceChange(PreferenceNames.DOF_SETTINGS, null);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    settings = client.getDofService().getSettings();

    SpinnerValueFactory.IntegerSpinnerValueFactory factory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 7);
    syncInterval.setValueFactory(factory1);
    syncInterval.getValueFactory().setValue(settings.getInterval());
    syncInterval.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> {
      settings.setInterval(newValue);
      try {
        client.getDofService().saveSettings(settings);
        refresh();
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
      }
    });
    syncInterval.setDisable(!settings.getSyncEnabled());

    syncCheckbox.setSelected(settings.getSyncEnabled());
    syncCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      syncInterval.setDisable(!newValue);
      settings.setSyncEnabled(newValue);
      try {
        client.getDofService().saveSettings(settings);
        refresh();
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
      }
    });

    installationFolderText.setText(settings.getInstallationPath());

    apiKeyText.setText(settings.getApiKey());
    apiKeyText.textProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("apiKeyText", () -> {
        try {
          settings.setApiKey(t1);
          client.getDofService().saveSettings(settings);
          refresh();
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }, 300);
    });

    installationFolderText.setText(settings.getInstallationPath());
    installationFolderText.textProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("installationFolderText", () -> {
        try {
          settings.setInstallationPath(t1);
          client.getDofService().saveSettings(settings);
          refresh();
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }, 300);
    });


    downloadBtn.setDisable(StringUtils.isEmpty(apiKeyText.getText()));
    refresh();
  }
}
