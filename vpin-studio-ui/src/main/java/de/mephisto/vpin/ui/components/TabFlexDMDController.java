package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabFlexDMDController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabFlexDMDController.class);

  @FXML
  private Button flexDMDBtn;

  @FXML
  private void onFlexDMD() {
    File file = client.getMameService().getFlexSetupFile();
    if (file == null || !file.exists()) {
      WidgetFactory.showAlert(Studio.stage, "Did not find FlexDMD UI", "The exe file was not found.");
    }
    else {
      Studio.open(file);
    }
  }

  @Override
  public void postProcessing(boolean simulate) {
    if (!simulate) {
      onFlexDMD();
    }
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.flexdmd;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
  }

  @Override
  protected void refreshTab(ComponentRepresentation component) {
    openFolderButton.setDisable(!component.isInstalled());
    flexDMDBtn.setDisable(!component.isInstalled() || !client.getSystemService().isLocal());
  }
}
