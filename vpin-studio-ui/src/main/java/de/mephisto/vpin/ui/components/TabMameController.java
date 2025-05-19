package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

import static de.mephisto.vpin.ui.Studio.client;

public class TabMameController extends AbstractComponentTab {
  private final static Logger LOG = LoggerFactory.getLogger(TabMameController.class);

  @FXML
  private Button mameBtn;

  @FXML
  private void onPrefsMame() {
    PreferencesController.open("mame");
  }

  @FXML
  private void onMameSetup() {
    File mameFolder = client.getMameService().getMameFolder();

    if (mameFolder != null && mameFolder.exists()) {
      File file = new File(mameFolder, "Setup64.exe");

      if (!file.exists()) {
        WidgetFactory.showAlert(Studio.stage, "Did not find Setup.exe", "The exe file " + file.getAbsolutePath() + " was not found.");
      }
      else {
        Studio.open(file);
      }
    }
    else {
      WidgetFactory.showAlert(Studio.stage, "VPinMAME folder invalid", "The server couldn't determine the PinMAME installation folder.");
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
  public void refreshTab(ComponentRepresentation component) {
    openFolderButton.setDisable(!component.isInstalled());
    mameBtn.setDisable(!component.isInstalled() || !client.getSystemService().isLocal());
  }
}
