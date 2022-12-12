package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.launcher.ServiceInstallationProgressModel;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class RootController implements Initializable {

  @FXML
  private StackPane rootStack;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }
}
