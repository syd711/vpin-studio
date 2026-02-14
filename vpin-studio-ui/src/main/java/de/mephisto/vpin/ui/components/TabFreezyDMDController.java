package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentSummaryEntry;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class TabFreezyDMDController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabFreezyDMDController.class);

  @FXML
  private Button flexDMDBtn;

  @FXML
  private Button dmdDeviceBtn;


  @FXML
  private void onReload() {
    client.getDmdService().clearCache();
    refreshCustomValues();
  }

  @FXML
  private void onFlexDMD() {
    if (!client.getMameService().runFlexSetup()) {
      WidgetFactory.showAlert(Studio.stage, "Did not find FlexDMD UI", "The exe file was not found.");
    }
  }

  @FXML
  private void onDmdDevice() {
    if (client.getSystemService().isLocal()) {
      File ini = client.getMameService().getDmdDeviceIni();
      super.editFile(ini);
    }
    else {
      try {
        boolean b = Dialogs.openTextEditor(new MonitoredTextFile(VPinFile.DmdDeviceIni), "DmdDevice.ini");
        if (b) {
          client.getMameService().clearCache();
          EventManager.getInstance().notifyTablesChanged();
        }
      }
      catch (Exception e) {
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
    JFXFuture.supplyAsync(() -> client.getDmdService().getFreezySummary())
      .thenAcceptLater(freezySummary -> {
        List<ComponentSummaryEntry> entries = freezySummary.getEntries();
        for (ComponentSummaryEntry entry : entries) {
          super.addCustomValue(entry);
        }
      });
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
    refreshCustomValues();

    flexDMDBtn.managedProperty().bind(flexDMDBtn.visibleProperty());
    flexDMDBtn.setVisible(!Features.IS_STANDALONE);

    dmdDeviceBtn.managedProperty().bind(dmdDeviceBtn.visibleProperty());
    dmdDeviceBtn.setVisible(!Features.IS_STANDALONE);
  }

  @Override
  protected void refreshTab(ComponentRepresentation component) {
    openFolderButton.setDisable(!component.isInstalled());
    flexDMDBtn.setDisable(!component.isInstalled() || !client.getSystemService().isLocal());
  }
}
