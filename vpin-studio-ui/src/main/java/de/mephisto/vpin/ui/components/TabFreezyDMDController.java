package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.components.ComponentSummaryEntry;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.tables.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabFreezyDMDController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabFreezyDMDController.class);


  @FXML
  private void onFolder() {
    GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
    File folder = new File(defaultGameEmulator.getMameDirectory());
    openFolder(folder);
  }

  @FXML
  private void onReload() {
    client.getDmdService().clearCache();
    refreshCustomValues();
  }

  @FXML
  private void onFlexDMD() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
        File file = new File(defaultGameEmulator.getMameDirectory(), "FlexDMDUI.exe");
        if (!file.exists()) {
          WidgetFactory.showAlert(Studio.stage, "Did not find FlexDMD UI", "The exe file " + file.getAbsolutePath() + " was not found.");
        }
        else {
          desktop.open(file);
        }
      } catch (Exception e) {
        LOG.error("Failed to open FlexDMD UI: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onDmdDevice() {
    GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
    File folder = new File(defaultGameEmulator.getMameDirectory());
    File exe = new File(folder, "DmdDevice.ini");
    super.editFile(exe);
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.freezy;
  }


  private void refreshCustomValues() {
    clearCustomValues();

    GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
    ComponentSummary freezySummary = client.getDmdService().getFreezySummary(defaultGameEmulator.getId());
    List<ComponentSummaryEntry> entries = freezySummary.getEntries();
    for (ComponentSummaryEntry entry : entries) {
      super.addCustomValue(entry);
    }
  }

  @Override
  public void postProcessing(boolean simulate) {
    if (!simulate) {
      onFlexDMD();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    refreshCustomValues();
  }
}
