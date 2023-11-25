package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class ComponentShortSummaryController implements Initializable, StudioEventListener {

  @FXML
  private Label titleLabel;

  @FXML
  private Label installedVersionLabel;

  @FXML
  private Label latestVersionLabel;

  private ComponentRepresentation component;

  public void refresh(@NonNull ComponentRepresentation component) {
    this.component = component;
    latestVersionLabel.getStyleClass().remove("orange-label");
    latestVersionLabel.getStyleClass().remove("green-label");

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
