package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabVpxController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabVpxController.class);

  @FXML
  private Button playBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private void onFolder() {
    GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
    File folder = new File(defaultGameEmulator.getInstallationDirectory());
    openFolder(folder);
  }

  @FXML
  private void onPlay() {
    client.getVpxService().playGame(-1);
  }

  @FXML
  private void onStop() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Stop all VPX and PinUP Popper processes?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getPinUPPopperService().terminatePopper();
    }
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.vpinball;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    playBtn.setDisable(!client.getSystemService().isLocal());
    stopBtn.setDisable(!client.getSystemService().isLocal());

    componentUpdateController.setLocalInstallOnly(false);
  }
}
