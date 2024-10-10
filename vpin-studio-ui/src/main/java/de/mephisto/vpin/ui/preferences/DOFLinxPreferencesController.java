package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.doflinx.DOFLinxSettings;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class DOFLinxPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(DOFLinxPreferencesController.class);
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private CheckBox toggleAutoStart;

  @FXML
  private Button restartBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Label dofFolderErrorLabel;

  @FXML
  private Button folderBtn;

  @FXML
  private TextField installationFolderText;


  private void refresh() {
    boolean valid = client.getDofLinxService().isValid();
    dofFolderErrorLabel.setVisible(!valid);
    restartBtn.setDisable(!valid);
    stopBtn.setDisable(!valid);
    toggleAutoStart.setDisable(!valid);
  }

  @FXML
  private void onFolder(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Select DOFLinx Installation Folder");
    File folder = chooser.showDialog(stage);
    if (folder != null && folder.exists()) {
      this.installationFolderText.setText(folder.getAbsolutePath());
    }
  }


  @FXML
  private void onRestart() {
    restartBtn.setDisable(true);
    Platform.runLater(() -> {
      stopBtn.setDisable(!client.getDofLinxService().restart());
      stopBtn.setDisable(false);
      restartBtn.setDisable(false);
    });
  }

  @FXML
  private void onStop() {
    stopBtn.setDisable(!client.getDofLinxService().kill());
    stopBtn.setDisable(true);
  }

  @FXML
  private void onLink(ActionEvent event) {
    Hyperlink link = (Hyperlink) event.getSource();
    String linkText = link.getText();
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (linkText != null && linkText.startsWith("http") && desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI(linkText));
      }
      catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    DOFLinxSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.DOFLINX_SETTINGS, DOFLinxSettings.class);

    stopBtn.setDisable(!client.getDofLinxService().isRunning());
    folderBtn.setVisible(client.getSystemService().isLocal());

    toggleAutoStart.setSelected(client.getDofLinxService().isAutoStartEnabled());
    toggleAutoStart.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
        client.getDofLinxService().toggleAutoStart();
      }
    });

    installationFolderText.setText(settings.getInstallationFolder());
    installationFolderText.textProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("installationFolderText", () -> {
        try {
          settings.setInstallationFolder(t1);
          client.getPreferenceService().setJsonPreference(PreferenceNames.DOFLINX_SETTINGS, settings);
          refresh();
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }, 300);
    });

    refresh();
  }
}
