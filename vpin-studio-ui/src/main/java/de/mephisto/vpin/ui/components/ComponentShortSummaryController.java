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
    titleLabel.setText(component.getType().toString());
    installedVersionLabel.setText(component.getInstalledVersion() != null ? component.getInstalledVersion() : "?");
    latestVersionLabel.setText(component.getLatestReleaseVersion() != null ? component.getLatestReleaseVersion() : "?");
  }
}
