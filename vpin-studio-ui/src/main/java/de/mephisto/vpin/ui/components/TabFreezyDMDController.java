package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.components.ComponentSummaryEntry;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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
  private Button flexDMDBtn;

  @FXML
  private Button dmdDeviceBtn;

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
    if (client.getSystemService().isLocal()) {
      GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
      File folder = new File(defaultGameEmulator.getMameDirectory());
      File exe = new File(folder, "DmdDevice.ini");
      super.editFile(exe);
    }
    else {
      try {
        boolean b = Dialogs.openTextEditor(new TextFile(VPinFile.DmdDeviceIni), "DmdDevice.ini");
        if (b) {
          client.getMameService().clearCache();
          EventManager.getInstance().notifyTablesChanged();
        }
      } catch (Exception e) {
        LOG.error("Failed to open DmdDeviceIni text file: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open DmdDeviceIni file: " + e.getMessage());
      }
    }
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.freezy;
  }


  private void refreshCustomValues() {
    clearCustomValues();

    GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
    if (defaultGameEmulator!=null) {
      ComponentSummary freezySummary = client.getDmdService().getFreezySummary(defaultGameEmulator.getId());
      List<ComponentSummaryEntry> entries = freezySummary.getEntries();
      for (ComponentSummaryEntry entry : entries) {
        super.addCustomValue(entry);
      }
    }
  }

  @Override
  public void postProcessing(boolean simulate) {
    if (!simulate) {
      // called in TabFlexDMDController
      //onFlexDMD();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    flexDMDBtn.setDisable(!client.getSystemService().isLocal());
    refreshCustomValues();
  }
}
