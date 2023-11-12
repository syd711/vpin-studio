package de.mephisto.vpin.ui.system;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.util.Optional;

import static de.mephisto.vpin.ui.Studio.client;

public class SystemTab implements StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(SystemTab.class);

  @FXML
  public Label installedVersionLabel;

  @FXML
  public Label latestVersionLabel;

  @FXML
  public Label lastModifiedLabel;

  @FXML
  public Label lastCheckLabel;

  @FXML
  public BorderPane installerPane;

  @FXML
  private Hyperlink githubLink;

  protected ComponentRepresentation component;

  protected ComponentUpdateController componentUpdateController;

  @FXML
  public void onHyperlink(ActionEvent event) {
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

  @FXML
  public void onVersionSet() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Apply Version", "Apply \"" + latestVersionLabel.getText() + "\" as the current version of " + component.getType() + "?", null, "Apply");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getComponentService().setVersion(component.getType(), component.getLatestReleaseVersion());
        EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to apply version: " + e.getMessage());
      }
    }
  }

  protected void refreshUpdate() {
    latestVersionLabel.getStyleClass().remove("orange-label");
    latestVersionLabel.getStyleClass().remove("green-label");

    installedVersionLabel.setText("?");
    latestVersionLabel.setText("?");
    lastCheckLabel.setText("?");
    lastModifiedLabel.setText("?");

    if (component != null) {
      if (component.isVersionDiff()) {
        latestVersionLabel.getStyleClass().add("orange-label");
      }
      else if (component.getInstalledVersion() != null) {
        latestVersionLabel.getStyleClass().add("green-label");
      }

      installedVersionLabel.setText(component.getInstalledVersion() != null ? component.getInstalledVersion() : "?");
      latestVersionLabel.setText(component.getLatestReleaseVersion() != null ? component.getLatestReleaseVersion() : "?");
      lastCheckLabel.setText(component.getLastCheck() != null ? DateFormat.getDateTimeInstance().format(component.getLastCheck()) : "?");
      lastModifiedLabel.setText(component.getLastModified() != null ? DateFormat.getDateTimeInstance().format(component.getLastModified()) : "?");

      githubLink.setText(component.getUrl());
    }
  }


  public static String getSystemPreset() {
    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.SYSTEM_PRESET);
    String preset = preference.getValue();
    if (preset == null) {
      preset = PreferenceNames.SYSTEM_PRESET_64_BIT;
    }
    return preset;
  }

  protected void initialize() {
    try {
      FXMLLoader loader = new FXMLLoader(ComponentUpdateController.class.getResource("component-update-panel.fxml"));
      Parent builtInRoot = loader.load();
      componentUpdateController = loader.getController();
      installerPane.setCenter(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }
    refresh();

    componentUpdateController.setComponent(component);
    EventManager.getInstance().addListener(this);
  }

  protected void refresh() {
    component = client.getComponentService().getComponent(ComponentType.vpinmame);
    refreshUpdate();
  }

  @Override
  public void thirdPartyVersionUpdated(@NonNull ComponentType type) {
    Platform.runLater(() -> {
      refresh();
    });
  }
}
