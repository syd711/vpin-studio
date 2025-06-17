package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class TabMameController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabMameController.class);

  @FXML
  private Button mameBtn;

  @FXML
  private void onPrefsMame() {
    PreferencesController.open("mame");
  }

  @FXML
  private void onMameSetup() {
    if (!client.getMameService().runSetup()) {
      WidgetFactory.showAlert(Studio.stage, "Did not find Setup.exe", "The exe file was not found.");
    }
  }

  @Override
  public void postProcessing(boolean simulate) {
    if (!simulate) {
      File file = new File(component.getTargetFolder(), "Setup64.exe");
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList(file.getName()));
      executor.setDir(file.getParentFile());
      executor.executeCommandAsync();
    }
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.vpinmame;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize();

    mameBtn.managedProperty().bind(mameBtn.visibleProperty());
    mameBtn.setVisible(!Features.IS_STANDALONE);
  }

  @Override
  public void refreshTab(ComponentRepresentation component) {
    openFolderButton.setDisable(!component.isInstalled());
    mameBtn.setDisable(!component.isInstalled() || !client.getSystemService().isLocal());
  }
}
