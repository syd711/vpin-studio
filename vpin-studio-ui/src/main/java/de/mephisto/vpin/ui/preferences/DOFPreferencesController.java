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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
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
  private TextField installationFolderText32;

  @FXML
  private Button downloadBtn;

  @FXML
  private Button folderBtn;

  @FXML
  private Button folderBtn32;

  @FXML
  private Label dofFolderErrorLabel;

  @FXML
  private Label dofFolderErrorLabel32;

  @FXML
  private Spinner<Integer> syncInterval;

  @FXML
  private CheckBox syncCheckbox;

  private DOFSettings settings;

  @FXML
  private void onFolder(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Select DOF Installation Folder");
    File folder = chooser.showDialog(stage);
    if (folder != null && folder.exists()) {
      this.installationFolderText.setText(folder.getAbsolutePath());
    }
  }

  @FXML
  private void onFolder32(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Select DOF Installation Folder");
    File folder = chooser.showDialog(stage);
    if (folder != null && folder.exists()) {
      this.installationFolderText32.setText(folder.getAbsolutePath());
    }
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

  private void refresh() {
    settings = client.getDofService().getSettings();
    downloadBtn.setDisable(StringUtils.isEmpty(apiKeyText.getText()) || !settings.isValidDOFFolder());
    dofFolderErrorLabel.setVisible(!settings.isValidDOFFolder());
    dofFolderErrorLabel32.setVisible(!settings.isValidDOFFolder32());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    settings = client.getDofService().getSettings();

    SpinnerValueFactory.IntegerSpinnerValueFactory factory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 7);
    syncInterval.setValueFactory(factory1);
    syncInterval.getValueFactory().setValue(settings.getInterval());
    syncInterval.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> settings.setInterval(newValue));
    syncInterval.setDisable(!settings.isSyncEnabled());

    syncCheckbox.setSelected(settings.isSyncEnabled());
    syncCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      syncInterval.setDisable(!newValue);
      settings.setSyncEnabled(newValue);
    });

    folderBtn.setVisible(client.getSystemService().isLocal());

    installationFolderText.setText(settings.getInstallationPath());
    installationFolderText32.setText(settings.getInstallationPath32());

    apiKeyText.setText(settings.getApiKey());
    apiKeyText.textProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("apiKeyText", () -> {
        try {
          settings.setApiKey(t1);
          client.getDofService().saveSettings(settings);
          refresh();
        } catch (Exception e) {
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
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }, 300);
    });

    installationFolderText32.setText(settings.getInstallationPath32());
    installationFolderText32.textProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("installationFolderText32", () -> {
        try {
          settings.setInstallationPath32(t1);
          client.getDofService().saveSettings(settings);
          refresh();
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }, 300);
    });

    downloadBtn.setDisable(StringUtils.isEmpty(apiKeyText.getText()));
    refresh();
  }
}
