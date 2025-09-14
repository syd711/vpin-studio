package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabVpxController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabVpxController.class);

  @FXML
  private Button openBtn;

  @FXML
  private void onOpen() {
    try {
      Dialogs.openTextEditor(new MonitoredTextFile(VPinFile.VPinballXIni), "VPinballX.ini");
    }
    catch (Exception e) {
      LOG.error("Failed to open file: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open file: " + e.getMessage());
    }
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.vpinball;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    componentUpdateController.setLocalInstallOnly(false);
  }

  @Override
  protected void refreshTab(ComponentRepresentation component) {
    openFolderButton.setDisable(!component.isInstalled());
    openBtn.setDisable(!component.isInstalled() || client.getVpxService().getVpxFile() == null);
  }
}
