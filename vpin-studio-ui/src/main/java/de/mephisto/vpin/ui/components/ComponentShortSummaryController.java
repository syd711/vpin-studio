package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ComponentShortSummaryController {

  @FXML
  private Label titleLabel;

  @FXML
  private Label installedVersionLabel;

  @FXML
  private Label latestVersionLabel;

  public void refresh(@NonNull ComponentRepresentation component) {
    latestVersionLabel.getStyleClass().remove("orange-label");
    latestVersionLabel.getStyleClass().remove("green-label");

    if (component.isVersionDiff()) {
      latestVersionLabel.getStyleClass().add("orange-label");
    }
    else if (component.getInstalledVersion() != null) {
      latestVersionLabel.getStyleClass().add("green-label");
    }

    titleLabel.setText(component.getType().toString());
    installedVersionLabel.setText(component.getInstalledVersion() != null ? component.getInstalledVersion() : "?");
    latestVersionLabel.setText(component.getLatestReleaseVersion() != null ? component.getLatestReleaseVersion() : "?");
  }
}
