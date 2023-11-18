package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.text.DateFormat;
import java.util.Optional;

import static de.mephisto.vpin.ui.Studio.client;

public class ComponentSummaryController {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentSummaryController.class);

  @FXML
  private Button setVersionBtn;

  @FXML
  private Button resetVersionBtn;

  @FXML
  private Label installedVersionLabel;

  @FXML
  private Label latestVersionLabel;

  @FXML
  private Label folderLabel;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label lastCheckLabel;

  @FXML
  private Hyperlink githubLink;

  private ComponentRepresentation component;

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

  @FXML
  public void onVersionReset() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Reset Version", "Reset version \"" + latestVersionLabel.getText() + "\"?", null, "Reset");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getComponentService().setVersion(component.getType(), "-");
        EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to reset version: " + e.getMessage());
      }
    }
  }

  protected void refreshComponent(ComponentRepresentation component) {
    this.component = component;

    latestVersionLabel.getStyleClass().remove("orange-label");
    latestVersionLabel.getStyleClass().remove("green-label");

    installedVersionLabel.setText("?");
    latestVersionLabel.setText("?");
    lastCheckLabel.setText("?");
    lastModifiedLabel.setText("?");
    folderLabel.setText("-");

    if (component != null) {
      setVersionBtn.setDisable(!StringUtils.isEmpty(component.getInstalledVersion()) && component.getInstalledVersion().equals(component.getLatestReleaseVersion()));
      resetVersionBtn.setDisable(StringUtils.isEmpty(component.getInstalledVersion()));

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
      folderLabel.setText(component.getTargetFolder() != null ? component.getTargetFolder() : "?");

      githubLink.setText(component.getUrl());
    }
  }
}
