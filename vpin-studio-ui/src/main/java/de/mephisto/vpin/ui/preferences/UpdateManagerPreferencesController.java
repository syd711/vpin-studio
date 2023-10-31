package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.tables.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class UpdateManagerPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(UpdateManagerPreferencesController.class);

  @FXML
  private ComboBox<String> presetCombo;

  @FXML
  private Button refreshBtn;

  @FXML
  private Button mameInstallBtn;

  @FXML
  private Button mameSetVersionBtn;

  @FXML
  private Button mameCheckBtn;

  @FXML
  private Button mameBtn;

  @FXML
  private Label mameTitleLabel;

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
      refreshUpdate(ComponentType.vpinmame, mameTitleLabel, mameInstalledVersionLabel, mameLatestVersionLabel);
    }
  }


  @FXML
  private void onMameSetup() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
        File file = new File(defaultGameEmulator.getMameDirectory(), "Setup64.exe");
        if (presetCombo.getValue().equals(PreferenceNames.SYSTEM_PRESET_32_BIT)) {
          file = new File(defaultGameEmulator.getMameDirectory(), "Setup.exe");
        }

        if (!file.exists()) {
          WidgetFactory.showAlert(Studio.stage, "Did not find Setup.exe", "The exe file " + file.getAbsolutePath() + " was not found.");
        }
        else {
          desktop.open(file);
        }
      } catch (Exception e) {
        LOG.error("Failed to open Mame Setup: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onHyperlink(ActionEvent event) {
    Hyperlink link = (Hyperlink) event.getSource();
    String linkText = link.getText();
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (linkText != null && linkText.startsWith("http") && desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI(linkText));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
      }
    }
  }

  private void refreshUpdate(ComponentType type, Label titleLabel, Label installedLabel, Label latestLabel) {
    ComponentRepresentation component = client.getComponentService().getComponent(type);
    latestLabel.getStyleClass().remove("orange");
    titleLabel.setGraphic(null);

    if (component != null) {
      installedLabel.setText(component.getInstalledVersion() != null ? component.getInstalledVersion() : "?");
      latestLabel.setText(component.getLatestReleaseVersion() != null ? component.getLatestReleaseVersion() : "?");

      if (component.isVersionDiff()) {
        titleLabel.setGraphic(WidgetFactory.createUpdateIcon());
        latestLabel.getStyleClass().add("orange");
      }
    }
  }

  private static String getSystemPreset() {
    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.SYSTEM_PRESET);
    String preset = preference.getValue();
    if (preset == null) {
      preset = PreferenceNames.SYSTEM_PRESET_64_BIT;
    }
    return preset;
  }

  private void refreshAll() {
    Platform.runLater(() -> {
      refreshUpdate(ComponentType.vpinmame, mameTitleLabel, mameInstalledVersionLabel, mameLatestVersionLabel);
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    presetCombo.setItems(FXCollections.observableList(Arrays.asList(PreferenceNames.SYSTEM_PRESET_32_BIT, PreferenceNames.SYSTEM_PRESET_64_BIT)));

    String preset = getSystemPreset();
    presetCombo.setValue(preset);
    presetCombo.valueProperty().addListener((observableValue, s, t1) -> client.getPreferenceService().setPreference(PreferenceNames.SYSTEM_PRESET, t1));

    mameBtn.setDisable(!client.getSystemService().isLocal());

    refreshAll();
  }
}
