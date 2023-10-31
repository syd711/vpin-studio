package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class UpdateManagerPreferencesController implements Initializable {


  @FXML
  private Button refreshBtn;


  @FXML
  private Button mameInstallBtn;

  @FXML
  private Button mameSetVersionBtn;

  @FXML
  private Button mameCheckBtn;

  @FXML
  private Label mameInstalledVersionLabel;

  @FXML
  private Label mameLatestVersionLabel;

  @FXML
  private Label mameLastModifiedLabel;

  @FXML
  private void onMameCheck() {

  }

  @FXML
  private void onMameInstall() {
    Dialogs.openComponentUpdateDialog(ComponentType.vpinmame, "Installation of \"VPin MAME " + this.mameLatestVersionLabel.getText() + "\"");
  }

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

  @FXML
  private void onMameVersionSet() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Set Version", "Apply \"" + mameLatestVersionLabel.getText() + "\" as the current version of VPin MAME?", null, "Apply");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        ComponentRepresentation component = client.getComponentService().getComponent(ComponentType.vpinmame);
        client.getComponentService().setVersion(component.getType(), component.getLatestReleaseVersion());
        EventManager.getInstance().notify3rdPartyVersionUpdate();
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to apply version: " + e.getMessage());
      }
      refreshUpdate(ComponentType.vpinmame, mameInstalledVersionLabel, mameLatestVersionLabel);
    }
  }

  private void refreshUpdate(ComponentType type, Label installedLabel, Label latestLabel) {
    ComponentRepresentation component = client.getComponentService().getComponent(type);
    if (component != null) {
      installedLabel.setText(component.getInstalledVersion() != null ? component.getInstalledVersion() : "?");
      latestLabel.setText(component.getLatestReleaseVersion() != null ? component.getLatestReleaseVersion() : "?");
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    refreshAll();
  }

  private void refreshAll() {
    Platform.runLater(() -> {
      refreshUpdate(ComponentType.vpinmame, mameInstalledVersionLabel, mameLatestVersionLabel);
    });
  }
}
