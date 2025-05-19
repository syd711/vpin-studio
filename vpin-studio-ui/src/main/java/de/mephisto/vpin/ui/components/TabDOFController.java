package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.components.ComponentSummaryEntry;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.PreferencesController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabDOFController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabDOFController.class);

  @FXML
  private Button configBtn;

  @Override
  protected void refresh() {
    super.refresh();
    onReload();
  }

  @FXML
  private void onPrefs() {
    PreferencesController.open("dof");
  }


  @FXML
  private void onReload() {
    try {
      openFolderButton.setDisable(true);
      configBtn.setDisable(true);
      clearCustomValues();

      if(client.getDofService().isValid()) {
        openFolderButton.setDisable(false);
        configBtn.setDisable(false);
        ComponentSummary dofSummary = client.getDofService().getDOFSummary();
        List<ComponentSummaryEntry> entries = dofSummary.getEntries();
        for (ComponentSummaryEntry entry : entries) {
          super.addCustomValue(entry);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to refresh DOF: " + e.getMessage());
    }
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.dof;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    onReload();
  }
}
