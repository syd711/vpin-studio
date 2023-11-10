package de.mephisto.vpin.ui.system;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.text.DateFormat;

import static de.mephisto.vpin.ui.Studio.client;

public class SystemTab {
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
  private Hyperlink githubLink;


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

  protected void refreshUpdate(ComponentType type) {
    ComponentRepresentation component = client.getComponentService().getComponent(type);
    latestVersionLabel.getStyleClass().remove("orange-label");
    latestVersionLabel.getStyleClass().remove("green-label");

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
}
