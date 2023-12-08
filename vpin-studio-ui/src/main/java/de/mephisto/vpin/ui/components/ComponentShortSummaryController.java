package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

public class ComponentShortSummaryController implements Initializable, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentShortSummaryController.class);

  @FXML
  private Label titleLabel;

  @FXML
  private Label installedVersionLabel;

  @FXML
  private Label latestVersionLabel;

  @FXML
  private Hyperlink link;

  private ComponentRepresentation component;

  @FXML
  private void onLink(ActionEvent event) {
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

  public void refresh(@NonNull ComponentRepresentation component) {
    this.component = component;
    latestVersionLabel.getStyleClass().remove("orange-label");
    latestVersionLabel.getStyleClass().remove("green-label");
    link.setText(component.getUrl());

    if (component.isVersionDiff()) {
      latestVersionLabel.getStyleClass().add("orange-label");
    }
    else if (component.getInstalledVersion() != null && !component.getInstalledVersion().equals("?")) {
      latestVersionLabel.getStyleClass().add("green-label");
    }

    titleLabel.setText(component.getType().toString());
    installedVersionLabel.setText(component.getInstalledVersion() != null ? component.getInstalledVersion() : "?");
    latestVersionLabel.setText(component.getLatestReleaseVersion() != null ? component.getLatestReleaseVersion() : "?");
  }

  @Override
  public void thirdPartyVersionUpdated(@NonNull ComponentType type) {
    if (component.getType().equals(type)) {
      Platform.runLater(() -> {
        ComponentRepresentation updatedComponent = Studio.client.getComponentService().getComponent(type);
        refresh(updatedComponent);
      });
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    EventManager.getInstance().addListener(this);
  }
}
